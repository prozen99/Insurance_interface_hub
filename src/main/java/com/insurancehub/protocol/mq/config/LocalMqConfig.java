package com.insurancehub.protocol.mq.config;

import java.util.Map;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.TransportConstants;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.apache.activemq.artemis.core.settings.impl.AddressSettings;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MqProperties.class)
public class LocalMqConfig {

    @Bean(destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "app.mq.embedded", name = "enabled", havingValue = "true", matchIfMissing = true)
    public EmbeddedActiveMQ embeddedActiveMQ(MqProperties properties) throws Exception {
        org.apache.activemq.artemis.core.config.Configuration configuration = new ConfigurationImpl()
                .setPersistenceEnabled(false)
                .setSecurityEnabled(false)
                .addAcceptorConfiguration(new TransportConfiguration(
                        InVMAcceptorFactory.class.getName(),
                        Map.of(TransportConstants.SERVER_ID_PROP_NAME, properties.getEmbedded().getServerId())
                ));

        AddressSettings addressSettings = new AddressSettings()
                .setAutoCreateAddresses(true)
                .setAutoCreateQueues(true);
        configuration.addAddressesSetting("#", addressSettings);

        EmbeddedActiveMQ embeddedActiveMQ = new EmbeddedActiveMQ();
        embeddedActiveMQ.setConfiguration(configuration);
        embeddedActiveMQ.start();
        return embeddedActiveMQ;
    }

    @Bean
    @ConditionalOnMissingBean(ConnectionFactory.class)
    public ConnectionFactory mqConnectionFactory(MqProperties properties) {
        return new ActiveMQConnectionFactory("vm://" + properties.getEmbedded().getServerId());
    }
}
