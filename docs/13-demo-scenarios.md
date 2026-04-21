# Demo Scenarios

## Scenario 1 - Project Orientation

1. Open `README.md`.
2. Explain the business goal.
3. Show the phase roadmap.
4. Show `com.insurancehub.interfacehub.application.execution`.
5. Show `com.insurancehub.protocol.rest` and `com.insurancehub.protocol.soap`.

What this proves:

- The project has clear business context.
- REST and SOAP are real protocol adapters behind the same common execution engine.

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

- REST still works after SOAP was added.

## Scenario 4 - SOAP Configuration

1. Open `/admin/interfaces`.
2. Open `IF_SOAP_POLICY_001`.
3. Review the SOAP settings panel.
4. Click Edit SOAP config.
5. Confirm endpoint URL, SOAPAction, operation name, namespace URI, timeout, and request XML template.

What this proves:

- SOAP-specific configuration is visible and editable.
- Protocol-specific setup remains separate from interface master data.

## Scenario 5 - SOAP Manual Success

1. Open `IF_SOAP_POLICY_001`.
2. Keep the sample request XML.
3. Click Execute now.
4. Review the execution detail.

What this proves:

- Manual execution creates an execution record.
- SOAP execution makes a real call to the local simulator.
- Endpoint URL, SOAPAction, status code, latency, request XML, and response XML are persisted.

## Scenario 6 - SOAP Fault Failure

1. Open the SOAP interface detail page.
2. Add `FAIL` inside the request XML.
3. Click Execute now.
4. Confirm FAILED status and SOAP fault response XML.

What this proves:

- Failure handling is driven by a real SOAP fault response.
- Retry task creation still works.

## Scenario 7 - SOAP Retry

1. Open the failed SOAP execution detail.
2. Click Retry.
3. Review the new retry execution.
4. Return to the original failed execution and review retry task status.

What this proves:

- Retry creates a new execution.
- Retry source linkage is understandable.
- SOAP retry uses the same real executor path.

## Scenario 8 - Remaining Mock Boundary

1. Create or open an MQ, BATCH, SFTP, or FTP interface.
2. Execute with a normal payload.
3. Execute with a payload containing `FAIL`.

What this proves:

- Only REST and SOAP are real in Phase 4.
- Other protocols remain safely mock-driven.

## Scenario 9 - Execution History

1. Open `/admin/executions`.
2. Filter by FAILED.
3. Filter by SOAP.
4. Open an execution detail.

What this proves:

- Operators can navigate execution history.
- Protocol result inspection is available from history.
