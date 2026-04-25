# 장애 대응 기록

이 문서는 개발과 로컬 데모 과정에서 실제로 만났거나 재발 가능성이 있는 문제를 정리한 기록입니다. 새 문제가 발생하면 같은 형식으로 아래에 계속 추가합니다.

기본 형식:

- 증상
- 원인
- 해결
- 재발 방지

## MySQL Password Environment Variable 누락

증상:

- 애플리케이션 시작 시 DB 연결에 실패한다.
- `Access denied for user` 또는 password 관련 오류가 발생한다.
- Flyway migration이 시작되지 못한다.

원인:

- `INSURANCE_HUB_DB_PASSWORD` 환경 변수가 설정되지 않았다.
- IntelliJ Run Configuration에 환경 변수를 넣지 않았거나 적용되지 않았다.
- local profile이 기대하는 DB 계정과 실제 MySQL 계정이 다르다.

해결:

- PowerShell 또는 IntelliJ Run Configuration에 다음 값을 설정한다.

```powershell
$env:INSURANCE_HUB_DB_URL="jdbc:mysql://localhost:3306/insurance_hub?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true"
$env:INSURANCE_HUB_DB_USERNAME="insurance_hub_app"
$env:INSURANCE_HUB_DB_PASSWORD="change-me"
```

- MySQL에서 해당 계정과 권한이 실제로 존재하는지 확인한다.

재발 방지:

- README와 runbook에 환경 변수 이름을 명확히 적는다.
- 실제 비밀번호는 문서, commit, chat text에 노출하지 않는다.
- `application-local.example.yml`은 placeholder만 유지한다.

## Flyway Checksum Mismatch

증상:

- 애플리케이션 시작 중 Flyway validation이 실패한다.
- 이미 적용된 migration의 checksum이 다르다는 오류가 나온다.

원인:

- 이미 local DB에 적용된 `V1__...sql` 같은 migration file을 나중에 수정했다.
- Flyway는 적용된 migration의 checksum을 `flyway_schema_history`에 저장하기 때문에 파일 내용이 바뀌면 불일치가 발생한다.

해결:

- 이미 적용된 migration은 되돌린다.
- 변경이 필요하면 새 migration file을 추가한다.
- local disposable DB라면 DB를 재생성하고 migration을 처음부터 다시 적용할 수 있다.

재발 방지:

- "적용된 migration은 수정하지 않는다"를 원칙으로 유지한다.
- schema 변경은 항상 `Vn__description.sql` 형태의 새 migration으로 추가한다.

## Database Name 혼동

증상:

- migration은 정상인데 table이 보이지 않는다.
- 애플리케이션은 다른 DB schema에 연결된다.
- `insurance`와 `insurance_hub` 중 어느 DB를 사용하는지 혼란이 생긴다.

원인:

- 개발 중 local DB 이름이 바뀌었거나, 환경 변수와 local YAML default가 서로 다른 DB를 가리킨다.
- IntelliJ 실행 설정과 PowerShell 환경 변수가 서로 다르다.

해결:

- 현재 실행 중인 profile과 datasource URL을 로그에서 확인한다.
- 사용하려는 DB 이름을 하나로 정하고 MySQL에서 직접 확인한다.
- evaluator용 안내는 `insurance_hub` 예시를 사용하고, 개인 local override는 별도로 관리한다.

재발 방지:

- runbook에 권장 DB 이름과 환경 변수 예시를 명확히 둔다.
- local-only 설정과 제출용 예시 설정을 혼동하지 않는다.

## DB User 혼동

증상:

- 같은 password를 입력했는데도 MySQL 접속이 거부된다.
- `root`와 `insurance_hub_app` 중 어떤 계정이 사용되는지 혼란이 생긴다.

원인:

- local YAML default, 환경 변수, IntelliJ 실행 설정이 서로 다른 username을 사용한다.
- MySQL 계정 권한이 대상 schema에 부여되지 않았다.

해결:

- 실행 로그의 JDBC URL과 username을 확인한다.
- MySQL에서 다음과 같이 전용 계정을 생성하고 권한을 준다.

```sql
create database if not exists insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
create user if not exists 'insurance_hub_app'@'localhost' identified by 'change-me';
grant all privileges on insurance_hub.* to 'insurance_hub_app'@'localhost';
flush privileges;
```

