# Product Vision

## Project Identity

- English title: Insurance Interface Hub
- Korean title: &#48372;&#54744;&#49324; &#44552;&#50997; IT &#51064;&#53552;&#54168;&#51060;&#49828; &#53685;&#54633;&#44288;&#47532;&#49884;&#49828;&#53596;
- Current phase: Phase 9 - final polish and portfolio submission readiness

Insurance Interface Hub is a centralized operations console for insurance and financial interfaces. It demonstrates how enterprise integrations can be registered, configured, executed, monitored, retried, and reviewed from one Spring Boot modular monolith.

## Problem

Insurance companies often operate many point-to-point integrations between internal systems, partner insurers, banks, payment vendors, call centers, and data providers. These interfaces commonly use mixed protocols such as REST, SOAP, MQ, Batch, SFTP, and FTP.

Without a central hub, teams struggle to answer operational questions quickly:

- Which systems own an interface?
- Which protocol and endpoint does it use?
- Did today's execution succeed?
- Which file transfer failed?
- Is a retry pending?
- Which batch job ran and what did it process?
- Where can an operator inspect request, response, latency, and errors?

## Product Goal

Build a single Spring Boot application that feels like a realistic insurance interface operations hub. The system makes interface master data visible, executes each supported protocol through local demo infrastructure, records execution history consistently, and provides monitoring views for demo and evaluator review.

## Target Users

- Interface operations engineers
- Backend developers maintaining insurance integrations
- Batch operators
- IT auditors
- Technical interviewers reviewing the portfolio

## Final Implemented Scope

- Java 21, Spring Boot 3.x, Gradle, Thymeleaf, Spring Security, JPA, Flyway, and local MySQL
- DB-backed admin login with BCrypt password storage
- Partner company, internal system, and interface definition CRUD
- Common execution engine with execution history, step logs, retry tasks, and detail pages
- Real local REST execution with simulator endpoints
- Real local SOAP execution with simulator endpoints
- Real local MQ publish/consume with embedded Artemis
- Real local SFTP and FTP upload/download flows
- Real local Spring Batch manual and scheduled execution support
- Operations dashboard and monitoring pages for failures, retries, protocols, file transfer, MQ, and batch
- Documentation, troubleshooting history, runbook, and demo scenarios for portfolio submission

## Product Principles

- Keep one application until module boundaries are proven.
- Prefer clean, interview-friendly code over clever abstractions.
- Treat Flyway as the owner of database schema changes.
- Keep secrets out of source control.
- Design protocol modules around shared execution concepts.
- Make the first five minutes of demo flow obvious: login, dashboard, interface detail, execution, history, failure, retry, monitoring.

## Known Production Gaps

This portfolio project is intentionally local-demo focused. Production adoption would need external secret management, production broker/server infrastructure, alert delivery, distributed scheduling/locking, audit hardening, and deeper performance testing.
