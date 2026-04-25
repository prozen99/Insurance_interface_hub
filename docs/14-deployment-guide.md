# Railway 배포 가이드

이 문서는 Insurance Interface Hub를 Railway에 데모 URL로 배포하기 위한 안내입니다. 목적은 포트폴리오 평가자가 웹 화면을 빠르게 확인할 수 있게 하는 것이며, 운영용 production hardening을 완료했다는 의미는 아닙니다.

## Railway 배포 개요

필요한 Railway service는 두 개입니다.

1. Spring Boot web service
2. MySQL database service

Spring Boot web service는 `prod` profile로 실행합니다. Railway가 제공하는 `PORT`에 bind해야 하므로 `application-prod.yml`은 `server.address=0.0.0.0`, `server.port=${PORT:8080}`을 사용합니다.

## 필수 환경 변수

Spring Boot web service에 다음 환경 변수를 설정합니다.

```text
SPRING_PROFILES_ACTIVE=prod
INSURANCE_HUB_DB_URL=jdbc:mysql://<MYSQL_HOST>:<MYSQL_PORT>/<MYSQL_DATABASE>?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true
INSURANCE_HUB_DB_USERNAME=<MYSQL_USER>
INSURANCE_HUB_DB_PASSWORD=<MYSQL_PASSWORD>
APP_BATCH_SCHEDULER_ENABLED=false
APP_FILE_TRANSFER_SFTP_ENABLED=false
APP_FILE_TRANSFER_FTP_ENABLED=false
```

선택 환경 변수:

```text
INSURANCE_HUB_DB_POOL_MAX=5
INSURANCE_HUB_DB_POOL_MIN_IDLE=1
```

## Railway MySQL 값을 JDBC URL로 매핑

Railway MySQL service에서 제공하는 값을 확인한 뒤 다음 형태로 `INSURANCE_HUB_DB_URL`을 만듭니다.

```text
jdbc:mysql://<MYSQL_HOST>:<MYSQL_PORT>/<MYSQL_DATABASE>?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true
```

예시 형식:

```text
INSURANCE_HUB_DB_URL=jdbc:mysql://containers-us-west-xxx.railway.app:12345/railway?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true
INSURANCE_HUB_DB_USERNAME=root
INSURANCE_HUB_DB_PASSWORD=<Railway MySQL password>
```

실제 password는 GitHub, 문서, issue, chat에 남기지 않습니다. Railway Variables 또는 Secrets에만 저장합니다.

## Build Command

Railway build command:

```bash
./gradlew clean bootJar -x test
```

`bootJar`만 생성하도록 해서 plain jar가 실행되는 문제를 피합니다.

## Start Command

Railway start command:

```bash
java -Dserver.port=$PORT -jar build/libs/Insurance_Interface_Hub-0.0.1-SNAPSHOT.jar
```

이 프로젝트에는 `railway.json`이 포함되어 있어 Railway가 위 build/start command를 사용하도록 고정합니다.

## Demo Login

- Login ID: `admin`
- Password: `admin123!`

Flyway migration이 demo admin 계정을 seed합니다. DB에는 plain password가 아니라 BCrypt hash가 저장됩니다.

## 배포 후 Smoke Check

배포가 완료되면 다음 URL을 확인합니다.

```text
https://<railway-service-domain>/actuator/health
https://<railway-service-domain>/login
https://<railway-service-domain>/admin
```

확인 순서:

1. `/actuator/health`가 `UP`인지 확인합니다.
2. `/login` 화면이 열리는지 확인합니다.
3. `admin` / `admin123!`로 로그인합니다.
4. `/admin` dashboard가 열리는지 확인합니다.
5. `/admin/interfaces`, `/admin/executions`, `/admin/monitoring`을 확인합니다.

## REST/SOAP Simulator URL 확인

seed data의 REST/SOAP demo 설정은 local demo 기준으로 `http://localhost:8080`을 사용합니다. Railway에서 내부 port나 public domain이 달라 REST/SOAP 실행이 실패하면 admin UI에서 다음 설정을 배포 URL 기준으로 수정합니다.

- REST config `baseUrl`: `https://<railway-service-domain>`
- REST config `path`: `/simulator/rest/premium/calculate`
- SOAP config `endpointUrl`: `https://<railway-service-domain>/simulator/soap/policy-inquiry`

MQ와 수동 Batch는 같은 app process 안의 embedded/local service를 사용하므로 별도 public URL 수정이 필요하지 않습니다.

## 로컬 실행과 Railway 실행의 차이

| 구분 | 로컬 실행 | Railway 실행 |
| --- | --- | --- |
| Profile | `local` | `prod` |
| DB | local MySQL | Railway MySQL |
| Port | 보통 `8080` | Railway `PORT` |
| REST/SOAP simulator | 같은 app 내부 endpoint | 같은 app 내부 endpoint |
| MQ | embedded Artemis | embedded Artemis |
| Batch 수동 실행 | 가능 | 가능 |
| Batch scheduler | 기본 비활성화 | 기본 비활성화 |
| SFTP/FTP demo server | 기본 활성화 | 기본 비활성화 권장 |

Railway 환경에서는 외부 port bind와 filesystem write 제약이 있을 수 있으므로 SFTP/FTP demo server는 기본적으로 비활성화합니다. 전체 파일 전송 시연은 로컬 환경에서 진행하는 것을 권장합니다.

## 알려진 배포 한계

- 이 배포는 포트폴리오 데모용이며 full production operation 구성이 아닙니다.
- Railway free/low tier 환경에서는 sleep, cold start, filesystem persistence 제약이 있을 수 있습니다.
- embedded SFTP/FTP server는 Railway에서 port 제약을 받을 수 있어 기본 비활성화합니다.
- Batch scheduler는 기본 비활성화합니다. 데모에서는 수동 Batch 실행을 권장합니다.
- REST/SOAP seed URL은 local demo 기준이므로 Railway public domain으로 config를 조정해야 할 수 있습니다.
- 운영용 secret vault, alerting, tracing, distributed lock, production MQ/SFTP/FTP infrastructure는 포함하지 않습니다.
- MySQL migration은 Flyway가 app startup 시 수행하므로 첫 배포 시 DB 권한과 JDBC URL을 정확히 설정해야 합니다.

## 문제 발생 시 확인할 것

- `SPRING_PROFILES_ACTIVE=prod` 설정 여부
- `INSURANCE_HUB_DB_URL`이 `jdbc:mysql://...` 형식인지 여부
- Railway MySQL host, port, database, username, password가 정확한지 여부
- `/actuator/health` 응답
- Railway deploy logs의 Flyway validation/migration 결과
- SFTP/FTP 관련 오류가 있으면 `APP_FILE_TRANSFER_SFTP_ENABLED=false`, `APP_FILE_TRANSFER_FTP_ENABLED=false` 설정 여부
