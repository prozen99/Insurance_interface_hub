# Screen Spec

Phase 6 uses Thymeleaf for the admin UI and adds SFTP/FTP configuration, manual file-transfer execution fields, and transfer history visibility.

## Common Layout

Admin pages share:

- Left navigation
- Phase 6 product branding
- Logout button
- Flash success/error messages
- Table-based enterprise admin layout

## Interface Detail

Path: `/admin/interfaces/{id}`

Sections:

- Interface summary
- Protocol-specific settings panel
- Manual execution form
- Recent execution table

SFTP/FTP interface behavior:

- Shows host, port, username, secret reference, base remote path, local path, timeout, active flag, and FTP passive mode.
- Provides an Edit SFTP/FTP config button.
- Shows file-transfer fields instead of the generic payload textarea.
- Upload reads from local `input`; download writes to local `download`.

## File Transfer Config Form

Path: `/admin/interfaces/{id}/file-transfer-config`

Fields:

- Host
- Port
- Username
- Secret reference
- Base remote path
- Local path
- File name pattern
- Timeout millis
- FTP passive mode
- Active for manual execution

Validation:

- Host, port, username, secret reference, base remote path, and local path are required.
- Base remote path must start with `/`.
- Timeout must be between 100 and 60000 ms.

## Manual File Transfer Execution

Fields:

- Transfer direction: `UPLOAD` or `DOWNLOAD`
- Local file name
- Remote file path

Default upload demo:

- Direction: `UPLOAD`
- Local file: `sample-upload.txt`
- Remote path: `/inbox/sample-upload.txt`

Default download demo:

- Direction: `DOWNLOAD`
- Local file: `sample-download.txt`
- Remote path: `/outbox/sample-download.txt`

## Execution Detail

Path: `/admin/executions/{id}`

Sections:

- Execution summary
- Protocol exchange details
- File transfer history for SFTP/FTP executions
- MQ message history for MQ executions
- Step logs
- Request payload
- Response payload
- Retry tasks

File transfer detail shows:

- Protocol
- Direction
- Local file name and path
- Remote file path
- Transfer status
- File size
- Latency
- Error message

Failed executions show a retry button.
