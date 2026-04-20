# Troubleshooting

## Gradle Cannot Find Java 21

Confirm JDK 21 is installed and visible:

```powershell
java -version
```

In IntelliJ, set the project SDK and Gradle JVM to JDK 21.

## MySQL Connection Fails

Check that MySQL is running and the database exists:

```sql
show databases;
select user, host from mysql.user;
```

Confirm environment variables:

```powershell
$env:INSURANCE_HUB_DB_URL
$env:INSURANCE_HUB_DB_USERNAME
$env:INSURANCE_HUB_DB_PASSWORD
```

## Flyway Migration Fails

Common causes:

- Database user lacks DDL privileges.
- Schema was manually changed.
- A migration was edited after being applied.
- A previous local run partially applied a migration.

For disposable local data:

```sql
drop database insurance_hub;
create database insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
```

Then restart the app.

## Login Fails

Use the local demo account:

- Login ID: `admin`
- Password: `admin123!`

If missing, confirm V2 ran.

## Manual Execution Is Rejected

Check:

- The interface exists.
- The interface status is ACTIVE.
- The request payload is 4000 characters or less.

## Mock Execution Fails

This is expected when the interface code or request payload contains `FAIL`.

## Retry Button Is Missing

Only FAILED executions can be retried. SUCCESS, RUNNING, and PENDING executions do not show the retry action.

## POST Form Returns 403

Spring Security CSRF protection is enabled. Thymeleaf forms must include the CSRF hidden field.

## Port 8080 Is Already In Use

```powershell
$env:INSURANCE_HUB_PORT="8081"
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```
