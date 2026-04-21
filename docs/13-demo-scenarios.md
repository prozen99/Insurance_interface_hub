# Demo Scenarios

## Scenario 1 - Project Orientation

1. Open `README.md`.
2. Explain the business goal.
3. Show the phase roadmap.
4. Show `com.insurancehub.interfacehub.application.execution`.
5. Show `com.insurancehub.protocol.rest`.

What this proves:

- The project has clear business context.
- Phase 3 keeps a common execution engine while REST becomes a real adapter.

## Scenario 2 - Login And Dashboard

1. Start the app with the local profile.
2. Open `/login`.
3. Log in with `admin` / `admin123!`.
4. Open `/admin`.

What this proves:

- DB-backed form login still works.
- Dashboard shows execution metrics.

## Scenario 3 - REST Configuration

1. Open `/admin/interfaces`.
2. Open `IF_REST_POLICY_001`.
3. Review the REST settings panel.
4. Click Edit REST config.
5. Confirm base URL, method, path, timeout, headers JSON, and sample request body.

What this proves:

- REST-specific configuration is visible and editable.
- Protocol-specific setup is isolated from the master interface definition.

## Scenario 4 - REST Manual Success

1. Open `IF_REST_POLICY_001`.
2. Keep the sample request payload.
3. Click Execute now.
4. Review the execution detail.

What this proves:

- Manual execution creates an execution record.
- REST execution makes a real HTTP call to the local simulator.
- URL, method, status code, latency, headers, and payloads are persisted.

## Scenario 5 - REST Manual Failure

1. Open the same REST interface detail page.
2. Enter a payload containing `FAIL`.
3. Click Execute now.
4. Confirm FAILED status and HTTP 422 details.

What this proves:

- Failure handling is driven by a real HTTP response.
- Retry task creation still works.

## Scenario 6 - REST Retry

1. Open the failed REST execution detail.
2. Click Retry.
3. Review the new retry execution.
4. Return to the original failed execution and review retry task status.

What this proves:

- Retry creates a new execution.
- Retry source linkage is understandable.
- REST retry uses the same real executor path.

## Scenario 7 - Non-REST Mock Boundary

1. Create or open a SOAP, MQ, BATCH, SFTP, or FTP interface.
2. Execute with a normal payload.
3. Execute with a payload containing `FAIL`.

What this proves:

- Only REST has moved to real integration in Phase 3.
- Other protocols remain safely mock-driven.

## Scenario 8 - Execution History

1. Open `/admin/executions`.
2. Filter by FAILED.
3. Filter by REST.
4. Open an execution detail.

What this proves:

- Operators can navigate execution history.
- Status badges and filters are available.
- REST result inspection is available from history.
