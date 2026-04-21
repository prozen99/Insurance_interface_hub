# Protocol Design

## Phase 5 Purpose

Phase 5 proves MQ as the third real protocol path. REST remains real from Phase 3. SOAP remains real from Phase 4. BATCH, SFTP, and FTP remain mock-driven.

## Executor Contract

`InterfaceExecutor` defines:

- `supports()`: returns the supported `ProtocolType`
- `execute(ExecutionRequest request)`: returns `ExecutionResult`

`InterfaceExecutorFactory` resolves the correct executor by protocol type.

## Current Executors

| Protocol | Executor | Phase 5 Behavior |
| --- | --- | --- |
| REST | `RestInterfaceExecutor` | Sends real HTTP calls with Spring `RestClient` |
| SOAP | `SoapInterfaceExecutor` | Sends real SOAP XML over HTTP to the local simulator |
| MQ | `MqInterfaceExecutor` | Publishes and consumes a real JMS text message through embedded Artemis |
| BATCH | `BatchMockInterfaceExecutor` | Mock |
| SFTP | `SftpMockInterfaceExecutor` | Mock |
| FTP | `FtpMockInterfaceExecutor` | Mock |

## MQ Execution Rules

`MqInterfaceExecutor`:

1. Loads active `MqChannelConfig`.
2. Uses the manual request payload when provided; otherwise it uses the sample MQ payload.
3. Resolves the correlation key from `correlationKeyExpression`.
4. Creates `MqMessageHistory` with PENDING publish/consume statuses.
5. Publishes a JMS text message to the configured destination.
6. Consumes a message from the same destination by `JMSCorrelationID`.
7. Marks publish and consume statuses separately.
8. Captures destination, message id, correlation key, payloads, latency, and error details.
9. Returns SUCCESS when publish and consumer processing both succeed.
10. Returns FAILED when publish fails, consume times out, or consumed payload contains `FAIL`.

## Local MQ Broker Rule

Phase 5 uses an embedded in-vm Artemis broker:

- Enabled by `app.mq.embedded.enabled=true`
- Server id configured with `app.mq.embedded.server-id`
- No Docker
- No external broker install
- No production credentials

The broker starts inside the Spring Boot process. This is intentionally demo-friendly and should be replaced by external broker configuration in a production phase.

## SOAP Execution Rules

SOAP remains unchanged from Phase 4:

- Sends HTTP POST with `Content-Type: text/xml`.
- Sends `SOAPAction` when configured.
- Treats HTTP 2xx without SOAP Fault as success.
- Treats SOAP Faults, HTTP non-2xx, or client errors as failure.

## REST Rule

REST remains unchanged from Phase 3. It sends a real HTTP request to the configured endpoint and treats HTTP 2xx as success.

## Mock Rule For Remaining Protocols

- If interface code contains `FAIL`, return failure.
- If request payload contains `FAIL`, return failure.
- Otherwise return success.

## Future Real Adapter Rules

When SFTP/FTP and Batch are added:

- Keep `InterfaceExecutionService` as the orchestration layer.
- Keep protocol-specific network details inside `com.insurancehub.protocol.*`.
- Record execution steps consistently.
- Avoid logging secrets or sensitive payloads.
- Use timeouts for all external calls.
- Preserve retry behavior unless a protocol needs documented exceptions.
