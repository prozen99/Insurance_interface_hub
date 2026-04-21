# ERD

Phase 5 extends the schema through `V6__phase_5_real_mq_integration.sql`.

## Logical ERD

```mermaid
erDiagram
    PARTNER_COMPANY ||--o{ INTERFACE_DEFINITION : owns
    INTERNAL_SYSTEM ||--o{ INTERFACE_DEFINITION : connects
    INTERFACE_DEFINITION ||--o{ INTERFACE_EXECUTION : runs
    INTERFACE_DEFINITION ||--o| REST_ENDPOINT_CONFIG : configures
    INTERFACE_DEFINITION ||--o| SOAP_ENDPOINT_CONFIG : configures
    INTERFACE_DEFINITION ||--o| MQ_CHANNEL_CONFIG : configures
    INTERFACE_EXECUTION ||--o{ INTERFACE_EXECUTION_STEP : contains
    INTERFACE_EXECUTION ||--o{ INTERFACE_RETRY_TASK : creates
    INTERFACE_EXECUTION ||--o{ INTERFACE_EXECUTION : retry_source
    INTERFACE_EXECUTION ||--o{ MQ_MESSAGE_HISTORY : records
    MQ_CHANNEL_CONFIG ||--o{ MQ_MESSAGE_HISTORY : emits

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

    MQ_CHANNEL_CONFIG {
        bigint id PK
        bigint interface_definition_id FK
        varchar broker_type
        varchar queue_name
        varchar destination_name
        varchar routing_key
        varchar connection_alias
        varchar message_type
        varchar correlation_key_expression
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

    MQ_MESSAGE_HISTORY {
        bigint id PK
        bigint interface_execution_id FK
        bigint interface_definition_id FK
        bigint mq_channel_config_id FK
        varchar message_id
        varchar correlation_key
        varchar destination_name
        varchar message_type
        varchar publish_status
        varchar consume_status
        longtext outbound_payload
        longtext consumed_payload
        varchar error_code
        varchar error_message
        bigint latency_ms
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

## Phase 5 Migration Notes

V6 adds:

- `mq_channel_config.destination_name`
- `mq_channel_config.message_type`
- `mq_channel_config.correlation_key_expression`
- `mq_channel_config.timeout_millis`
- `mq_channel_config.active_yn`
- `mq_message_history`
- sample MQ interface `IF_MQ_POLICY_001`
- sample MQ config for the local embedded Artemis demo destination

`mq_message_history` separates producer and consumer outcomes with `publish_status` and `consume_status`. This keeps a successful publish but failed consumer processing understandable in the admin UI.
