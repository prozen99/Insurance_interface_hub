# Product Vision

## Project Identity

- English title: Insurance Interface Hub
- Korean title: 보험사 금융 IT 인터페이스 통합관리시스템
- Phase: Phase 0 - foundation, documentation baseline, local bootable skeleton

Insurance Interface Hub is a centralized operations platform for insurance and financial interface management. It is designed as a realistic backend portfolio project that shows how enterprise integrations can be registered, configured, executed, monitored, retried, and audited from one place.

## Problem

Insurance companies often operate many point-to-point integrations between internal systems, partner insurers, banks, payment vendors, call centers, and data providers. These interfaces commonly use mixed protocols such as REST, SOAP, MQ, Batch, SFTP, and FTP.

Without a central hub, teams struggle to answer operational questions quickly:

- Which systems own an interface?
- Which protocol and endpoint does it use?
- Did today's execution succeed?
- Which file transfer failed?
- Is a retry pending?
- Who changed a configuration?

## Product Goal

Build a single Spring Boot application that can evolve into an interface operations hub. The system should make interface metadata visible, keep execution records consistent, and provide a foundation for protocol-specific adapters without turning into separate services too early.

## Target Users

- Interface operations engineers
- Backend developers maintaining insurance integrations
- Batch operators
- IT auditors
- Technical interviewers reviewing the portfolio

## Phase 0 Scope

Phase 0 must prove that the project is serious and bootable:

- Java 21 and Spring Boot 3.x baseline
- Local MySQL configuration
- Flyway-managed schema baseline
- Modular monolith package structure
- Minimal admin dashboard placeholder
- Smoke API and actuator health endpoint
- Documentation set for later implementation phases

Phase 0 intentionally does not implement production authentication, real protocol execution, file transfer, queue consumers, SOAP mappings, or business workflows.

## Product Principles

- Keep one application until module boundaries are proven.
- Prefer clean, interview-friendly code over clever abstractions.
- Treat Flyway as the owner of database schema changes.
- Keep secrets out of source control.
- Design protocol modules around shared execution concepts.
- Make future phases demonstrable in small increments.
