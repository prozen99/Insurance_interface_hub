# Batch Design

## Batch Role

Batch support handles scheduled or manually triggered interface executions that are not immediate request/response flows. Phase 7 implements this with Spring Batch and keeps results visible through the same admin execution history used by REST, SOAP, MQ, SFTP, and FTP.

## Phase 7 Components

- `BatchJobConfig`: DB-backed job configuration linked to an interface definition.
- `BatchInterfaceExecutor`: protocol executor for `ProtocolType.BATCH`.
- `BatchExecutionService`: launches Spring Batch jobs and records results.
- `BatchScheduleService`: optional local scheduler for enabled configs.
- `BatchRunHistory`: portfolio-facing batch run record.
- `BatchStepHistory`: step-level counts and status.
- Spring Batch metadata tables: framework-owned operational metadata created by Flyway.

## Demo Jobs

`interfaceSettlementSummaryJob`:

- Summarizes today’s interface executions by protocol and status.
- Writes a local output text file.
- Records read/write counts.

`failedExecutionRetryAggregationJob`:

- Counts today’s failed executions.
- Counts pending retry tasks.
- Writes a local output text file.

## Job Parameters

Default manual payload:

```json
{"businessDate":"TODAY","forceFail":false}
```

Rules:

- `businessDate=TODAY` resolves to the local current date.
- `forceFail=true` triggers a controlled job failure.
- A unique `run.id` is added automatically so reruns do not collide.

## Scheduling

Scheduling is disabled by default:

```yaml
app:
  batch:
    scheduler:
      enabled: false
```

To demo scheduling:

1. Enable `app.batch.scheduler.enabled`.
2. Enable a batch config in the admin UI.
3. Use a short cron such as `0/30 * * * * *`.

## Design Rules

- Batch jobs create normal `interface_execution` records through the common engine.
- Spring Batch metadata is managed by Flyway, not automatic initialization.
- Batch launch must not happen inside a broad interface execution transaction.
- Retry reruns the original failed payload for auditability.
- Production calendars, locks, partitions, and external schedulers are future concerns.
