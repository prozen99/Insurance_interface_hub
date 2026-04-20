# Demo Scenarios

## Scenario 1 - Project Orientation

1. Open `README.md`.
2. Explain the business goal.
3. Show the phase roadmap.
4. Show `com.insurancehub.interfacehub.application.execution`.

What this proves:

- The project has clear business context.
- Phase 2 added a common execution engine without real protocol calls.

## Scenario 2 - Login And Dashboard

1. Start the app with the local profile.
2. Open `/login`.
3. Log in with `admin` / `admin123!`.
4. Open `/admin`.

What this proves:

- DB-backed form login still works.
- Dashboard shows execution metrics.

## Scenario 3 - Manual Success

1. Open `/admin/interfaces`.
2. Open an active interface detail page.
3. Enter a normal request payload.
4. Click Execute now.
5. Review the execution detail.

What this proves:

- Manual execution creates an execution record.
- Step logs are persisted.
- Mock protocol strategy resolves by protocol type.

## Scenario 4 - Manual Failure

1. Open the same interface detail page.
2. Enter a payload containing `FAIL`.
3. Click Execute now.
4. Confirm FAILED status and error details.

What this proves:

- Failure handling is deterministic.
- Retry task creation works.

## Scenario 5 - Retry

1. Open the failed execution detail.
2. Click Retry.
3. Review the new retry execution.
4. Return to the original failed execution and review retry task status.

What this proves:

- Retry creates a new execution.
- Retry source linkage is understandable.
- Retry task moves from WAITING to DONE.

## Scenario 6 - Execution History

1. Open `/admin/executions`.
2. Filter by FAILED.
3. Filter by protocol.
4. Open an execution detail.

What this proves:

- Operators can navigate execution history.
- Status badges and filters are available.
