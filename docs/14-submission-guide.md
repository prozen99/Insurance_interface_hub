# 제출 가이드

이 문서는 Insurance Interface Hub를 GitHub, 배포 URL, 시연 자료로 제출할 때 평가자가 빠르게 이해할 수 있도록 정리한 최종 안내서입니다.

## 제출물 구성

권장 제출물은 다음과 같습니다.

- GitHub repository URL
- 배포 URL이 있다면 login URL과 dashboard URL
- README
- 한국어 문서 `/docs`
- 핵심 화면 screenshot 또는 짧은 demo video
- test/build 실행 결과
- 로컬 실행이 필요한 경우 사용할 MySQL/환경 변수 안내

## GitHub 링크 안내

GitHub repository에서는 다음 파일을 먼저 보면 됩니다.

- `README.md`: 프로젝트 전체 요약과 실행 방법
- `docs/14-submission-guide.md`: 제출/평가용 안내
- `docs/13-demo-scenarios.md`: 5분 데모 흐름
- `docs/12-local-runbook.md`: 로컬 실행 절차
- `docs/02-architecture.md`: modular monolith와 공통 실행 엔진 구조
- `docs/05-protocol-design.md`: REST, SOAP, MQ, SFTP, FTP, Batch 설계
- `docs/09-troubleshooting.md`: 개발 중 만난 이슈와 대응 기록

## 배포 URL 안내

Railway 배포 절차와 환경 변수는 [Railway 배포 가이드](14-deployment-guide.md)를 기준으로 확인합니다.

배포 URL이 준비되어 있다면 다음 순서로 안내합니다.

- Login: `/login`
- Dashboard: `/admin`
- Interfaces: `/admin/interfaces`
- Executions: `/admin/executions`
- Monitoring: `/admin/monitoring`

배포 환경에서는 platform 제약으로 embedded SFTP/FTP port, filesystem write, long-running process 동작이 로컬과 다를 수 있습니다. 따라서 배포 URL은 화면과 흐름 확인용으로 제시하고, 전체 protocol execution은 로컬 시연으로 보완하는 방식을 권장합니다.

## 데모 계정

- Login ID: `admin`
- Password: `admin123!`

데모 계정은 Flyway seed data로 생성됩니다. DB에는 BCrypt hash가 저장되며 plain password가 저장되지 않습니다.

## 주요 시연 흐름

5분 데모 기준:

1. 로그인
2. dashboard에서 전체 운영 상태 확인
3. interface list에서 seeded interface 선택
4. REST 실행 결과 확인
5. SOAP 실행 결과 확인
6. MQ publish/consume 결과 확인
7. SFTP/FTP upload/download 결과 확인
8. Batch 실행 결과와 read/write count 확인
9. 실패 케이스 생성 후 execution detail과 retry task 확인
10. monitoring pages에서 장애, 재처리, 프로토콜별 요약 확인

## 로컬 실행이 필요한 경우 절차

1. JDK 21과 MySQL 8.x를 준비한다.
2. local DB와 계정을 생성한다.

```sql
create database if not exists insurance_hub character set utf8mb4 collate utf8mb4_0900_ai_ci;
create user if not exists 'insurance_hub_app'@'localhost' identified by 'change-me';
alter user 'insurance_hub_app'@'localhost' identified by 'change-me';
grant all privileges on insurance_hub.* to 'insurance_hub_app'@'localhost';
flush privileges;
```

3. 환경 변수를 설정한다.

```powershell
$env:INSURANCE_HUB_DB_URL="jdbc:mysql://localhost:3306/insurance_hub?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true"
$env:INSURANCE_HUB_DB_USERNAME="insurance_hub_app"
$env:INSURANCE_HUB_DB_PASSWORD="change-me"
```

4. build와 test를 실행한다.

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

5. local profile로 실행한다.

```powershell
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

6. http://localhost:8080/login 으로 접속한다.

## 배포 시 필요한 환경 변수

배포 환경에서는 DB 정보를 platform secret 또는 environment variable로 주입합니다.

```powershell
INSURANCE_HUB_DB_URL=jdbc:mysql://host:3306/insurance_hub?serverTimezone=Asia/Seoul&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true
INSURANCE_HUB_DB_USERNAME=insurance_hub_app
INSURANCE_HUB_DB_PASSWORD=change-me
```

선택적으로 port를 지정할 수 있습니다.

```powershell
INSURANCE_HUB_PORT=8080
```

실제 운영 secret은 repository, issue, 문서, chat에 노출하지 않습니다.

## 평가자가 확인하면 좋은 포인트

- Spring Boot 3.x와 Java 21 기반의 단일 애플리케이션 구조
- protocol별 package boundary가 분리된 modular monolith
- `InterfaceExecutionService` 중심의 공통 실행 엔진
- REST, SOAP, MQ, SFTP, FTP, Batch를 하나의 execution history로 통합한 점
- 실패 실행과 retry task가 운영 화면에서 연결되는 점
- Thymeleaf 기반으로 관리자 UI와 monitoring 화면을 구성한 점
- Flyway migration으로 schema 변경 이력을 관리한 점
- local simulator와 embedded infrastructure로 전체 흐름을 재현 가능하게 만든 점
- 한국어 README와 `/docs`로 제출/시연/실행 흐름을 정리한 점

## 알려진 한계

- 이 프로젝트는 포트폴리오용 local demo prototype이며 production-complete system이 아닙니다.
- 운영용 secret vault, 권한 세분화, 승인 workflow, audit hardening은 단순화되어 있습니다.
- MQ, SFTP, FTP는 demo-friendly embedded/in-process infrastructure를 사용합니다.
- 배포 platform에 따라 SFTP/FTP port, filesystem write, long-running process 제약이 있을 수 있습니다.
- monitoring은 요청 시점 집계 방식이며, Prometheus/Grafana/alerting stack은 포함하지 않습니다.
- Batch scheduler는 기본 비활성화이며, local demo 필요 시 property로 활성화합니다.
- 운영 도입 전에는 보안, 배포, 장애 복구, 성능, 외부 인프라 설계가 별도로 필요합니다.

## 추천 제출 설명 문장

Insurance Interface Hub는 보험사 금융 IT 환경에서 여러 프로토콜로 운영되는 인터페이스를 하나의 관리자 콘솔에서 등록, 실행, 이력 조회, 재처리, monitoring할 수 있도록 만든 Spring Boot 기반 포트폴리오 프로젝트입니다. REST, SOAP, MQ, SFTP, FTP, Batch를 모두 로컬 데모 환경에서 실제 실행 흐름으로 구현했고, 공통 실행 엔진과 protocol adapter 구조로 확장 가능성을 보여주는 데 초점을 맞추었습니다.
