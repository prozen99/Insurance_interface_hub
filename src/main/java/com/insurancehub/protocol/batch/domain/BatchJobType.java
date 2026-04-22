package com.insurancehub.protocol.batch.domain;

public enum BatchJobType {
    INTERFACE_SETTLEMENT_SUMMARY("interfaceSettlementSummaryJob", "Daily interface settlement summary"),
    FAILED_RETRY_AGGREGATION("failedExecutionRetryAggregationJob", "Failed execution retry aggregation");

    private final String jobName;
    private final String description;

    BatchJobType(String jobName, String description) {
        this.jobName = jobName;
        this.description = description;
    }

    public String getJobName() {
        return jobName;
    }

    public String getDescription() {
        return description;
    }
}
