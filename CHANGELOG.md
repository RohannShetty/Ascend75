# Changelog

All notable changes to the **Ascend 75** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0] - 2026-09-23

### Architecture Refactor: Single-Owner Persistence & Type-Safe Navigation

The UI used to talk to Room directly: all eight feature modules declared `:core:database`, ViewModels
injected DAOs, and Room entities *were* the UI model (`DashboardUiState.tasks` was a
`List<TaskEntryEntity>`). This release puts one module in charge of persistence and gives the UI its own
vocabulary.

### Added

- **`:core:domain`** — a JVM-only module (no Android plugin, no `jvmToolchain`) holding the domain models
  (`HabitType`, `TaskEntry`, `DailyRecord`, `TodayProtocol`, `ChallengeInstance`, `ScienceCard`,
  `WaterLog`, `ReadingSession`, `WorkoutSession`, `ProgressPhoto`, `UserPreferences`) and the repository
  contracts every consumer programs against. It is testable on a bare JVM in milliseconds.
- **`:core:data`** — the only module that knows how the data is stored: Room-backed repository
  implementations (`DefaultTodayProtocolRepository`, `DefaultChallengeRepository`, `DefaultTaskRepository`,
  `DefaultDailyRecordRepository`, `DefaultVaultRepository`, `DefaultWaterRepository`,
  `DefaultReadingRepository`, `DefaultWorkoutRepository`, `DefaultScienceRepository`,
  `DefaultSettingsRepository`), entity↔model mappers, and a Hilt `@Binds` module.
- **Type-safe navigation** — `app/.../navigation/Routes.kt` declares each destination as a
  `@Serializable` key and `AscendNavGraph.kt` registers it with `composable<Route> { … }`. Route strings
  and the `Screen` sealed class are gone.
- **Room schema export** — `ksp { arg("room.schemaLocation", …) }` plus committed snapshots under
  `core/database/schemas/`, so a future migration can be written against a known schema.
- **Test baseline record** (`docs/refactor-baseline.md`) and **`docs/architecture-persistence.md`**,
  which states the boundary rules and how CI enforces them.
- New tests: `HabitTypeTest`, `MappersTest`, `DefaultTodayProtocolRepositoryTest` (moved out of the
  feature module), and `ChallengeRulesEngineTest.strictAndSoftModesNeverExposeADuplicateHabitType`.

### Changed

- **Every `:feature:*` module** dropped `:core:database` and `:core:datastore` and now depends on
  `:core:domain`. ViewModels inject repositories, not DAOs; UI state carries domain models.
- **`OnboardingViewModel.completeOnboarding`** starts the challenge through
  `ChallengeRepository.startAttempt`, which writes the attempt, its day-1 record and day-1 tasks inside a
  single `withTransaction` — previously three independent DAO writes could leave a half-created attempt.
- **Strict-reset path** (`DashboardViewModel.archiveAndResetStrictAttempt`) archives the attempt, closes
  the day and starts the next one through repositories, so the reset is atomic and the UI cannot observe
  a partially archived attempt.
- **`DataExportManager`** reads through repositories; the exported JSON keys are unchanged, so existing
  archives remain readable.
- **`MainActivity` shrank from 315 to 158 lines** — it picks the start destination, drains
  `ascend75://task/<id>` and requests `POST_NOTIFICATIONS`; the destination table lives in
  `AscendNavGraph.kt`.
- **`:core:common` and `:core:datastore`** expose `:core:domain` via `api(project(":core:domain"))`, so a
  consumer of the rules engine sees the same model types as the repositories.
- **CI now runs `./gradlew test`** on every push and pull request and fails the build if a feature's
  `src/main` imports `com.ascend75.core.database` or `com.ascend75.core.datastore`.
- `.gitignore` also ignores `.codegraph/`, `graphify-out/` and `transcripts/`; the generated graph output
  is no longer tracked.

### Removed

- `feature/dashboard/.../data/DailyProtocolRepository.kt` and its test — superseded by
  `:core:data`'s `DefaultTodayProtocolRepository` (the contract now lives in `:core:domain`).
- `app/.../navigation/Screen.kt` (string routes) and the DAO/DataStore imports across all eight features.

### Notes

- No schema change: `AscendDatabase` stays at version 1, and the destructive-fallback policy in
  `DatabaseModule` is documented in `core/database/README.md` as a deliberate v1 decision.
- Still on the roadmap, not in this release: splitting the remaining god composables, Gradle
  convention plugins for the 14 duplicated build blocks, and instrumented (`androidTest`) coverage.

---

## [1.0.0] - 2026-09-19

### Initial Release: Offline-First Sovereign Discipline Platform

Ascend 75 is a standalone, offline-first discipline and habit tracking platform for Android, engineered around modern behavioral science, data sovereignty, and unyielding psychological design.

