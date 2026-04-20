# Batch Design

## Batch Role

Batch support will handle scheduled or manually triggered interface executions that are not immediate request/response flows. Examples include daily policy exports, monthly settlement files, partner reconciliation, and retry sweeps.

## Phase 0 Baseline

Phase 0 includes:

- `spring-boot-starter-batch`
- `batch_job_config` table
- Batch package placeholder
- `spring.batch.job.enabled=false` to avoid accidental startup execution

No real batch job is implemented yet.

## Future Batch Components

- Batch job registry
- Job parameter builder
- Manual run service
- Scheduler integration
- Execution history integration
- Failure retry integration
- Operator-visible run logs

## Batch Job Configuration

`batch_job_config` stores:

- Linked interface definition
- Job name
- Cron expression
- Enabled flag
- Max parallel count

## Design Rules

- Batch jobs should create `interface_execution` rows before doing business work.
- Long-running jobs should record major steps in `interface_execution_step`.
- Batch jobs should not store raw file credentials.
- Schedules should be disabled by default until explicitly enabled by an admin.
- Job restart behavior must be documented when real jobs are added.
