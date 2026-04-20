# Screen Spec

Phase 1 uses Thymeleaf for the admin UI.

## Common Layout

Admin pages share:

- Left navigation
- Phase 1 product branding
- Logout button
- Flash success/error messages
- Table-based enterprise admin layout

Navigation:

- Dashboard
- Interfaces
- Partners
- Internal Systems

## Login

Path: `/login`

Fields:

- Login ID
- Password

Behavior:

- Invalid credentials return to `/login?error`.
- Logout returns to `/login?logout`.

## Dashboard

Path: `/admin`

Cards:

- Total interfaces
- Active interfaces
- Partner companies
- Internal systems

Protocol cards:

- REST
- SOAP
- MQ
- Batch
- SFTP
- FTP

## Partner Company Screens

Paths:

- `/admin/partners`
- `/admin/partners/new`
- `/admin/partners/{id}/edit`

Fields:

- Partner code
- Partner name
- Status
- Description

## Internal System Screens

Paths:

- `/admin/systems`
- `/admin/systems/new`
- `/admin/systems/{id}/edit`

Fields:

- System code
- System name
- Owner department
- Status
- Description

## Interface Definition Screens

Paths:

- `/admin/interfaces`
- `/admin/interfaces/new`
- `/admin/interfaces/{id}`
- `/admin/interfaces/{id}/edit`

List filters:

- Keyword
- Protocol type
- Status

Fields:

- Interface code
- Interface name
- Protocol type
- Direction
- Status
- Partner company
- Internal system
- Description

Detail actions:

- Edit
- Enable
- Disable

## Future Screens

- Protocol-specific configuration forms
- Execution history
- Retry queue
- Batch schedule
- Monitoring dashboard
- Audit log viewer
