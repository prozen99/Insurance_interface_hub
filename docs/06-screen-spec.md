# 화면 명세

Phase 9의 UI는 Thymeleaf 기반 server-rendered admin console입니다. CRUD, 실행, 프로토콜 설정, monitoring, Batch 화면을 일관된 layout으로 구성하여 평가자가 dashboard에서 문제 상황을 보고 상세 이력까지 자연스럽게 이동할 수 있도록 했습니다.

## 공통 Layout

관리자 화면은 다음 요소를 공유합니다.

- 왼쪽 navigation
- Phase 9 product branding
- Dashboard, Monitoring, Interfaces, Executions, Batch Runs, Partners, Internal Systems link
- Logout button
- 성공/실패 flash message
- table 중심의 enterprise admin layout
- execution, retry, transfer, MQ, Batch status badge

## Dashboard

Path: `/admin`

표시 항목:

- 주요 운영 metric card
- 장애, 재처리, 프로토콜 상태, 실행 이력 quick link
- 최근 7일 실행 추이
- 실패 상위 인터페이스
- REST, SOAP, MQ, BATCH, SFTP, FTP protocol card
- 최근 실행
- 재처리 대기 task
- 파일 전송, MQ, Batch summary card

## Monitoring Overview

Path: `/admin/monitoring`

역할:

- 운영자가 전체 상태를 빠르게 확인한다.
- 장애, 재처리, 프로토콜, 파일, MQ, Batch 상세 monitoring 화면으로 이동한다.
- Dashboard와 같은 read-only summary service를 재사용한다.

## Failure Monitoring

Path: `/admin/monitoring/failures`

표시 항목:

- 최근 7일 실패 상위 인터페이스
- 최근 실패 execution
- interface detail과 execution detail link

## Retry Monitoring

Path: `/admin/monitoring/retries`

표시 항목:

- 재처리 대기 count
- 오늘 새로 생성된 waiting retry count
- 최근 7일 완료된 retry count
- waiting retry task table
- 최근 retry task table

## Protocol Monitoring

Path: `/admin/monitoring/protocols`

표시 항목:

- 프로토콜별 전체/활성 인터페이스 수
- 프로토콜별 오늘 성공/실패 count
- 프로토콜별 최근 7일 실행량
- 최근 7일 일자별 trend

## File Transfer Monitoring

Path: `/admin/monitoring/files`

표시 항목:

- 오늘 파일 전송 전체/성공/실패 count
- 최근 SFTP/FTP transfer history
- transfer direction, status, file name, remote path, latency, execution detail link

## MQ Monitoring

Path: `/admin/monitoring/mq`

표시 항목:

- 오늘 publish 성공/실패 count
- 오늘 consume 성공/실패 count
- 최근 message destination, correlation key, publish status, consume status, latency, execution detail link

## Batch Monitoring

Path: `/admin/monitoring/batch`

표시 항목:

- 오늘 Batch 전체/완료/실패/실행중 count
- 최근 Batch run read/write/skip count
- Batch run detail과 unified execution detail link

## Interface List

Path: `/admin/interfaces`

역할:

- 인터페이스 master data를 조회한다.
- keyword, protocol type, status 기준으로 검색한다.
- interface detail로 이동하여 설정과 실행을 확인한다.

## Interface Detail

Path: `/admin/interfaces/{id}`

역할:

- 인터페이스 기본 정보, 제휴사, 내부 시스템, 상태를 보여준다.
- protocol type에 맞는 설정 panel을 보여준다.
- manual execution form을 제공한다.
- 최근 execution 이력을 보여준다.

## Execution History

Path: `/admin/executions`

Filter:

- Keyword
- Protocol type
- Execution status
- Trigger type
- Started from date
- Started to date

목록에서 execution detail로 이동하면 protocol request/response, step log, retry task, MQ message, file transfer, batch run 정보를 확인할 수 있습니다.

## Reviewer Demo Flow

UI는 다음 시연 흐름에 맞춰 구성되어 있습니다.

1. Login
2. Dashboard
3. Interface list
4. Interface detail and protocol configuration
5. Manual execution
6. Execution detail
7. Controlled failure and retry
8. Monitoring pages for failures, retries, protocols, files, MQ, and batch
