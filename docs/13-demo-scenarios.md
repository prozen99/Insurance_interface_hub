# Demo Scenarios

## Scenario 1 - Project Orientation

1. Open `README.md`.
2. Explain the business goal.
3. Show the phase roadmap.
4. Show `com.insurancehub.interfacehub.application.execution`.
5. Show `com.insurancehub.protocol.rest`, `com.insurancehub.protocol.soap`, and `com.insurancehub.protocol.mq`.

What this proves:

- The project has clear business context.
- REST, SOAP, and MQ are real protocol adapters behind the same common execution engine.

## Scenario 2 - Login And Dashboard

1. Start the app with the local profile.
2. Open `/login`.
3. Log in with `admin` / `admin123!`.
4. Open `/admin`.

What this proves:

- DB-backed form login still works.
- Dashboard shows execution metrics.

## Scenario 3 - REST Regression

1. Open `IF_REST_POLICY_001`.
2. Execute with the sample JSON payload.
3. Review the execution detail.

What this proves:

- REST still works after SOAP and MQ were added.

## Scenario 4 - SOAP Regression

1. Open `IF_SOAP_POLICY_001`.
2. Execute with the sample SOAP XML.
3. Review the execution detail.

What this proves:

- SOAP still works after MQ was added.

## Scenario 5 - MQ Configuration

1. Open `/admin/interfaces`.
2. Open `IF_MQ_POLICY_001`.
3. Review the MQ settings panel.
4. Click Edit MQ config.
5. Confirm broker type, destination, routing key, message type, correlation key expression, timeout, and active flag.

What this proves:

- MQ-specific configuration is visible and editable.
- Protocol-specific setup remains separate from interface master data.

## Scenario 6 - MQ Manual Success

1. Open `IF_MQ_POLICY_001`.
2. Keep the sample payload.
3. Click Execute now.
4. Review the execution detail.

What this proves:

- Manual execution creates an execution record.
- MQ execution publishes a real JMS message to the embedded broker.
- MQ execution consumes the message by correlation key.
- Destination, message id, correlation key, publish status, consume status, payload, and latency are persisted.

## Scenario 7 - MQ Consumer Failure

1. Open the MQ interface detail page.
2. Add `FAIL` inside the payload.
3. Click Execute now.
4. Confirm FAILED status.
5. Confirm MQ message history shows publish SUCCESS and consume FAILED.

What this proves:

- Failure handling can represent a successful producer and failed consumer separately.
- Retry task creation still works.

## Scenario 8 - MQ Retry

1. Open the failed MQ execution detail.
2. Click Retry.
3. Review the new retry execution.
4. Return to the original failed execution and review retry task status.

What this proves:

- Retry creates a new execution.
- Retry source linkage is understandable.
- MQ retry uses the same real executor path.

## Scenario 9 - Remaining Mock Boundary

1. Create or open a BATCH, SFTP, or FTP interface.
2. Execute with a normal payload.
3. Execute with a payload containing `FAIL`.

What this proves:

- REST, SOAP, and MQ are real in Phase 5.
- Other protocols remain safely mock-driven until their phases.

## Scenario 10 - Execution History

1. Open `/admin/executions`.
2. Filter by FAILED.
3. Filter by MQ.
4. Open an execution detail.

What this proves:

- Operators can navigate execution history.
- Protocol result inspection is available from history.
