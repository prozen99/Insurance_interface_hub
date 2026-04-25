# 테스트 전략

## 목표

테스트의 목적은 포트폴리오 데모에서 중요한 흐름이 깨지지 않도록 보호하는 것입니다. 특히 로그인, interface CRUD, 수동 실행, 실패, 재처리, 프로토콜별 executor, monitoring summary, build verification을 우선적으로 확인합니다.

## 테스트 범위

- application context load
- 관리자 로그인 화면과 인증 접근 제어
- PartnerCompany, InterfaceDefinition 등 service 계층
- interface definition controller/MVC
- common execution service 성공/실패/재처리
- REST executor success/failure
- SOAP executor success/failure
- MQ publish/consume success/failure
- SFTP/FTP file transfer success/failure
- Batch launch, failure, rerun/retry 관련 service
- monitoring dashboard summary service
- monitoring controller page rendering

## 주요 테스트 파일

- `InsuranceInterfaceHubApplicationTests.contextLoads()`
- `AuthControllerTest.loginPageRenders()`
- `AdminSecurityAccessControlTest`
- `PartnerCompanyServiceTest`
- `InterfaceDefinitionServiceTest`
- `InterfaceExecutionServiceTest`
- `InterfaceDefinitionControllerTest`
- `InterfaceExecutionControllerTest`
- `RestInterfaceExecutorTest`
- `SoapInterfaceExecutorTest`
- `MqInterfaceExecutorTest`
- `FileTransferExecutionServiceTest`
- `BatchExecutionServiceTest`
- `OperationsMonitoringServiceTest`
- `MonitoringControllerTest`

## 수동 검증

local MySQL 설정 후 다음을 확인합니다.

1. `.\gradlew.bat bootRun --args='--spring.profiles.active=local'`로 실행한다.
2. local demo admin 계정으로 로그인한다.
3. 정상 payload로 REST, SOAP, MQ, SFTP/FTP, Batch를 실행한다.
4. `FAIL` 또는 `forceFail=true`로 실패 실행을 만든다.
5. 실패 실행의 detail과 retry task를 확인한다.
6. 실패 실행을 재처리하거나 정상 payload로 재실행한다.
7. dashboard와 monitoring page에서 count와 link를 확인한다.

## Build Verification

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

## Phase Coverage

| Phase | Test Focus |
| --- | --- |
| Phase 1 | 로그인, master CRUD |
| Phase 2 | 공통 실행 엔진, 실패, 재처리 |
| Phase 3 | REST executor와 REST config |
| Phase 4 | SOAP executor와 SOAP config |
| Phase 5 | MQ executor와 MQ config |
| Phase 6 | SFTP/FTP file transfer |
| Phase 7 | Spring Batch job과 Batch config |
| Phase 8 | Monitoring aggregation |
| Phase 9 | Security access control, grouped monitoring aggregation, final build/test verification |

## 남은 테스트 확장 여지

- browser-level end-to-end test
- dashboard와 monitoring query에 대한 대량 데이터 성능 테스트
- 운영용 외부 broker/server와의 contract test
- 권한 세분화와 audit workflow가 추가될 경우 별도 보안 테스트
