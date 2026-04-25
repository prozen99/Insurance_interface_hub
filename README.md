# Insurance Interface Hub

Korean title: 보험사 금융 IT 인터페이스 통합관리시스템

## 제출용 요약

보험/금융권에서 운영되는 REST, SOAP, MQ, SFTP, FTP, Batch 인터페이스를 하나의 관리자 콘솔에서 등록, 설정, 실행, 이력 조회, 재처리, 모니터링할 수 있도록 만든 Spring Boot 기반 포트폴리오 프로젝트입니다.

이 프로젝트는 운영 환경에 바로 배포하는 완제품이 아니라, 보험사 인터페이스 운영 시스템을 어떤 구조로 설계하고 단계적으로 확장할 수 있는지 보여주는 제출용 프로토타입입니다. 평가자는 GitHub 문서, 데모 화면, 로컬 실행 또는 배포 URL을 통해 다음 흐름을 확인할 수 있습니다.

- 관리자 로그인
- 인터페이스 master data 관리
- REST, SOAP, MQ, SFTP, FTP, Batch 설정 확인
- 프로토콜별 실제 로컬 실행
- 실행 이력/상세/단계 로그 확인
- 실패 케이스와 재처리 확인
- 운영 dashboard와 monitoring 화면 확인

추천 시연 순서:

1. `/login`에서 데모 계정으로 로그인
2. `/admin` dashboard에서 전체 운영 현황 확인
3. `/admin/interfaces`에서 인터페이스 상세 이동
4. REST, SOAP, MQ, SFTP/FTP, Batch 실행 결과 확인
5. `FAIL` 또는 `forceFail=true`로 실패 케이스 생성
6. execution detail과 retry/monitoring 화면 확인

## 배포 데모

- Demo URL: `https://insuranceinterfacehub-production.up.railway.app/login`
- Base URL: `https://insuranceinterfacehub-production.up.railway.app`
- Dashboard: `https://insuranceinterfacehub-production.up.railway.app/admin`
- Demo account: `admin` / `admin123!`
- Railway 배포 문서: [Railway 배포 가이드](docs/14-deployment-guide.md)

Railway 배포는 portfolio reviewer가 화면과 주요 흐름을 빠르게 확인하기 위한 데모 URL입니다. REST/SOAP simulator, MQ embedded Artemis, 수동 Batch 실행은 같은 Spring Boot app 안에서 동작하도록 구성했습니다. SFTP/FTP embedded demo server는 Railway port 제약을 피하기 위해 `prod` profile에서 기본 비활성화하며, 전체 파일 전송 시연은 로컬 실행을 권장합니다.

배포 후 `/actuator/health`, 로그인, 관리자 콘솔, REST 실행, SOAP 실행까지 확인했습니다. MQ 실행은 확인 예정이며, monitoring 화면에서는 실행 집계를 확인할 수 있습니다.

REST/SOAP seed data는 local demo 기준 `localhost:8080` URL을 사용합니다. Railway에서 REST/SOAP 실행까지 시연하려면 배포 후 admin UI에서 REST `baseUrl`과 SOAP `endpointUrl`을 Railway public domain으로 수정해야 합니다.

Railway/Nixpacks는 Gradle 9 wrapper에서 build가 실패할 수 있어 Gradle wrapper는 `8.14.3`을 사용합니다. Railway build/start command는 [Railway 배포 가이드](docs/14-deployment-guide.md)에 정리되어 있습니다.

로컬 IntelliJ terminal에서 Gradle을 실행할 때 `JAVA_HOME is not set` 오류가 나면 JDK 21 경로를 `JAVA_HOME`과 `Path`에 설정한 뒤 다시 실행합니다.

추천 배포 URL 시연 흐름:

1. 로그인
2. 대시보드 확인
3. 인터페이스 목록 이동
4. REST 실행
5. SOAP 실행
6. MQ 실행
7. 실행 이력 확인
8. 모니터링 확인

## 왜 만들었는가

보험사 IT 시스템은 내부 기간계, 외부 제휴사, 은행, 결제사, 콜센터, 파일 송수신 서버 등과 다양한 방식으로 연동됩니다. 실제 운영에서는 다음과 같은 문제가 자주 발생합니다.

