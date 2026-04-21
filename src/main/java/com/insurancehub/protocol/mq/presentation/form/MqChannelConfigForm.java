package com.insurancehub.protocol.mq.presentation.form;

import com.insurancehub.protocol.mq.domain.MqBrokerType;
import com.insurancehub.protocol.mq.domain.MqMessageType;
import com.insurancehub.protocol.mq.domain.entity.MqChannelConfig;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MqChannelConfigForm {

    private Long id;

    @NotNull(message = "Broker type is required.")
    private MqBrokerType brokerType = MqBrokerType.EMBEDDED_ARTEMIS;

    @NotBlank(message = "Destination name is required.")
    @Size(max = 180, message = "Destination name must be 180 characters or less.")
    private String destinationName = "insurancehub.demo.policy.events";

    @Size(max = 180, message = "Routing key must be 180 characters or less.")
    private String routingKey = "policy.event";

    @NotNull(message = "Message type is required.")
    private MqMessageType messageType = MqMessageType.TEXT;

    @Size(max = 300, message = "Correlation key expression must be 300 characters or less.")
    private String correlationKeyExpression = "MQ-{executionNo}";

    @NotNull(message = "Timeout is required.")
    @Min(value = 100, message = "Timeout must be at least 100 ms.")
    @Max(value = 60000, message = "Timeout must be 60000 ms or less.")
    private Integer timeoutMillis = 5000;

    private boolean active = true;

    public static MqChannelConfigForm empty() {
        return new MqChannelConfigForm();
    }

    public static MqChannelConfigForm from(MqChannelConfig config) {
        MqChannelConfigForm form = new MqChannelConfigForm();
        form.setId(config.getId());
        form.setBrokerType(config.getBrokerType());
        form.setDestinationName(config.getDestinationName());
        form.setRoutingKey(config.getRoutingKey());
        form.setMessageType(config.getMessageType());
        form.setCorrelationKeyExpression(config.getCorrelationKeyExpression());
        form.setTimeoutMillis(config.getTimeoutMillis());
        form.setActive(config.isActive());
        return form;
    }
}
