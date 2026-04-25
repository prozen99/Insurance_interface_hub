# Phase 계획

이 문서는 Insurance Interface Hub가 포트폴리오 프로젝트로 어떤 순서로 확장되었는지 정리합니다. 모든 Phase는 완료 상태이며, Phase 9는 제출 준비와 문서/테스트 정리를 담당합니다.

## Phase 0 - Foundation

상태: 완료

- Java 21, Spring Boot 3.x, Gradle baseline 구성
- modular monolith package structure 생성
- local MySQL profile과 Flyway baseline 구성
- 문서 baseline 작성
- admin dashboard placeholder와 smoke API 구성

## Phase 1 - Admin Authentication And Master CRUD

상태: 완료

- DB 기반 admin login
- BCrypt password 저장
- PartnerCompany CRUD
- InternalSystem CRUD
- InterfaceDefinition CRUD
- interface 활성/비활성 처리

## Phase 2 - Common Execution Engine, History, Failure Handling, Retry

상태: 완료

- 공통 execution service와 executor contract 구성
- 수동 실행, 실행 이력, step log, retry task 구현
- dashboard 기본 metric 구현
- 모든 protocol type에 대한 mock executor 구성

## Phase 3 - Real REST Integration

상태: 완료

- REST endpoint 설정 UI와 validation
- Spring `RestClient` 기반 실제 REST executor
- local REST simulator API
- REST request/response metadata capture

## Phase 4 - Real SOAP Integration

상태: 완료

- SOAP endpoint 설정 UI와 validation
- 실제 SOAP-over-HTTP executor
- local SOAP simulator API
- SOAP request XML, response XML, SOAPAction, HTTP status, latency, fault visibility

## Phase 5 - Real MQ Integration

상태: 완료

- MQ channel 설정 UI와 validation
- Docker 없이 동작하는 embedded in-vm Artemis broker
- 실제 JMS text message publish/consume flow
- publish status와 consume status를 분리한 MQ message history

## Phase 6 - Real SFTP/FTP Integration

상태: 완료

- SFTP/FTP file-transfer 설정 UI와 validation
- Docker 없이 동작하는 embedded SFTP/FTP server
- 실제 upload/download flow
- file transfer history와 retry support

## Phase 7 - Real Batch Integration

상태: 완료

- Batch job 설정 UI와 validation
- Spring Batch manual launch
- 선택적으로 켤 수 있는 local scheduler support
- interface settlement summary와 failed retry aggregation demo job
- Flyway가 관리하는 Spring Batch metadata tables
- Batch run/step history
- job parameter, read/write count, status, output, error를 execution detail에서 확인
- 공통 실행 엔진을 통한 retry/rerun
- REST, SOAP, MQ, SFTP, FTP regression path 유지

## Phase 8 - Monitoring/Dashboard

상태: 완료

- active interfaces, today success/failure, pending retries, recent retry outcome dashboard
- REST, SOAP, MQ, Batch, SFTP, FTP protocol summary card
- 최근 7일 execution trend
- 실패 상위 인터페이스 summary
- recent executions와 pending retry task visibility
- failure, retry, protocol, file transfer, MQ, Batch monitoring page
- keyword, protocol, status, trigger, date range execution filter
- `OperationsMonitoringService` 기반 read-only aggregation

## Phase 9 - Testing, Performance, Final Polish

상태: 완료

- unauthenticated redirect와 valid login을 검증하는 admin security access test 보강
- dashboard protocol summary와 failure summary를 검증하는 monitoring aggregation test 보강
- protocol summary count를 grouped repository query로 개선
- README, requirements, architecture, ERD, protocol, screen, runbook, troubleshooting, demo scenario 문서 최종 정리
- REST, SOAP, MQ, SFTP, FTP, Batch execution path 유지
- `application-local.yml` datasource default 변경 금지 원칙 유지

완료 기준:

- `.\gradlew.bat test` 성공
- `.\gradlew.bat build` 성공
- local DB credential이 제공되면 local profile로 app startup 가능
- admin login 동작
- dashboard와 monitoring page rendering
- 대표 protocol execution test green
- setup, demo data, known limitation, troubleshooting history 문서화
