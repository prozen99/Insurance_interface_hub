# ERD

Phase 3 extends the execution schema through `V4__phase_3_real_rest_integration.sql`.

## Logical ERD

```mermaid
erDiagram
    ADMIN_USER ||--o{ AUDIT_LOG : writes
    PARTNER_COMPANY ||--o{ INTERFACE_DEFINITION : owns
    INTERNAL_SYSTEM ||--o{ INTERFACE_DEFINITION : connects
    INTERFACE_DEFINITION ||--o{ INTERFACE_EXECUTION : runs
    INTERFACE_DEFINITION ||--o| REST_ENDPOINT_CONFIG : configures
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

    REST_ENDPOINT_CONFIG {
        bigint id PK
        bigint interface_definition_id FK
        varchar http_method
        varchar endpoint_url
        varchar base_url
        varchar path
        int timeout_millis
        longtext headers_json
        longtext sample_request_body
        tinyint active_yn
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
        varchar request_url
        varchar request_method
        longtext request_headers
        longtext response_payload
        int response_status_code
        longtext response_headers
        bigint latency_ms
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

## Phase 3 Migration Notes

V4 adds:

- `rest_endpoint_config.base_url`
- `rest_endpoint_config.path`
- `rest_endpoint_config.headers_json`
- `rest_endpoint_config.sample_request_body`
- `rest_endpoint_config.active_yn`
- `interface_execution.request_url`
- `interface_execution.request_method`
- `interface_execution.request_headers`
- `interface_execution.response_status_code`
- `interface_execution.response_headers`
- `interface_execution.latency_ms`
- sample REST config for `IF_REST_POLICY_001`

The legacy `rest_endpoint_config.endpoint_url` column remains populated for compatibility with the Phase 0 baseline. The application uses `base_url + path` and synchronizes `endpoint_url`.

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
