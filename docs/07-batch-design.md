# Batch 설계

## 목적

Batch는 즉시 request/response로 끝나지 않는 정기성 또는 대량 처리성 업무를 표현하기 위한 프로토콜 경로입니다. 이 프로젝트에서는 Spring Batch를 사용하여 수동 실행과 로컬 스케줄 실행을 지원하고, REST/SOAP/MQ/SFTP/FTP와 같은 공통 execution history 안에서 결과를 확인할 수 있도록 구성했습니다.

## 구성 요소

| 구성 요소 | 책임 |
| --- | --- |
| `BatchJobConfig` | 인터페이스별 Batch job 설정 |
| `BatchExecutionService` | 설정 조회, parameter parsing, job launch, run/step history 저장 |
| `BatchInterfaceExecutor` | 공통 실행 엔진과 Spring Batch 실행 서비스 연결 |
| `BatchScheduleService` | enabled config를 polling하여 scheduled execution 생성 |
| `BatchRunHistory` | Batch run 단위 결과 저장 |
| `BatchStepHistory` | Batch step 단위 count와 상태 저장 |

## Demo Jobs

### `interfaceSettlementSummaryJob`

목적:

- 오늘 실행된 인터페이스를 프로토콜과 상태별로 집계한다.
- 운영자가 일일 인터페이스 처리 현황을 확인하는 상황을 가정한다.

결과:

- `build/batch-demo/output` 아래 summary file 생성
- read/write count 기록
- output summary 저장

### `failedExecutionRetryAggregationJob`

목적:

- 오늘 실패 실행과 재처리 대기 task를 집계한다.
- 장애 대응 회의나 운영 점검에서 필요한 retry candidate summary를 가정한다.

결과:

- 실패/재처리 요약 file 생성
- read/write count 기록
- output summary 저장

## 실행 방식

Manual execution:

1. 운영자가 interface detail에서 Batch payload를 입력한다.
2. 공통 실행 엔진이 `interface_execution`을 생성한다.
3. `BatchInterfaceExecutor`가 `BatchExecutionService`를 호출한다.
4. `BatchExecutionService`가 configured `jobName`으로 Spring Batch job을 찾아 실행한다.
5. `BatchRunHistory`, `BatchStepHistory`, `InterfaceExecutionStep`을 저장한다.

Scheduled execution:

1. `app.batch.scheduler.enabled=true`로 scheduler를 활성화한다.
2. Batch config의 `enabled` flag를 켠다.
3. `cronExpression`에 맞춰 `BatchScheduleService`가 실행 대상을 찾는다.
4. 수동 실행과 같은 공통 execution path로 실행한다.

## Parameter 예시

```json
{"businessDate":"TODAY","forceFail":false}
```

통제 실패:

```json
{"businessDate":"TODAY","forceFail":true}
```

`forceFail=true` 또는 payload 값에 `FAIL`이 포함되면 demo failure가 발생합니다.

## 재처리 정책

Batch 재처리는 원본 실패 execution의 request payload를 사용해 새 execution을 생성합니다. 이는 운영 감사 관점에서 "동일 요청을 다시 실행했다"는 기록을 남기기 위한 방식입니다. 원본 payload가 `forceFail=true`라면 retry도 같은 실패를 반복하므로, 회복 시연은 `forceFail=false`인 새 manual execution으로 보여주는 것이 좋습니다.

## Spring Batch Metadata

`spring.batch.jdbc.initialize-schema=never`로 설정되어 있으며, Spring Batch metadata table은 Flyway migration이 생성합니다. 운영자가 보는 포트폴리오용 Batch 이력은 `batch_run_history`와 `batch_step_history`에 저장합니다.
