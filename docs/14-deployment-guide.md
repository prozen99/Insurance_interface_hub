# Railway 배포 가이드

이 문서는 Insurance Interface Hub를 Railway에 데모 URL로 배포하기 위한 안내입니다. 목적은 포트폴리오 평가자가 웹 화면을 빠르게 확인할 수 있게 하는 것이며, 운영용 production hardening을 완료했다는 의미는 아닙니다.

## Railway 배포 개요

필요한 Railway service는 두 개입니다.

1. Spring Boot web service
2. MySQL database service

Spring Boot web service는 `prod` profile로 실행합니다. Railway가 제공하는 `PORT`에 bind해야 하므로 `application-prod.yml`은 `server.address=0.0.0.0`, `server.port=${PORT:8080}`을 사용합니다.

## GitHub Repository 연결

1. Railway에서 New Project를 선택합니다.
2. Deploy from GitHub repo를 선택합니다.
3. `Insurance_Interface_Hub` repository를 연결합니다.
4. Root directory는 repository root를 사용합니다.
5. build/start command는 `railway.json` 또는 Railway service settings에서 아래 명령으로 지정합니다.

## MySQL Service 생성

1. 같은 Railway project 안에 MySQL service를 추가합니다.
2. Railway MySQL Variables에서 host, port, database, username, password를 확인합니다.
3. Spring Boot web service Variables에 JDBC URL과 DB 계정 정보를 등록합니다.
4. 첫 deploy 시 Flyway가 migration과 seed data를 적용합니다.

Railway에서 사용하는 DB는 local MySQL이 아니라 Railway MySQL이어야 합니다.

## 필수 환경 변수

Spring Boot web service에 다음 환경 변수를 설정합니다.

```text
SPRING_PROFILES_ACTIVE=prod
INSURANCE_HUB_DB_URL=jdbc:mysql://<MYSQL_HOST>:<MYSQL_PORT>/<MYSQL_DATABASE>?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true
INSURANCE_HUB_DB_USERNAME=<MYSQL_USER>
INSURANCE_HUB_DB_PASSWORD=<MYSQL_PASSWORD>
NIXPACKS_JDK_VERSION=21
NIXPACKS_GRADLE_VERSION=8
APP_BATCH_SCHEDULER_ENABLED=false
APP_FILE_TRANSFER_SFTP_ENABLED=false
APP_FILE_TRANSFER_FTP_ENABLED=false
```

선택 환경 변수:

```text
INSURANCE_HUB_DB_POOL_MAX=5
INSURANCE_HUB_DB_POOL_MIN_IDLE=1
```

실제 password는 GitHub, 문서, issue, chat에 남기지 않습니다. Railway Variables 또는 Secrets에만 저장합니다.

## Railway MySQL 값을 JDBC URL로 매핑

Railway MySQL service에서 제공하는 값을 확인한 뒤 다음 형태로 `INSURANCE_HUB_DB_URL`을 만듭니다.

```text
jdbc:mysql://<MYSQL_HOST>:<MYSQL_PORT>/<MYSQL_DATABASE>?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true
```

예시 형식:

```text
INSURANCE_HUB_DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true
INSURANCE_HUB_DB_USERNAME=${{MySQL.MYSQLUSER}}
INSURANCE_HUB_DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
```

Railway가 제공하는 `MYSQL_URL`은 보통 `mysql://...` 형식입니다. Spring Boot JDBC datasource에는 그대로 넣지 말고 반드시 `jdbc:mysql://...` 형식으로 조합해야 합니다.

## Gradle Wrapper 호환성

Railway/Nixpacks에서 Gradle 9 wrapper를 지원하지 않아 build가 실패할 수 있습니다. 이 프로젝트는 Railway 호환성을 위해 Gradle wrapper를 `8.14.3`으로 맞춥니다.

확인 파일:

```text
gradle/wrapper/gradle-wrapper.properties
```

기대값:

```text
distributionUrl=https\://services.gradle.org/distributions/gradle-8.14.3-bin.zip
```

## Build Command

Railway build command:

```bash
chmod +x gradlew && ./gradlew clean bootJar -x test
```

`bootJar`를 사용해 Spring Boot executable jar를 생성합니다. Railway Linux 환경에서는 `gradlew` 실행 권한이 없을 수 있으므로 `chmod +x gradlew`를 함께 실행합니다.

## Start Command

Railway start command:

```bash
java -Dserver.port=$PORT -jar $(ls -1 build/libs/*.jar | grep -v plain | head -n 1)
```

Gradle build는 executable bootJar와 plain jar를 함께 만들 수 있습니다. 위 명령은 `plain` jar를 제외하고 executable jar만 실행하므로 Railway가 잘못된 jar를 실행하는 문제를 피할 수 있습니다.

이 프로젝트에는 `railway.json`이 포함되어 있어 Railway가 위 build/start command를 사용하도록 고정합니다.

## Demo Login

