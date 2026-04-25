# Phase Plan

## Phase 0 - Foundation

Status: Complete

- Java 21, Spring Boot 3.x, and Gradle baseline.
- Modular monolith package structure.
- Local MySQL profile and Flyway.
- Documentation baseline.
- Admin dashboard placeholder and smoke API.

## Phase 1 - Admin Authentication And Master CRUD

Status: Complete

- DB-backed admin login.
- BCrypt password storage.
- Partner company CRUD.
- Internal system CRUD.
- Interface definition CRUD.
- Interface enable/disable.

## Phase 2 - Common Execution Engine, History, Failure Handling, Retry

Status: Complete

- Common execution service and executor contract.
- Manual execution, execution history, step logs, retry tasks, and dashboard metrics.
- Mock executors for all protocol types.

## Phase 3 - Real REST Integration

Status: Complete

- REST endpoint configuration UI and validation.
- Real REST executor using Spring `RestClient`.
- Local REST simulator API.
- REST request/response metadata capture.

## Phase 4 - Real SOAP Integration

Status: Complete

- SOAP endpoint configuration UI and validation.
- Real SOAP-over-HTTP executor.
- Local SOAP simulator API.
- SOAP request XML, response XML, SOAPAction, HTTP status, latency, and fault visibility.

## Phase 5 - Real MQ Integration

Status: Complete

- MQ channel configuration UI and validation.
- Embedded in-vm Artemis broker for local no-Docker demos.
- Real JMS text message publish and consume flow.
- MQ message history with separate publish and consume statuses.

## Phase 6 - Real SFTP/FTP Integration

Status: Complete

- SFTP/FTP file-transfer configuration UI and validation.
- Embedded local SFTP and FTP servers for no-Docker demos.
- Real upload and download flows.
- File transfer history and retry support.

## Phase 7 - Real Batch Integration

Status: Complete

- Batch job configuration UI and validation.
- Real Spring Batch manual launch.
- Optional local scheduler support.
- Demo jobs for interface settlement summary and failed retry aggregation.
- Spring Batch metadata tables managed by Flyway.
- Batch run and step history.
- Execution detail visibility for job parameters, read/write counts, status, output, and errors.
- Retry/rerun through the common execution engine.
- REST, SOAP, MQ, SFTP, and FTP regression paths preserved.

Exit criteria:

- `.\gradlew.bat build` succeeds.
- App starts with the local profile.
- Admin login works.
- REST, SOAP, MQ, SFTP, and FTP still work.
- Batch config page is reachable.
- Manual Batch execution runs a real Spring Batch job.
- Execution detail shows batch run information.
- Batch Runs page is navigable.

## Phase 8 - Monitoring/Dashboard

Status: Complete in the current codebase

- Operations dashboard with active interfaces, today success/failure, pending retries, recent retry outcomes, and quick links.
- Protocol summary cards for REST, SOAP, MQ, BATCH, SFTP, and FTP.
- 7-day execution trend view.
- Top failed interface summary.
- Recent executions and pending retry task visibility.
- Monitoring pages for failures, retries, protocols, file transfers, MQ, and batch.
- Execution history filters for keyword, protocol, status, trigger, and date range.
- Read-only aggregation through `OperationsMonitoringService`.

Exit criteria:

- `.\gradlew.bat build` succeeds.
- App starts with the local profile when local DB credentials are provided.
- Admin login still works.
- REST, SOAP, MQ, SFTP/FTP, and BATCH execution paths remain unchanged.
- Dashboard and monitoring pages render meaningful operational summaries.
- `application-local.yml` datasource defaults are not changed by Phase 8.

## Phase 9 - Testing, Performance, Final Polish

Status: Complete in the current codebase

- Reinforced admin security access tests for unauthenticated redirects and valid login.
- Reinforced monitoring aggregation tests for dashboard protocol summaries and failure summaries.
- Replaced repetitive protocol summary counts with grouped repository queries.
- Finalized README, requirements, architecture, ERD, protocol, screen, runbook, troubleshooting, and demo scenario documentation.
- Kept REST, SOAP, MQ, SFTP, FTP, and BATCH execution paths unchanged.
- Kept `application-local.yml` datasource defaults unchanged.

Exit criteria:

- `.\gradlew.bat test` succeeds.
- `.\gradlew.bat build` succeeds.
- App starts with the local profile when local DB credentials are available.
- Admin login works.
- Dashboard and monitoring pages render.
- Representative protocol execution tests remain green.
- Final docs explain setup, demo data, known limitations, and troubleshooting history.
