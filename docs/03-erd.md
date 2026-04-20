# ERD

Phase 2 extends the Phase 0 execution tables through `V3__phase_2_execution_engine.sql`.

## Logical ERD

```mermaid
erDiagram
    ADMIN_USER ||--o{ AUDIT_LOG : writes
    PARTNER_COMPANY ||--o{ INTERFACE_DEFINITION : owns
    INTERNAL_SYSTEM ||--o{ INTERFACE_DEFINITION : connects
    INTERFACE_DEFINITION ||--o{ INTERFACE_EXECUTION : runs
    INTERFACE_EXECUTION ||--o{ INTERFACE_EXECUTION_STEP : contains
    INTERFACE_EXECUTION ||--o{ INTERFACE_RETRY_TASK : creates
    INTERFACE_EXECUTION ||--o{ INTERFACE_EXECUTION : retry_source

    INTERFACE_DEFINITION {
        bigint id PK
        varchar interface_code UK
        varchar interface_name
        varchar protocol_type
        varchar direction_type
        varchar status
        bigint partner_company_id FK
        bigint internal_system_id FK
    }

    INTERFACE_EXECUTION {
        bigint id PK
        varchar execution_no UK
        varchar execution_key UK
        bigint interface_definition_id FK
        bigint retry_source_execution_id FK
        varchar protocol_type
        varchar trigger_type
        varchar status
        longtext request_payload
        longtext response_payload
        varchar error_code
        varchar error_message
        datetime started_at
        datetime finished_at
    }

    INTERFACE_EXECUTION_STEP {
        bigint id PK
        bigint interface_execution_id FK
        int step_order
        varchar step_name
        varchar status
        varchar message
        datetime started_at
        datetime finished_at
    }

    INTERFACE_RETRY_TASK {
        bigint id PK
        bigint interface_execution_id FK
        bigint interface_definition_id FK
        varchar retry_status
        int retry_count
        datetime last_retried_at
        datetime next_retry_at
    }
```

## Phase 2 Migration Notes

V3 adds:

- `interface_execution.execution_no`
- `interface_execution.retry_source_execution_id`
- `interface_execution.protocol_type`
- `interface_execution.request_payload`
- `interface_execution.response_payload`
- `interface_retry_task.last_retried_at`
- indexes for execution number, protocol, retry source, and retry task status

## Status Enums

Execution status:

- PENDING
- RUNNING
- SUCCESS
- FAILED

Retry status:

- WAITING
- DONE
- CANCELLED