재발 방지:

- 제출용 문서에는 전용 계정 예시를 사용한다.
- 실제 로컬 계정 정보는 commit하지 않는다.

## JMS Health Check 실패

증상:

- `/actuator/health`가 DOWN으로 표시된다.
- Artemis broker를 별도로 실행하지 않았다는 오류처럼 보인다.
- 앱 기능은 아직 MQ를 사용하지 않는데 health 때문에 시작/확인이 불안정해진다.

원인:

- Actuator JMS health indicator가 broker 연결을 검사한다.
- 개발 초기에 broker가 local에서 실행되지 않는 상태였거나, embedded broker 설정과 health check 기대가 맞지 않았다.

해결:

- local profile에서 JMS health check를 비활성화한다.
- Phase 5 이후에는 embedded in-vm Artemis를 사용하여 별도 broker 없이 MQ demo를 수행한다.

재발 방지:

- local demo infrastructure는 app startup과 함께 동작하도록 둔다.
- health check는 실제 demo 운영에 필요한 항목만 켠다.

## IntelliJ Environment Variable 설정 위치 혼동

증상:

- PowerShell에서는 실행되지만 IntelliJ Run Configuration에서는 DB 접속이 실패한다.
- 환경 변수를 넣었다고 생각했지만 애플리케이션이 읽지 못한다.

원인:

- IntelliJ Run Configuration에서 Environment variables 입력란이 option 설정 아래에 숨겨져 있다.
- 실행 configuration을 복사하면서 환경 변수 값이 빠졌다.

해결:

- Run/Debug Configurations에서 Environment variables 입력란을 열고 DB 관련 값을 명시한다.
- 적용 후 실행 로그에서 datasource URL과 active profile을 확인한다.

재발 방지:

- runbook에 IntelliJ 환경 변수 설정을 별도로 언급한다.
- demo 전에는 PowerShell 실행과 IntelliJ 실행 중 하나를 기준으로 정하고 확인한다.

## Environment Variable 이름 오타

증상:

- 환경 변수를 설정했는데도 기본값 또는 빈 password가 사용된다.
- DB 접속 실패 원인이 password 누락처럼 보인다.

원인:

- `INSURANCE_HUB_DB_PASSWORD` 같은 변수 이름에 오타가 있다.
- 변수명 prefix나 underscore가 문서와 실행 환경에서 다르다.

해결:

- README와 runbook의 변수명을 그대로 복사해 사용한다.
- PowerShell에서 `Get-ChildItem Env:INSURANCE_HUB*`로 실제 값 존재 여부를 확인한다.

재발 방지:

- 환경 변수 이름은 한 곳에 표준화한다.
- 새 설정을 추가할 때 `app.*` namespace와 문서 예시를 함께 갱신한다.

## DB Password 노출 위험

증상:

- local config나 채팅/문서에 실제 DB password가 노출될 수 있다.
- `git diff`에 개인 local credential이 보인다.

원인:

- local demo를 빠르게 실행하기 위해 실제 password를 YAML에 직접 입력했거나, 로그/채팅에 그대로 붙여 넣었다.

해결:

- 실제 password가 포함된 파일은 commit하지 않는다.
- 제출용 문서는 placeholder와 environment variable 예시만 사용한다.
- 노출된 password는 즉시 변경한다.

재발 방지:

- commit 전 `git diff`를 확인한다.
- `application-local.yml`은 local-only 파일로 취급한다.
- `application-local.example.yml`에는 실제 credential을 넣지 않는다.

## REST Simulator URL 포트 불일치

증상:

- REST 실행이 connection refused로 실패한다.
- 애플리케이션은 다른 port로 실행 중인데 REST config는 `http://localhost:8080`을 가리킨다.

원인:

- smoke test나 IntelliJ 실행에서 `server.port`를 변경했지만 REST endpoint config의 `baseUrl`은 갱신하지 않았다.

해결:

- admin REST config 화면에서 `baseUrl`을 현재 실행 port에 맞춘다.
- 기본 demo는 `http://localhost:8080` 기준으로 실행한다.

재발 방지:

- demo 중 port를 변경했다면 REST/SOAP config도 함께 확인한다.
- port 충돌이 없으면 기본 8080을 사용한다.

