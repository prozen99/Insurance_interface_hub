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

Status: Complete in the current codebase

- Common `InterfaceExecutionService`.
- `InterfaceExecutor` contract.
- `InterfaceExecutorFactory`.
- REST, SOAP, MQ, BATCH, SFTP, and FTP mock executors.
- Manual execution from interface detail.
- Execution history list.
- Execution detail with step logs.
- Failed execution retry action.
- Retry task persistence.
- Dashboard metrics for success/failure/retry.

Exit criteria:

- `.\gradlew.bat build` succeeds.
- App starts with local profile.
- Admin login works.
- Manual execution succeeds with normal payload.
- Manual execution fails with payload containing `FAIL`.
- Failed execution retry creates a new execution.

## Phase 3 - REST

- Add REST endpoint configuration UI and validation.
- Implement outbound REST executor.
- Support headers, methods, timeout, and response recording.
- Add mock-server based tests.

## Phase 4 - SOAP

- Add SOAP endpoint configuration UI and validation.
- Implement SOAP executor using Spring Web Services.
- Support SOAP action, namespace, timeout, and XML error handling.
- Add XML fixture tests.

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
