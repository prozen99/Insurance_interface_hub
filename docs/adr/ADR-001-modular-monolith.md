# ADR-001: Use A Modular Monolith

## Status

Accepted

## Context

Insurance Interface Hub will eventually support several integration protocols: REST, SOAP, MQ, Batch, SFTP, and FTP. Each protocol has different implementation details, but they share core operational concepts:

- Interface registration
- Execution history
- Retry state
- Monitoring
- Audit logs

The project is a portfolio application and must remain easy to run locally. Splitting into multiple services too early would add deployment, networking, security, and data consistency overhead before the domain boundaries are proven.

## Decision

Start as one Spring Boot application organized as a modular monolith.

The root package is `com.insurancehub`. Protocol code lives under `com.insurancehub.protocol.*`, while shared interface registration and execution concepts live under `com.insurancehub.interfacehub`.

## Consequences

Positive:

- Simple local development.
- One Gradle build.
- One MySQL database.
- Clear internal module boundaries without distributed-system overhead.
- Easier interview walkthrough.

Tradeoffs:

- Module boundaries rely on package discipline until stricter architecture tests are added.
- A single deployment unit may become large if future phases grow significantly.
- Scaling is application-wide at first.

## Follow-Up

Later phases may add architecture tests to enforce dependency direction. If one protocol becomes independently deployable and operationally distinct, it can be extracted after the shared domain model is stable.
