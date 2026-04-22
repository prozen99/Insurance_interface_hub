# ERD

Phase 7 extends the schema through `V8__phase_7_real_batch_integration.sql`.

## Logical ERD

```mermaid
erDiagram
    PARTNER_COMPANY ||--o{ INTERFACE_DEFINITION : owns
    INTERNAL_SYSTEM ||--o{ INTERFACE_DEFINITION : connects
    INTERFACE_DEFINITION ||--o{ INTERFACE_EXECUTION : runs
    INTERFACE_DEFINITION ||--o| REST_ENDPOINT_CONFIG : configures
    INTERFACE_DEFINITION ||--o| SOAP_ENDPOINT_CONFIG : configures
    INTERFACE_DEFINITION ||--o| MQ_CHANNEL_CONFIG : configures
    INTERFACE_DEFINITION ||--o| FILE_TRANSFER_CONFIG : configures
    INTERFACE_DEFINITION ||--o| BATCH_JOB_CONFIG : configures
    INTERFACE_EXECUTION ||--o{ INTERFACE_EXECUTION_STEP : contains
    INTERFACE_EXECUTION ||--o{ INTERFACE_RETRY_TASK : creates
    INTERFACE_EXECUTION ||--o{ MQ_MESSAGE_HISTORY : records
    INTERFACE_EXECUTION ||--o{ FILE_TRANSFER_HISTORY : records
    INTERFACE_EXECUTION ||--o{ BATCH_RUN_HISTORY : records
    FILE_TRANSFER_CONFIG ||--o{ FILE_TRANSFER_HISTORY : emits
    BATCH_JOB_CONFIG ||--o{ BATCH_RUN_HISTORY : launches
    BATCH_RUN_HISTORY ||--o{ BATCH_STEP_HISTORY : contains

    BATCH_JOB_CONFIG {
        bigint id PK
        bigint interface_definition_id FK
        varchar job_name
        varchar job_type
        varchar cron_expression
        longtext parameter_template_json
        tinyint enabled_yn
        int max_parallel_count
        tinyint retryable_yn
        int timeout_millis
        tinyint active_yn
    }

    BATCH_RUN_HISTORY {
        bigint id PK
        bigint interface_execution_id FK
        bigint interface_definition_id FK
        bigint batch_job_config_id FK
        bigint spring_batch_job_execution_id
        varchar job_name
        varchar job_type
        longtext job_parameters_json
        varchar batch_status
        bigint read_count
        bigint write_count
        bigint skip_count
        bigint latency_ms
        varchar error_message
        varchar output_summary
    }

    BATCH_STEP_HISTORY {
        bigint id PK
        bigint batch_run_history_id FK
        varchar step_name
        varchar step_status
        bigint read_count
        bigint write_count
        bigint commit_count
        bigint rollback_count
        bigint skip_count
    }
```

## Phase 7 Migration Notes

V8 adds:

- batch job config fields for job type, parameters, retryability, timeout, and active flag
- `batch_run_history`
- `batch_step_history`
- Spring Batch metadata tables owned by Flyway
- sample batch interfaces `IF_BATCH_SETTLEMENT_001` and `IF_BATCH_RETRY_AGG_001`

Spring Batch metadata tables are operational framework tables. Portfolio-facing batch visibility is stored in `batch_run_history` and `batch_step_history`.