## SOAP Fault 확인

증상:

- SOAP 실행이 실패하고 execution detail에 fault/error message가 표시된다.

원인:

- request XML에 `FAIL`이 포함되어 simulator가 통제된 SOAP fault를 반환했다.
- 또는 SOAP endpoint URL, SOAPAction, XML 구조가 설정과 맞지 않는다.

해결:

- 정상 payload로 다시 실행한다.
- SOAP config의 `endpointUrl`, `soapAction`, `requestTemplateXml`을 확인한다.

재발 방지:

- demo용 정상 XML과 실패 XML을 분리해 보관한다.
- SOAP simulator path를 변경하지 않는다.

## Embedded Artemis Startup Issue

증상:

- 앱 시작 시 Artemis 관련 로그가 길게 출력되거나 broker startup이 실패한다.
- MQ 실행이 destination 연결 오류로 실패한다.

원인:

- embedded Artemis broker가 app process 내부에서 시작되며, 이전 실행의 runtime file이나 port/resource 상태가 영향을 줄 수 있다.
- local 환경에서 broker persistence directory가 꼬일 수 있다.

해결:

- 애플리케이션 process를 완전히 종료하고 다시 실행한다.
- 필요하면 local runtime directory를 정리한다.
- MQ health check와 embedded broker 설정을 확인한다.

재발 방지:

- demo 전에 이전 Java process가 남아 있지 않은지 확인한다.
- MQ는 별도 외부 broker가 아니라 embedded demo broker임을 문서에 명확히 둔다.

## MQ Consumer Failure Demo

증상:

- MQ publish는 성공했지만 consume/process status가 FAILED로 기록된다.

원인:

- payload에 `FAIL`이 포함되어 consumer processing failure를 의도적으로 발생시켰다.

해결:

- 정상 payload로 다시 실행한다.
- execution detail과 MQ monitoring에서 publish status와 consume status를 분리해 확인한다.

재발 방지:

- demo에서 "producer 성공"과 "consumer 처리 실패"를 구분해 설명한다.

## Local Port Conflict

증상:

- 앱 시작 시 `Port 8080 was already in use` 또는 SFTP/FTP port bind 오류가 발생한다.
- `10021`, `10022` port를 사용할 수 없다는 메시지가 나온다.

원인:

- IntelliJ에서 이전 app instance가 아직 실행 중이다.
- 다른 process가 8080, 10021, 10022 port를 사용하고 있다.

해결:

- 기존 Java process를 종료한다.
- smoke test에서는 임시로 `--server.port=18080`을 사용할 수 있다.
- 파일 전송 서버가 필요 없는 smoke test라면 `--app.file-transfer.sftp.enabled=false`, `--app.file-transfer.ftp.enabled=false`를 사용할 수 있다.

재발 방지:

- demo 전 Task Manager나 terminal에서 실행 중인 Java process를 확인한다.
- 기본 demo port 목록을 runbook에 적어둔다.

## Embedded SFTP/FTP Server Startup Issue

증상:

- SFTP 또는 FTP demo server가 시작되지 않는다.
- file transfer 실행이 connection refused 또는 login failure로 실패한다.

원인:

- port conflict, local firewall, 이전 process 잔존, runtime directory 문제 중 하나일 가능성이 높다.

해결:

- 앱을 완전히 종료 후 재시작한다.
- `build/file-transfer-demo` directory 상태를 확인한다.
- SFTP는 `127.0.0.1:10022`, FTP는 `127.0.0.1:10021`을 사용한다.

재발 방지:

- demo 전 port 사용 여부를 확인한다.
- file transfer demo directory는 generated runtime file로 취급한다.

## File Download Path Missing

증상:

- download 실행이 실패하고 remote file을 찾을 수 없다는 오류가 나온다.

원인:

- embedded server remote root에 요청한 remote file path가 존재하지 않는다.

해결:

- 성공 demo에는 `/outbox/sample-download.txt`를 사용한다.
- custom file은 먼저 upload하거나 remote demo directory에 file을 만든다.

재발 방지:

- demo remote root를 문서화한다.
  - SFTP: `build/file-transfer-demo/remote/sftp`
  - FTP: `build/file-transfer-demo/remote/ftp`

