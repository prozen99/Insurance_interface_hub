# Insurance Interface Hub

Korean title: 보험사 금융 IT 인터페이스 통합관리시스템

Insurance Interface Hub is a Spring Boot portfolio project for centrally managing insurance and financial interfaces across multiple integration protocols. Phase 2 adds a protocol-agnostic execution engine, mock protocol executors, execution history, failure handling, and retry.

## Current Phase

Phase 2 - Common execution engine, execution history, failure handling, and retry

Implemented:

- DB-backed Spring Security form login
- Partner company CRUD
- Internal system CRUD
- Interface definition CRUD
- Manual execution from interface detail
- Protocol-specific mock executors for REST, SOAP, MQ, BATCH, SFTP, and FTP
- Execution history list and detail page
- Execution step logs
- Failure status and retry task creation
- Retry action for failed executions
- Dashboard metrics for today success, today failure, and pending retries

Still intentionally out of scope:

- Real HTTP calls
- SOAP clients or XML mapping
- Artemis/JMS producer or consumer flows
- SFTP/FTP sessions or file upload/download
- Spring Batch scheduling

## Mock Execution Rule

Phase 2 is mock-driven. If the interface code or request payload contains `FAIL`, execution fails. Otherwise, execution succeeds.

## Supported Protocol Classification

- REST
- SOAP
- MQ
- BATCH
- SFTP
- FTP

## Tech Baseline

- Java 21
- Spring Boot 3.x
- Gradle
- Thymeleaf
- Spring Security form login
- Spring Data JPA
- Flyway
- Local MySQL

## Local Run Guide

Create a local database and user:

```sql
create database if not exists insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
create user if not exists 'insurance_hub_app'@'localhost' identified by 'change-me';
alter user 'insurance_hub_app'@'localhost' identified by 'change-me';
grant all privileges on insurance_hub.* to 'insurance_hub_app'@'localhost';
flush privileges;
```

Set environment variables:

```powershell
$env:INSURANCE_HUB_DB_URL="jdbc:mysql://localhost:3306/insurance_hub?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true"
$env:INSURANCE_HUB_DB_USERNAME="insurance_hub_app"
$env:INSURANCE_HUB_DB_PASSWORD="change-me"
```

Build and run:

```powershell
.\gradlew.bat build
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

Open:

- Login: http://localhost:8080/login
- Dashboard: http://localhost:8080/admin
- Interfaces: http://localhost:8080/admin/interfaces
- Executions: http://localhost:8080/admin/executions
- Smoke API: http://localhost:8080/api/smoke

## Local Demo Login

Flyway seeds a local demo admin user:

- Login ID: `admin`
- Password: `admin123!`

The database stores a BCrypt hash, not the plain password.

## Document Index

- [Product vision](docs/00-product-vision.md)
- [Requirements](docs/01-requirements.md)
- [Architecture](docs/02-architecture.md)
- [ERD](docs/03-erd.md)
- [API spec](docs/04-api-spec.md)
- [Protocol design](docs/05-protocol-design.md)
- [Screen spec](docs/06-screen-spec.md)
- [Batch design](docs/07-batch-design.md)
- [Test strategy](docs/08-test-strategy.md)
- [Troubleshooting](docs/09-troubleshooting.md)
- [Phase plan](docs/10-phase-plan.md)
- [Commit rules](docs/11-commit-rules.md)
- [Local runbook](docs/12-local-runbook.md)
- [Demo scenarios](docs/13-demo-scenarios.md)
- [ADR-001 Modular Monolith](docs/adr/ADR-001-modular-monolith.md)

## Phase Roadmap

- Phase 0: foundation, documentation baseline, local bootable skeleton
- Phase 1: admin authentication and master CRUD
- Phase 2: common execution engine, history, failure handling, retry
- Phase 3: REST
- Phase 4: SOAP
- Phase 5: MQ
- Phase 6: SFTP/FTP
- Phase 7: Batch
- Phase 8: monitoring/dashboard
- Phase 9: testing/performance/final polish
