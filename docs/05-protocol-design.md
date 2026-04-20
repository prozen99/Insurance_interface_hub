# Protocol Design

## Phase 2 Purpose

Phase 2 introduces the protocol adapter contract without real external integration. The system can now execute an interface definition through a common engine and delegate to a protocol-specific mock executor.

## Executor Contract

`InterfaceExecutor` defines:

- `supports()`: returns the supported `ProtocolType`
- `execute(ExecutionRequest request)`: returns `ExecutionResult`

`InterfaceExecutorFactory` resolves the correct executor by protocol type.

## Current Mock Executors

| Protocol | Mock Executor |
| --- | --- |
| REST | `RestMockInterfaceExecutor` |
| SOAP | `SoapMockInterfaceExecutor` |
| MQ | `MqMockInterfaceExecutor` |
| BATCH | `BatchMockInterfaceExecutor` |
| SFTP | `SftpMockInterfaceExecutor` |
| FTP | `FtpMockInterfaceExecutor` |

## Mock Rule

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
