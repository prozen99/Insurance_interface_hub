package com.insurancehub.protocol.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.batch")
public class BatchProperties {

    private final Demo demo = new Demo();
    private final Scheduler scheduler = new Scheduler();

    public Demo getDemo() {
        return demo;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public static class Demo {

        private String outputDirectory = "build/batch-demo/output";

        public String getOutputDirectory() {
            return outputDirectory;
        }

        public void setOutputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
        }
    }

    public static class Scheduler {

        private boolean enabled;

        private long pollDelayMs = 30000;

        private long initialDelayMs = 30000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getPollDelayMs() {
            return pollDelayMs;
        }

        public void setPollDelayMs(long pollDelayMs) {
            this.pollDelayMs = pollDelayMs;
        }

        public long getInitialDelayMs() {
            return initialDelayMs;
        }

        public void setInitialDelayMs(long initialDelayMs) {
            this.initialDelayMs = initialDelayMs;
        }
    }
}
