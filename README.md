# Insurance Interface Hub

Korean title: 보험사 금융 IT 인터페이스 통합관리시스템

보험/금융권에서 운영되는 REST, SOAP, MQ, SFTP, FTP, Batch 인터페이스를 하나의 관리자 콘솔에서 등록, 설정, 실행, 이력 조회, 재처리, 모니터링할 수 있도록 만든 Spring Boot 기반 포트폴리오 프로젝트입니다.

이 프로젝트는 운영 환경에 바로 투입하는 완성형 제품이 아니라, 보험사 인터페이스 운영 시스템을 어떻게 설계하고 단계적으로 확장할 수 있는지 보여주기 위한 로컬 데모형 프로토타입입니다.

## 왜 만들었는가

보험사 IT 시스템은 내부 기간계, 외부 제휴사, 은행, 결제사, 콜센터, 파일 송수신 서버 등과 다양한 방식으로 연동됩니다. 실제 운영에서는 다음과 같은 문제가 자주 발생합니다.

- 인터페이스 정의와 담당 시스템이 문서나 담당자 기억에 흩어져 있음
- REST, SOAP, MQ, 파일 전송, Batch가 서로 다른 방식으로 운영됨
- 실패 이력과 재처리 상태를 한 화면에서 확인하기 어려움
- 요청/응답, 파일 전송 결과, Batch 처리 건수, 오류 메시지가 분산됨
- 장애 대응과 면접 설명에 필요한 운영 흐름을 일관되게 보여주기 어려움

Insurance Interface Hub는 이런 문제를 하나의 공통 실행 엔진과 관리자 콘솔로 정리한 예시입니다.

## 현재 상태

Phase 9 - 최종 정리, 테스트 보강, 성능/문서 정리, 제출 준비 완료

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
- 오늘 성공/실패, 프로토콜 요약, 7일 추이, 실패 상위 인터페이스, 재처리 대기 현황을 보여주는 운영 대시보드
- 장애, 재처리, 프로토콜 상태, MQ, 파일 전송, Batch 모니터링 화면
- 요구사항, 아키텍처, 프로토콜 설계, 화면 명세, 장애 대응 기록, 실행 가이드, 데모 시나리오 문서

의도적으로 제외한 범위:

- 운영용 secret vault 연동
- 외부 운영 MQ broker 구성과 고가용성 큐 토폴로지
- 운영 Batch 캘린더, 분산 락, 파티셔닝, 원격 스케줄링
- Prometheus, Grafana, tracing, 알림 발송, SLO 기반 운영 관측 체계

## 핵심 기능

- 관리자 로그인 및 로그아웃
- 제휴사, 내부 시스템, 인터페이스 정의 관리
- 프로토콜별 설정 관리
- 수동 실행 및 실패 재처리
- 실행 이력, 실행 상세, 단계 로그 조회
- REST 요청/응답, SOAP XML, MQ publish/consume, SFTP/FTP 파일 전송, Batch read/write count 확인
- 실패 중심 모니터링, 재처리 큐, 프로토콜별 운영 요약
- 로컬 데모용 시뮬레이터와 embedded 인프라 제공

## 지원 프로토콜

| 프로토콜 | 구현 상태 |
| --- | --- |
| REST | 로컬 simulator endpoint로 실제 HTTP 호출 |
| SOAP | 로컬 SOAP simulator endpoint로 실제 SOAP-over-HTTP 호출 |
| MQ | embedded Artemis를 통한 실제 JMS publish/consume |
| SFTP | embedded SFTP 서버를 통한 로컬 upload/download |
| FTP | embedded FTP 서버를 통한 로컬 upload/download |
| Batch | Spring Batch JobLauncher 기반 수동/스케줄 실행 |

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

- `admin`: 로그인, 대시보드 진입점
- `interfacehub`: 인터페이스 정의, 실행, 재처리 공통 도메인
- `protocol.rest`: REST 설정, 실행기, 로컬 simulator
- `protocol.soap`: SOAP 설정, 실행기, 로컬 simulator
- `protocol.mq`: MQ 설정, embedded Artemis, 메시지 이력
- `protocol.sftp`, `protocol.ftp`, `protocol.filetransfer`: 파일 전송 설정, 클라이언트, 로컬 데모 서버, 전송 이력
- `protocol.batch`: Spring Batch Job, Batch 설정, Batch 실행 이력
- `monitoring`: 대시보드 및 운영 모니터링 조회 모델
- `audit`: 감사 로그 확장 지점

