# Protocol Design

## Phase 6 Purpose

Phase 6 proves SFTP and FTP as real file-transfer protocol paths. REST remains real from Phase 3, SOAP from Phase 4, and MQ from Phase 5. BATCH remains mock-driven.

## Current Executors

| Protocol | Executor | Phase 6 Behavior |
| --- | --- | --- |
| REST | `RestInterfaceExecutor` | Real HTTP calls with Spring `RestClient` |
| SOAP | `SoapInterfaceExecutor` | Real SOAP XML over HTTP |
| MQ | `MqInterfaceExecutor` | Real JMS text message publish/consume through embedded Artemis |
| SFTP | `SftpInterfaceExecutor` | Real upload/download through embedded SFTP server |
| FTP | `FtpInterfaceExecutor` | Real upload/download through embedded FTP server |
| BATCH | `BatchMockInterfaceExecutor` | Mock |

## File Transfer Execution Rules

`FileTransferExecutionService`:

1. Loads active SFTP/FTP configuration.
2. Parses transfer direction, local file name, and remote path from the execution payload.
3. Resolves local files under `build/file-transfer-demo/local`.
4. Uses a protocol-specific client selected by `FileTransferClientFactory`.
5. Executes upload or download.
6. Records `FileTransferHistory`.
7. Returns SUCCESS with size, checksum, summary, and latency.
8. Returns FAILED for missing local files, path traversal attempts, connection errors, transfer errors, or missing remote files.

## Local Demo Servers

SFTP:

- Embedded Apache MINA SSHD server
- Default host `127.0.0.1`
- Default port `10022`

FTP:

- Embedded Apache FtpServer
- Default host `127.0.0.1`
- Default port `10021`
- Passive mode enabled by default

Both servers use local demo directories under `build/file-transfer-demo` and a local-only credential reference. No Docker or external server install is required.

## Safe Local File Strategy

- Upload reads from `build/file-transfer-demo/local/input`
- Download writes to `build/file-transfer-demo/local/download`
- SFTP remote root is `build/file-transfer-demo/remote/sftp`
- FTP remote root is `build/file-transfer-demo/remote/ftp`
- Local file names must be simple file names, not paths
- Remote paths cannot contain `..`

## Existing Protocol Rules

- REST treats HTTP 2xx as success.
- SOAP treats HTTP 2xx without SOAP Fault as success.
- MQ distinguishes publish success from consume/process success.
- BATCH uses the deterministic mock rule.

## Future Real Adapter Rules

When Batch is added:

- Keep `InterfaceExecutionService` as the orchestration layer.
- Keep protocol-specific details inside `com.insurancehub.protocol.*`.
- Record execution steps consistently.
- Avoid logging secrets or sensitive payloads.
- Use timeouts for external calls.
- Preserve retry behavior unless a protocol needs documented exceptions.
