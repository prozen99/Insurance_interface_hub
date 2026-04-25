# 프로토콜 설계

## 목적

이 문서는 REST, SOAP, MQ, SFTP, FTP, Batch를 공통 실행 모델로 묶는 방식을 설명합니다. 각 프로토콜은 설정과 실행 방식이 다르지만, 관리자 입장에서는 수동 실행, 실행 이력, 실패, 재처리, monitoring을 같은 흐름으로 볼 수 있어야 합니다.

## Current Executors

| 프로토콜 | Executor | 최종 동작 |
| --- | --- | --- |
| REST | `RestInterfaceExecutor` | Spring `RestClient` 기반 실제 HTTP 호출 |
| SOAP | `SoapInterfaceExecutor` | 실제 SOAP XML over HTTP 호출 |
| MQ | `MqInterfaceExecutor` | embedded Artemis 기반 JMS text message publish/consume |
| SFTP | `SftpInterfaceExecutor` | embedded SFTP server 기반 upload/download |
| FTP | `FtpInterfaceExecutor` | embedded FTP server 기반 upload/download |
| BATCH | `BatchInterfaceExecutor` | Spring Batch JobLauncher 기반 job 실행 |

## 공통 실행 규칙

1. `InterfaceExecutionService`가 실행 row를 생성한다.
2. `InterfaceExecutorFactory`가 `protocolType`에 맞는 executor를 찾는다.
3. executor가 프로토콜별 설정을 조회하고 실제 로컬 demo infrastructure를 호출한다.
4. 실행 결과, step log, protocol-specific history를 저장한다.
5. 실패하면 retry task를 생성한다.
6. 재처리는 원본 실패 실행과 연결된 새 실행으로 기록한다.

## REST

- 설정 table: `rest_endpoint_config`
- 주요 설정: `baseUrl`, `httpMethod`, `path`, `timeoutMillis`, `headersJson`, `sampleRequestBody`, `active`
- 실행: `baseUrl + path`로 URL을 만들고 GET/POST 요청을 보낸다.
- 결과 저장: request URL, method, headers, body, response status, response body, latency, error message
- demo setup: 같은 애플리케이션 안의 `/simulator/rest/**` endpoint
- 실패 조건: payload 또는 interface code에 `FAIL`이 포함되면 simulator가 실패 응답을 반환한다.
- 재처리: 원본 request payload로 다시 REST executor를 실행한다.

## SOAP

- 설정 table: `soap_endpoint_config`
- 주요 설정: `endpointUrl`, `soapAction`, `operationName`, `namespaceUri`, `requestTemplateXml`, `timeoutMillis`, `active`
- 실행: SOAP XML을 HTTP request body로 전송한다.
- 결과 저장: endpoint URL, SOAPAction, request XML, response XML, HTTP status, latency, fault/error message
- demo setup: 같은 애플리케이션 안의 `/simulator/soap/**` endpoint
- 실패 조건: XML payload에 `FAIL`이 포함되면 SOAP fault 또는 business failure를 반환한다.
- 재처리: 원본 XML payload를 사용해 다시 SOAP executor를 실행한다.

## MQ

- 설정 table: `mq_channel_config`
- 주요 설정: `brokerType`, `destinationName`, `routingKey`, `messageType`, `correlationKeyExpression`, `timeoutMillis`, `active`
- 실행: embedded Artemis destination에 JMS text message를 publish하고 local consumer가 처리한다.
- 결과 저장: destination, outbound payload, correlation key, publish status, consume status, processing latency, error details
- demo setup: 애플리케이션 process 내부의 in-vm Artemis broker
- 실패 조건: payload에 `FAIL`이 포함되면 consumer processing failure로 처리한다.
- 재처리: 원본 message payload로 publish/consume flow를 다시 실행한다.

## SFTP

- 설정 table: `file_transfer_config`
- 주요 설정: `protocolType`, `host`, `port`, `username`, `secretReference`, `baseRemotePath`, `timeoutMillis`, `active`
- 실행: embedded SFTP server를 대상으로 upload/download를 수행한다.
- 결과 저장: transfer direction, local file name/path, remote file path, file size, transfer status, latency, error message
- demo setup: `127.0.0.1:10022`, runtime directory `build/file-transfer-demo`
- 실패 조건: 존재하지 않는 파일, 잘못된 remote path, traversal path 등
- 재처리: 원본 transfer request를 다시 수행한다.

## FTP

- 설정 table: `file_transfer_config`
- 주요 설정: SFTP와 동일하며 FTP는 `passiveMode`를 함께 사용한다.
- 실행: embedded FTP server를 대상으로 upload/download를 수행한다.
- 결과 저장: SFTP와 동일한 file transfer history model 사용
- demo setup: `127.0.0.1:10021`, runtime directory `build/file-transfer-demo`
- 실패 조건: SFTP와 동일하며 passive mode나 local port 상태도 영향을 줄 수 있다.
- 재처리: 원본 transfer request를 다시 수행한다.

## Batch

- 설정 table: `batch_job_config`
- 주요 설정: `jobName`, `jobType`, `cronExpression`, `parameterTemplateJson`, `enabled`, `retryable`, `timeoutMillis`, `active`
- 실행: `JobLauncher`로 Spring Batch job을 실행한다.
- 결과 저장: job parameters, batch status, step status, read/write/skip count, output summary, latency, error message
- demo jobs: `interfaceSettlementSummaryJob`, `failedExecutionRetryAggregationJob`
- 실패 조건: `forceFail=true` 또는 payload 값에 `FAIL` 포함
- 재처리: 원본 job parameter payload로 새 Batch execution을 생성한다.

## Monitoring Rules

Monitoring은 read-only이며 protocol-agnostic입니다.

1. 실행 count와 trend는 `interface_execution`에서 조회한다.
2. 재처리 queue는 `interface_retry_task`에서 조회한다.
3. MQ 상세는 `mq_message_history`에서 조회한다.
4. SFTP/FTP 상세는 `file_transfer_history`에서 조회한다.
5. Batch 상세는 `batch_run_history`, `batch_step_history`에서 조회한다.
6. Dashboard link는 실행 로직을 중복하지 않고 owning detail page로 이동한다.
