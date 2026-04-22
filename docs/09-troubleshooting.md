# Troubleshooting

Append new troubleshooting entries to this document as development continues. Use the same format: symptom, cause, fix, and prevention.

## MySQL Access Denied From Missing Password Environment Variable

Symptom:

- App startup fails with MySQL access denied.
- Hikari cannot obtain a connection.

Cause:

- `INSURANCE_HUB_DB_PASSWORD` was not set, so the local profile used an empty password.

Fix:

```powershell
$env:INSURANCE_HUB_DB_PASSWORD="change-me"
```

Then restart the app.

Prevention:

- Keep required local environment variables in `docs/12-local-runbook.md`.
- Check IntelliJ Run Configuration environment variables before starting the app.
- Do not put real passwords in committed YAML files.

## Flyway Checksum Mismatch After Editing Applied V1

Symptom:

- App startup fails during Flyway validation.
- Error mentions checksum mismatch for `V1__phase_0_baseline.sql`.

Cause:

- An already-applied migration was changed after Flyway recorded its checksum in `flyway_schema_history`.

Fix:

- Do not edit the applied migration again.
- For disposable local data, drop and recreate the local database, then rerun all migrations.
- For preserved data, restore the original migration content and add a new migration for schema changes.

Prevention:

- Treat migration files as immutable once applied.
- Add new migrations such as V2, V3, V4 for later phases.

## Confusion Between `insurance` And `insurance_hub` Database Names

Symptom:

- Tables appear missing even though Flyway ran somewhere.
- Login seed data is not found.
- App points to a different schema than the one inspected in MySQL.

Cause:

- Local commands and configuration used inconsistent database names.

Fix:

- Standardize the app database as `insurance_hub`.
- Confirm the URL:

```powershell
$env:INSURANCE_HUB_DB_URL
```

Prevention:

- Use only `insurance_hub` in docs, run configs, and SQL examples.
- Avoid creating similarly named local schemas for this project.

## JMS Health Check Failure When Artemis Is Not Running

Symptom:

- Actuator health or startup diagnostics show JMS/Artemis connection failure.
- The project is not using real MQ yet.

Cause:

- Artemis dependencies are present for future MQ phases, but no local broker is running.

Fix:

- Keep local JMS health disabled:

```yaml
management:
  health:
    jms:
      enabled: false
```

Prevention:

- Do not enable JMS health checks until the MQ phase has a documented local broker setup.
- Keep Phase 3 focused on REST only.

## IntelliJ Environment Variable Field Is Hidden

Symptom:

- The app starts from IntelliJ without expected DB variables.
- PowerShell runs work, but IntelliJ runs fail.

Cause:

- IntelliJ Run Configuration hides Environment variables under expanded option settings depending on UI layout/version.

Fix:

- Open Run Configuration.
- Expand Modify options or More options.
- Enable or reveal Environment variables.
- Add `INSURANCE_HUB_DB_URL`, `INSURANCE_HUB_DB_USERNAME`, and `INSURANCE_HUB_DB_PASSWORD`.

Prevention:

- Prefer checking the run configuration before DB troubleshooting.
- Keep the PowerShell run command documented as a fallback.

## Environment Variable Name Mismatch Or Typo

Symptom:

- The app ignores a value that appears to be set.
- Startup still uses an empty password or default URL.

Cause:

- Variable names did not match the Spring placeholders exactly, or a typo was introduced.

Fix:

- Use these exact names:

```powershell
$env:INSURANCE_HUB_DB_URL
$env:INSURANCE_HUB_DB_USERNAME
$env:INSURANCE_HUB_DB_PASSWORD
$env:INSURANCE_HUB_PORT
```

Prevention:

- Copy variable names from `application-local.yml` or the runbook.
- Avoid similar names such as `INSURANCE_DB_PASSWORD`.

## Risk Of Exposing DB Password In Local Config Or Chat Text

Symptom:

- A real local DB password appears in a config file, command history, screenshot, or chat message.

Cause:

- Local setup commands can tempt developers to paste real secrets directly into text.

Fix:

- Rotate the local password if it was exposed.
- Replace committed examples with placeholders.
- Keep real values only in environment variables or local-only IDE settings.

Prevention:

- Never commit real credentials.
- Use placeholder values in docs.
- Avoid pasting real secrets into shared messages or screenshots.

## REST Simulator POST Returns 403

Symptom:

- REST execution reaches the same application but receives HTTP 403.
- Execution detail shows a REST HTTP error instead of simulator JSON.

Cause:

- Spring Security CSRF protection applies to POST requests by default.

Fix:

- Permit `/simulator/**`.
- Ignore CSRF for `/simulator/**` because these endpoints are local demo targets for the server-side executor.

Prevention:

- Keep simulator endpoints separate from admin endpoints.
- Do not reuse simulator CSRF behavior as a pattern for real external APIs.

## REST Execution Cannot Connect To Simulator

Symptom:

- Execution detail shows `REST_CLIENT_ERROR`.
- Error message mentions connection refused or timeout.

Cause:

- The REST config points to the wrong port or the app is not running.

Fix:

- If the app runs on 8080, use `http://localhost:8080`.
- If `INSURANCE_HUB_PORT` is changed, update the REST config base URL to the same port.

Prevention:

- Keep seeded demos on port 8080.
- When changing ports, update REST config before manual execution.

## Thymeleaf MVC Test Fails Because `_csrf` Is Null

Symptom:

- A `@WebMvcTest` that renders an admin template fails with a Thymeleaf `SpelEvaluationException`.
- The failing expression is `_csrf.parameterName` in the sidebar or form template.

Cause:

- The MVC test disabled filters or did not provide a CSRF request attribute, but the shared Thymeleaf layout expects `_csrf`.

Fix:

- Add a `DefaultCsrfToken` request attribute in the test request.

Prevention:

- Any MVC test that renders admin templates should provide `_csrf`, even when security filters are disabled.

## SOAP Execution Cannot Connect To Simulator

Symptom:

- Execution detail shows `SOAP_CLIENT_ERROR`.
- Error message mentions connection refused or timeout.

Cause:

- The SOAP config points to the wrong port, or the app is not running.

Fix:

- If the app runs on 8080, use `http://localhost:8080/simulator/soap/policy-inquiry`.
- If `INSURANCE_HUB_PORT` is changed, update the SOAP config endpoint URL to the same port.

Prevention:

- Keep seeded demos on port 8080.
- When changing ports, update REST and SOAP configs before manual execution.

## SOAP Config Rejects Request Template XML

Symptom:

- Saving the SOAP config form returns a validation error for request template XML.

Cause:

- The XML is not well-formed, or it contains a disallowed document type declaration.

Fix:

- Use a complete SOAP envelope with matching opening and closing tags.
- Do not include `DOCTYPE`.

Prevention:

- Start from the seeded template on `IF_SOAP_POLICY_001`.
- Keep simulator templates small and readable for local demos.

## SOAP Fault Appears As A Failed Execution

Symptom:

- Execution status is FAILED.
- Response XML contains `<soapenv:Fault>`.
- HTTP status is 500.

Cause:

- The local simulator returns a controlled SOAP fault when request XML contains `FAIL`.

Fix:

- Remove `FAIL` from the request XML for success demos.
- Keep `FAIL` when demonstrating failure handling and retry.

Prevention:

- Treat `FAIL` as a deliberate local simulator trigger, not a production rule.

## Phase 5 Compile Error From Ambiguous `Configuration` Import

Symptom:

- `compileJava` fails in `LocalMqConfig`.
- Error mentions that `Configuration` is ambiguous between Artemis and Spring.

Cause:

- The class imported both `org.apache.activemq.artemis.core.config.Configuration` and `org.springframework.context.annotation.Configuration`.

Fix:

- Keep the Spring annotation import.
- Use the fully qualified Artemis type in the local variable declaration.

Prevention:

- Avoid single-type imports when two framework types share common names such as `Configuration`.
- Prefer descriptive variable names and fully qualified names in integration configuration classes.

## Embedded Artemis Server Id Must Match Client URL

