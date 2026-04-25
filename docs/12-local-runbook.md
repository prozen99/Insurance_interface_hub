# 로컬 실행 가이드

이 문서는 평가자 또는 제출자가 로컬 PC에서 Insurance Interface Hub를 실행할 때 필요한 절차를 정리합니다. 전체 프로토콜 실행까지 확인하려면 로컬 실행이 가장 안정적입니다.

## 전제 조건

- Windows 10 또는 11
- JDK 21
- Local MySQL 8.x
- IntelliJ IDEA 또는 PowerShell

최종 제출 데모는 Docker, 외부 MQ broker, 외부 SFTP server, 외부 FTP server, 외부 scheduler, 외부 monitoring stack 없이 실행할 수 있습니다.

## 중요한 설정 주의사항

- 실제 DB password를 commit하지 않습니다.
- `application-local.yml`은 개발자 PC의 local-only 설정으로 취급합니다.
- 제출용 문서에는 실제 password를 적지 않습니다.
- GitHub에는 `application-local.example.yml`처럼 placeholder 기반 예시만 공유하는 것이 안전합니다.
- 평가자가 직접 실행해야 하는 경우 환경 변수로 DB 정보를 주입하는 방식을 권장합니다.

## Database Setup

MySQL에서 데모용 schema와 계정을 생성합니다.

```sql
create database if not exists insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
create user if not exists 'insurance_hub_app'@'localhost' identified by 'change-me';
alter user 'insurance_hub_app'@'localhost' identified by 'change-me';
grant all privileges on insurance_hub.* to 'insurance_hub_app'@'localhost';
flush privileges;
```

`change-me`는 로컬에서 직접 관리하는 비밀번호로 바꾸어 사용합니다. 실제 값은 문서, commit, issue, chat에 남기지 않습니다.

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

## 로컬 실행과 배포 실행의 차이

| 구분 | 로컬 실행 | 배포 실행 |
| --- | --- | --- |
| 목적 | 전체 기능 시연과 protocol 실행 검증 | URL 기반 화면 확인과 간단한 평가 |
| DB | local MySQL | 배포 platform의 MySQL 또는 managed DB |
| 환경 변수 | PowerShell/IntelliJ에서 직접 설정 | platform secret/environment variable |
| MQ | embedded Artemis | demo 기준 embedded Artemis 유지 |
| SFTP/FTP | embedded local server port 사용 | platform port/file 권한 제약 가능 |
| Batch | 수동 실행 안정적, scheduler 선택 | 수동 실행 권장, scheduler는 필요 시 설정 |

배포 URL만으로도 login, dashboard, master list, monitoring page는 확인할 수 있습니다. 다만 SFTP/FTP처럼 port와 filesystem 권한을 사용하는 기능은 로컬 시연이 더 안정적입니다.

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

## 5분 로컬 확인 절차

1. http://localhost:8080/login 접속
2. demo admin 계정으로 로그인
3. `/admin` dashboard 확인
4. `/admin/interfaces`에서 `IF_REST_POLICY_001` 열기
5. REST 정상 실행 후 execution detail 확인
6. SOAP, MQ, SFTP/FTP, Batch 대표 실행 확인
7. `FAIL` 또는 `forceFail=true`로 실패 실행 생성
8. execution detail에서 error와 retry task 확인
9. `/admin/monitoring/failures`, `/admin/monitoring/retries`, `/admin/monitoring/protocols` 확인

## 검증 명령

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

## Reset Local Database

로컬 disposable data만 초기화할 때 사용합니다.

```sql
drop database insurance_hub;
create database insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
```

앱을 다시 시작하면 Flyway가 migration과 seed data를 다시 적용합니다.

## 제출 전 Checklist

- GitHub repository URL 준비
- 배포 URL이 있다면 login URL과 dashboard URL 준비
- README와 `docs/14-submission-guide.md` 확인
- DB credential이 문서나 commit에 포함되지 않았는지 확인
- `application-local.yml`에 개인 credential이 있다면 commit하지 않기
- `.\gradlew.bat test`, `.\gradlew.bat build` 결과 확인
- login, dashboard, interface detail, execution detail, failure/retry, monitoring screenshot 또는 짧은 영상 준비
- embedded broker/server, local scheduler, request-time monitoring aggregation 같은 알려진 한계를 설명할 준비