## FTP Passive Mode Issue

증상:

- FTP login은 성공하지만 upload/download가 멈추거나 data transfer에서 실패한다.

원인:

- FTP는 control connection과 data connection이 분리되어 있어 passive mode 설정과 local firewall 영향을 받을 수 있다.

해결:

- local demo에서는 passive mode를 켠 상태로 유지한다.
- loopback data port가 보안 프로그램에 의해 차단되지 않는지 확인한다.

재발 방지:

- FTP passive mode를 config UI에서 확인 가능하게 둔다.
- 운영 환경에서는 FTP보다 SFTP 사용을 우선 검토한다.

## File Transfer Path Traversal Rejected

증상:

- SFTP/FTP 연결 전에 실행이 실패한다.
- local file name이나 remote path에 traversal을 사용할 수 없다는 오류가 나온다.

원인:

- local demo는 `..`, absolute local path, nested local file path를 의도적으로 차단한다.

해결:

- `sample-upload.txt` 같은 단순 file name을 사용한다.
- remote path는 `/inbox/sample-upload.txt`처럼 절대 remote path를 사용한다.

재발 방지:

- local demo file은 project-local demo directory 아래에서만 다룬다.
- admin form으로 임의 filesystem path를 노출하지 않는다.

## Spring Batch Metadata Table 누락

증상:

