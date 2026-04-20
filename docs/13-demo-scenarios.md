# Demo Scenarios

## Scenario 1 - Project Orientation

1. Open `README.md`.
2. Explain the business goal: central operations hub for insurance and financial interfaces.
3. Show the phase roadmap.
4. Open the package tree under `com.insurancehub`.

What this proves:

- The project has a clear business context.
- The package layout is ready for multiple protocols.
- Phase 1 uses layered packages inside the modular monolith.

## Scenario 2 - Local Boot And Login

1. Start local MySQL.
2. Set environment variables.
3. Run `.\gradlew.bat bootRun --args='--spring.profiles.active=local'`.
4. Open `/login`.
5. Log in with `admin` / `admin123!`.

What this proves:

- Flyway runs V1 and V2 migrations.
- The admin user is DB-backed.
- Spring Security form login is working.

## Scenario 3 - Partner And System CRUD

1. Open `/admin/partners`.
2. Create a partner company.
3. Edit the partner name or status.
4. Open `/admin/systems`.
5. Create and edit an internal system.

What this proves:

- Master data CRUD is available.
- Server-side validation and uniqueness checks work.

## Scenario 4 - Interface Definition CRUD

1. Open `/admin/interfaces`.
2. Create an interface definition using REST or SOAP.
3. Open the detail page.
4. Disable and re-enable the interface.
5. Filter the list by protocol and status.

What this proves:

- Core interface registration is usable.
- Protocol classification exists before real execution logic.
- Enable/disable is handled through interface status.

## Scenario 5 - Smoke API

1. Open `/api/smoke`.
2. Confirm the `ApiResponse` wrapper.
3. Mention that future JSON APIs should use the same response shape.

What this proves:

- JSON endpoint conventions remain available alongside Thymeleaf.

## Scenario 6 - Schema Walkthrough

1. Open `V1__phase_0_baseline.sql`.
2. Open `V2__phase_1_admin_master_crud.sql`.
3. Show `interface_definition` as the center.
4. Show master tables, status fields, and seeded demo data.

What this proves:

- The schema evolves through Flyway.
- Phase 1 did not rewrite old migrations.