---

### Added

#### Architectural Foundation & Build Infrastructure
- **Modular Multi-Module Clean Architecture**:
  - `:app`: Application entry point, Hilt dependency injection root, and navigation orchestrator.
  - Core Modules:
    - `:core:model`: Pure Kotlin domain models and value classes (`ChallengeMode`, `HabitCategory`, `DayStatus`).
    - `:core:database`: Room database (`AscendDatabase`), DAOs, entity models, type converters, and asset seeders.
    - `:core:datastore`: Jetpack DataStore preferences for reactive user settings, flags, and schedule targets.
    - `:core:domain`: Pure business rules engine (`ChallengeRulesEngine`, `DayBoundaryEvaluator`).
    - `:core:designsystem`: Stitch-derived visual system, typography tokens, custom composables, and dark glassmorphic styling.
    - `:core:crypto`: Hardware-backed Android Keystore AES-256-GCM encryption vault for progress photos and sensitive metrics.
  - Feature Modules:
    - `:feature:onboarding`: Goal-setting wizard and challenge mode selector.
    - `:feature:dashboard`: Central command center, dynamic progress ring, and daily checklist.
    - `:feature:habittracker`: Specialized daily habit loggers (diet, hydration, workouts, reading).
    - `:feature:workouttimer`: Foreground chronometer service for indoor/outdoor 45-minute sessions.
    - `:feature:photovault`: Encrypted progress photo gallery with biometric authentication and comparison slider.
    - `:feature:science`: Daily 75-day behavioral science evidence cards with PubMed citations.
    - `:feature:analytics`: Long-term discipline analytics, completion heatmaps, and streak tracking.
    - `:feature:settings`: Data sovereign controls, sleep boundary cutoff adjustments, and emergency wipe.
- **Gradle Version Catalog**: Centralized dependency management via `gradle/libs.versions.toml` targeting Android SDK 34, Kotlin 2.0, Compose BOM 2024.06.00, Room 2.6.1, and Hilt 2.51.1.
- **Gradle Wrapper**: Bundled official Gradle 8.9 distribution wrapper.
- **CI/CD Pipeline**: GitHub Actions workflow (`.github/workflows/build-apk.yml`) building `app-debug.apk` on every push/tag and automatically attaching preview artifacts to GitHub Releases.

#### Core Engines & Behavioral Features
- **4 Distinct Challenge Modes**:
  - **Strict 75**: The classic iron-will standard — any incomplete daily habit resets the active day counter to 1 while preserving historical reflections.
  - **Flexible 75**: High-accountability challenge with grace windows and adaptive pacing.
  - **75 Soft**: Accessible wellness baseline focusing on physical activity, hydration, and positive habit formation.
  - **Custom Mode**: User-defined habit rules, targets, and flexible criteria.
- **Intelligent Day Boundary Evaluator**:
  - Replaces rigid midnight transitions with a configurable sleep-boundary cutoff (default 03:00 AM).
  - Protects night owls and late-shift athletes from accidental streak resets while finishing workouts or hydration.
- **Empathetic Reset & Reflection Engine**:
  - When a reset occurs under Strict 75, historical completion logs and progress records remain permanently intact.
  - Prompts qualitative reflection capture ("What caused the friction? What system adapts tomorrow?").
- **Workout Timer Foreground Chronometer Service**:
  - Background-persistent dual 45-minute workout timer with sticky notification updates.
  - Categorization for indoor vs. outdoor sessions.
  - Minimum 3-hour separation warning between consecutive workout sessions.
- **Hyponatremia-Aware Hydration Tracker**:
  - Paced daily logging towards 1-gallon (3.785L) or metric equivalent targets.
  - Safety rate-limiting alert triggered when consumption exceeds 1.2 liters within a rolling 60-minute window.
- **Hardware-Backed Encrypted Photo Vault**:
  - Progress photos encrypted on-disk using AES-256-GCM via Android Keystore.
  - Protected behind BiometricPrompt (fingerprint/face) or device credential PIN/pattern fallback.
  - Before/After interactive split comparison slider with horizontal scrub controls.
- **75-Day Behavioral Science Curriculum**:
  - Pre-seeded local SQLite knowledge base with 75 evidence-backed micro-learning cards.
  - Covers habit loop mechanics, dopaminergic baseline regulation, circadian rhythm optimization, and cognitive endurance.
  - Full primary literature citations with direct DOI and PubMed links.
- **10-Page Reading Habit Tracker**:
  - Non-fiction book progress tracking with page counter, reading session log, and milestone celebrations.
- **Intelligent Local Notification Schedulers**:
  - Battery-conscious `AlarmManager` and `WorkManager` notifications with exact time scheduling.
  - Daytime interval reminders and evening cutoff warnings aligned with the user's custom sleep boundary.
