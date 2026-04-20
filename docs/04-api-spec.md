# API Spec

Phase 1 is primarily a server-rendered admin console. JSON APIs remain intentionally minimal.

## Public/System Endpoints

### `GET /login`

Renders the admin login page.

### `POST /login`

Handled by Spring Security form login.

Form fields:

- `username`
- `password`
- CSRF token

### `POST /logout`

Handled by Spring Security logout.

### `GET /api/smoke`

Returns a lightweight success response for local verification.

### `GET /actuator/health`

Returns Spring Boot actuator health.

## Admin Page Endpoints

All `/admin/**` endpoints require authentication.

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/admin` | Dashboard |
| GET | `/admin/partners` | Partner company list |
| GET | `/admin/partners/new` | Partner company create form |
| POST | `/admin/partners` | Create partner company |
| GET | `/admin/partners/{id}/edit` | Partner company edit form |
| POST | `/admin/partners/{id}` | Update partner company |
| GET | `/admin/systems` | Internal system list |
| GET | `/admin/systems/new` | Internal system create form |
| POST | `/admin/systems` | Create internal system |
| GET | `/admin/systems/{id}/edit` | Internal system edit form |
| POST | `/admin/systems/{id}` | Update internal system |
| GET | `/admin/interfaces` | Interface definition list and filters |
| GET | `/admin/interfaces/new` | Interface definition create form |
| POST | `/admin/interfaces` | Create interface definition |
| GET | `/admin/interfaces/{id}` | Interface definition detail |
| GET | `/admin/interfaces/{id}/edit` | Interface definition edit form |
| POST | `/admin/interfaces/{id}` | Update interface definition |
| POST | `/admin/interfaces/{id}/activate` | Enable interface |
| POST | `/admin/interfaces/{id}/deactivate` | Disable interface |

## Response Standard For Future JSON APIs

Application JSON APIs should use `ApiResponse<T>`:

- `success`
- `message`
- `data`
- `timestamp`

## Future API Groups

| Area | Candidate Paths |
| --- | --- |
| Protocol configuration | `/api/admin/interfaces/{id}/protocol-config` |
| Manual execution | `/api/admin/interfaces/{id}/execute` |
| Execution history | `/api/admin/executions` |
| Retry tasks | `/api/admin/retries` |
| Audit logs | `/api/admin/audit-logs` |
| Monitoring | `/api/admin/monitoring` |
