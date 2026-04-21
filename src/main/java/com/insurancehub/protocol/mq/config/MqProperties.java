package com.insurancehub.protocol.mq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mq")
public class MqProperties {

    private final Embedded embedded = new Embedded();

    private String defaultDestination = "insurancehub.demo.policy.events";

    private int defaultTimeoutMillis = 5000;

    public Embedded getEmbedded() {
        return embedded;
    }

    public String getDefaultDestination() {
        return defaultDestination;
    }

    public void setDefaultDestination(String defaultDestination) {
        this.defaultDestination = defaultDestination;
    }

    public int getDefaultTimeoutMillis() {
        return defaultTimeoutMillis;
    }

    public void setDefaultTimeoutMillis(int defaultTimeoutMillis) {
        this.defaultTimeoutMillis = defaultTimeoutMillis;
    }

    public static class Embedded {

        private boolean enabled = true;

        private int serverId = 0;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getServerId() {
            return serverId;
        }

        public void setServerId(int serverId) {
            this.serverId = serverId;
        }
    }
}
