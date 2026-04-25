# 데모 시나리오

이 문서는 한국어 기술 면접과 포트폴리오 제출 시 설명하기 쉬운 순서로 작성한 최종 데모 흐름입니다. 전체 실행 데모는 로컬 환경을 권장하고, 배포 URL이 있다면 dashboard와 주요 화면 확인 용도로 함께 제시하면 좋습니다.

## 5분 핵심 데모 경로

1. `/login`에서 `admin` / `admin123!`로 로그인한다.
2. `/admin` dashboard에서 오늘 성공/실패, 재처리 대기, 프로토콜 요약, 실패 상위 인터페이스를 확인한다.
3. `/admin/interfaces`에서 seeded interface를 선택한다.
4. interface detail에서 REST 설정을 확인하고 REST 실행을 수행한다.
5. SOAP interface로 이동해 SOAP XML 실행 결과를 확인한다.
6. MQ interface로 이동해 publish/consume 결과를 확인한다.
7. SFTP/FTP interface로 이동해 upload/download 결과를 확인한다.
8. Batch interface로 이동해 Spring Batch job 실행 결과와 read/write count를 확인한다.
9. `FAIL` 또는 `forceFail=true`로 실패 케이스를 만들고 재처리/재실행 흐름을 설명한다.
10. `/admin/monitoring`과 하위 monitoring 화면에서 운영 관점의 요약을 확인한다.

## Scenario 1 - 프로젝트 개요 설명

1. `README.md`를 연다.
2. 보험/금융 인터페이스 운영 문제를 설명한다.
3. 지원 프로토콜별 구현 현황 표를 보여준다.
4. 공통 실행 엔진과 protocol adapter 구조를 설명한다.
5. Phase 0부터 Phase 10까지의 진행 흐름을 설명한다.

평가자가 확인할 포인트:

- 단순 CRUD가 아니라 운영 흐름 전체를 다룬다.
- 여섯 가지 protocol path를 하나의 운영 콘솔로 묶었다.
- 포트폴리오 prototype과 production system의 차이를 명확히 설명한다.

## Scenario 2 - 로그인과 Dashboard

1. local profile 또는 배포 URL로 애플리케이션에 접속한다.
2. `/login` 접속
3. `admin` / `admin123!` 로그인
4. `/admin` 확인
5. primary metrics, quick links, 7-day trend, protocol cards, top failures, recent executions, pending retries를 설명한다.

평가자가 확인할 포인트:

- DB 기반 form login이 동작한다.
- dashboard가 운영 상황을 한눈에 보여준다.
- 문제가 있는 인터페이스로 이동할 수 있는 link가 있다.

## Scenario 3 - REST 실행

1. `IF_REST_POLICY_001`을 연다.
2. REST config의 `baseUrl`, `path`, `httpMethod`를 확인한다.
3. sample JSON payload로 실행한다.
4. execution detail에서 request URL, method, request body, response status, response body, latency를 확인한다.

평가자가 확인할 포인트:

- mock이 아니라 local simulator endpoint로 실제 HTTP 호출을 수행한다.
- 실행 결과가 공통 이력에 저장된다.

## Scenario 4 - SOAP 실행

1. `IF_SOAP_POLICY_001`을 연다.
2. SOAP config의 `endpointUrl`, `soapAction`, `requestTemplateXml`을 확인한다.
3. sample XML payload로 실행한다.
4. execution detail에서 request XML, response XML, HTTP status, latency, fault/error 영역을 확인한다.

평가자가 확인할 포인트:

- SOAP도 REST와 같은 공통 실행 모델 안에서 처리된다.
- XML 요청/응답이 운영자가 확인 가능한 형태로 남는다.

## Scenario 5 - MQ 실행

1. `IF_MQ_POLICY_001`을 연다.
2. MQ destination과 correlation key 설정을 확인한다.
3. sample message payload를 실행한다.
4. execution detail 또는 MQ monitoring에서 publish status와 consume status를 확인한다.

평가자가 확인할 포인트:

- embedded Artemis를 통해 실제 publish/consume 흐름이 동작한다.
- producer 성공과 consumer 처리 결과를 분리해 볼 수 있다.

## Scenario 6 - SFTP/FTP 실행

1. `IF_SFTP_POLICY_001`에서 upload/download를 실행한다.
2. `IF_FTP_POLICY_001`에서 upload/download를 실행한다.
3. file transfer history에서 direction, local file, remote path, file size, status, latency를 확인한다.

