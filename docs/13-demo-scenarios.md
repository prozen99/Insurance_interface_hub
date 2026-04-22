# Demo Scenarios

## Scenario 1 - Project Orientation

1. Open `README.md`.
2. Explain the business goal.
3. Show the phase roadmap.
4. Show the common execution engine.
5. Show protocol packages for REST, SOAP, MQ, SFTP, FTP, and BATCH.

What this proves:

- The project has clear business context.
- All six planned protocol paths now have real local execution.

## Scenario 2 - Login And Dashboard

1. Start the app with the local profile.
2. Open `/login`.
3. Log in with `admin` / `admin123!`.
4. Open `/admin`.
5. Point out primary metrics, quick links, 7-day trend, protocol cards, top failures, recent executions, and pending retries.

What this proves:

- DB-backed form login still works.
- Dashboard shows an operations-center overview.

## Scenario 3 - Existing Protocol Regression

1. Open `IF_REST_POLICY_001` and execute the sample payload.
2. Open `IF_SOAP_POLICY_001` and execute the sample XML.
3. Open `IF_MQ_POLICY_001` and execute the sample message.
4. Open `IF_SFTP_POLICY_001` and run upload/download.
5. Open `IF_FTP_POLICY_001` and run upload/download.

What this proves:

- Phase 8 did not break existing real protocols.

## Scenario 4 - Batch Configuration

1. Open `IF_BATCH_SETTLEMENT_001`.
2. Review the Batch settings panel.
3. Click Configure Batch.
4. Confirm job type, job name, cron expression, active flag, schedule enabled flag, and parameter template.

What this proves:

- Batch-specific settings are visible and editable.

## Scenario 5 - Manual Batch Success

1. Open `IF_BATCH_SETTLEMENT_001`.
2. Execute with `{"businessDate":"TODAY","forceFail":false}`.
3. Open execution detail.
4. Review Batch run history, step logs, read/write counts, and output summary.
5. Open `/admin/batch-runs`.

What this proves:

- Manual BATCH execution launches a real Spring Batch job.
- Batch results are persisted and visible to operators.

## Scenario 6 - Batch Failure And Retry

1. Open `IF_BATCH_SETTLEMENT_001`.
2. Execute with `{"businessDate":"TODAY","forceFail":true}`.
3. Confirm execution status is FAILED.
4. Open execution detail.
5. Change the original payload path by retrying after using a non-failing payload in a new run, or demo retry with a failure payload to explain deterministic failure.

What this proves:

- Batch failure is captured with an error message and retry task.
- Rerun/retry behavior is understandable from execution history.

## Scenario 7 - Scheduled Batch Demo

1. Set `app.batch.scheduler.enabled=true`.
2. Enable a batch config in the Batch config form.
3. Use cron `0/30 * * * * *`.
4. Wait for the scheduler poll.
5. Open `/admin/batch-runs`.

What this proves:

- Scheduled execution uses the same common execution and history model as manual execution.

## Scenario 8 - Unified Execution History

1. Open `/admin/executions`.
2. Filter by BATCH.
3. Filter by `MANUAL`, `RETRY`, or `SCHEDULED`.
4. Use a date range filter.
5. Open a Batch execution detail.
6. Compare it with REST, SOAP, MQ, SFTP, or FTP execution details.

What this proves:

- Operators can inspect every protocol through one consistent execution history.
- Operators can narrow the list quickly during incident review.

## Scenario 9 - Monitoring Pages

1. Open `/admin/monitoring`.
2. Open `/admin/monitoring/failures`.
3. Open `/admin/monitoring/retries`.
4. Open `/admin/monitoring/protocols`.
5. Open `/admin/monitoring/files`.
6. Open `/admin/monitoring/mq`.
7. Open `/admin/monitoring/batch`.

What this proves:

- Operators can move from broad operational status to focused protocol and incident views.
- Monitoring is unified without adding a separate frontend stack.
