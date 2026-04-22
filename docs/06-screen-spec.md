# Screen Spec

Phase 8 uses Thymeleaf for a demo-ready operations console. It keeps the Phase 7 CRUD, execution, protocol configuration, and batch screens while adding monitoring navigation and summary pages.

## Common Layout

Admin pages share:

- Left navigation
- Phase 8 product branding
- Dashboard, Monitoring, Interfaces, Executions, Batch Runs, Partners, and Internal Systems links
- Logout button
- Flash success/error messages
- Table-based enterprise admin layout
- Consistent status badges for execution, retry, transfer, MQ, and batch statuses

## Dashboard

Path: `/admin`

Sections:

- Primary operational metrics
- Quick links for failures, retries, protocol health, and execution history
- 7-day execution trend
- Top failed interfaces
- Protocol cards for REST, SOAP, MQ, BATCH, SFTP, and FTP
- Recent executions
- Pending retry tasks
- File transfer, MQ, batch, and actuator health summary cards

## Monitoring Overview

Path: `/admin/monitoring`

Purpose:

- Give operators a compact operational snapshot.
- Link to specialized monitoring views.
- Reuse the same read-only monitoring summaries as the dashboard.

## Failure Monitoring

Path: `/admin/monitoring/failures`

Shows:

- Top failed interfaces in the last 7 days
- Recent failed executions
- Links to interface detail and execution detail

## Retry Monitoring

Path: `/admin/monitoring/retries`

Shows:

- Pending retry count
- New waiting retries today
- Retry tasks completed in the last 7 days
- Waiting retry task table
- Recent retry task table

## Protocol Monitoring

Path: `/admin/monitoring/protocols`

Shows:

- Total and active interface count by protocol
- Today success and failure count by protocol
- Last 7-day execution volume by protocol
- Last 7-day daily trend

## File Transfer Monitoring

Path: `/admin/monitoring/files`

Shows:

- Today transfer total, success, and failure counts
- Recent SFTP/FTP transfer history
- Direction, status, file name, remote path, latency, and linked execution detail

## MQ Monitoring

Path: `/admin/monitoring/mq`

Shows:

- Today publish success/failure counts
- Today consume success/failure counts
- Recent message destination, correlation key, publish status, consume status, latency, and linked execution detail

## Batch Monitoring

Path: `/admin/monitoring/batch`

Shows:

- Today batch total, completed, failed, and running counts
- Recent batch runs with read/write/skip counts
- Links to batch run detail and unified execution detail

## Execution History

Path: `/admin/executions`

Filters:

- Keyword
- Protocol type
- Execution status
- Trigger type
- Started from date
- Started to date

The list links to execution detail pages where protocol request/response, step logs, retry tasks, MQ messages, file transfers, and batch runs remain visible.
