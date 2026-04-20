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

Confirm these environment variables match your local setup:

```powershell
$env:INSURANCE_HUB_DB_URL
$env:INSURANCE_HUB_DB_USERNAME
$env:INSURANCE_HUB_DB_PASSWORD
```

## Access Denied For `insurance_hub_app`

Create or reset the local app user:

```sql
create user 'insurance_hub_app'@'localhost' identified by 'change-me';
grant all privileges on insurance_hub.* to 'insurance_hub_app'@'localhost';
flush privileges;
```

If the user already exists:

```sql
alter user 'insurance_hub_app'@'localhost' identified by 'change-me';
```

Then update `INSURANCE_HUB_DB_PASSWORD`.

## Flyway Migration Fails

Common causes:

- The database user does not have DDL privileges.
- A table was created manually before Flyway ran.
- The schema was partially created during a failed run.
- V2 was edited after being applied locally.

For a disposable local database, drop and recreate the schema, then run the app again.

## Login Fails

Use the local demo account:

- Login ID: `admin`
- Password: `admin123!`

If the account is missing, confirm `V2__phase_1_admin_master_crud.sql` ran successfully.

## POST Form Returns 403

Spring Security CSRF protection is enabled. Thymeleaf forms must include the CSRF hidden field. The current admin forms already include it.

## Port 8080 Is Already In Use

Run with another port:

```powershell
$env:INSURANCE_HUB_PORT="8081"
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

## Build Passes But BootRun Fails

The build tests do not require MySQL. `bootRun` with the local profile requires MySQL configuration because the application uses local MySQL and Flyway.
