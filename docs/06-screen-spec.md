# Screen Spec

Phase 7 uses Thymeleaf for the admin UI and adds Batch configuration, manual batch execution, batch run history, and batch result visibility.

## Common Layout

Admin pages share:

- Left navigation
- Phase 7 product branding
- Logout button
- Flash success/error messages
- Table-based enterprise admin layout
- Batch Runs navigation item

## Interface Detail

Path: `/admin/interfaces/{id}`

Sections:

- Interface summary
- Protocol-specific settings panel
- Manual execution form
- Recent execution table

Batch interface behavior:

- Shows job type, job name, cron expression, schedule enabled flag, retryable flag, timeout, active flag, and parameter template JSON.
- Provides an Edit Batch config button.
- Uses the generic request payload textarea for job parameters.
- Defaults to the configured parameter template.

## Batch Config Form

Path: `/admin/interfaces/{id}/batch-config`

Fields:

- Job type
- Spring Batch job name
- Cron expression
- Timeout millis
- Max parallel count
- Schedule enabled
- Retryable
- Active for manual execution
- Parameter template JSON

Validation:

- Job type and job name are required.
- Parameter template must be valid JSON.
- Timeout must be between 1000 and 3600000 ms.
- Phase 7 keeps max parallel count at 1.

## Manual Batch Execution

Payload example:

```json
{"businessDate":"TODAY","forceFail":false}
```

Failure demo:

```json
{"businessDate":"TODAY","forceFail":true}
```

## Execution Detail

Path: `/admin/executions/{id}`

Sections:

- Execution summary
- Protocol exchange details
- Batch run history for BATCH executions
- File transfer history for SFTP/FTP executions
- MQ message history for MQ executions
- Step logs
- Request payload
- Response payload
- Retry tasks

Batch detail shows:

- Job name
- Job type
- Batch status
- Read/write/skip counts
- Latency
- Output summary
- Error message
- Link to the Batch Run detail page

## Batch Run History

Path: `/admin/batch-runs`

Shows recent batch runs with:

- Job name and type
- Interface code
- Status
- Read/write/skip counts
- Start time
- Link to execution detail

## Batch Run Detail

Path: `/admin/batch-runs/{id}`

Shows:

- Run summary
- Spring Batch job execution id
- Exit code
- Job parameters
- Output summary
- Step-level read/write/skip/commit/rollback counts