- 인터페이스 정의와 담당 시스템이 문서나 담당자 기억에 흩어져 있음
- REST, SOAP, MQ, 파일 전송, Batch가 서로 다른 방식으로 운영됨
- 실패 이력과 재처리 상태를 한 화면에서 확인하기 어려움
- 요청/응답, 파일 전송 결과, Batch 처리 건수, 오류 메시지가 분산됨
- 장애 대응과 면접 설명에 필요한 운영 흐름을 일관되게 보여주기 어려움

Insurance Interface Hub는 이런 문제를 하나의 공통 실행 엔진과 관리자 콘솔로 정리한 예시입니다.

## 현재 상태

Phase 10 - 제출 패키지 정리 및 시연 자료 준비

Phase 9까지 구현은 완료되어 있으며, Phase 10은 기능 추가가 아니라 평가자가 빠르게 이해할 수 있도록 README, demo scenario, runbook, submission guide를 정리하는 단계입니다.

구현된 범위:

- DB 기반 Spring Security form login
- PartnerCompany, InternalSystem, InterfaceDefinition CRUD
- 공통 실행 엔진, 실행 이력, 단계 로그, 재처리 작업, 프로토콜별 결과 저장
- REST, SOAP, MQ, SFTP, FTP, Batch의 로컬 실제 실행 경로
- MQ용 embedded in-vm Artemis broker
- Docker 없이 실행되는 embedded SFTP/FTP 데모 서버
- Spring Batch 기반 정산 요약 및 실패 재처리 집계 Job
- REST, SOAP, MQ, SFTP/FTP, Batch 설정 UI
- 파일 전송 이력, MQ 메시지 이력, Batch 실행 이력
- 오늘 성공/실패, 프로토콜 요약, 7일 추이, 실패 상위 인터페이스, 재처리 대기 현황을 보여주는 운영 dashboard
- 장애, 재처리, 프로토콜 상태, MQ, 파일 전송, Batch monitoring 화면
- 한국어 README와 `/docs` 문서

의도적으로 제외한 범위:

- 운영용 secret vault 연동
- 외부 운영 MQ broker 구성과 고가용성 queue topology
- 운영 Batch calendar, 분산 lock, partitioning, 원격 scheduling
- Prometheus, Grafana, tracing, alerting, SLO 기반 운영 관측 체계

## 핵심 기능

- 관리자 로그인 및 로그아웃
- 제휴사, 내부 시스템, 인터페이스 정의 관리
- 프로토콜별 설정 관리
- 수동 실행 및 실패 재처리
- 실행 이력, 실행 상세, 단계 로그 조회
- REST 요청/응답, SOAP XML, MQ publish/consume, SFTP/FTP 파일 전송, Batch read/write count 확인
- 실패 중심 monitoring, 재처리 queue, 프로토콜별 운영 요약
- 로컬 데모용 simulator와 embedded infrastructure 제공

## 지원 프로토콜별 구현 현황

| 프로토콜 | 구현 현황 | 데모 방식 | 주요 확인 포인트 |
| --- | --- | --- | --- |
| REST | 실제 HTTP 호출 | local REST simulator endpoint | URL, method, request/response, status code, latency |
| SOAP | 실제 SOAP-over-HTTP 호출 | local SOAP simulator endpoint | SOAPAction, request XML, response XML, fault/error |
| MQ | 실제 JMS publish/consume | embedded Artemis | destination, correlation key, publish/consume status |
| SFTP | 실제 파일 upload/download | embedded SFTP server | direction, local/remote path, file size, transfer status |
| FTP | 실제 파일 upload/download | embedded FTP server | passive mode, local/remote path, transfer status |
| Batch | 실제 Spring Batch job 실행 | JobLauncher + demo jobs | job parameter, step status, read/write/skip count |

## 데모 인터페이스

- `IF_REST_POLICY_001`: REST 보험료 산출 simulator
- `IF_SOAP_POLICY_001`: SOAP 계약 조회 simulator
- `IF_MQ_POLICY_001`: embedded Artemis publish/consume 데모
- `IF_SFTP_POLICY_001`: embedded SFTP upload/download 데모
- `IF_FTP_POLICY_001`: embedded FTP upload/download 데모
- `IF_BATCH_SETTLEMENT_001`: 일일 인터페이스 정산 요약 Batch
- `IF_BATCH_RETRY_AGG_001`: 실패 실행 재처리 집계 Batch

