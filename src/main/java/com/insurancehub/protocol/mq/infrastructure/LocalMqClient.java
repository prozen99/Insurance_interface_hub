package com.insurancehub.protocol.mq.infrastructure;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.springframework.stereotype.Component;

@Component
public class LocalMqClient {

    private final ConnectionFactory connectionFactory;

    public LocalMqClient(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public PublishedMessage publish(String destinationName, String payload, String correlationKey) {
        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            Destination destination = session.createQueue(destinationName);
            TextMessage message = session.createTextMessage(payload);
            message.setJMSCorrelationID(correlationKey);
            message.setStringProperty("interfaceHubCorrelationKey", correlationKey);

            try (MessageProducer producer = session.createProducer(destination)) {
                producer.send(message);
            }
            return new PublishedMessage(message.getJMSMessageID());
        } catch (JMSException exception) {
            throw new IllegalStateException("Could not publish MQ message.", exception);
        }
    }

    public ConsumedMessage consume(String destinationName, String correlationKey, int timeoutMillis) {
        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            Destination destination = session.createQueue(destinationName);
            String selector = "JMSCorrelationID = '" + escapeSelectorValue(correlationKey) + "'";
            connection.start();
            try (MessageConsumer consumer = session.createConsumer(destination, selector)) {
                Message message = consumer.receive(timeoutMillis);
                if (message == null) {
                    throw new IllegalStateException("Timed out waiting for MQ message with correlation key " + correlationKey + ".");
                }
                if (message instanceof TextMessage textMessage) {
                    return new ConsumedMessage(textMessage.getText());
                }
                throw new IllegalStateException("Unsupported MQ message type: " + message.getClass().getSimpleName());
            }
        } catch (JMSException exception) {
            throw new IllegalStateException("Could not consume MQ message.", exception);
        }
    }

    private String escapeSelectorValue(String value) {
        return value.replace("'", "''");
    }

    public record PublishedMessage(String messageId) {
    }

    public record ConsumedMessage(String payload) {
    }
}
