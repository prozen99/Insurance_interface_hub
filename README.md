# Insurance Interface Hub

Korean title: 보험사 금융 IT 인터페이스 통합관리시스템

Insurance Interface Hub is a Spring Boot portfolio project for centrally managing insurance and financial interfaces across multiple integration protocols. Phase 5 adds MQ as the third real protocol path. REST, SOAP, and MQ now execute through real local demo infrastructure, while BATCH, SFTP, and FTP intentionally remain mock-driven.

## Current Phase

Phase 5 - Real MQ integration, message publish/consume flow, and admin monitoring pages

Implemented:

- DB-backed Spring Security form login
- Partner company, internal system, and interface definition CRUD
- Common execution engine, execution history, step logs, retry tasks, and dashboard metrics
- REST endpoint configuration UI and real REST executor
- SOAP endpoint configuration UI and real SOAP-over-HTTP executor
- MQ channel configuration UI and real JMS publish/consume executor
- Embedded in-vm Artemis broker for local demo use without Docker
- Local REST and SOAP simulator endpoints under `/simulator/**`
- REST/SOAP/MQ request, response, action, status, payload, latency, and error persistence
- Retry flow for failed REST, SOAP, and MQ executions
- Mock executors retained for BATCH, SFTP, and FTP

Still intentionally out of scope:

- Real SFTP/FTP sessions or file upload/download
- Spring Batch scheduling and job launching
- Production broker topology, durable queues, DLQ policy, or external MQ credentials

## REST Demo Flow

Seed data includes `IF_REST_POLICY_001` with a REST config targeting:

- `POST http://localhost:8080/simulator/rest/premium/calculate`

## SOAP Demo Flow

Seed data includes `IF_SOAP_POLICY_001` with a SOAP config targeting:

- `POST http://localhost:8080/simulator/soap/policy-inquiry`
- SOAPAction: `urn:PolicyInquiry`

The SOAP simulator returns HTTP 200 with a SOAP response for normal XML and HTTP 500 with a SOAP fault when the XML contains `FAIL`.

## MQ Demo Flow

Seed data includes `IF_MQ_POLICY_001` with an MQ config targeting:

- Broker type: `EMBEDDED_ARTEMIS`
- Destination: `insurancehub.demo.policy.events`
- Correlation key expression: `MQ-{executionNo}`

The app starts an embedded in-vm Artemis broker by default through `app.mq.embedded.enabled=true`. Manual MQ execution publishes a text message, consumes it by correlation key, records publish/consume status, and fails deterministically when the consumed payload contains `FAIL`.

## Supported Protocol Classification

- REST: real local HTTP execution
- SOAP: real local SOAP-over-HTTP execution
- MQ: real local JMS publish/consume execution through embedded Artemis
- BATCH: mock executor
- SFTP: mock executor
- FTP: mock executor

## Tech Baseline

- Java 21
- Spring Boot 3.x
- Gradle
- Thymeleaf
- Spring Security form login
- Spring Data JPA
- Spring JMS with embedded Artemis
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
- REST simulator: http://localhost:8080/simulator/rest/policy/POL-001
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
- Phase 6: SFTP/FTP
- Phase 7: Batch
- Phase 8: monitoring/dashboard
- Phase 9: testing/performance/final polish
