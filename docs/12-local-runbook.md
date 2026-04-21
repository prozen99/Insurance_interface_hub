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

If the port is changed, update REST and SOAP endpoint config URLs in the admin UI.

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

## Verify Phase 4

1. Open http://localhost:8080/login.
2. Log in.
3. Open http://localhost:8080/admin/interfaces.
4. Open `IF_REST_POLICY_001` and run the REST success demo to confirm REST still works.
5. Open `IF_SOAP_POLICY_001`.
6. Confirm the SOAP settings panel points to `http://localhost:8080/simulator/soap/policy-inquiry`.
7. Open Edit SOAP config and confirm endpoint URL, SOAPAction, operation, namespace, timeout, and XML template are editable.
8. Return to the SOAP interface detail page.
9. Execute with the sample SOAP XML and confirm SUCCESS.
10. Open execution detail and confirm endpoint URL, SOAPAction, status code, latency, request XML, and response XML are visible.
11. Execute again with `FAIL` in the XML and confirm FAILED with SOAP fault XML.
12. Open the failed execution detail and click Retry.

## SOAP Simulator Smoke Check

```powershell
Invoke-WebRequest `
  -Method Post `
  -Uri http://localhost:8080/simulator/soap/policy-inquiry `
  -ContentType "text/xml" `
  -Headers @{"SOAPAction"="urn:PolicyInquiry"} `
  -Body '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"><soapenv:Body><PolicyInquiryRequest><policyNo>POL-001</policyNo></PolicyInquiryRequest></soapenv:Body></soapenv:Envelope>'
```

## Reset Local Database

For disposable local data only:

```sql
drop database insurance_hub;
create database insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
```

Restart the app and Flyway will recreate all migrations and seed demo data.