- Batch 실행이 launch 단계에서 실패한다.
- `BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, sequence table이 없다는 오류가 나온다.

원인:

- Spring Batch metadata table이 필요하지만, 이 프로젝트는 `spring.batch.jdbc.initialize-schema=never`로 두고 Flyway가 table을 생성한다.

해결:

- `V8__phase_7_real_batch_integration.sql`이 적용되었는지 확인한다.
- Flyway schema version이 8인지 확인한다.

재발 방지:

- Spring Batch auto schema initialization을 켜지 않는다.
- Batch metadata 변경은 Flyway migration에 포함한다.

## Duplicate Spring Batch Job Parameters

증상:

- 같은 parameter로 Batch를 다시 실행하면 job instance가 이미 존재한다는 오류가 발생한다.

원인:

- Spring Batch는 identifying parameter로 job instance를 구분한다.
- 완전히 같은 parameter로 재실행하면 같은 job instance로 판단될 수 있다.

해결:

- 매 실행마다 고유한 `run.id` parameter를 추가한다.

재발 방지:

- Batch launch code에서 `run.id` 또는 동등한 unique parameter를 유지한다.

## 긴 Transaction 안에서 Batch Launch

증상:

- Batch launch 중 transaction state 오류가 발생하거나 lock이 예상보다 오래 유지된다.

원인:

- Spring Batch job repository 작업은 넓은 business transaction 안에서 실행하지 않는 것이 안전하다.

해결:

- running `interface_execution`을 먼저 저장한다.
- protocol executor 호출은 긴 transaction 밖에서 수행한다.
- 결과 저장은 별도 흐름으로 처리한다.

재발 방지:

- 외부 protocol 호출이나 Spring Batch launch를 하나의 큰 DB transaction으로 감싸지 않는다.

## Batch Scheduler Does Not Fire

증상:

- Batch config에 cron expression이 있지만 scheduled execution이 생성되지 않는다.

원인:

- app-level scheduler가 기본적으로 꺼져 있다.
- Batch config의 `enabled` flag가 꺼져 있다.

해결:

- `app.batch.scheduler.enabled=true`를 설정한다.
- admin UI에서 Batch config를 enabled로 변경한다.
- `0/30 * * * * *` 같은 six-field Spring cron expression을 사용한다.

재발 방지:

- scheduler 기본값이 비활성화라는 점을 runbook에 명시한다.
- 시연에서는 timing이 중요하지 않으면 manual execution을 사용한다.

## Controlled Batch Failure Keeps Failing On Retry

증상:

- 실패한 Batch execution을 retry했는데 같은 오류로 다시 실패한다.

원인:

- retry는 auditability를 위해 원본 request payload를 그대로 사용한다.
- 원본 payload에 `FAIL` 또는 `forceFail=true`가 있으면 deterministic failure가 반복된다.

해결:

- recovery demo는 `forceFail=false`인 새 manual execution으로 보여준다.
- retry는 같은 요청을 다시 실행한다는 audit linkage를 설명하는 용도로 사용한다.

재발 방지:

- demo에서 retry와 corrected rerun의 차이를 명확히 설명한다.

## Batch `forceFail=false`가 실패로 처리됨

증상:

- `{"businessDate":"TODAY","forceFail":false}`로 실행했는데 Batch가 실패한다.
- execution detail의 Spring Batch parameter에 `forceFail=true`가 보인다.

원인:

- 초기 failure detector가 raw JSON text 전체에서 `FAIL`을 검색했다.
- field name `forceFail` 자체에 `FAIL`이 포함되어 false 값도 실패 신호로 처리되었다.

해결:

- JSON payload를 먼저 parsing한다.
- `forceFail=true`만 명시적 실패로 처리한다.
- JSON payload에서는 field name이 아니라 value에서 demo `FAIL` token을 검사한다.

재발 방지:

- launched Spring Batch parameter를 확인하는 regression test를 둔다.
- demo failure rule은 control field name과 충돌하지 않게 좁게 정의한다.

## Monitoring MVC Test에서 CSRF 누락

증상:

- monitoring controller MVC test가 shared sidebar fragment 렌더링 중 실패한다.
- `_csrf.parameterName`이 null이라는 오류가 나온다.

원인:

- 일부 MVC test는 Spring Security filter를 비활성화한다.
- shared sidebar가 항상 logout form의 hidden CSRF input을 렌더링하려고 했다.

해결:

- `_csrf`가 존재할 때만 hidden CSRF input을 렌더링한다.
- 실제 인증 browser session에서는 Spring Security가 CSRF token을 제공하므로 logout form은 정상 동작한다.

재발 방지:

- shared Thymeleaf fragment는 security filter가 없는 test context도 견딜 수 있게 만든다.
- 새 admin page를 추가할 때 controller rendering test를 함께 추가한다.

## Dashboard Aggregation Load Concern

증상:

- 실행 이력이 많아질수록 dashboard 조회가 느려질 수 있다.

원인:

- 현재 monitoring summary는 운영 table에서 요청 시점에 집계한다.
- 포트폴리오 demo를 위해 단순하고 설명 가능한 구조를 선택했다.

해결:

- 오늘, 최근 7일 같은 bounded time window를 사용한다.
- protocol, status, started time 같은 column을 기준으로 grouped query를 사용한다.

재발 방지:

- 운영 규모로 커지면 rollup table, scheduled metric snapshot, external observability store 도입을 검토한다.

## Repetitive Protocol Summary Queries

증상:

- dashboard protocol card는 정상 동작하지만 protocol/status마다 count query가 반복된다.

원인:

- 초기 monitoring 구현은 명시적인 repository method를 우선했다.
- REST, SOAP, MQ, SFTP, FTP, Batch까지 늘어나면서 반복 count가 불필요해졌다.

해결:

- interface count를 protocol/status 기준 grouped query로 조회한다.
- execution count를 protocol/status/time window 기준 grouped query로 조회한다.
- memory counter로 dashboard protocol summary를 구성한다.

재발 방지:

- overview 화면에는 grouped read-model query를 우선 사용한다.
- production volume이 커질 때만 rollup table을 도입한다.

## Final Local Config Secret Review

증상:

- 개발자의 local profile file에 개인 DB 값이 남아 있을 수 있다.
- command output이나 commit에 실제 password가 노출될 수 있다.

원인:

- IntelliJ local demo를 빠르게 실행하기 위해 개인 MySQL credential을 local config에 넣는 경우가 있다.
- 예시 파일과 실제 local-only 파일의 역할이 혼동될 수 있다.

해결:

- 실제 local password를 ticket, docs, commit, chat text에 붙여 넣지 않는다.
- `INSURANCE_HUB_DB_URL`, `INSURANCE_HUB_DB_USERNAME`, `INSURANCE_HUB_DB_PASSWORD` 환경 변수를 우선 사용한다.
- 실제 credential이 들어간 `application-local.yml`은 local-only로 취급한다.

재발 방지:

- commit 전 `git diff`를 확인한다.
- `application-local.example.yml`은 placeholder만 유지한다.
- README와 runbook에 credential handling을 명확히 적는다.
