# Local Runbook

## Prerequisites

- Windows 10 or 11
- JDK 21
- Local MySQL 8.x
- IntelliJ IDEA or PowerShell

No Docker, external MQ broker, external SFTP server, external FTP server, or external scheduler is required for Phase 7.

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

If the HTTP port is changed, update REST and SOAP endpoint config URLs in the admin UI. MQ, file-transfer, and batch demo infrastructure use local in-process settings.

## Build

```powershell
.\gradlew.bat build
```

## Run

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

Embedded local infrastructure starts inside the app process:

- Artemis in-vm broker
- SFTP server on `127.0.0.1:10022`
- FTP server on `127.0.0.1:10021`
- Spring Batch infrastructure backed by local MySQL metadata tables

## Demo Login

- Login ID: `admin`
- Password: `admin123!`

## Batch Demo

Manual batch output:

- `build/batch-demo/output`

Seeded interfaces:

- `IF_BATCH_SETTLEMENT_001`
- `IF_BATCH_RETRY_AGG_001`

Manual payload:

```json
{"businessDate":"TODAY","forceFail":false}
```

Failure payload:

```json
{"businessDate":"TODAY","forceFail":true}
```

Scheduling:

- Batch config rows are seeded with `enabled_yn=0`.
- App scheduler is disabled by default with `app.batch.scheduler.enabled=false`.
- To demo scheduling, set `app.batch.scheduler.enabled=true`, enable a batch config in the UI, and use a short cron such as `0/30 * * * * *`.

## File Transfer Demo Directories

Generated at runtime:

- Upload source: `build/file-transfer-demo/local/input`
- Download target: `build/file-transfer-demo/local/download`
- SFTP remote root: `build/file-transfer-demo/remote/sftp`
- FTP remote root: `build/file-transfer-demo/remote/ftp`

## Verify Phase 7

1. Open http://localhost:8080/login.
2. Log in.
3. Run one REST execution.
4. Run one SOAP execution.
5. Run one MQ execution.
6. Run one SFTP upload or download.
7. Run one FTP upload or download.
8. Open `IF_BATCH_SETTLEMENT_001`.
9. Confirm Batch settings are visible.
10. Execute with `{"businessDate":"TODAY","forceFail":false}`.
11. Open execution detail and confirm batch run history is visible.
12. Open `/admin/batch-runs`.
13. Trigger a batch failure with `{"forceFail":true}` and retry the failed execution.

## Reset Local Database

For disposable local data only:

```sql
drop database insurance_hub;
create database insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
```

Restart the app and Flyway will recreate all migrations and seed demo data.
