package com.insurancehub.protocol.mq.domain.entity;

import com.insurancehub.common.entity.BaseTimeEntity;
import com.insurancehub.interfacehub.domain.entity.InterfaceDefinition;
import com.insurancehub.protocol.mq.domain.MqBrokerType;
import com.insurancehub.protocol.mq.domain.MqMessageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "mq_channel_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MqChannelConfig extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interface_definition_id", nullable = false)
    private InterfaceDefinition interfaceDefinition;

    @Enumerated(EnumType.STRING)
    @Column(name = "broker_type", nullable = false, length = 60)
    private MqBrokerType brokerType;

    @Column(name = "queue_name", nullable = false, length = 180)
    private String queueName;

    @Column(name = "destination_name", length = 180)
    private String destinationName;

    @Column(name = "exchange_name", length = 180)
    private String exchangeName;

    @Column(name = "routing_key", length = 180)
    private String routingKey;

    @Column(name = "connection_alias", length = 120)
    private String connectionAlias;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 60)
    private MqMessageType messageType;

    @Column(name = "correlation_key_expression", length = 300)
    private String correlationKeyExpression;

    @Column(name = "timeout_millis", nullable = false)
    private Integer timeoutMillis;

    @Column(name = "active_yn", nullable = false)
    private boolean active;

    private MqChannelConfig(
            InterfaceDefinition interfaceDefinition,
            MqBrokerType brokerType,
            String destinationName,
            String routingKey,
            MqMessageType messageType,
            String correlationKeyExpression,
            Integer timeoutMillis,
            boolean active
    ) {
        this.interfaceDefinition = interfaceDefinition;
        this.connectionAlias = "local-in-vm-artemis";
        update(brokerType, destinationName, routingKey, messageType, correlationKeyExpression, timeoutMillis, active);
    }

    public static MqChannelConfig create(
            InterfaceDefinition interfaceDefinition,
            MqBrokerType brokerType,
            String destinationName,
            String routingKey,
            MqMessageType messageType,
            String correlationKeyExpression,
            Integer timeoutMillis,
            boolean active
    ) {
        return new MqChannelConfig(
                interfaceDefinition,
                brokerType,
                destinationName,
                routingKey,
                messageType,
                correlationKeyExpression,
                timeoutMillis,
                active
        );
    }

    public void update(
            MqBrokerType brokerType,
            String destinationName,
            String routingKey,
            MqMessageType messageType,
            String correlationKeyExpression,
            Integer timeoutMillis,
            boolean active
    ) {
        this.brokerType = brokerType;
        this.destinationName = normalizeDestination(destinationName);
        this.queueName = this.destinationName;
        this.exchangeName = null;
        this.routingKey = trimToNull(routingKey);
        this.messageType = messageType;
        this.correlationKeyExpression = trimToNull(correlationKeyExpression);
        this.timeoutMillis = timeoutMillis;
        this.active = active;
        if (this.connectionAlias == null) {
            this.connectionAlias = "local-in-vm-artemis";
        }
    }

    private String normalizeDestination(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
