# :core:database

Room schema snapshots live in `schemas/` and are **committed**. `ascend75.db` is at version 1.

## Migration policy (decided 2026-09-23)

`DatabaseModule` uses `.fallbackToDestructiveMigration()`. That is acceptable **only** while the
schema has never shipped a change. From the first shipped schema change onwards:

1. Bump `@Database(version = ...)`.
2. Add a `Migration` in `core/database/src/main/java/com/ascend75/core/database/migrations/`.
3. Register it in `DatabaseModule` with `.addMigrations(...)`.
4. Delete `.fallbackToDestructiveMigration()` in the same commit.

Shipping a schema change without a migration silently deletes user data — treat it as a release blocker.

## Ownership rule

DAOs may be imported only by `:core:data` and `:core:notifications`. UI (`:feature:*`) modules talk to
`:core:domain` repository interfaces. CI enforces this (see `.github/workflows/build-apk.yml`).