Symptom:

- MQ publish or consume fails with an in-vm connection error.
- The app starts, but the JMS client cannot connect to `vm://{serverId}`.

Cause:

- The embedded broker acceptor used the default in-vm server id while the connection factory URL used the configured `app.mq.embedded.server-id`.

Fix:

- Pass `TransportConstants.SERVER_ID_PROP_NAME` into the in-vm acceptor configuration.
- Keep `ActiveMQConnectionFactory("vm://" + app.mq.embedded.server-id)` aligned with the embedded broker.

Prevention:

- When adding local broker properties, verify both server startup and client factory creation use the same value.
- Keep an automated publish/consume test with a non-default server id.

## MQ Consumer Failure Is Separate From Publish Success

Symptom:

- Execution status is FAILED even though an MQ message id was generated.
- MQ message history shows publish SUCCESS and consume FAILED.

Cause:

- Phase 5 deliberately distinguishes producer success from consumer processing success.
- Payloads containing `FAIL` are published, then rejected by the local demo consumer rule.

Fix:

- Remove `FAIL` from the payload for success demos.
- Keep `FAIL` when demonstrating failed consumer processing and retry.

Prevention:

- Explain the two-stage MQ status model during demos.
- Check the MQ message history section before assuming publish failed.

## Embedded Broker Is For Local Demo Only

Symptom:

- A developer expects an external Artemis console, port, or Docker container.
- No external broker process appears in Task Manager.

Cause:

- Phase 5 uses an embedded in-vm Artemis broker inside the Spring Boot process.

Fix:

- Start only the Spring Boot application.
- Use the seeded `IF_MQ_POLICY_001` interface to publish and consume messages locally.

Prevention:

- Keep production broker setup out of Phase 5.
- Document any future external broker migration as a separate phase decision.

## Embedded SFTP Or FTP Port Conflict

Symptom:

- App startup fails during Phase 6.
- Error mentions that port `10022` or `10021` is already in use.

Cause:

- The embedded local SFTP or FTP demo server tries to bind a port already used by another process.

Fix:

- Stop the process using the port, or change `app.file-transfer.sftp.port` / `app.file-transfer.ftp.port`.
- If the port is changed, update the matching SFTP/FTP config row in the admin UI.

Prevention:

- Keep only one local app instance running during demos.
- Use `Get-NetTCPConnection -LocalPort 10022` or `10021` before starting if startup fails.

## SFTP Host Key Warning In Tests Or Logs

Symptom:

- Logs show an unverified SFTP host key warning.
- File transfer still succeeds.

Cause:

- The local demo SFTP client allows unknown keys because the embedded server generates a project-local demo host key.

Fix:

- No fix is required for the local demo path.
- For production, configure a known-hosts file or a pinned host key.

Prevention:

- Do not copy `allowUnknownKeys=true` into production SFTP configuration.
- Keep the warning documented as local-demo-only behavior.

## File Upload Fails Because Local File Is Missing

Symptom:

- Execution status is FAILED.
- Error mentions that the local upload file does not exist.

Cause:

- Upload reads from `build/file-transfer-demo/local/input`, and the requested file name is not present there.

Fix:

- Use `sample-upload.txt`, or place a demo file in `build/file-transfer-demo/local/input`.

Prevention:

- Keep demo file names simple.
- Avoid entering full local paths in the manual execution form.

## File Download Fails Because Remote File Is Missing

Symptom:

- Execution status is FAILED.
- Error mentions a transfer or read failure for the remote path.

Cause:

- Download reads from the embedded server remote root. The requested remote file path does not exist.

Fix:

- Use `/outbox/sample-download.txt` for a success demo.
- For custom files, upload first or create the file under the correct remote demo directory.

Prevention:

- Document demo remote roots:
  - SFTP: `build/file-transfer-demo/remote/sftp`
  - FTP: `build/file-transfer-demo/remote/ftp`

## FTP Passive Mode Issues

Symptom:

- FTP login succeeds but upload/download hangs or fails during data transfer.

Cause:

- FTP uses a separate data connection. Active/passive mode can matter even on localhost.

