# Local Runbook

## Prerequisites

- Windows 10 or 11
- JDK 21
- Local MySQL 8.x
- IntelliJ IDEA or PowerShell

No Docker or external MQ broker is required for Phase 5.

## Database Setup

```sql
create database if not exists insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
create user if not exists 'insurance_hub_app'@'localhost' identified by 'change-me';
alter user 'insurance_hub_app'@'localhost' identified by 'change-me';
grant all privileges on insurance_hub.* to 'insurance_hub_app'@'localhost';
flush privileges;
```

Use a local password you control. Do not commit it.

## Environment Variables

```powershell
$env:INSURANCE_HUB_DB_URL="jdbc:mysql://localhost:3306/insurance_hub?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true"
$env:INSURANCE_HUB_DB_USERNAME="insurance_hub_app"
$env:INSURANCE_HUB_DB_PASSWORD="change-me"
```

Optional:

```powershell
$env:INSURANCE_HUB_PORT="8080"
```

If the port is changed, update REST and SOAP endpoint config URLs in the admin UI. MQ uses the embedded in-vm broker and does not depend on the HTTP port.

## Build

```powershell
.\gradlew.bat build
```

## Run

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

The embedded Artemis broker starts inside the app process by default through:

```yaml
app:
  mq:
    embedded:
      enabled: true
```

## Demo Login

- Login ID: `admin`
- Password: `admin123!`

## Verify Phase 5

1. Open http://localhost:8080/login.
2. Log in.
3. Open http://localhost:8080/admin/interfaces.
4. Open `IF_REST_POLICY_001` and run the REST success demo to confirm REST still works.
5. Open `IF_SOAP_POLICY_001` and run the SOAP success demo to confirm SOAP still works.
6. Open `IF_MQ_POLICY_001`.
7. Confirm the MQ settings panel shows broker type `EMBEDDED_ARTEMIS` and destination `insurancehub.demo.policy.events`.
8. Open Edit MQ config and confirm destination, routing key, correlation key expression, timeout, and active flag are editable.
9. Return to the MQ interface detail page.
10. Execute with the sample payload and confirm SUCCESS.
11. Open execution detail and confirm destination, correlation key, publish metadata, consume metadata, message history, and latency are visible.
12. Execute again with `FAIL` in the payload and confirm FAILED with publish SUCCESS and consume FAILED in the MQ message history.
13. Open the failed execution detail and click Retry.

## SOAP Simulator Smoke Check

```powershell
Invoke-WebRequest `
  -Method Post `
  -Uri http://localhost:8080/simulator/soap/policy-inquiry `
  -ContentType "text/xml" `
  -Headers @{"SOAPAction"="urn:PolicyInquiry"} `
  -Body '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"><soapenv:Body><PolicyInquiryRequest><policyNo>POL-001</policyNo></PolicyInquiryRequest></soapenv:Body></soapenv:Envelope>'
```

## MQ Local Notes

- No external Artemis service should be started for the demo.
- `/actuator/health` should not depend on a separately installed broker.
- Use `IF_MQ_POLICY_001` to exercise publish/consume from the admin UI.
- `FAIL` in the payload is the deterministic local consumer failure trigger.

## Reset Local Database

For disposable local data only:

```sql
drop database insurance_hub;
create database insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
```

Restart the app and Flyway will recreate all migrations and seed demo data.
