# 로컬 실행 가이드

## 전제 조건

- Windows 10 또는 11
- JDK 21
- Local MySQL 8.x
- IntelliJ IDEA 또는 PowerShell

최종 Phase 9 데모는 Docker, 외부 MQ broker, 외부 SFTP server, 외부 FTP server, 외부 scheduler, 외부 monitoring stack이 없어도 실행할 수 있습니다.

## 중요한 설정 주의사항

- 실제 DB password를 commit하지 않습니다.
- `application-local.yml`은 개발자 PC의 local-only 설정으로 취급합니다.
- 제출용 예시는 환경 변수 또는 `application-local.example.yml`의 placeholder를 사용합니다.
- 이 문서는 실제 password를 포함하지 않습니다.

## Database Setup

MySQL에서 데모용 schema와 계정을 생성합니다.

```sql
create database if not exists insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
create user if not exists 'insurance_hub_app'@'localhost' identified by 'change-me';
alter user 'insurance_hub_app'@'localhost' identified by 'change-me';
grant all privileges on insurance_hub.* to 'insurance_hub_app'@'localhost';
flush privileges;
```

`change-me`는 로컬에서 직접 관리하는 비밀번호로 바꾸어 사용합니다. 실제 값은 문서나 commit에 남기지 않습니다.

## Environment Variables

PowerShell 예시:

```powershell
$env:INSURANCE_HUB_DB_URL="jdbc:mysql://localhost:3306/insurance_hub?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true"
$env:INSURANCE_HUB_DB_USERNAME="insurance_hub_app"
$env:INSURANCE_HUB_DB_PASSWORD="change-me"
```

선택 사항:

```powershell
$env:INSURANCE_HUB_PORT="8080"
```

IntelliJ에서 실행할 경우 Run/Debug Configurations의 Environment variables에 같은 값을 넣습니다. 입력란이 보이지 않으면 configuration option에서 Environment variables 항목을 펼쳐야 합니다.

HTTP port를 바꾸면 admin UI의 REST/SOAP endpoint config URL도 현재 port에 맞게 수정해야 합니다. MQ, file-transfer, Batch demo infrastructure는 app process 내부 또는 local 설정으로 동작합니다.

## Build

```powershell
.\gradlew.bat build
```

## Run

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

앱 process 안에서 다음 local demo infrastructure가 함께 시작됩니다.

- Artemis in-vm broker
- SFTP server on `127.0.0.1:10022`
- FTP server on `127.0.0.1:10021`
- local MySQL metadata table을 사용하는 Spring Batch infrastructure

## Demo Login

- Login ID: `admin`
- Password: `admin123!`

DB에는 BCrypt hash가 저장됩니다.

## 주요 URL

- Login: http://localhost:8080/login
- Dashboard: http://localhost:8080/admin
- Monitoring: http://localhost:8080/admin/monitoring
- Interfaces: http://localhost:8080/admin/interfaces
- Executions: http://localhost:8080/admin/executions
- Batch Runs: http://localhost:8080/admin/batch-runs
- Smoke API: http://localhost:8080/api/smoke

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

- Batch config rows는 기본적으로 `enabled_yn=0`입니다.
- app scheduler는 `app.batch.scheduler.enabled=false`가 기본값입니다.
- scheduling demo가 필요하면 `app.batch.scheduler.enabled=true`로 실행하고, UI에서 Batch config를 enable한 뒤 `0/30 * * * * *` 같은 짧은 cron을 사용합니다.

## File Transfer Demo Directories

runtime에 생성되는 directory:

- Upload source: `build/file-transfer-demo/local/input`
- Download target: `build/file-transfer-demo/local/download`
- SFTP remote root: `build/file-transfer-demo/remote/sftp`
- FTP remote root: `build/file-transfer-demo/remote/ftp`

## Phase 9 검증 절차

1. http://localhost:8080/login 접속
2. demo admin 계정으로 로그인
3. REST execution 실행
4. SOAP execution 실행
5. MQ execution 실행
6. SFTP upload 또는 download 실행
7. FTP upload 또는 download 실행
8. `IF_BATCH_SETTLEMENT_001` 열기
9. Batch setting 확인
10. `{"businessDate":"TODAY","forceFail":false}`로 Batch 실행
11. execution detail에서 Batch run history 확인
12. `/admin/batch-runs` 열기
13. `{"forceFail":true}`로 Batch 실패를 만들고 retry 또는 정상 payload rerun 시연
14. `/admin` dashboard 확인
15. protocol summary, recent executions, pending retries, top failures 확인
16. `/admin/monitoring` 열기
17. `/admin/monitoring/failures`, `/admin/monitoring/retries`, `/admin/monitoring/protocols`, `/admin/monitoring/files`, `/admin/monitoring/mq`, `/admin/monitoring/batch` 확인
18. `/admin/executions`에서 protocol, status, trigger, date range filter 확인
19. `.\gradlew.bat test` 실행
20. `.\gradlew.bat build` 실행

## Reset Local Database

로컬 disposable data만 초기화할 때 사용합니다.

```sql
drop database insurance_hub;
create database insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
```

앱을 다시 시작하면 Flyway가 migration과 seed data를 다시 적용합니다.

## 제출 전 Checklist

- DB credential을 환경 변수 또는 local-only 설정으로 관리했는지 확인
- `application-local.yml`에 개인 credential이 있다면 commit하지 않기
- README와 `/docs`가 현재 구현 상태와 일치하는지 확인
- login, dashboard, interface detail, execution detail, failure/retry, monitoring screenshot 또는 짧은 영상 준비
- embedded broker/server, local scheduler, request-time monitoring aggregation 같은 알려진 한계를 설명할 준비
