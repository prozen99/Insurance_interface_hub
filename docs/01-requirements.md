# Requirements

## Phase 2 Functional Requirements

Operators can:

- Log in to the admin console.
- Manage partner companies, internal systems, and interface definitions.
- Manually execute an active interface definition.
- Enter an optional request payload.
- View execution history.
- View execution detail with step logs.
- See execution status: PENDING, RUNNING, SUCCESS, FAILED.
- Retry failed executions.
- View dashboard metrics for today success, today failure, and pending retries.

## Execution Rules

- Phase 2 does not call real external systems.
- Execution is routed through a common execution engine.
- Protocol-specific mock executors exist for REST, SOAP, MQ, BATCH, SFTP, and FTP.
- If the interface code or request payload contains `FAIL`, the mock executor returns failure.
- Failed executions create a WAITING retry task.
- Retrying a failed execution creates a new execution with trigger type RETRY.
- Retrying a non-failed execution is rejected.
- Inactive interfaces cannot be executed.

## Validation Requirements

- Manual request payload is optional.
- Manual request payload is limited to 4000 characters in the admin form.
- Interface IDs and execution IDs must exist.
- Enum filters reject invalid values through Spring MVC binding.

## Non-Functional Requirements

- Java 21.
- Spring Boot 3.x.
- Gradle build.
- Local MySQL only.
- Flyway migration history is append-only.
- Thymeleaf admin UI.
- Windows-compatible commands.
- No real secrets in repository files.

## Out Of Scope For Phase 2

- Real REST calls.
- SOAP XML mapping.
- MQ producer or consumer logic.
- SFTP/FTP transfer logic.
- Batch scheduling or job execution.
- Distributed execution workers.
- Production alerting.
