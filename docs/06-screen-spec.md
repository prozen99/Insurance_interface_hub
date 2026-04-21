# Screen Spec

Phase 3 uses Thymeleaf for the admin UI and adds visible REST configuration and REST exchange inspection.

## Common Layout

Admin pages share:

- Left navigation
- Phase 3 product branding
- Logout button
- Flash success/error messages
- Table-based enterprise admin layout

Navigation:

- Dashboard
- Interfaces
- Executions
- Partners
- Internal Systems

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
- Manual execution form
- Recent execution table

REST interface behavior:

- Shows configured method, base URL, path, endpoint URL, timeout, active status, and headers JSON.
- Provides an Edit REST config button.
- Preloads the configured sample request body into the manual execution form when available.
- Payload containing `FAIL` causes the simulator to return a controlled failure.

Inactive interfaces show the execution button disabled and server-side execution is rejected.

## REST Config Form

Path: `/admin/interfaces/{id}/rest-config`

Fields:

- Base URL
- HTTP method
- Path
- Timeout millis
- Headers JSON
- Sample request body
- Active for manual execution

Validation:

- Base URL is required and must start with `http://` or `https://`.
- Method must be GET or POST.
- Path is required.
- Timeout must be between 100 and 60000 ms.
- Headers must be a valid JSON object when provided.

## Execution History

Path: `/admin/executions`

Filters:

- Keyword
- Protocol
- Status

Table columns:

- Execution No
- Interface
- Protocol
- Trigger
- Status
- Started
- Duration
- Action

## Execution Detail

Path: `/admin/executions/{id}`

Sections:

- Execution summary
- REST exchange details when available
- Step logs
- Request payload
- Response payload
- Retry tasks

REST exchange details:

- Method
- URL
- Status code
- Latency
- Request headers
- Response headers

Failed executions show a retry button.

## Failed Execution Shortcut

Path: `/admin/executions/failed`

Shows execution history filtered to FAILED status.
