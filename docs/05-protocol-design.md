# Protocol Design

## Purpose

The protocol layer will allow one interface hub core to support multiple integration styles without duplicating execution history, retry, audit, and monitoring behavior.

## Supported Protocol Families

| Protocol | Future Responsibility |
| --- | --- |
| REST | HTTP request/response calls, partner callbacks, headers, timeouts |
| SOAP | XML SOAP requests, WSDL-aware clients, SOAP action handling |
| MQ | JMS queue send/receive, message correlation, asynchronous processing |
| Batch | Scheduled and manual batch execution |
| SFTP | Secure file upload/download and file pickup patterns |
| FTP | Legacy file transfer support where SFTP is not available |

## Common Execution Concept

Later phases should use a common execution service that:

1. Loads `interface_definition`.
2. Loads protocol-specific configuration.
3. Creates an `interface_execution`.
4. Delegates to the matching protocol adapter.
5. Records execution steps.
6. Creates retry tasks when needed.
7. Emits audit or monitoring events.

## Adapter Boundary

Protocol adapters should expose a small internal contract such as:

```java
public interface ProtocolExecutor {
    ProtocolType supports();
    ExecutionResult execute(ExecutionRequest request);
}
```

This contract is intentionally not implemented in Phase 0. The exact request and result models should be introduced when Phase 2 builds the common execution engine.

## Phase 0 Decisions

- Add dependencies for future protocol work.
- Create packages for each protocol family.
- Create protocol configuration tables in Flyway.
- Do not implement real network calls, queues, file sessions, or batch jobs yet.

## Future Safety Rules

- Store credentials by alias, not raw secret values.
- Use timeouts for every network operation.
- Make protocol execution idempotent where practical.
- Record enough execution detail for operators to troubleshoot without logging sensitive payloads.
