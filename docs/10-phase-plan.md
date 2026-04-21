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

Status: Complete in the current codebase

- SOAP endpoint configuration UI and validation.
- Real SOAP-over-HTTP executor.
- Local SOAP simulator API.
- SOAP request XML, response XML, SOAPAction, HTTP status, latency, and fault visibility.
- SOAP retry through the common execution engine.
- REST regression path preserved.

Exit criteria:

- `.\gradlew.bat build` succeeds.
- App starts with the local profile.
- Admin login works.
- REST execution still works.
- SOAP config page is reachable from a SOAP interface detail page.
- Manual SOAP execution calls the local simulator.
- SOAP XML containing `FAIL` returns a failed execution with SOAP fault details.
- Failed SOAP execution can be retried.

## Phase 5 - MQ

- Add MQ channel configuration UI and validation.
- Implement JMS send/receive foundation.
- Support queue names, correlation keys, and message status.

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