평가자가 확인할 포인트:

- Docker 없이 embedded SFTP/FTP demo server로 실제 파일 전송을 수행한다.
- 파일 전송 결과가 공통 execution detail과 monitoring에 연결된다.

## Scenario 7 - Batch 성공 실행

1. `IF_BATCH_SETTLEMENT_001`을 연다.
2. Batch settings panel을 확인한다.
3. 다음 payload로 실행한다.

```json
{"businessDate":"TODAY","forceFail":false}
```

4. execution detail에서 Batch run history, step logs, read/write count, output summary를 확인한다.
5. `/admin/batch-runs`를 연다.

평가자가 확인할 포인트:

- Spring Batch job이 실제로 launch된다.
- Batch 결과가 운영자가 볼 수 있는 형태로 저장된다.

## Scenario 8 - 실패와 재처리

1. REST, SOAP, MQ 중 하나에서 payload에 `FAIL`을 포함해 실행한다.
2. 또는 Batch에서 다음 payload를 사용한다.

```json
{"businessDate":"TODAY","forceFail":true}
```

3. execution status가 FAILED인지 확인한다.
4. execution detail에서 error message와 retry task를 확인한다.
5. retry button 또는 정상 payload 재실행으로 회복 흐름을 설명한다.

평가자가 확인할 포인트:

- 실패가 숨겨지지 않고 이력과 monitoring에 남는다.
- retry가 원본 실패 실행과 연결된다.
- deterministic failure와 corrected rerun의 차이를 설명할 수 있다.

## Scenario 9 - Monitoring Pages

1. `/admin/monitoring` 열기
2. `/admin/monitoring/failures` 열기
3. `/admin/monitoring/retries` 열기
4. `/admin/monitoring/protocols` 열기
5. `/admin/monitoring/files` 열기
6. `/admin/monitoring/mq` 열기
7. `/admin/monitoring/batch` 열기

평가자가 확인할 포인트:

- 운영자가 전체 상태에서 장애 중심 view로 빠르게 이동할 수 있다.
- protocol별 history를 별도 frontend stack 없이 Thymeleaf로 통합했다.

## Scenario 10 - 제출 패키지 확인

1. GitHub repository link를 제시한다.
2. 배포 URL이 있다면 `/login`과 `/admin` URL을 제시한다.
3. `docs/14-submission-guide.md`를 연다.
4. `docs/12-local-runbook.md`를 통해 로컬 실행 절차를 설명한다.
5. `docs/09-troubleshooting.md`에서 실제 개발 중 만난 이슈와 대응을 설명한다.
6. `.\gradlew.bat test`, `.\gradlew.bat build` 결과를 제시한다.

평가자가 확인할 포인트:

- 프로젝트가 제출 가능한 형태로 정리되어 있다.
- 문서가 실제 구현과 일치한다.
- local demo prototype과 production system의 차이를 정직하게 설명한다.

## 제출 자료 Checklist

- GitHub repository URL
- 배포 URL 또는 로컬 실행 안내
- 데모 계정
- README
- `docs/14-submission-guide.md`
- 핵심 화면 screenshot 또는 짧은 demo video
- test/build 결과
- 알려진 한계 설명

## Railway 배포 검증 결과

배포 URL:

- `https://insuranceinterfacehub-production.up.railway.app/login`

확인 완료:

- `/actuator/health` 성공
- 로그인 성공
- 관리자 콘솔 접근 성공
- REST 실행 성공
- SOAP 실행 성공
- Monitoring에서 실행 집계 확인 가능

확인 예정:

- MQ 수동 실행

배포 URL 기준 추천 시연 흐름:

1. `https://insuranceinterfacehub-production.up.railway.app/login` 접속
2. `admin` / `admin123!` 로그인
3. 대시보드 확인
4. 인터페이스 목록 이동
5. REST 실행 및 execution detail 확인
6. SOAP 실행 및 response XML 확인
7. MQ 실행
8. 실행 이력 확인
9. Monitoring 화면에서 execution summary 확인

배포 환경 한계:

- SFTP/FTP demo server는 Railway 추가 포트 노출 제약 때문에 비활성화한다.
- Batch scheduler는 데모 URL에서 기본 비활성화한다.
- REST/SOAP 설정은 Railway public URL 기준으로 조정해야 한다.
