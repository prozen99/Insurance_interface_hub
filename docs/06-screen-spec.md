# Screen Spec

Phase 5 uses Thymeleaf for the admin UI and adds visible MQ configuration, MQ publish/consume execution, and MQ message history inspection.

## Common Layout

Admin pages share:

- Left navigation
- Phase 5 product branding
- Logout button
- Flash success/error messages
- Table-based enterprise admin layout

## Dashboard

Path: `/admin`

Metrics:

- Total interfaces
- Active interfaces
- Today success
- Today failure
- Pending retries
- Partner companies
- Internal systems

## Interface Detail

Path: `/admin/interfaces/{id}`

Sections:

- Interface summary
- REST settings panel for REST interfaces
- SOAP settings panel for SOAP interfaces
- MQ settings panel for MQ interfaces
- Manual execution form
- Recent execution table

MQ interface behavior:

- Shows broker type, destination, routing key, message type, correlation key expression, timeout, and active status.
- Provides an Edit MQ config button.
- Preloads a sample JSON text message into the manual execution form.
- Payload containing `FAIL` publishes successfully but fails during local consumer processing.

## MQ Config Form

Path: `/admin/interfaces/{id}/mq-config`

Fields:

- Broker type
- Destination name
- Routing key
- Message type
- Correlation key expression
- Timeout millis
- Active for manual execution

Validation:

- Destination name is required.
- Broker type and message type are required.
- Timeout must be between 100 and 60000 ms.
- Optional text fields are length-limited.

## SOAP Config Form

Path: `/admin/interfaces/{id}/soap-config`

Fields:

- Endpoint URL
- SOAPAction
- Operation name
- Namespace URI
- Timeout millis
- Request template XML
- Active for manual execution

## Execution History

Path: `/admin/executions`

Filters:

- Keyword
- Protocol
- Status

## Execution Detail

Path: `/admin/executions/{id}`

Sections:

- Execution summary
- REST, SOAP, or MQ exchange details when available
- MQ message history section for MQ executions
- Step logs
- Request payload
- Response payload
- Retry tasks

MQ exchange details:

- Destination
- Flow name
- Correlation key
- Publish metadata
- Consume metadata
- Latency
- Message id
- Publish status
- Consume status
- Error message when failed

Failed executions show a retry button.
