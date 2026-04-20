# Insurance Interface Hub

Korean title: 보험사 금융 IT 인터페이스 통합관리시스템

Insurance Interface Hub is a Spring Boot portfolio project for centrally managing insurance and financial interfaces across multiple integration protocols. Phase 1 turns the Phase 0 skeleton into a usable admin operations console with form login and master data CRUD.

## Current Phase

Phase 1 - Admin authentication and interface master management CRUD

Implemented in this phase:

- DB-backed Spring Security form login
- Logout
- Seeded local demo admin account
- Partner company CRUD
- Internal system CRUD
- Interface definition CRUD
- Interface enable/disable
- Interface filtering by keyword, protocol, and status
- Thymeleaf admin pages with shared navigation and flash messages

Still intentionally out of scope:

- Real REST/SOAP/MQ/SFTP/FTP/Batch execution
- Protocol-specific endpoint setup screens
- Production-grade authorization rules
- External credential storage

## Supported Protocol Classification

Interface definitions can be classified as:

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
- Spring Batch, Spring Web Services, Spring Integration FTP/SFTP, and Artemis JMS dependencies reserved for later phases

## Local Run Guide

Prerequisites:

- JDK 21
- Local MySQL 8.x
- Windows PowerShell or IntelliJ IDEA terminal

Create a local database and user:

```sql
create database insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
create user 'insurance_hub_app'@'localhost' identified by 'change-me';
grant all privileges on insurance_hub.* to 'insurance_hub_app'@'localhost';
flush privileges;
```

Set environment variables before running:

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
- Admin dashboard: http://localhost:8080/admin
- Interface list: http://localhost:8080/admin/interfaces
- Smoke API: http://localhost:8080/api/smoke
- Actuator health: http://localhost:8080/actuator/health

## Local Demo Login

Flyway seeds a local demo admin user:

- Login ID: `admin`
- Password: `admin123!`

This is for local portfolio demos only. Change or remove the seed before using the project outside a local demo environment.

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
- Phase 2: common execution engine
- Phase 3: REST
- Phase 4: SOAP
- Phase 5: MQ
- Phase 6: SFTP/FTP
- Phase 7: Batch
- Phase 8: monitoring/dashboard
- Phase 9: testing/performance/final polish