## 기술 스택

- Java 21
- Spring Boot 3.x
- Gradle
- Thymeleaf
- Spring Security form login
- Spring Data JPA
- Spring Batch
- Spring JMS with embedded Artemis
- Spring Integration FTP/SFTP
- Apache MINA SSHD, Apache FtpServer
- Flyway
- Local MySQL

## 아키텍처 요약

이 프로젝트는 하나의 Spring Boot 애플리케이션 안에서 모듈 경계를 나눈 modular monolith 구조입니다.

- `admin`: 로그인, dashboard 진입점
- `interfacehub`: 인터페이스 정의, 실행, 재처리 공통 도메인
- `protocol.rest`: REST 설정, 실행기, 로컬 simulator
- `protocol.soap`: SOAP 설정, 실행기, 로컬 simulator
- `protocol.mq`: MQ 설정, embedded Artemis, 메시지 이력
- `protocol.sftp`, `protocol.ftp`, `protocol.filetransfer`: 파일 전송 설정, client, 로컬 demo server, 전송 이력
- `protocol.batch`: Spring Batch Job, Batch 설정, Batch 실행 이력
- `monitoring`: dashboard 및 운영 monitoring 조회 모델
- `audit`: 감사 로그 확장 지점

공통 실행 흐름은 `InterfaceExecutionService`가 담당하고, 프로토콜별 실제 수행은 `InterfaceExecutor` 구현체가 담당합니다. 실행 이력, 단계 로그, 재처리 정책은 공통으로 유지하면서 프로토콜별 구현은 독립적으로 확장할 수 있습니다.

## 로컬 실행과 배포 실행의 차이

| 구분 | 로컬 실행 | 배포 실행 |
| --- | --- | --- |
| 목적 | 개발/시연/면접 데모 | 평가자가 URL로 빠르게 화면 확인 |
| DB | local MySQL | 배포 환경에서 제공하는 MySQL 또는 managed DB |
| secret | 환경 변수 또는 local-only 설정 | platform secret/environment variable |
| MQ | embedded Artemis | 현재 demo 기준 embedded Artemis 사용 |
| SFTP/FTP | embedded local server | platform에서 port 제약이 있으면 로컬 시연 권장 |
| Batch scheduler | 기본 비활성화 | 필요 시 환경 변수/property로 활성화 |
| 권장 용도 | 전체 프로토콜 실행 검증 | UI/문서/주요 흐름 확인 |

배포 환경에서는 outbound/inbound port, embedded SFTP/FTP port, file system write 권한이 제한될 수 있습니다. 따라서 전체 프로토콜 실행은 로컬 시연이 가장 안정적이고, 배포 URL은 dashboard와 주요 화면 확인 용도로 사용하는 구성이 현실적입니다.

## 배포 시 필요한 환경 변수

배포 환경에서도 실제 DB credential은 코드에 넣지 않고 환경 변수 또는 platform secret으로 주입합니다.

```powershell
INSURANCE_HUB_DB_URL=jdbc:mysql://host:3306/insurance_hub?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true
INSURANCE_HUB_DB_USERNAME=insurance_hub_app
INSURANCE_HUB_DB_PASSWORD=change-me
```

선택 설정:

```powershell
INSURANCE_HUB_PORT=8080
```

Batch scheduler를 배포 환경에서 켤 경우에는 `app.batch.scheduler.enabled=true` 같은 application property를 platform 방식에 맞게 주입합니다. 기본 제출 데모에서는 scheduler를 켜지 않아도 수동 Batch 실행이 가능합니다.

## 로컬 실행 방법

```powershell
$env:INSURANCE_HUB_DB_URL="jdbc:mysql://localhost:3306/insurance_hub?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true"
$env:INSURANCE_HUB_DB_USERNAME="insurance_hub_app"
$env:INSURANCE_HUB_DB_PASSWORD="change-me"
```

빌드 및 실행:

