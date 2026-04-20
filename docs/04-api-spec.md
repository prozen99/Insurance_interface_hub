# API Spec

Phase 2 is still primarily a server-rendered Thymeleaf admin console. JSON APIs remain minimal.

## Public/System Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/login` | Login page |
| POST | `/login` | Spring Security login processing |
| POST | `/logout` | Spring Security logout |
| GET | `/api/smoke` | Smoke JSON response |
| GET | `/actuator/health` | Actuator health |

## Admin Page Endpoints

All `/admin/**` endpoints require authentication.

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/admin` | Dashboard with execution metrics |
| GET | `/admin/interfaces` | Interface definition list |
| GET | `/admin/interfaces/{id}` | Interface detail with manual execution form |
| POST | `/admin/interfaces/{id}/execute` | Run mock manual execution |
| GET | `/admin/executions` | Execution history list |
| GET | `/admin/executions/failed` | Failed execution list |
| GET | `/admin/executions/{id}` | Execution detail with steps and retry task |
| POST | `/admin/executions/{id}/retry` | Retry a failed execution |

Phase 1 master CRUD endpoints remain available for partners, systems, and interface definitions.

## Form Fields

Manual execution:

- `requestPayload`: optional, max 4000 characters

Execution history filters:

- `keyword`
- `protocolType`
- `executionStatus`

## Response Standard For Future JSON APIs

Application JSON APIs should use `ApiResponse<T>`:

- `success`
- `message`
- `data`
- `timestamp`
