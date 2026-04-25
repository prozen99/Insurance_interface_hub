# 요구사항

## 기능 요구사항

운영자는 다음 기능을 사용할 수 있어야 합니다.

- 관리자 콘솔에 로그인하고 로그아웃할 수 있다.
- 제휴사를 등록, 조회, 수정, 비활성화할 수 있다.
- 내부 시스템을 등록, 조회, 수정, 비활성화할 수 있다.
- 인터페이스 정의를 등록, 조회, 수정, 활성/비활성 처리할 수 있다.
- 인터페이스를 REST, SOAP, MQ, SFTP, FTP, Batch로 분류할 수 있다.
- REST endpoint 설정을 관리할 수 있다.
- SOAP endpoint 설정을 관리할 수 있다.
- MQ channel 설정을 관리할 수 있다.
- SFTP/FTP 파일 전송 설정을 관리할 수 있다.
- Batch job 설정을 관리할 수 있다.
- 활성화된 인터페이스를 수동 실행할 수 있다.
- 실행 이력, 실행 상세, 단계 로그를 조회할 수 있다.
- 프로토콜별 요청/응답, payload, latency, status, error를 확인할 수 있다.
- 실패한 실행을 재처리할 수 있다.
- 대시보드에서 오늘 성공/실패, 재처리 대기, 프로토콜 요약, 실패 상위 인터페이스를 확인할 수 있다.
- MQ 메시지 이력, 파일 전송 이력, Batch 실행 이력을 모니터링할 수 있다.

## 실행 규칙

- 비활성 인터페이스는 실행할 수 없다.
- 수동 실행은 `MANUAL` trigger type의 `interface_execution` row를 생성한다.
- 재처리는 원본 실패 실행과 연결된 새 실행을 만들고 `RETRY` trigger type을 사용한다.
- Batch 스케줄 실행은 `SCHEDULED` trigger type을 사용한다.
- 실패 실행은 `WAITING` 상태의 retry task를 생성한다.
- 실패하지 않은 실행을 재처리하려고 하면 거부한다.
- 모든 프로토콜 executor는 공통 execution result model을 통해 결과를 반환한다.
- 데모용 통제 실패는 `FAIL` payload 또는 Batch의 `forceFail=true`로 발생시킨다.

## 지원 프로토콜

| 프로토콜 | 최종 동작 |
| --- | --- |
| REST | 로컬 simulator endpoint로 실제 HTTP 호출 |
| SOAP | 로컬 simulator endpoint로 실제 SOAP-over-HTTP 호출 |
| MQ | embedded Artemis를 통한 실제 JMS publish/consume |
| SFTP | embedded SFTP 서버를 통한 실제 파일 upload/download |
| FTP | embedded FTP 서버를 통한 실제 파일 upload/download |
| Batch | Spring Batch JobLauncher 기반 실제 job 실행 |

## 검증 요구사항

- 필수 code/name field는 server-side validation을 수행한다.
- 주요 code는 중복을 허용하지 않는다.
- enum 값은 Spring MVC binding과 validation으로 검증한다.
- 프로토콜 설정 화면은 endpoint, destination, path, job 등 필수 값을 검증한다.
- JSON 설정 field는 JSON 형식 검증을 수행한다.
- SOAP request template은 XML 파싱 가능 여부를 검증한다.
- 파일 전송 local path와 remote path는 traversal을 허용하지 않는다.

## 비기능 요구사항

- Java 21과 Spring Boot 3.x를 사용한다.
- Gradle로 build/test를 수행한다.
- DB는 local MySQL만 사용한다.
- schema 변경은 Flyway migration으로 관리한다.
- UI는 Thymeleaf 기반 server-rendered admin console로 구성한다.
- Windows 환경에서 실행 가능한 명령어와 경로를 사용한다.
- 실제 secret은 저장소에 hardcode하지 않는다.
- Docker 없이 local demo infrastructure가 실행되어야 한다.
- monitoring page는 bounded time window와 grouped query를 사용하여 명확한 N+1 렌더링 문제를 피한다.

## 제외 범위

- 운영용 secret vault 연동
- 운영용 외부 MQ, SFTP, FTP, scheduler 구성
- 분산 worker, 분산 lock, 대용량 Batch partitioning
- 결재/승인 workflow와 고도화된 audit workflow
- 운영용 alerting, tracing, SLO, 장기 metric 저장소