공통 실행 흐름은 `InterfaceExecutionService`가 담당하고, 프로토콜별 실제 수행은 `InterfaceExecutor` 구현체가 담당합니다. 따라서 실행 이력, 단계 로그, 재처리 정책은 공통으로 유지하면서 프로토콜별 구현은 독립적으로 확장할 수 있습니다.

## 로컬 데모 인프라

- MQ: embedded in-vm Artemis
- SFTP: `127.0.0.1:10022`
- FTP: `127.0.0.1:10021`
- 파일 전송 runtime root: `build/file-transfer-demo`
- Batch output directory: `build/batch-demo/output`

Batch 수동 실행 예시:

```json
{"businessDate":"TODAY","forceFail":false}
```

`forceFail`을 `true`로 설정하거나 payload 값에 `FAIL`을 포함하면 실패 및 재처리 흐름을 시연할 수 있습니다.

## 실행 방법

로컬 DB 비밀번호는 소스에 고정하지 말고 환경 변수나 IntelliJ Run Configuration의 Environment variables로 주입하는 방식을 권장합니다.

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

## 5분 시연 흐름

1. `/login`에서 데모 계정으로 로그인합니다.
2. `/admin`에서 오늘 성공/실패, 재처리 대기, 프로토콜 요약, 실패 상위 인터페이스를 확인합니다.
3. `/admin/interfaces`에서 seeded interface를 선택하고 프로토콜별 설정을 확인합니다.
4. 정상 payload로 수동 실행 후 execution detail에서 요청/응답과 단계 로그를 확인합니다.
5. `FAIL` 또는 `forceFail=true`로 실패를 발생시키고 재처리/재실행 흐름을 확인합니다.
6. `/admin/monitoring/failures`, `/admin/monitoring/retries`, `/admin/monitoring/protocols`에서 운영 관점의 요약을 확인합니다.

## 테스트와 빌드

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

테스트 범위에는 로그인 접근 제어, 서비스 계층, controller/MVC, 실행/재처리 흐름, REST/SOAP/MQ/SFTP/FTP/Batch 대표 경로, 모니터링 요약 서비스가 포함됩니다.

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
- [ADR-001 Modular Monolith](docs/adr/ADR-001-modular-monolith.md)

## 알려진 한계

- 이 프로젝트의 MQ, SFTP, FTP 인프라는 로컬 데모를 위한 embedded/in-process 구성입니다.
- 운영 secret 관리, 권한 세분화, 감사 승인 workflow는 단순화되어 있습니다.
- 모니터링은 요청 시점 집계 방식이며, 운영용 metric warehouse나 alerting stack은 포함하지 않습니다.
- Batch scheduling은 로컬 데모용이며 기본값은 비활성화입니다.
- 운영 환경 적용 전에는 보안, 인프라, 성능, 장애 복구, 배포 전략에 대한 별도 설계가 필요합니다.

## 포트폴리오 관점에서 강조할 점

- 단순 CRUD를 넘어 실제 운영 흐름인 실행, 이력, 실패, 재처리, 모니터링까지 연결했습니다.
- REST, SOAP, MQ, SFTP, FTP, Batch를 하나의 공통 실행 모델로 묶었습니다.
- local MySQL과 Flyway 기반으로 schema 변경 이력을 관리합니다.
- React 없이 Thymeleaf만으로 서버 렌더링 기반 관리자 콘솔을 구성했습니다.
- 모든 외부 연동은 로컬 simulator 또는 embedded 인프라로 시연 가능하게 설계했습니다.
- 문서, 테스트, 데모 시나리오를 함께 정리하여 평가자가 빠르게 구조와 의도를 파악할 수 있도록 했습니다.

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
- Phase 9: testing, performance cleanup, final polish, and submission readiness
