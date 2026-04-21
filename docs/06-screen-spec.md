# Screen Spec

Phase 4 uses Thymeleaf for the admin UI and adds visible SOAP configuration and SOAP exchange inspection.

## Common Layout

Admin pages share:

- Left navigation
- Phase 4 product branding
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
- Manual execution form
- Recent execution table

SOAP interface behavior:

- Shows endpoint URL, SOAPAction, operation name, namespace URI, timeout, active status, and request template XML.
- Provides an Edit SOAP config button.
- Preloads the configured SOAP XML template into the manual execution form when available.
- XML containing `FAIL` causes the simulator to return a SOAP fault.

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

Validation:

- Endpoint URL is required and must start with `http://` or `https://`.
- Operation name and namespace URI are required.
- Request template XML must be well-formed.
- Timeout must be between 100 and 60000 ms.

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
- REST or SOAP exchange details when available
- Step logs
- Request payload
- Response payload
- Retry tasks

SOAP exchange details:

- Endpoint URL
- Method
- SOAPAction
- HTTP status code
- Latency
- Request headers
- Response headers
- Request XML
- Response XML
- Fault/error message when failed

Failed executions show a retry button.
