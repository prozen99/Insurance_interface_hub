# API Spec

Phase 7 is still primarily a server-rendered Thymeleaf admin console. JSON/XML endpoints are limited to smoke and local REST/SOAP simulator endpoints. MQ, SFTP, FTP, and BATCH are driven through admin page actions and local in-process infrastructure.

## Public/System Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/login` | Login page |
| POST | `/login` | Spring Security login processing |
| POST | `/logout` | Spring Security logout |
| GET | `/api/smoke` | Smoke JSON response |
| GET | `/actuator/health` | Actuator health |

## Simulator Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/simulator/rest/premium/calculate` | Premium calculation REST demo target |
| GET | `/simulator/rest/policy/{policyNo}` | Policy lookup REST demo target |
| POST | `/simulator/rest/claim/register` | Claim registration REST demo target |
| POST | `/simulator/soap/policy-inquiry` | Policy inquiry SOAP demo target |
| POST | `/simulator/soap/claim-status` | Claim status SOAP demo target |
| POST | `/simulator/soap/premium-confirmation` | Premium confirmation SOAP demo target |

## Admin Page Endpoints

All `/admin/**` endpoints require authentication.

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/admin` | Dashboard with execution metrics |
| GET | `/admin/interfaces` | Interface definition list |
| GET | `/admin/interfaces/{id}` | Interface detail with execution form and protocol settings |
| POST | `/admin/interfaces/{id}/execute` | Run manual execution through the common engine |
| GET | `/admin/interfaces/{id}/rest-config` | REST endpoint configuration form |
| POST | `/admin/interfaces/{id}/rest-config` | Save REST endpoint configuration |
| GET | `/admin/interfaces/{id}/soap-config` | SOAP endpoint configuration form |
| POST | `/admin/interfaces/{id}/soap-config` | Save SOAP endpoint configuration |
| GET | `/admin/interfaces/{id}/mq-config` | MQ channel configuration form |
| POST | `/admin/interfaces/{id}/mq-config` | Save MQ channel configuration |
| GET | `/admin/interfaces/{id}/file-transfer-config` | SFTP/FTP configuration form |
| POST | `/admin/interfaces/{id}/file-transfer-config` | Save SFTP/FTP configuration |
| GET | `/admin/interfaces/{id}/batch-config` | Batch job configuration form |
| POST | `/admin/interfaces/{id}/batch-config` | Save Batch job configuration |
| GET | `/admin/executions` | Execution history list |
| GET | `/admin/executions/failed` | Failed execution list |
| GET | `/admin/executions/{id}` | Execution detail with protocol-specific history |
| POST | `/admin/executions/{id}/retry` | Retry a failed execution |
| GET | `/admin/batch-runs` | Batch run history list |
| GET | `/admin/batch-runs/{id}` | Batch run detail with step counts |

## Batch Config Fields

- `jobType`: `INTERFACE_SETTLEMENT_SUMMARY` or `FAILED_RETRY_AGGREGATION`
- `jobName`: `interfaceSettlementSummaryJob` or `failedExecutionRetryAggregationJob`
- `cronExpression`: Spring cron format with seconds
- `parameterTemplateJson`: default manual/scheduled parameters
- `enabled`: config-level scheduled execution switch
- `retryable`: operator hint for retry/rerun behavior
- `timeoutMillis`: demo timeout value
- `active`: enables or disables manual execution

## Manual Batch Parameters

Manual batch execution uses the generic `requestPayload` field as JSON:

```json
{"businessDate":"TODAY","forceFail":false}
```

Set `forceFail` to `true`, or include `FAIL`, to trigger a controlled failed batch run.
