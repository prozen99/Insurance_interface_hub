# API Spec

Phase 3 is still primarily a server-rendered Thymeleaf admin console. JSON APIs are limited to smoke and local simulator endpoints.

## Public/System Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/login` | Login page |
| POST | `/login` | Spring Security login processing |
| POST | `/logout` | Spring Security logout |
| GET | `/api/smoke` | Smoke JSON response |
| GET | `/actuator/health` | Actuator health |

## Local REST Simulator Endpoints

Simulator endpoints are unauthenticated and are intended for local demo execution only.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/simulator/rest/premium/calculate` | Premium calculation demo target |
| GET | `/simulator/rest/policy/{policyNo}` | Policy lookup demo target |
| POST | `/simulator/rest/claim/register` | Claim registration demo target |

Failure rule:

- If the path variable or request body contains `FAIL`, the simulator returns HTTP 422 with a failure payload.
- Otherwise it returns HTTP 200 with a simple JSON success payload.

## Admin Page Endpoints

All `/admin/**` endpoints require authentication.

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/admin` | Dashboard with execution metrics |
| GET | `/admin/interfaces` | Interface definition list |
| GET | `/admin/interfaces/{id}` | Interface detail with manual execution form and REST settings summary |
| POST | `/admin/interfaces/{id}/execute` | Run manual execution through the common engine |
| GET | `/admin/interfaces/{id}/rest-config` | REST endpoint configuration form |
| POST | `/admin/interfaces/{id}/rest-config` | Save REST endpoint configuration |
| GET | `/admin/executions` | Execution history list |
| GET | `/admin/executions/failed` | Failed execution list |
| GET | `/admin/executions/{id}` | Execution detail with steps, REST exchange data, payloads, and retry task |
| POST | `/admin/executions/{id}/retry` | Retry a failed execution |

Phase 1 master CRUD endpoints remain available for partners, systems, and interface definitions.

## REST Config Form Fields

- `baseUrl`: required, starts with `http://` or `https://`, max 500 characters
- `httpMethod`: GET or POST
- `path`: required, max 300 characters
- `timeoutMillis`: 100 to 60000
- `headersJson`: optional JSON object
- `sampleRequestBody`: optional sample body for manual demos
- `active`: enables or disables this REST config for execution

## Manual Execution Form Fields

- `requestPayload`: optional, max 4000 characters

For REST, `requestPayload` is sent as the HTTP request body for POST. For GET, no body is sent.

## Execution History Filters

- `keyword`
- `protocolType`
- `executionStatus`

## Response Standard For Future JSON APIs

Application JSON APIs should use `ApiResponse<T>`:

- `success`
- `message`
- `data`
- `timestamp`
