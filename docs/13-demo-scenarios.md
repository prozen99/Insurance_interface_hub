# Demo Scenarios

## Scenario 1 - Project Orientation

1. Open `README.md`.
2. Explain the business goal.
3. Show the phase roadmap.
4. Show the common execution engine.
5. Show protocol packages for REST, SOAP, MQ, SFTP, and FTP.

What this proves:

- The project has clear business context.
- Five protocol paths are real while still sharing one execution engine.

## Scenario 2 - Login And Dashboard

1. Start the app with the local profile.
2. Open `/login`.
3. Log in with `admin` / `admin123!`.
4. Open `/admin`.

What this proves:

- DB-backed form login still works.
- Dashboard shows execution metrics.

## Scenario 3 - REST/SOAP/MQ Regression

1. Open `IF_REST_POLICY_001` and execute the sample payload.
2. Open `IF_SOAP_POLICY_001` and execute the sample XML.
3. Open `IF_MQ_POLICY_001` and execute the sample message.

What this proves:

- Phase 6 did not break existing real protocols.

## Scenario 4 - SFTP Configuration

1. Open `IF_SFTP_POLICY_001`.
2. Review the SFTP settings panel.
3. Click Edit SFTP config.
4. Confirm host, port, username, secret reference, paths, timeout, and active flag.

What this proves:

- SFTP-specific settings are visible and editable.

## Scenario 5 - SFTP Upload And Download

1. Open `IF_SFTP_POLICY_001`.
2. Upload `sample-upload.txt` to `/inbox/sample-upload.txt`.
3. Open execution detail and review file transfer history.
4. Download `/outbox/sample-download.txt` as `sample-download.txt`.
5. Confirm the downloaded file appears under `build/file-transfer-demo/local/download`.

What this proves:

- SFTP uses a real local protocol client and embedded server.
- File size, checksum, paths, latency, and status are persisted.

## Scenario 6 - FTP Upload And Download

1. Open `IF_FTP_POLICY_001`.
2. Upload `sample-upload.txt` to `/inbox/sample-upload.txt`.
3. Download `/outbox/sample-download.txt` as `sample-download.txt`.
4. Review execution details.

What this proves:

- FTP uses a real local protocol client and embedded server.
- FTP passive mode is configurable.

## Scenario 7 - File Transfer Failure And Retry

1. Open an SFTP or FTP interface.
2. Enter a missing local file name such as `missing.txt`.
3. Execute upload and confirm FAILED.
4. Create `missing.txt` under `build/file-transfer-demo/local/input`.
5. Open the failed execution detail.
6. Click Retry.

What this proves:

- Failure handling and retry task creation work for file transfers.
- Execution detail exposes the cause clearly.

## Scenario 8 - Remaining Mock Boundary

1. Create or open a BATCH interface.
2. Execute with a normal payload.
3. Execute with a payload containing `FAIL`.

What this proves:

- BATCH remains safely mock-driven until Phase 7.

## Scenario 9 - Execution History

1. Open `/admin/executions`.
2. Filter by SFTP or FTP.
3. Open an execution detail.

What this proves:

- Operators can inspect file transfers from the same execution history used by every protocol.
