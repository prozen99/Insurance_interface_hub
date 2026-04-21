# Protocol Design

## Phase 3 Purpose

Phase 3 proves one real protocol end to end. REST now uses a real HTTP execution path through a local simulator API. The common execution engine, retry behavior, and execution history stay protocol-agnostic.

## Executor Contract

`InterfaceExecutor` defines:

- `supports()`: returns the supported `ProtocolType`
- `execute(ExecutionRequest request)`: returns `ExecutionResult`

`InterfaceExecutorFactory` resolves the correct executor by protocol type.

## Current Executors

| Protocol | Executor | Phase 3 Behavior |
| --- | --- | --- |
| REST | `RestInterfaceExecutor` | Sends real HTTP calls with Spring `RestClient` |
| SOAP | `SoapMockInterfaceExecutor` | Mock |
| MQ | `MqMockInterfaceExecutor` | Mock |
| BATCH | `BatchMockInterfaceExecutor` | Mock |
| SFTP | `SftpMockInterfaceExecutor` | Mock |
| FTP | `FtpMockInterfaceExecutor` | Mock |

## REST Execution Rules

`RestInterfaceExecutor`:

1. Loads active `RestEndpointConfig`.
2. Builds the URL from `baseUrl + path`.
3. Parses `headersJson` into HTTP headers.
4. Sends GET or POST using Spring `RestClient`.
5. Applies the configured timeout.
6. Captures request URL, method, headers, response status, response headers, body, and latency.
7. Returns SUCCESS for HTTP 2xx.
8. Returns FAILED for HTTP non-2xx or client errors.

REST does not use the old mock `FAIL` rule directly. The local simulator applies the controlled failure rule so the execution still performs a real HTTP round trip.

## Local Simulator Rule

- Normal request: HTTP 200 and a JSON success payload.
- Request body or path variable containing `FAIL`: HTTP 422 and a JSON failure payload.

Simulator endpoints:

- `POST /simulator/rest/premium/calculate`
- `GET /simulator/rest/policy/{policyNo}`
- `POST /simulator/rest/claim/register`

## Mock Rule For Non-REST Protocols

- If interface code contains `FAIL`, return failure.
- If request payload contains `FAIL`, return failure.
- Otherwise return success.

Every mock executor records step logs:

1. Load interface definition.
2. Resolve protocol mock adapter.
3. Execute mock protocol call.

## Future Real Adapter Rules

When real adapters are added:

- Keep `InterfaceExecutionService` as the orchestration layer.
- Keep protocol-specific network details inside `com.insurancehub.protocol.*`.
- Record execution steps consistently.
- Avoid logging secrets or sensitive payloads.
- Use timeouts for all external calls.
- Preserve the same retry behavior unless a protocol needs documented exceptions.