- **Sovereign Data Privacy & Zero-Wipe**:
  - 100% offline-first architecture — zero third-party telemetry, tracking SDKs, or cloud dependencies.
  - Single-tap cryptographic zero-wipe utility in Settings to securely purge all databases, encrypted keys, and cache instantly.

---

## [Unreleased] — UX overhaul: tab shell, smoothness, wiring

### Added

- **Five-tab information architecture (Today · Trackers · Learn · Vault · More)** on both clients.
  - `AscendBottomBar` in `:core:designsystem` renders the shell with an alpha backdrop and a hairline
    separator (no blur/render effect), and stays visible inside Workout/Water/Reading/Settings with the
    owning tab highlighted.
  - `MainActivity` is now a `FragmentActivity` hosting all nine destinations, finite tween transitions,
    `NavigationBar` state save/restore, and `ascend75://task/<id>` deep-link handling.
- **Trackers hub** (`TrackersHubScreen` / `TrackersHubViewModel`) with cards for both workout sessions,
  hydration, reading and the progress-photo vault.
- **`DailyProtocolRepository`** — the single owner of the "today" flow and of day advancement. Replaces
  two nested `combine{}.collect{…collect{…}}` chains that froze the day number after the first emission
  and leaked one live Room observer per upstream emission.
- **Real day advancement**: crossing the sleep cutoff now materialises the next day with the active mode's
  habit set, carrying custom water/reading targets forward. Strict 75 still waits for explicit consent via
  the reset dialog.
- **Honest derived state**: streak counts consecutive completed days instead of echoing the day number;
  the dashboard's science-card title comes from the seeded curriculum; the sleep-cutoff label reads the
  persisted preference; milestone days (1/7/14/21/30/45/60/75) raise `MilestoneCelebrationDialog` once.
- **Session persistence**: `WorkoutSessionDao`, `WaterLogDao`, `ReadingSessionDao` and `ProgressPhotoDao`
  (schema unchanged, `version` stays 1) with rows written by the workout, hydration, reading and vault
  flows. Hydration restores its rolling-hour window from Room across process death; reading has a real
  1 Hz session timer.
- **Working photo vault**: real CameraX capture (`LifecycleCameraController`), thumbnail decryption at a
  bounded sample size, `BiometricPrompt` unlock honouring the preference, and a before/after split slider
  that no longer recomposes the grid on every drag frame.
- **Functional More tab**: editable sleep-cutoff slider, protocol-mode chips that update the active
  challenge row, app version, medical disclaimer, a full JSON archive export shared through
  `FileProvider`, and a wipe that also clears the session tables and destroys the Keystore master key.
- **Background wiring**: `WorkoutTimerService` declared with `foregroundServiceType="specialUse"` and
  rebound from the workout screen (the on-screen countdown previously never moved); boot/timezone/nudge
  receivers and the `FileProvider` registered; `ScheduleEvaluatorWorker` enqueued periodically from
  `AscendApplication`; `POST_NOTIFICATIONS` requested on API 33+.
- **50 new science cards** (days 26-75), every citation resolved through Crossref and verified to
  resolve; five dead DOIs in the original day 1-25 set were replaced.
- **Web prototype restructured** into the same five tabs, with the Zenith token palette, a self-hosted
  Plus Jakarta Sans face, a repaired Radix `data-[state=…]`/`data-[orientation=…]` contract, the
  `tailwindcss-animate` plugin registered, and the light/dark toggle removed.

### Changed

- All nine state collections switched from `collectAsState()` to `collectAsStateWithLifecycle()`
  (the lifecycle-aware collector was not even on the feature modules' classpath before).
- `VaultFileStorage` is suspend + `Dispatchers.IO`; `KeystoreManager` builds its `KeyStore` lazily so no
  hardware keystore work runs on the main thread during Hilt singleton construction.
- `ScienceCardSeeder` repairs the shipped asset on open instead of only on create.
- Cold start paints `#0F131C` from `windowBackground` and a branded `AscendLoadingGate`/skeleton instead
  of a platform-grey window and a full-screen spinner.
- `AscendProgressRing` implements the multi-segment arc ring the design system specifies.
- `AscendTheme` no longer takes an unused `darkTheme` parameter (the theme is unconditionally dark).

### Removed

- Five tautological unit tests that asserted their own inputs (`DashboardViewModelTest`,
  `UserPreferencesTest`, `AccessibilityAuditTest`, `VaultCryptoTest`, `ChallengeDaoTest`) plus
  `WaterRateLimiterTest`, whose cases are subsumed by the new behavioural suite.

---

[1.1.0]: https://github.com/RohannShetty/Ascend75/releases/tag/v1.1.0
[1.0.0]: https://github.com/RohannShetty/Ascend75/releases/tag/v1.0.0-preview
