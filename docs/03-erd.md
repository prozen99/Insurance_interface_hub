# ERD

Phase 4 extends the schema through `V5__phase_4_real_soap_integration.sql`.

## Logical ERD

```mermaid
erDiagram
    PARTNER_COMPANY ||--o{ INTERFACE_DEFINITION : owns
    INTERNAL_SYSTEM ||--o{ INTERFACE_DEFINITION : connects
    INTERFACE_DEFINITION ||--o{ INTERFACE_EXECUTION : runs
    INTERFACE_DEFINITION ||--o| REST_ENDPOINT_CONFIG : configures
    INTERFACE_DEFINITION ||--o| SOAP_ENDPOINT_CONFIG : configures
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

    SOAP_ENDPOINT_CONFIG {
        bigint id PK
        bigint interface_definition_id FK
        varchar service_url
        varchar soap_action
        varchar operation_name
        varchar namespace_uri
        longtext request_template_xml
        int timeout_millis
        tinyint active_yn
    }

    INTERFACE_EXECUTION {
        bigint id PK
        varchar execution_no UK
        bigint interface_definition_id FK
        bigint retry_source_execution_id FK
        varchar protocol_type
        varchar trigger_type
        varchar status
        longtext request_payload
        varchar request_url
        varchar request_method
        varchar protocol_action
        longtext request_headers
        longtext response_payload
        int response_status_code
        longtext response_headers
        bigint latency_ms
        varchar error_code
        varchar error_message
    }

    INTERFACE_EXECUTION_STEP {
        bigint id PK
        bigint interface_execution_id FK
        int step_order
        varchar step_name
        varchar status
        varchar message
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

## Phase 4 Migration Notes

V5 adds:

- `soap_endpoint_config.operation_name`
- `soap_endpoint_config.request_template_xml`
- `soap_endpoint_config.active_yn`
- `interface_execution.protocol_action`
- sample SOAP interface `IF_SOAP_POLICY_001`
- sample SOAP config for the local policy inquiry simulator

The existing `soap_endpoint_config.service_url` column is mapped as the SOAP endpoint URL in application code.
