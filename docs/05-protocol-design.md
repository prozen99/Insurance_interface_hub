# Protocol Design

## Phase 4 Purpose

Phase 4 proves SOAP as the second real protocol path. REST remains real from Phase 3. MQ, BATCH, SFTP, and FTP remain mock-driven.

## Executor Contract

`InterfaceExecutor` defines:

- `supports()`: returns the supported `ProtocolType`
- `execute(ExecutionRequest request)`: returns `ExecutionResult`

`InterfaceExecutorFactory` resolves the correct executor by protocol type.

## Current Executors

| Protocol | Executor | Phase 4 Behavior |
| --- | --- | --- |
| REST | `RestInterfaceExecutor` | Sends real HTTP calls with Spring `RestClient` |
| SOAP | `SoapInterfaceExecutor` | Sends real SOAP XML over HTTP to the local simulator |
| MQ | `MqMockInterfaceExecutor` | Mock |
| BATCH | `BatchMockInterfaceExecutor` | Mock |
| SFTP | `SftpMockInterfaceExecutor` | Mock |
| FTP | `FtpMockInterfaceExecutor` | Mock |

## SOAP Execution Rules

`SoapInterfaceExecutor`:

1. Loads active `SoapEndpointConfig`.
2. Uses the manual request XML when provided; otherwise it uses the configured template.
3. Sends HTTP POST with `Content-Type: text/xml`.
4. Sends `SOAPAction` when configured.
5. Applies the configured timeout.
6. Captures endpoint URL, SOAPAction, request headers, response status, response headers, response XML, and latency.
7. Returns SUCCESS for HTTP 2xx without a SOAP Fault.
8. Returns FAILED for SOAP faults, HTTP non-2xx, or client errors.

## Local SOAP Simulator Rule

- Normal SOAP XML: HTTP 200 with a SOAP success envelope.
- SOAP XML containing `FAIL`: HTTP 500 with a SOAP fault.

Simulator endpoints:

- `POST /simulator/soap/policy-inquiry`
- `POST /simulator/soap/claim-status`
- `POST /simulator/soap/premium-confirmation`

## REST Rule

REST remains unchanged from Phase 3. It sends a real HTTP request to the configured endpoint and treats HTTP 2xx as success.

## Mock Rule For Remaining Protocols

- If interface code contains `FAIL`, return failure.
- If request payload contains `FAIL`, return failure.
- Otherwise return success.

## Future Real Adapter Rules

When MQ, SFTP/FTP, and Batch are added:

- Keep `InterfaceExecutionService` as the orchestration layer.
- Keep protocol-specific network details inside `com.insurancehub.protocol.*`.
- Record execution steps consistently.
- Avoid logging secrets or sensitive payloads.
- Use timeouts for all external calls.
- Preserve retry behavior unless a protocol needs documented exceptions.