- Login ID: `admin`
- Password: `admin123!`

Flyway migration이 demo admin 계정을 seed합니다. DB에는 plain password가 아니라 BCrypt hash가 저장됩니다.

## 배포 후 Smoke Check

배포가 완료되면 다음 URL을 확인합니다.

```text
https://insuranceinterfacehub-production.up.railway.app/actuator/health
https://insuranceinterfacehub-production.up.railway.app/login
https://insuranceinterfacehub-production.up.railway.app/admin
```

확인 순서:

1. `/actuator/health`가 `UP`인지 확인합니다.
2. `/login` 화면이 열리는지 확인합니다.
3. `admin` / `admin123!`로 로그인합니다.
4. `/admin` dashboard가 열리는지 확인합니다.
5. `/admin/interfaces`, `/admin/executions`, `/admin/monitoring`을 확인합니다.

실제 확인 결과:

- `/actuator/health`: `{"status":"UP"}`
- `/login`: 정상 접근
- `/admin`: 로그인 후 정상 접근
- REST 실행: SUCCESS, HTTP 200, step log SUCCESS
- SOAP 실행: SUCCESS, HTTP 200, response XML status SUCCESS
- MQ 실행: 확인 예정
- Monitoring: 실행 집계 확인 가능

## REST/SOAP Simulator URL 확인

seed data의 REST/SOAP demo 설정은 local demo 기준으로 `http://localhost:8080`을 사용합니다. Railway에서 내부 port나 public domain이 달라 REST/SOAP 실행이 실패하면 admin UI에서 다음 설정을 배포 URL 기준으로 수정합니다.

- REST config `baseUrl`: `https://insuranceinterfacehub-production.up.railway.app`
- REST config `path`: `/simulator/rest/premium/calculate`
- SOAP config `endpointUrl`: `https://insuranceinterfacehub-production.up.railway.app/simulator/soap/policy-inquiry`

MQ와 수동 Batch는 같은 app process 안의 embedded/local service를 사용하므로 별도 public URL 수정이 필요하지 않습니다.

## Railway Public URL 생성

배포가 성공해도 Railway가 `Unexposed service`로 표시되면 외부에서 접근할 수 없습니다.

해결 절차:

1. Railway service의 Settings로 이동합니다.
2. Networking 메뉴를 엽니다.
3. Generate Domain을 선택합니다.
4. 생성된 public URL로 `/actuator/health`를 확인합니다.

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
- `NIXPACKS_JDK_VERSION=21`, `NIXPACKS_GRADLE_VERSION=8` 설정 여부
- Railway MySQL host, port, database, username, password가 정확한지 여부
- `/actuator/health` 응답
- Railway deploy logs의 Flyway validation/migration 결과
- `Unsupported Gradle version: 9` 오류가 나오면 Gradle wrapper가 `8.14.3`인지 확인
- SFTP/FTP 관련 오류가 있으면 `APP_FILE_TRANSFER_SFTP_ENABLED=false`, `APP_FILE_TRANSFER_FTP_ENABLED=false` 설정 여부

## 실제 Railway 배포 중 해결한 문제 요약

| 문제 | 원인 | 해결 |
| --- | --- | --- |
| `Unsupported Gradle version: 9` | Nixpacks가 Gradle 9 wrapper를 지원하지 않음 | Gradle wrapper `8.14.3` 사용 |
| `JAVA_HOME is not set` | 로컬 terminal에 JDK 환경변수 없음 | PowerShell에서 `JAVA_HOME`, `Path` 임시 설정 |
| `Cannot find a Java installation matching languageVersion=21` | Nixpacks build 환경에서 Java 21 toolchain을 찾지 못함 | `NIXPACKS_JDK_VERSION=21`, `NIXPACKS_GRADLE_VERSION=8` 추가 |
| filetransfer/monitoring package compile error | Railway가 최신 source 또는 누락 package를 보지 못함 | GitHub `origin/main`에 누락 package commit/push 후 재배포 |
| repository count method compile error | service 변경과 repository 변경이 함께 반영되지 않음 | `MqMessageHistoryRepository`, `BatchRunHistoryRepository` count method 반영 |
| DB 연결 설정 필요 | local MySQL이 아닌 Railway MySQL 사용 필요 | `INSURANCE_HUB_DB_URL`, username, password를 Railway 변수로 설정 |
| 외부 접속 불가 | Public Networking domain 미생성 | Generate Domain으로 public URL 생성 |
| REST 실행 502 | REST base URL이 `localhost:8080`으로 남아 있음 | REST `baseUrl`을 Railway public domain으로 변경 |
| SOAP 호출 실패 가능성 | SOAP endpoint가 `localhost:8080` 기준 | SOAP `endpointUrl`을 Railway public domain으로 변경 |
| `/favicon.ico` 500/502 부가 로그 | favicon 리소스 또는 인증/에러 처리 영향 | 핵심 기능 영향 낮음, 필요 시 정적 favicon 추가 |
