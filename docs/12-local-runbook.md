# Local Runbook

## Prerequisites

- Windows 10 or 11
- JDK 21
- Local MySQL 8.x
- IntelliJ IDEA or PowerShell

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

If the port is changed, update REST endpoint config base URLs in the admin UI.

## Build

```powershell
.\gradlew.bat build
```

## Run

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

## Demo Login

- Login ID: `admin`
- Password: `admin123!`

## Verify Phase 3

1. Open http://localhost:8080/login.
2. Log in.
3. Open http://localhost:8080/admin/interfaces.
4. Open `IF_REST_POLICY_001`.
5. Confirm the REST settings panel points to `http://localhost:8080/simulator/rest/premium/calculate`.
6. Open Edit REST config and confirm method, path, timeout, headers, and sample body are editable.
7. Return to the interface detail page.
8. Execute with the sample JSON payload and confirm SUCCESS.
9. Open execution detail and confirm URL, method, status code, latency, request headers, response headers, request payload, and response payload are visible.
10. Execute again with `FAIL` in the payload and confirm FAILED.
11. Open the failed execution detail and click Retry.

## Simulator Smoke Checks

GET policy success:

```powershell
Invoke-RestMethod http://localhost:8080/simulator/rest/policy/POL-001
```

POST premium failure:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri http://localhost:8080/simulator/rest/premium/calculate `
  -ContentType "application/json" `
  -Body '{"policyNo":"FAIL"}'
```

The failure command returns HTTP 422 by design.

## Reset Local Database

For disposable local data only:

```sql
drop database insurance_hub;
create database insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
```

Restart the app and Flyway will recreate all migrations and seed demo data.
