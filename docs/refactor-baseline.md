# Unit-test baseline — before the "single owner" refactor

Recorded on branch `refactor/single-owner` at commit `24ef9d3` (main), 2026-09-23.

Command: `cd D:/Ascend75 && ./gradlew test --console=plain`
Result: `BUILD SUCCESSFUL in 1m 35s` (789 actionable tasks, all up-to-date).

`@Test` counts per module (what the refactor must not lose):

| Module | Tests |
|---|---|
| `:app` (AscendEndToEndJourneyTest) | 1 |
| `:core:common` (ChallengeRulesEngineTest) | 4 |
| `:core:crypto` (VaultFileStorageTest) | 3 |
| `:core:database` (ScienceCardSeederTest) | 1 |
| `:core:designsystem` (ThemeContrastTest) | 5 |
| `:core:notifications` (SmartReminderSchedulerTest) | 3 |
| `:feature:dashboard` (DailyProtocolRepositoryTest) | 7 |
| `:feature:photos` (PhotoVaultViewModelTest) | 7 |
| `:feature:reading` (ReadingViewModelTest) | 3 |
| `:feature:settings` (DataWipeIntegrationTest) | 2 |
| `:feature:water` (WaterViewModelTest) | 5 |
| **Total** | **41** |

There are **no `androidTest` sources** in the repository at all.

## After the refactor (updated as phases land)

P0–P2 (domain + data layers extracted):

| Module | Tests | Note |
|---|---|---|
| `:core:domain` (HabitTypeTest) | 3 | new |
| `:core:common` (ChallengeRulesEngineTest) | 5 | +1 duplicate-habit test |
| `:core:data` (MappersTest 2, DefaultTodayProtocolRepositoryTest 7) | 9 | the dashboard day-advance suite moved here, unmodified |
