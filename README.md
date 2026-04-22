# Insurance Interface Hub

Korean title: 보험사 금융 IT 인터페이스 통합관리시스템

Insurance Interface Hub is a Spring Boot portfolio project for centrally managing insurance and financial interfaces across multiple integration protocols. Phase 7 adds real Spring Batch execution. REST, SOAP, MQ, SFTP, FTP, and BATCH now execute through local demo infrastructure and share one common execution history.

## Current Phase

Phase 7 - Real Batch integration, manual and scheduled job execution, and admin batch visibility

Implemented:

- DB-backed Spring Security form login
- Partner company, internal system, and interface definition CRUD
- Common execution engine, execution history, step logs, retry tasks, and dashboard metrics
- Real REST, SOAP, MQ, SFTP, FTP, and BATCH execution paths
- Embedded in-vm Artemis broker for MQ
- Embedded local SFTP and FTP demo servers without Docker
- Real Spring Batch jobs for interface settlement summary and failed retry aggregation
- Protocol-specific configuration UI for REST, SOAP, MQ, SFTP/FTP, and Batch
- File transfer history and batch run history
- Retry flow for failed executions across all supported protocols

Still intentionally out of scope:

- Production credential vaulting
- External MQ broker topology and durable production queues
- Production batch calendars, distributed locks, partitioning, and remote scheduling

## Demo Interfaces

- `IF_REST_POLICY_001`: REST premium calculation simulator
- `IF_SOAP_POLICY_001`: SOAP policy inquiry simulator
- `IF_MQ_POLICY_001`: embedded Artemis publish/consume demo
- `IF_SFTP_POLICY_001`: embedded SFTP upload/download demo
- `IF_FTP_POLICY_001`: embedded FTP upload/download demo
- `IF_BATCH_SETTLEMENT_001`: daily interface settlement summary batch
- `IF_BATCH_RETRY_AGG_001`: failed execution retry aggregation batch

## Local Demo Infrastructure

- MQ: embedded in-vm Artemis
- SFTP: `127.0.0.1:10022`
- FTP: `127.0.0.1:10021`
- File transfer runtime root: `build/file-transfer-demo`
- Batch output directory: `build/batch-demo/output`

Manual batch parameter sample:

```json
{"businessDate":"TODAY","forceFail":false}
```

Set `forceFail` to `true`, or include `FAIL` in the manual payload, to demonstrate a controlled batch failure and retry.

## Supported Protocol Classification

- REST: real local HTTP execution
- SOAP: real local SOAP-over-HTTP execution
- MQ: real local JMS publish/consume execution through embedded Artemis
- SFTP: real local upload/download through embedded SFTP server
- FTP: real local upload/download through embedded FTP server
- BATCH: real local Spring Batch job launch with run and step history

## Tech Baseline

- Java 21
- Spring Boot 3.x
- Gradle
- Thymeleaf
- Spring Security form login
- Spring Data JPA
- Spring Batch
- Spring JMS with embedded Artemis
- Spring Integration FTP/SFTP
- Apache MINA SSHD and Apache FtpServer for local demo servers
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
- Batch Runs: http://localhost:8080/admin/batch-runs
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
- Phase 3: real REST integration and simulator
- Phase 4: real SOAP integration and simulator
- Phase 5: real MQ integration with embedded Artemis
- Phase 6: real SFTP/FTP integration with local demo servers
- Phase 7: real Batch integration with manual and scheduled launch support
- Phase 8: monitoring/dashboard
- Phase 9: testing/performance/final polish
