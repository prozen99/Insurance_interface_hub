# Requirements

## Final Functional Requirements

Operators can:

- Log in to the admin console.
- Manage partner companies.
- Manage internal systems.
- Manage interface definitions.
- Enable or disable interfaces.
- Configure REST endpoint settings.
- Configure SOAP endpoint settings.
- Configure MQ channel settings.
- Configure SFTP/FTP file transfer settings.
- Configure Batch job settings.
- Manually execute active interfaces.
- Trigger local demo REST, SOAP, MQ, SFTP, FTP, and BATCH flows.
- View execution history and execution detail.
- View protocol-specific request, response, payload, latency, status, and error data.
- View execution step logs.
- Retry failed executions.
- View dashboard metrics, protocol summaries, failure summaries, retry queues, MQ history, file transfer history, and batch run history.

## Execution Rules

- Inactive interfaces cannot be executed.
- Manual execution creates an `interface_execution` row with trigger type `MANUAL`.
- Retry creates a new execution linked to the failed source execution with trigger type `RETRY`.
- Scheduled batch execution uses trigger type `SCHEDULED`.
- Failed executions create a WAITING retry task.
- Retrying a non-failed execution is rejected.
- Protocol-specific executors return results through the common execution result model.
- Controlled demo failures use `FAIL` payload values or protocol-specific failure switches such as `forceFail=true` for Batch.

## Supported Protocols

| Protocol | Final behavior |
| --- | --- |
| REST | Real local HTTP call to simulator endpoint |
| SOAP | Real local SOAP-over-HTTP call to simulator endpoint |
| MQ | Real local JMS publish/consume through embedded Artemis |
| SFTP | Real local file upload/download through embedded SFTP server |
| FTP | Real local file upload/download through embedded FTP server |
| BATCH | Real local Spring Batch job launch with run and step history |

## Validation Requirements

- Required code/name fields are validated.
- Unique codes are enforced where applicable.
- Enum filters reject invalid values through Spring MVC binding.
- Protocol config forms validate required endpoint, channel, path, and job fields.
- JSON configuration fields must be valid JSON where expected.
- SOAP request templates must be parseable XML where expected.
- File transfer paths reject traversal or unsafe local filesystem access.

## Non-Functional Requirements

- Java 21.
- Spring Boot 3.x.
- Gradle build.
- Local MySQL only.
- Flyway migration history is append-only.
- Thymeleaf admin UI.
- Windows-compatible commands.
- No real secrets in repository files.
- Local demo infrastructure should start from IntelliJ without Docker.
- Monitoring pages should use bounded summary windows and avoid obvious N+1 page rendering issues.

## Out Of Scope

- Production credential vaulting.
- External production MQ, SFTP, FTP, and scheduler operations.
- Distributed workers, distributed locks, and high-volume partitioned batch processing.
- Full audit workflow and approval workflow.
- Production alerting, tracing, SLOs, and long-term metric storage.
