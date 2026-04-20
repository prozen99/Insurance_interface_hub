# Local Runbook

## Prerequisites

- Windows 10 or 11
- JDK 21
- Local MySQL 8.x
- IntelliJ IDEA or PowerShell

## Database Setup

Run in MySQL as an admin user:

```sql
create database insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
create user 'insurance_hub_app'@'localhost' identified by 'change-me';
grant all privileges on insurance_hub.* to 'insurance_hub_app'@'localhost';
flush privileges;
```

Use a local password you control. Do not commit it.

## Environment Variables

Set these in PowerShell before running:

```powershell
$env:INSURANCE_HUB_DB_URL="jdbc:mysql://localhost:3306/insurance_hub?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true"
$env:INSURANCE_HUB_DB_USERNAME="insurance_hub_app"
$env:INSURANCE_HUB_DB_PASSWORD="change-me"
```

Optional:

```powershell
$env:INSURANCE_HUB_PORT="8080"
```

## Build

```powershell
.\gradlew.bat build
```

## Run

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

The local profile is also the default profile, but passing it explicitly is clearer for demos.

## Demo Login

Flyway seeds this local demo account:

- Login ID: `admin`
- Password: `admin123!`

The password stored in MySQL is a BCrypt hash, not plain text.

## Verify

Open these URLs:

- http://localhost:8080/login
- http://localhost:8080/admin
- http://localhost:8080/admin/partners
- http://localhost:8080/admin/systems
- http://localhost:8080/admin/interfaces
- http://localhost:8080/api/smoke
- http://localhost:8080/actuator/health

## Reset Local Database

Only do this for disposable local development data:

```sql
drop database insurance_hub;
create database insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
```

Restart the app and Flyway will recreate the schema and seed demo data.
