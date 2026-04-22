# API Spec

Phase 8 remains a server-rendered Thymeleaf admin console. JSON/XML endpoints are limited to smoke and local REST/SOAP simulator endpoints. Monitoring views are authenticated admin pages backed by read-only aggregation services.

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
| GET | `/admin` | Operations dashboard with execution, retry, protocol, MQ, file-transfer, and batch summaries |
| GET | `/admin/monitoring` | Monitoring overview |
| GET | `/admin/monitoring/failures` | Failure-focused monitoring view |
| GET | `/admin/monitoring/retries` | Retry queue and retry activity view |
| GET | `/admin/monitoring/protocols` | Protocol health and 7-day trend view |
| GET | `/admin/monitoring/files` | File transfer monitoring view |
| GET | `/admin/monitoring/mq` | MQ publish/consume monitoring view |
| GET | `/admin/monitoring/batch` | Batch monitoring view |
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
| GET | `/admin/executions` | Execution history list with keyword, protocol, status, trigger, and date filters |
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

## Monitoring Query Parameters

`/admin/executions` supports:

- `keyword`: execution number, interface code, or interface name
- `protocolType`: `REST`, `SOAP`, `MQ`, `SFTP`, `FTP`, `BATCH`
- `executionStatus`: `PENDING`, `RUNNING`, `SUCCESS`, `FAILED`
- `triggerType`: `MANUAL`, `RETRY`, `SCHEDULED`
- `startedFrom`: ISO date, inclusive
- `startedTo`: ISO date, inclusive at the UI level and converted to an exclusive next-day bound internally
