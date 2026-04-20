# Test Strategy

## Phase 1 Test Goals

- Prove the application context can load.
- Prove the Gradle build works on Windows.
- Keep build tests independent from a developer's local MySQL instance.
- Cover basic service behavior for master CRUD rules.
- Cover at least one MVC page render path.

## Current Tests

- `InsuranceInterfaceHubApplicationTests.contextLoads()`
- `AuthControllerTest.loginPageRenders()`
- `PartnerCompanyServiceTest`
- `InterfaceDefinitionServiceTest`

The context load test mocks repositories and excludes database, Flyway, JPA, and Batch auto-configuration so `gradlew build` can run without local database setup.

## Manual Verification

After MySQL is configured:

1. Run `.\gradlew.bat bootRun --args='--spring.profiles.active=local'`.
2. Confirm Flyway creates or migrates the schema through V2.
3. Open `/login`.
4. Log in with the local demo admin account.
5. Navigate partner, system, and interface CRUD pages.
6. Call `/api/smoke`.
7. Check `/actuator/health`.

## Future Automated Tests

| Phase | Test Focus |
| --- | --- |
| Phase 2 | Execution engine unit tests and transactional integration tests |
| Phase 3 | REST adapter mock-server tests |
| Phase 4 | SOAP XML mapping and client tests |
| Phase 5 | MQ contract and message serialization tests |
| Phase 6 | File transfer adapter tests with local test doubles |
| Phase 7 | Spring Batch job tests |
| Phase 8 | Monitoring aggregation tests |
| Phase 9 | Performance smoke tests and demo regression suite |

## Test Data Rule

Use local fixtures and fake partners. Never commit real partner endpoint URLs, credentials, account data, policy data, or personally identifiable information.
