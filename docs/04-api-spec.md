# API Spec

Phase 6 is still primarily a server-rendered Thymeleaf admin console. JSON/XML endpoints are limited to smoke and local REST/SOAP simulator endpoints. MQ, SFTP, and FTP are driven through admin page actions and embedded local infrastructure.

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
| GET | `/admin/executions` | Execution history list |
| GET | `/admin/executions/failed` | Failed execution list |
| GET | `/admin/executions/{id}` | Execution detail with protocol-specific history |
| POST | `/admin/executions/{id}/retry` | Retry a failed execution |

## File Transfer Config Fields

- `protocolType`: SFTP or FTP
- `host`: local demo default `127.0.0.1`
- `port`: default `10022` for SFTP, `10021` for FTP
- `username`: local demo default `demo`
- `secretReference`: local demo default `LOCAL_DEMO_FILE_TRANSFER_PASSWORD`
- `baseRemotePath`: default `/inbox`
- `localPath`: default `build/file-transfer-demo/local`
- `fileNamePattern`: optional display/filter hint
- `passiveMode`: FTP only
- `timeoutMillis`: 100 to 60000
- `active`: enables or disables execution

## Manual File Transfer Fields

- `transferDirection`: `UPLOAD` or `DOWNLOAD`
- `localFileName`: simple file name only
- `remoteFilePath`: absolute remote path such as `/inbox/sample-upload.txt`

The controller serializes these fields into the execution request payload so retry can repeat the same transfer.

## Response Standard For Future JSON APIs

Application JSON APIs should use `ApiResponse<T>`:

- `success`
- `message`
- `data`
- `timestamp`
