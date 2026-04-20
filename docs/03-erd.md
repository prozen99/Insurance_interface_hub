# ERD

Phase 1 uses the Phase 0 baseline tables and adds a V2 migration for admin login and master CRUD fields.

## Logical ERD

```mermaid
erDiagram
    ADMIN_USER ||--o{ AUDIT_LOG : writes
    PARTNER_COMPANY ||--o{ INTERFACE_DEFINITION : owns
    INTERNAL_SYSTEM ||--o{ INTERFACE_DEFINITION : connects
    INTERFACE_DEFINITION ||--o{ INTERFACE_EXECUTION : runs
    INTERFACE_EXECUTION ||--o{ INTERFACE_EXECUTION_STEP : contains
    INTERFACE_EXECUTION ||--o{ INTERFACE_RETRY_TASK : schedules
    INTERFACE_DEFINITION ||--o{ INTERFACE_RETRY_TASK : retries
    INTERFACE_DEFINITION ||--o| REST_ENDPOINT_CONFIG : configures
    INTERFACE_DEFINITION ||--o| SOAP_ENDPOINT_CONFIG : configures
    INTERFACE_DEFINITION ||--o| MQ_CHANNEL_CONFIG : configures
    INTERFACE_DEFINITION ||--o| FILE_TRANSFER_CONFIG : configures
    INTERFACE_DEFINITION ||--o| BATCH_JOB_CONFIG : configures

    ADMIN_USER {
        bigint id PK
        varchar login_id UK
        varchar password_hash
        varchar display_name
        varchar role_code
        varchar status
    }

    PARTNER_COMPANY {
        bigint id PK
        varchar partner_code UK
        varchar partner_name
        varchar status
        varchar description
    }

    INTERNAL_SYSTEM {
        bigint id PK
        varchar system_code UK
        varchar system_name
        varchar owner_department
        varchar status
        varchar description
    }

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
```

## Phase 1 Migration Notes

`V2__phase_1_admin_master_crud.sql` adds:

- `admin_user.password_hash`
- `admin_user.description`
- `partner_company.status`
- `partner_company.description`
- `internal_system.status`
- `internal_system.description`
- `interface_definition.status`
- supporting status indexes
- local demo seed data

## Master Data Rules

- `partner_company.partner_code` is unique.
- `internal_system.system_code` is unique.
- `interface_definition.interface_code` is unique.
- `interface_definition.status` is the Phase 1 enable/disable source of truth.
- Legacy `active_yn` and `enabled_yn` columns remain synchronized by the JPA entity methods for now.

## Future Adjustments

Later phases may add:

- Credential alias tables
- Protocol-specific create/edit screens
- File transfer history detail
- Payload metadata tables
- Batch run parameter tables
- Dashboard aggregation tables
- Audit event persistence from services
