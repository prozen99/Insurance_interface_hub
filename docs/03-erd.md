# ERD

Phase 6 extends the schema through `V7__phase_6_real_file_transfer_integration.sql`.

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
    INTERFACE_EXECUTION ||--o{ INTERFACE_EXECUTION_STEP : contains
    INTERFACE_EXECUTION ||--o{ INTERFACE_RETRY_TASK : creates
    INTERFACE_EXECUTION ||--o{ MQ_MESSAGE_HISTORY : records
    INTERFACE_EXECUTION ||--o{ FILE_TRANSFER_HISTORY : records
    FILE_TRANSFER_CONFIG ||--o{ FILE_TRANSFER_HISTORY : emits

    FILE_TRANSFER_CONFIG {
        bigint id PK
        bigint interface_definition_id FK
        varchar transfer_protocol
        varchar host
        int port
        varchar username
        varchar secret_reference
        varchar base_remote_path
        varchar local_path
        varchar file_name_pattern
        tinyint passive_mode_yn
        int timeout_millis
        tinyint active_yn
    }

    FILE_TRANSFER_HISTORY {
        bigint id PK
        bigint interface_execution_id FK
        bigint interface_definition_id FK
        bigint file_transfer_config_id FK
        varchar protocol_type
        varchar transfer_direction
        varchar local_file_name
        varchar local_file_path
        varchar remote_file_path
        bigint file_size_bytes
        varchar transfer_status
        bigint latency_millis
        varchar error_message
        varchar checksum_sha256
        varchar content_summary
    }

    INTERFACE_EXECUTION {
        bigint id PK
        varchar execution_no UK
        bigint interface_definition_id FK
        varchar protocol_type
        varchar trigger_type
        varchar status
        longtext request_payload
        varchar request_url
        varchar request_method
        varchar protocol_action
        longtext response_payload
        bigint latency_ms
        varchar error_code
        varchar error_message
    }
```

## Phase 6 Migration Notes

V7 adds:

- `file_transfer_config.host`
- `file_transfer_config.username`
- `file_transfer_config.secret_reference`
- `file_transfer_config.base_remote_path`
- `file_transfer_config.timeout_millis`
- `file_transfer_config.active_yn`
- `file_transfer_history`
- sample SFTP interface `IF_SFTP_POLICY_001`
- sample FTP interface `IF_FTP_POLICY_001`

`file_transfer_history` records transfer direction, local and remote paths, size, checksum, latency, status, and errors so operators can inspect file-transfer results from execution detail pages.
