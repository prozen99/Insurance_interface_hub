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
