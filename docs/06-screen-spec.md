# Screen Spec

Phase 2 uses Thymeleaf for the admin UI.

## Common Layout

Admin pages share:

- Left navigation
- Phase 2 product branding
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

Phase 2 additions:

- Manual execution form
- Optional request payload textarea
- Execute now button
- Recent execution table
- Link to execution detail

Inactive interfaces show the execution button disabled and server-side execution is rejected.

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
- Step logs
- Request payload
- Response payload
- Retry tasks

Failed executions show a retry button.

## Failed Execution Shortcut

Path: `/admin/executions/failed`

Shows execution history filtered to FAILED status.
