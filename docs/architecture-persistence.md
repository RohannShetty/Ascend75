# Persistence Boundary

*Last updated: 2026-09-23 (release 1.1.0).*

## The rule

`Room` and `DataStore` are an implementation detail of **`:core:data`**. Everything else talks to
`:core:domain`.

| Module | May import `:core:database` / `:core:datastore`? | What it holds |
| :--- | :--- | :--- |
| `:app` | Yes (composition root) | Hilt entry points, navigation shell |
| `:core:data` | Yes | Repository implementations, mappers, Hilt `@Binds` |
| `:core:database` | — | Room database, entities, DAOs, seeder |
| `:core:datastore` | — | Preferences DataStore |
| `:core:domain` | **No** (and has no Android plugin at all) | Models + repository contracts |
| `:core:common`, `:core:crypto`, `:core:designsystem`, `:core:notifications` | No | Rules engine, vault, theme, notifications |
| `:feature:*` | **No, except `testImplementation` in `:feature:settings`** | Screens, ViewModels, UI state |

```
feature:*  ──▶  :core:domain  ◀──  :core:data  ──▶  :core:database / :core:datastore
   (UI)          (contracts)        (the only      (Room + DataStore)
                                  implementation)
```

## How it is enforced

CI runs this on every push and pull request and fails the build on any match:

```bash
grep -rn "com\.ascend75\.core\.database\|com\.ascend75\.core\.datastore" feature/*/src/main --include=*.kt
# expected: no matches (exit code 1)
```

Only `src/main` is scanned. `:feature:settings` keeps a `testImplementation` dependency on
`:core:database`/`:core:datastore` so `DataWipeIntegrationTest` can exercise the wipe and the export
against a real in-memory schema — a deliberate, test-only exception. Everything else in the feature layer
is verified by the compiler: the dependencies simply are not on the classpath.

## Why

1. **One owner for a write.** Day advancement, strict resets and onboarding used to write across several
   DAOs with no transaction. `:core:data` wraps those flows in `withTransaction`, so the UI can no longer
   observe a half-written day.
2. **The UI stops modelling the database.** UI state carries `HabitType`/`TaskEntry`, not
   `TaskEntryEntity`; a column rename is now a mapper change, not a change in eight feature modules.
3. **Fast tests.** `:core:domain` is a plain JVM module: contract and model tests run in milliseconds
   without Robolectric, while the Room behaviour is tested once, in `:core:data`.

## Adding a repository

1. Declare the contract (interface + model types) in `core/domain/src/main/kotlin/com/ascend75/core/domain/`.
2. Implement it in `core/data/src/main/kotlin/com/ascend75/core/data/repository/`.
3. Map entities to models in `core/data/.../mapper/Mappers.kt` — never leak an entity past this file.
4. Bind it in `core/data/.../di/RepositoryModule.kt` (`@Binds`, `@Singleton`).
5. Inject the interface in the ViewModel. If you find yourself needing a DAO in a feature module, the
   contract is missing a method — add it there instead.
