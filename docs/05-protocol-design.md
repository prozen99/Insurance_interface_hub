# Protocol Design

## Phase 7 Purpose

Phase 7 proves Batch as the final real protocol path. REST remains real from Phase 3, SOAP from Phase 4, MQ from Phase 5, and SFTP/FTP from Phase 6.

## Current Executors

| Protocol | Executor | Phase 7 Behavior |
| --- | --- | --- |
| REST | `RestInterfaceExecutor` | Real HTTP calls with Spring `RestClient` |
| SOAP | `SoapInterfaceExecutor` | Real SOAP XML over HTTP |
| MQ | `MqInterfaceExecutor` | Real JMS text message publish/consume through embedded Artemis |
| SFTP | `SftpInterfaceExecutor` | Real upload/download through embedded SFTP server |
| FTP | `FtpInterfaceExecutor` | Real upload/download through embedded FTP server |
| BATCH | `BatchInterfaceExecutor` | Real Spring Batch job launch |

## Batch Execution Rules

`BatchExecutionService`:

1. Loads active Batch configuration.
2. Resolves the configured Spring Batch job by name.
3. Parses manual payload or parameter template JSON.
4. Adds unique run parameters for rerun support.
5. Launches the job through `JobLauncher`.
6. Records `BatchRunHistory`.
7. Records `BatchStepHistory`.
8. Returns SUCCESS when Spring Batch status is `COMPLETED`.
9. Returns FAILED for launch errors, job failures, invalid parameters, or unknown job names.

## Demo Batch Jobs

`interfaceSettlementSummaryJob`:

- Summarizes today’s executions by protocol and status.
- Writes output under `build/batch-demo/output`.
- Records read/write counts from summary rows.

`failedExecutionRetryAggregationJob`:

- Counts today’s failed executions.
- Counts pending retry tasks.
- Writes output under `build/batch-demo/output`.

Controlled failure:

- `{"forceFail":true}` fails the job.
- A manual payload containing `FAIL` also sets `forceFail=true`.

## Scheduling Rules

Scheduling is intentionally local-demo-friendly:

- `app.batch.scheduler.enabled=false` by default.
- Admins can enable a batch config in the UI.
- When the app scheduler property is enabled, `BatchScheduleService` polls enabled configs.
- Scheduled runs use the same common execution path as manual runs.

## Existing Protocol Rules

- REST treats HTTP 2xx as success.
- SOAP treats HTTP 2xx without SOAP Fault as success.
- MQ distinguishes publish success from consume/process success.
- SFTP/FTP record transfer metadata and reject unsafe paths.
- BATCH records Spring Batch job and step metadata.
