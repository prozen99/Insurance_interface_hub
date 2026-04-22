# Local Runbook

## Prerequisites

- Windows 10 or 11
- JDK 21
- Local MySQL 8.x
- IntelliJ IDEA or PowerShell

No Docker, external MQ broker, external SFTP server, or external FTP server is required for Phase 6.

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

If the HTTP port is changed, update REST and SOAP endpoint config URLs in the admin UI. MQ and file-transfer demo servers use separate local ports.

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

## Demo Login

- Login ID: `admin`
- Password: `admin123!`

## File Transfer Demo Directories

Generated at runtime:

- Upload source: `build/file-transfer-demo/local/input`
- Download target: `build/file-transfer-demo/local/download`
- SFTP remote root: `build/file-transfer-demo/remote/sftp`
- FTP remote root: `build/file-transfer-demo/remote/ftp`

Sample files:

- Upload file: `sample-upload.txt`
- Download remote path: `/outbox/sample-download.txt`

## Verify Phase 6

1. Open http://localhost:8080/login.
2. Log in.
3. Open `/admin/interfaces`.
4. Run one REST execution to confirm REST still works.
5. Run one SOAP execution to confirm SOAP still works.
6. Run one MQ execution to confirm MQ still works.
7. Open `IF_SFTP_POLICY_001`.
8. Confirm SFTP settings point to `127.0.0.1:10022`.
9. Execute upload with `sample-upload.txt` and `/inbox/sample-upload.txt`.
10. Execute download with `sample-download.txt` and `/outbox/sample-download.txt`.
11. Open execution detail and confirm file transfer history is visible.
12. Repeat the same flow for `IF_FTP_POLICY_001`.
13. Trigger a failure with a missing local file name and retry the failed execution.

## Reset Local Database

For disposable local data only:

```sql
drop database insurance_hub;
create database insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
```

Restart the app and Flyway will recreate all migrations and seed demo data.