```powershell
.\gradlew.bat build
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

접속 URL:

- Login: http://localhost:8080/login
- Dashboard: http://localhost:8080/admin
- Monitoring: http://localhost:8080/admin/monitoring
- Interfaces: http://localhost:8080/admin/interfaces
- Executions: http://localhost:8080/admin/executions
- Batch Runs: http://localhost:8080/admin/batch-runs
- Smoke API: http://localhost:8080/api/smoke

## 데모 계정

Flyway migration이 로컬 데모용 관리자 계정을 seed합니다.

- Login ID: `admin`
- Password: `admin123!`

DB에는 plain password가 아니라 BCrypt hash가 저장됩니다.

## 제출자가 준비하면 좋은 파일/링크

- GitHub repository URL
- 배포 URL이 있다면 login URL과 dashboard URL
- `README.md`
- [제출 가이드](docs/14-submission-guide.md)
- [데모 시나리오](docs/13-demo-scenarios.md)
- [로컬 실행 가이드](docs/12-local-runbook.md)
- 핵심 화면 screenshot 또는 짧은 시연 영상
- `.\gradlew.bat test`, `.\gradlew.bat build` 결과

## 테스트와 빌드

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

테스트 범위에는 로그인 접근 제어, service 계층, controller/MVC, 실행/재처리 흐름, REST/SOAP/MQ/SFTP/FTP/Batch 대표 경로, monitoring summary service가 포함됩니다.

## 문서 목록

- [제품 비전](docs/00-product-vision.md)
- [요구사항](docs/01-requirements.md)
- [아키텍처](docs/02-architecture.md)
- [ERD](docs/03-erd.md)
- [API 명세](docs/04-api-spec.md)
- [프로토콜 설계](docs/05-protocol-design.md)
- [화면 명세](docs/06-screen-spec.md)
- [Batch 설계](docs/07-batch-design.md)
- [테스트 전략](docs/08-test-strategy.md)
- [장애 대응 기록](docs/09-troubleshooting.md)
- [Phase 계획](docs/10-phase-plan.md)
- [커밋 규칙](docs/11-commit-rules.md)
- [로컬 실행 가이드](docs/12-local-runbook.md)
- [데모 시나리오](docs/13-demo-scenarios.md)
- [제출 가이드](docs/14-submission-guide.md)
- [Railway 배포 가이드](docs/14-deployment-guide.md)
- [ADR-001 Modular Monolith](docs/adr/ADR-001-modular-monolith.md)

## 알려진 한계

- 이 프로젝트의 MQ, SFTP, FTP infrastructure는 로컬 데모를 위한 embedded/in-process 구성입니다.
- 운영 secret 관리, 권한 세분화, 감사 승인 workflow는 단순화되어 있습니다.
- monitoring은 요청 시점 집계 방식이며, 운영용 metric warehouse나 alerting stack은 포함하지 않습니다.
- Batch scheduling은 local demo용이며 기본값은 비활성화입니다.
- 배포 platform에 따라 embedded SFTP/FTP port, file system write, long-running process 제약이 있을 수 있습니다.
- 운영 환경 적용 전에는 보안, 인프라, 성능, 장애 복구, 배포 전략에 대한 별도 설계가 필요합니다.

## 포트폴리오 관점에서 강조할 점

- 단순 CRUD를 넘어 실제 운영 흐름인 실행, 이력, 실패, 재처리, monitoring까지 연결했습니다.
- REST, SOAP, MQ, SFTP, FTP, Batch를 하나의 공통 실행 모델로 묶었습니다.
- local MySQL과 Flyway 기반으로 schema 변경 이력을 관리합니다.
- React 없이 Thymeleaf만으로 server-rendered 관리자 콘솔을 구성했습니다.
- 모든 외부 연동은 local simulator 또는 embedded infrastructure로 시연 가능하게 설계했습니다.
- 한국어 문서, 테스트, 데모 시나리오, 제출 가이드를 함께 정리하여 평가자가 빠르게 구조와 의도를 파악할 수 있도록 했습니다.

## Phase Roadmap

- Phase 0: foundation, documentation baseline, local bootable skeleton
- Phase 1: admin authentication and master CRUD
- Phase 2: common execution engine, history, failure handling, retry
- Phase 3: real REST integration and simulator
- Phase 4: real SOAP integration and simulator
- Phase 5: real MQ integration with embedded Artemis
- Phase 6: real SFTP/FTP integration with local demo servers
- Phase 7: real Batch integration with manual and scheduled launch support
- Phase 8: monitoring dashboard, operational visibility, metrics views, and UI polish
- Phase 9: testing, performance cleanup, final polish, and Korean documentation
- Phase 10: final submission guide and demo package
