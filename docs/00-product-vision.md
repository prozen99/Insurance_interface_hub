# 제품 비전

## 프로젝트 정체성

- English title: Insurance Interface Hub
- Korean title: 보험사 금융 IT 인터페이스 통합관리시스템
- 현재 단계: Phase 9 - 최종 정리 및 포트폴리오 제출 준비 완료

Insurance Interface Hub는 보험/금융권 인터페이스 운영을 하나의 콘솔에서 관리하기 위한 Spring Boot 기반 포트폴리오 프로젝트입니다. 인터페이스 등록, 프로토콜별 설정, 수동 실행, 실행 이력, 실패 재처리, 모니터링, Batch 실행 결과를 하나의 흐름으로 보여주는 것을 목표로 합니다.

## 문제 정의

보험사 IT 환경에서는 내부 기간계, 제휴 보험사, 은행, 결제사, 콜센터, 외부 데이터 제공자와 다양한 방식으로 연동합니다. REST, SOAP, MQ, Batch, SFTP, FTP 같은 프로토콜이 함께 사용되기 때문에 운영 정보가 분산되기 쉽습니다.

운영자가 빠르게 확인해야 하는 질문은 다음과 같습니다.

- 이 인터페이스의 담당 제휴사와 내부 시스템은 무엇인가?
- 어떤 프로토콜과 endpoint, destination, remote path, job을 사용하는가?
- 오늘 실행이 성공했는가, 실패했는가?
- 어떤 파일 전송이나 Batch step이 실패했는가?
- 재처리 대기 건이 있는가?
- 요청/응답, payload, latency, 오류 메시지를 어디에서 확인할 수 있는가?

이 프로젝트는 위 질문에 답할 수 있는 통합 운영 콘솔의 기본 형태를 구현합니다.

## 목표 사용자

- 보험사 인터페이스 운영 담당자
- 금융권 backend 개발자
- Batch 운영자
- IT 감사 및 장애 대응 담당자
- 포트폴리오를 검토하는 기술 면접관과 평가자

## 제품 목표

하나의 Spring Boot 애플리케이션 안에서 인터페이스 master data, 프로토콜 설정, 실행, 이력, 재처리, 모니터링을 일관되게 관리합니다. 운영자와 평가자가 첫 화면에서 전체 상태를 파악하고, 문제가 있는 인터페이스의 상세 이력까지 자연스럽게 내려갈 수 있도록 구성합니다.

## 현재 구현 범위

- Java 21, Spring Boot 3.x, Gradle, Thymeleaf, Spring Security, JPA, Flyway, local MySQL
- DB 기반 관리자 로그인과 BCrypt password 저장
- PartnerCompany, InternalSystem, InterfaceDefinition CRUD
- 공통 실행 엔진, 실행 이력, 단계 로그, 재처리 작업
- REST 로컬 simulator 기반 실제 HTTP 실행
- SOAP 로컬 simulator 기반 실제 SOAP-over-HTTP 실행
- MQ embedded Artemis 기반 실제 JMS publish/consume
- SFTP/FTP embedded server 기반 실제 파일 upload/download
- Spring Batch 기반 수동 실행 및 로컬 스케줄 실행 지원
- 장애, 재처리, 프로토콜, MQ, 파일 전송, Batch 모니터링 화면
- 실행 가이드, 장애 대응 기록, 데모 시나리오, Phase 계획 문서

## 제품 원칙

- 검증 전에는 하나의 애플리케이션을 유지하고, package boundary로 modular monolith를 구성합니다.
- clever한 추상화보다 읽기 쉽고 면접에서 설명 가능한 코드를 우선합니다.
- schema 변경은 Flyway migration으로 관리하고 이미 적용된 migration은 수정하지 않습니다.
- 실제 secret은 저장소에 넣지 않고, 로컬 환경 변수나 local-only 설정으로 관리합니다.
- 프로토콜별 구현은 독립적으로 두되, 실행 이력과 재처리 모델은 공통화합니다.
- 첫 5분 데모 흐름을 명확히 유지합니다: 로그인, 대시보드, 인터페이스 상세, 실행, 이력, 실패, 재처리, 모니터링.

## 운영 제품과의 차이

이 프로젝트는 포트폴리오 제출용 로컬 데모 시스템입니다. 실제 운영 적용을 위해서는 secret vault, 운영용 MQ/SFTP/FTP 인프라, alerting, tracing, 권한 세분화, 감사 승인, 분산 Batch scheduling, 성능 검증과 같은 추가 설계가 필요합니다.
