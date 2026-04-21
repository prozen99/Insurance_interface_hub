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
- SOAP retry through the common execution engine.
- REST regression path preserved.

## Phase 5 - Real MQ Integration

Status: Complete in the current codebase

- MQ channel configuration UI and validation.
- Embedded in-vm Artemis broker for local no-Docker demos.
- Real JMS text message publish and consume flow.
- Correlation key generation and consume-by-correlation behavior.
- MQ message history with separate publish and consume statuses.
- MQ execution detail visibility for destination, message id, correlation key, metadata, payloads, latency, and errors.
- MQ retry through the common execution engine.
- REST and SOAP regression paths preserved.

Exit criteria:

- `.\gradlew.bat build` succeeds.
- App starts with the local profile.
- Admin login works.
- REST execution still works.
- SOAP execution still works.
- MQ config page is reachable from an MQ interface detail page.
- Manual MQ execution publishes and consumes through the embedded broker.
- Payload containing `FAIL` creates a failed MQ execution with publish success and consume failure visible.
- Failed MQ execution can be retried.

## Phase 6 - SFTP/FTP

- Add file transfer configuration UI and validation.
- Implement SFTP and FTP adapters.
- Record file transfer history.

## Phase 7 - Batch

- Add batch job configuration UI.
- Implement scheduled and manual batch execution.
- Integrate batch runs with interface execution history.

## Phase 8 - Monitoring/Dashboard

- Add richer operational metrics.
- Add execution trend views.
- Add failure and retry dashboard.

## Phase 9 - Testing, Performance, Final Polish

- Expand automated test suite.
- Add performance smoke tests.
- Polish demo data and screens.