Fix:

- Keep passive mode enabled for the local demo.
- Confirm local firewall or security software is not blocking loopback data ports.

Prevention:

- Keep FTP passive mode visible in the config UI.
- Prefer SFTP for simpler production network posture unless FTP is explicitly required.

## File Transfer Path Traversal Rejected

Symptom:

- Execution fails before opening SFTP/FTP connection.
- Error mentions that local file name or remote path cannot contain traversal.

Cause:

- The local demo intentionally blocks `..`, absolute local paths, and nested local file paths.

Fix:

- Use a simple local file name such as `sample-upload.txt`.
- Use an absolute remote path such as `/inbox/sample-upload.txt`.

Prevention:

- Keep local demo files under the project-local demo directory.
- Do not expose arbitrary filesystem paths through the admin form.

## Spring Batch Metadata Tables Missing

Symptom:

- Batch execution fails at launch.
- Error mentions missing `BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, or sequence tables.

Cause:

- Spring Batch needs metadata tables, and the project keeps `spring.batch.jdbc.initialize-schema=never` so Flyway owns schema creation.

Fix:

- Apply `V8__phase_7_real_batch_integration.sql`.
- Confirm Flyway has migrated the local database to version 8.

Prevention:

- Do not enable Spring Batch auto schema initialization for this project.
- Keep Batch metadata changes in Flyway migrations.

## Duplicate Spring Batch Job Parameters

Symptom:

- A manual rerun fails with a message that a job instance already exists.

Cause:

- Spring Batch identifies job instances by identifying parameters.
- Running with identical parameters can collide unless a unique run parameter is added.

Fix:

- Phase 7 adds a unique `run.id` parameter for every launch.

Prevention:

- Keep `run.id` or an equivalent unique launch parameter in future Batch launch code.

## Launching Batch Inside A Long Transaction

Symptom:

- Batch launch can fail with transaction state errors or lock unexpectedly.

Cause:

- Spring Batch job repository work should not be launched while a caller holds a broad business transaction.

Fix:

- Phase 7 records the running `interface_execution` first, invokes the protocol executor outside that transaction, then records results afterward.

Prevention:

- Do not wrap external protocol calls or Spring Batch launches in one large database transaction.

## Batch Scheduler Does Not Fire

Symptom:

- A batch config has a cron expression, but no scheduled execution appears.

Cause:

- The app-level scheduler is disabled by default, or the batch config `enabled` flag is off.

Fix:

- Set `app.batch.scheduler.enabled=true`.
- Enable the batch config in the admin UI.
- Use a six-field Spring cron expression such as `0/30 * * * * *`.

Prevention:

- Keep scheduler assumptions explicit in the runbook.
- Use manual execution for demos when timing is not the focus.

## Controlled Batch Failure Keeps Failing On Retry

Symptom:

- A failed batch execution is retried, but the retry fails with the same forced failure.

Cause:

- Retry reuses the original request payload for auditability.
- If the original payload contains `FAIL` or `forceFail=true`, retry repeats that same deterministic failure.

Fix:

- Run a new manual execution with `forceFail=false` to demonstrate recovery.
- Use retry to demonstrate audit linkage when the original input is intentionally unchanged.

Prevention:

- Explain retry as "rerun the same failed request" during demos.
- Use a transient operational failure for future retry demos if different retry behavior is needed.

## Batch `forceFail=false` Still Fails

Symptom:

- A manual batch execution with payload `{"businessDate":"TODAY","forceFail":false}` fails.
- The execution detail shows Spring Batch parameters with `forceFail=true`.

Cause:

- The initial controlled-failure detector searched the entire raw JSON text for `FAIL`.
- The key name `forceFail` itself contains `FAIL`, so even a false value was treated as a failure signal.

Fix:

- Parse JSON payloads first.
- Treat `forceFail=true` as explicit failure.
- For JSON payloads, scan values rather than field names for the demo `FAIL` token.

Prevention:

- Add regression tests that capture launched Spring Batch parameters.
- Keep demo failure rules narrow enough that control field names cannot trigger them accidentally.
