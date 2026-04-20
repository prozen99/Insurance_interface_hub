# Test Strategy

## Phase 2 Test Goals

- Keep the application context loading.
- Keep the Gradle build independent from local MySQL.
- Cover the common execution service.
- Cover success and failure execution paths.
- Cover retry behavior.
- Cover MVC redirect paths for manual execution and retry.

## Current Tests

- `InsuranceInterfaceHubApplicationTests.contextLoads()`
- `AuthControllerTest.loginPageRenders()`
- `PartnerCompanyServiceTest`
- `InterfaceDefinitionServiceTest`
- `InterfaceExecutionServiceTest`
- `InterfaceDefinitionControllerTest`
- `InterfaceExecutionControllerTest`

## Manual Verification

After MySQL is configured:

1. Run `.\gradlew.bat bootRun --args='--spring.profiles.active=local'`.
2. Log in with the local demo admin account.
3. Execute an interface with a normal payload.
4. Execute an interface with a payload containing `FAIL`.
5. Retry the failed execution.
6. Open execution history and detail pages.

## Future Automated Tests

| Phase | Test Focus |
| --- | --- |
| Phase 3 | REST adapter mock-server tests |
| Phase 4 | SOAP XML mapping and client tests |
| Phase 5 | MQ contract and message serialization tests |
| Phase 6 | File transfer adapter tests with local test doubles |
| Phase 7 | Spring Batch job tests |
| Phase 8 | Monitoring aggregation tests |
| Phase 9 | Performance smoke tests and demo regression suite |
