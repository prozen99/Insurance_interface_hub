# Insurance Interface Hub

Korean title: 보험사 금융 IT 인터페이스 통합관리시스템

Insurance Interface Hub is a Spring Boot portfolio project for centrally managing insurance and financial interfaces across multiple integration protocols. Phase 3 adds a real REST execution path backed by a local simulator API while SOAP, MQ, BATCH, SFTP, and FTP intentionally remain mock-driven.

## Current Phase

Phase 3 - Real REST integration, REST-specific configuration UI, and troubleshooting documentation update

Implemented:

- DB-backed Spring Security form login
- Partner company, internal system, and interface definition CRUD
- Common execution engine, execution history, step logs, retry tasks, and dashboard metrics
- REST endpoint configuration UI per REST interface
- Real REST executor using Spring `RestClient`
- Local REST simulator endpoints under `/simulator/rest/**`
- REST request URL, method, headers, response status, response headers, payload, latency, and error persistence
- Retry flow for failed REST executions
- Mock executors retained for SOAP, MQ, BATCH, SFTP, and FTP

Still intentionally out of scope:

- Real SOAP XML mapping and SOAP client calls
- Artemis/JMS producer or consumer flows
- SFTP/FTP sessions or file upload/download
- Spring Batch scheduling and job launching

## REST Demo Flow

Seed data includes `IF_REST_POLICY_001` with a REST config targeting:

- `POST http://localhost:8080/simulator/rest/premium/calculate`

Additional simulator endpoints:

- `GET /simulator/rest/policy/{policyNo}`
- `POST /simulator/rest/claim/register`

The simulator returns success for normal requests and a controlled failure when the path variable or request body contains `FAIL`.

## Supported Protocol Classification

- REST: real local HTTP execution in Phase 3
- SOAP: mock executor
- MQ: mock executor
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
- REST simulator smoke target: http://localhost:8080/simulator/rest/policy/POL-001
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
- Phase 4: SOAP
- Phase 5: MQ
- Phase 6: SFTP/FTP
- Phase 7: Batch
- Phase 8: monitoring/dashboard
- Phase 9: testing/performance/final polish
