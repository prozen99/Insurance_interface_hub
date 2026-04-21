# API Spec

Phase 4 is still primarily a server-rendered Thymeleaf admin console. JSON/XML endpoints are limited to smoke and local simulator endpoints.

## Public/System Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| GET | `/login` | Login page |
| POST | `/login` | Spring Security login processing |
| POST | `/logout` | Spring Security logout |
| GET | `/api/smoke` | Smoke JSON response |
| GET | `/actuator/health` | Actuator health |

## Local REST Simulator Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/simulator/rest/premium/calculate` | Premium calculation demo target |
| GET | `/simulator/rest/policy/{policyNo}` | Policy lookup demo target |
| POST | `/simulator/rest/claim/register` | Claim registration demo target |

## Local SOAP Simulator Endpoints

Simulator endpoints consume and produce SOAP XML.

| Method | Path | Purpose |
| --- | --- | --- |
| POST | `/simulator/soap/policy-inquiry` | Policy inquiry SOAP demo target |
| POST | `/simulator/soap/claim-status` | Claim status SOAP demo target |
| POST | `/simulator/soap/premium-confirmation` | Premium confirmation SOAP demo target |

Failure rule:

- If request XML contains `FAIL`, the simulator returns HTTP 500 with a SOAP fault.
- Otherwise it returns HTTP 200 with a SOAP success envelope.

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
| GET | `/admin/executions` | Execution history list |
| GET | `/admin/executions/failed` | Failed execution list |
| GET | `/admin/executions/{id}` | Execution detail with protocol exchange data |
| POST | `/admin/executions/{id}/retry` | Retry a failed execution |

## SOAP Config Form Fields

- `endpointUrl`: required, starts with `http://` or `https://`, max 500 characters
- `soapAction`: optional, max 300 characters
- `operationName`: required, max 160 characters
- `namespaceUri`: required, max 300 characters
- `requestTemplateXml`: required, well-formed XML, max 12000 characters
- `timeoutMillis`: 100 to 60000
- `active`: enables or disables this SOAP config for execution

## Manual Execution Form Fields

- `requestPayload`: optional, max 12000 characters

For REST, `requestPayload` is sent as the HTTP request body for POST. For SOAP, it is sent as the SOAP XML envelope. For mock protocols, it is only used by the deterministic mock rule.

## Response Standard For Future JSON APIs

Application JSON APIs should use `ApiResponse<T>`:

- `success`
- `message`
- `data`
- `timestamp`
