# Requirements

## Functional Scope

Insurance Interface Hub is expected to grow into a central operations hub for:

- Interface registration
- Protocol-specific configuration
- Manual execution
- Scheduled batch execution
- Execution history
- File transfer history
- Failure retry
- Monitoring dashboard
- Audit logs

## Phase 1 Functional Requirements

Phase 1 makes the admin console usable for master data management.

Admin authentication:

- Admin users can log in from `/login`.
- Spring Security handles form login and logout.
- Unauthenticated users are redirected to login before accessing `/admin/**`.
- Passwords are stored as BCrypt hashes.
- A local demo admin account is seeded by Flyway.

Master data CRUD:

- Operators can create and edit partner companies.
- Operators can create and edit internal systems.
- Operators can create, edit, view, activate, and deactivate interface definitions.
- Interface definitions can be classified by protocol type: REST, SOAP, MQ, BATCH, SFTP, FTP.
- Interface definitions can be classified by direction: OUTBOUND, INBOUND, BIDIRECTIONAL.
- Interface list supports keyword, protocol, and status filtering.

## Validation Requirements

- Codes and names are required.
- Codes are unique.
- Codes allow letters, numbers, underscore, and hyphen.
- Status, protocol type, direction, partner company, and internal system are validated server-side.
- Validation errors are rendered on Thymeleaf forms.

## Phase 1 Non-Functional Requirements

- Java 21.
- Spring Boot 3.x.
- Gradle build.
- Windows-compatible local run commands.
- Local MySQL only.
- Flyway owns schema changes.
- No real secrets in repository files.
- Clear layered organization inside the modular monolith.

## Out Of Scope For Phase 1

- Real protocol execution.
- SOAP XML mapping.
- MQ producer or consumer flows.
- SFTP/FTP network transfer.
- Spring Batch job implementation.
- Production-grade role management.
- External secret vault integration.
