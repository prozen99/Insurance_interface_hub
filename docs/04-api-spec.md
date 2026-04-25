# API 명세

최종 Phase 9 애플리케이션은 Thymeleaf 기반 server-rendered admin console입니다. JSON/XML endpoint는 smoke check와 로컬 REST/SOAP simulator 용도로만 제한적으로 제공합니다. `/admin/**` 화면은 인증된 관리자만 접근할 수 있습니다.

## Public/System Endpoints

| Method | Path | 목적 |
| --- | --- | --- |
| GET | `/login` | 로그인 화면 |
| POST | `/login` | Spring Security login processing |
| POST | `/logout` | Spring Security logout |
| GET | `/api/smoke` | smoke check용 JSON 응답 |
| GET | `/actuator/health` | Actuator health check |

## Simulator Endpoints

| Method | Path | 목적 |
| --- | --- | --- |
| POST | `/simulator/rest/premium/calculate` | 보험료 계산 REST demo target |
| GET | `/simulator/rest/policy/{policyNo}` | 계약 조회 REST demo target |
| POST | `/simulator/rest/claim/register` | 사고 접수 REST demo target |
| POST | `/simulator/soap/policy-inquiry` | 계약 조회 SOAP demo target |
| POST | `/simulator/soap/claim-status` | 사고 상태 조회 SOAP demo target |
| POST | `/simulator/soap/premium-confirmation` | 보험료 확정 SOAP demo target |

## Admin Page Endpoints

모든 `/admin/**` endpoint는 인증이 필요합니다.

| Method | Path | 목적 |
| --- | --- | --- |
| GET | `/admin` | 실행, 재처리, 프로토콜, MQ, 파일 전송, Batch 요약 dashboard |
| GET | `/admin/monitoring` | monitoring overview |
| GET | `/admin/monitoring/failures` | 실패 중심 monitoring view |
| GET | `/admin/monitoring/retries` | 재처리 queue와 retry activity view |
| GET | `/admin/monitoring/protocols` | 프로토콜 상태와 7일 추이 view |
| GET | `/admin/monitoring/files` | 파일 전송 monitoring view |
| GET | `/admin/monitoring/mq` | MQ publish/consume monitoring view |
| GET | `/admin/monitoring/batch` | Batch monitoring view |
| GET | `/admin/interfaces` | 인터페이스 정의 목록 |
| GET | `/admin/interfaces/{id}` | 인터페이스 상세, 실행 form, 프로토콜 설정 |
| POST | `/admin/interfaces/{id}/execute` | 공통 실행 엔진을 통한 수동 실행 |
| GET | `/admin/interfaces/{id}/rest-config` | REST endpoint 설정 form |
| POST | `/admin/interfaces/{id}/rest-config` | REST endpoint 설정 저장 |
| GET | `/admin/interfaces/{id}/soap-config` | SOAP endpoint 설정 form |
| POST | `/admin/interfaces/{id}/soap-config` | SOAP endpoint 설정 저장 |
| GET | `/admin/interfaces/{id}/mq-config` | MQ channel 설정 form |
| POST | `/admin/interfaces/{id}/mq-config` | MQ channel 설정 저장 |
| GET | `/admin/interfaces/{id}/file-transfer-config` | SFTP/FTP 설정 form |
| POST | `/admin/interfaces/{id}/file-transfer-config` | SFTP/FTP 설정 저장 |
| GET | `/admin/interfaces/{id}/batch-config` | Batch job 설정 form |
| POST | `/admin/interfaces/{id}/batch-config` | Batch job 설정 저장 |
| GET | `/admin/executions` | keyword, protocol, status, trigger, date filter가 있는 실행 이력 목록 |
| GET | `/admin/executions/failed` | 실패 실행 목록 |
| GET | `/admin/executions/{id}` | 프로토콜별 상세 이력이 포함된 실행 상세 |
| POST | `/admin/executions/{id}/retry` | 실패 실행 재처리 |
| GET | `/admin/batch-runs` | Batch run 이력 목록 |
| GET | `/admin/batch-runs/{id}` | step count가 포함된 Batch run 상세 |

## Batch Config Fields

- `jobType`: `INTERFACE_SETTLEMENT_SUMMARY` 또는 `FAILED_RETRY_AGGREGATION`
- `jobName`: `interfaceSettlementSummaryJob` 또는 `failedExecutionRetryAggregationJob`
- `cronExpression`: seconds field를 포함하는 Spring cron format
- `parameterTemplateJson`: manual/scheduled execution 기본 parameter
- `enabled`: config-level scheduled execution switch
- `retryable`: 재처리/재실행 가능 여부를 나타내는 운영자 hint
- `timeoutMillis`: demo timeout 값
- `active`: manual execution 가능 여부

## Manual Batch Parameters

Batch 수동 실행은 generic `requestPayload` field에 JSON을 입력합니다.

```json
{"businessDate":"TODAY","forceFail":false}
```

`forceFail`을 `true`로 설정하거나 payload 값에 `FAIL`을 포함하면 통제된 실패 실행을 만들 수 있습니다.

## Monitoring Query Parameters

`/admin/executions`는 다음 query parameter를 지원합니다.

- `keyword`: execution number, interface code, interface name
- `protocolType`: `REST`, `SOAP`, `MQ`, `SFTP`, `FTP`, `BATCH`
- `executionStatus`: `PENDING`, `RUNNING`, `SUCCESS`, `FAILED`
- `triggerType`: `MANUAL`, `RETRY`, `SCHEDULED`
- `startedFrom`: ISO date, inclusive
- `startedTo`: UI 기준 inclusive이며 내부적으로 next-day exclusive bound로 변환
