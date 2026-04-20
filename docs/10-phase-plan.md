# Phase Plan

## Phase 0 - Foundation

Status: Complete

- Java 21, Spring Boot 3.x, and Gradle baseline.
- Web, Thymeleaf, validation, JPA, actuator, security, batch, JMS, SOAP, SFTP, FTP, MySQL, Flyway, Lombok, and test dependencies.
- Modular monolith package structure.
- Local MySQL profile and Flyway.
- Initial schema migration.
- Documentation baseline.
- Admin dashboard placeholder and smoke API.

## Phase 1 - Admin Authentication And Master CRUD

Status: Complete in the current codebase

- DB-backed admin login with Spring Security form login.
- BCrypt password storage.
- Local demo admin seed account.
- Partner company CRUD.
- Internal system CRUD.
- Interface definition CRUD.
- Interface enable/disable.
- Interface filtering by keyword, protocol, and status.
- Thymeleaf admin pages with shared navigation and flash messages.
- Phase 1 tests for context loading, login page rendering, and service behavior.

Exit criteria:

- `.\gradlew.bat build` succeeds.
- App can start with local profile after MySQL database/user setup.
- `/login` accepts the seeded local demo admin account.
- CRUD pages are navigable under `/admin/**`.

## Phase 2 - Common Execution Engine

- Define common execution request/result models.
- Add protocol executor interface.
- Implement manual execution orchestration.
- Persist execution and step history.
- Add retry task creation policy.
- Keep real protocol adapters thin behind the common engine.

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
- Add local integration test strategy.

## Phase 6 - SFTP/FTP

- Add file transfer configuration UI and validation.
- Implement SFTP and FTP adapters.
- Record file transfer history.
- Support upload, download, file name patterns, and timeout behavior.

## Phase 7 - Batch

- Add batch job configuration UI.
- Implement scheduled and manual batch execution.
- Integrate batch runs with interface execution history.
- Add retry sweep job.

## Phase 8 - Monitoring/Dashboard

- Replace Phase 1 dashboard counts with richer operational metrics.
- Add execution trend views.
- Add failure and retry dashboard.
- Add operational health panels.

## Phase 9 - Testing, Performance, Final Polish

- Expand automated test suite.
- Add performance smoke tests.
- Review indexing and query plans.
- Polish demo data and screens.
- Prepare final portfolio presentation flow.
