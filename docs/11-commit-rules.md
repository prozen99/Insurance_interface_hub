# Commit Rules

## Commit Style

Use small, intentional commits with a clear prefix:

- `docs:` documentation-only changes
- `build:` Gradle or dependency changes
- `config:` application configuration
- `feat:` user-visible feature or new capability
- `fix:` bug fix
- `test:` test-only changes
- `refactor:` structure or cleanup without behavior change

## Examples

```text
feat: add phase 0 foundation skeleton
docs: add interface hub architecture baseline
config: add local mysql flyway profile
```

## Commit Checklist

Before committing:

- Run `.\gradlew.bat build`.
- Check `git status --short`.
- Confirm no real secrets were added.
- Confirm generated build files are not staged.
- Confirm Flyway migrations are append-only.
- Keep unrelated IntelliJ metadata out of the commit.

## Branch Rule

Use one branch per phase or focused feature:

```text
phase-0-foundation
phase-1-admin-crud
phase-2-execution-engine
```

## Flyway Rule

Never edit an already-applied migration in a shared environment. Add a new migration instead.

For this local portfolio project, Phase 0 may still be adjusted before the first public commit, but later phases should treat migrations as immutable.
