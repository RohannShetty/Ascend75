# Tasks

## 1. Project Setup, Gradle Catalogs & Design System Foundation

- [x] 1.1 Configure `gradle/libs.versions.toml` with AndroidX Compose BOM, Material 3, Hilt, Room, WorkManager, Security Crypto, Biometric, and Navigation Compose; verify syntax and version compatibility.
- [x] 1.2 Configure multi-module Gradle project structure in `settings.gradle.kts` and root `build.gradle.kts` declaring `:core:common`, `:core:designsystem`, `:core:database`, `:core:datastore`, `:core:crypto`, `:core:notifications`, and feature modules (`:feature:onboarding`, `:feature:dashboard`, `:feature:workout`, `:feature:water`, `:feature:reading`, `:feature:photos`, `:feature:learn`, `:feature:settings`).
- [x] 1.3 Implement semantic color tokens, typography hierarchy (Plus Jakarta Sans), and shape definitions in `:core:designsystem` mapped from Stitch project `6273376942936009112` (`DESIGN.md`); verify contrast ratios >= 4.5:1 (AA) and 7:1 (AAA) via unit tests.
- [x] 1.4 Build foundational Jetpack Compose components in `:core:designsystem` (`AscendButton`, `GlassCard`, `AscendProgressRing`, `HabitCheckCard`) and verify interactive Compose previews render across Dark and Light themes.

## 2. Local Persistence, Challenge Engine & Onboarding Flow

- [x] 2.1 Implement Room database entities (`ChallengeInstanceEntity`, `DailyRecordEntity`, `TaskEntryEntity`) with foreign-key cascade deletions and SQLite indexes in `:core:database`; verify schema integrity via in-memory Room unit tests.
- [x] 2.2 Implement Room DAOs (`ChallengeDao`, `DailyRecordDao`, `TaskEntryDao`) with reactive Coroutine `Flow` queries and transaction support; verify CRUD operations with automated unit tests.
- [x] 2.3 Implement `ChallengeRulesEngine` in `:core:common` supporting Strict 75, Flexible 75, 75 Soft, and Custom modes, day initialization, and sleep cutoff evaluation; verify mode transition behaviors via unit tests.
- [x] 2.4 Implement Proto DataStore preferences in `:core:datastore` for caching onboarding input, user sleep/wake schedules, and active challenge instance ID; verify data persistence across simulated process restarts.
- [x] 2.5 Build the 6-step Onboarding wizard in `:feature:onboarding` (Medical Disclaimer checkbox, goals, schedule with cutoff, mode selection, habit target customizer, biometric opt-in); verify that completing onboarding initializes Day 1 records in Room.

## 3. Daily Dashboard, Ring Progress & Task Checkoff Engine

- [x] 3.1 Implement `DashboardViewModel` in `:feature:dashboard` observing reactive `Flow<DailyRecordWithTasks>` from Room and exposing immutable `StateFlow<DashboardUiState>`.
- [x] 3.2 Build the Home Dashboard UI in `:feature:dashboard` featuring the multi-segment `AscendProgressRing`, streak counter, dynamic motivation header, and habit checklist.
- [x] 3.3 Wire atomic quick-complete habit toggles with tactile haptic clicks (`HapticFeedbackType.LongPress`) and verify immediate ring animation updates without UI stutter.
- [x] 3.4 Implement `DayResetDialog` for Strict 75 mode failure past the sleep cutoff, capturing qualitative reflections and cleanly archiving the attempt while offering restart to Day 1 or Flexible mode toggle.
- [x] 3.5 Implement `TaskDetailBottomSheet` displaying habit guidelines, physiological rationale ("Why it matters"), and reflection notes editing; verify bidirectional state updates with Room.

## 4. Specialized Habit Trackers

- [x] 4.1 Implement `WorkoutTimerService` as an Android Foreground Service with ongoing chronometer notification, action controls (pause/finish), and wake-lock safeguards in `:feature:workout`.
- [x] 4.2 Build `WorkoutScreen` in `:feature:workout` with 45-minute countdown, indoor/outdoor classification, minimum duration validation (<45m guardrail), and the 3-hour minimum rest separation warning.
- [x] 4.3 Implement `WaterViewModel` and `WaterScreen` in `:feature:water` with quick-add volume buttons, hourly pacing visualizer, and hyponatremia rate-limiting warning dialog (>1.2L in <60m); verify threshold alert with unit test.
- [x] 4.4 Implement `ReadingViewModel` and `ReadingScreen` in `:feature:reading` supporting page count logging (10 pages target), active reading timer, and qualitative key takeaway reflections.
- [x] 4.5 Connect all specialized habit trackers to parent `TaskEntry` records in `:core:database` and verify dashboard ring progress synchronization.

## 5. Encrypted Photo Vault & Biometric Security Gate

- [x] 5.1 Implement `KeystoreManager` and `VaultFileStorage` in `:core:crypto` managing hardware-backed AES-256-GCM master keys and `EncryptedFile` operations in private app sandbox (`files/vault/*.enc`); verify encrypted ciphertext on disk via unit tests.
- [x] 5.2 Implement `BiometricAuthHelper` in `:core:crypto` wrapping `BiometricPrompt` with `BIOMETRIC_STRONG | DEVICE_CREDENTIAL` fallback; verify authentication callbacks and dismissal handling.
- [x] 5.3 Integrate CameraX capture pipeline in `:feature:photos` writing encrypted bytes directly to sandbox with zero public media gallery leakage; verify photos are absent from `MediaStore`.
- [x] 5.4 Build `PhotoVaultScreen` in `:feature:photos` featuring biometric obfuscation gate, thumbnail grid, and interactive Before-and-After split-slider comparing Day 1 baseline against current day.

## 6. Hybrid Intelligent Notification Engine

- [x] 6.1 Define Android Notification Channels in `:core:notifications` (`channel_briefing`, `channel_reminders`, `channel_workout_timer`, `channel_milestones`) with distinct importance, sounds, and vibration patterns.
- [x] 6.2 Implement `SmartReminderScheduler` and `ScheduleEvaluatorWorker` in `:core:notifications` calculating remaining daily habits, evaluating quiet hours, and scheduling exact triggers with `AlarmManager.setAndAllowWhileIdle()`.
- [x] 6.3 Implement dynamic notification cancellation logic: cancel pending evening habit reminders immediately upon 100% daily task completion; verify with unit test.
- [x] 6.4 Register `BootReceiver` (`BOOT_COMPLETED`) and `TimezoneReceiver` (`ACTION_TIMEZONE_CHANGED`, `ACTION_TIME_CHANGED`) to reschedule alarms after device reboot or timezone changes.
- [x] 6.5 Add contextual action buttons (`Complete Task`, `Log 250ml`, `Snooze 30m`) and deep links (`ascend75://task/{taskId}`, `ascend75://workout/active`) to reminder notifications.

## 7. Science Content Library & Milestone Celebrations

- [x] 7.1 Author and bundle 75 peer-reviewed behavioral science cards in `science_cards.json` with categories (Circadian, Focus, Dopamine, Recovery, Habits), takeaways, mechanisms, action steps, and DOI/PubMed citations.
- [x] 7.2 Implement `ScienceCardSeeder` in `:core:database` to populate `science_cards` table on database creation via `RoomDatabase.Callback`; verify complete 75-card insertion with unit test.
- [x] 7.3 Build `ScienceCardDetailScreen` and `ScienceLibraryScreen` in `:feature:learn` featuring progressive daily unlock (Day N unlocks Card N), keyword search, category filtering, and bookmarking.
- [x] 7.4 Implement `MilestoneCelebrationDialog` triggered on Days 1, 7, 14, 21, 30, 45, 60, and 75 with personalized milestone statistics and celebratory haptics.

## 8. Data Sovereignty, Accessibility, Hardening & Verification

- [x] 8.1 Implement `DataExportManager` in `:core:common` and `:feature:settings` providing encrypted zip export and cryptographic zero-fill single-tap data erasure; verify complete wipe via integration test.
- [x] 8.2 Conduct TalkBack and accessibility audit across Onboarding, Dashboard, and Tracker screens ensuring all touch targets >= 48x48dp and content descriptions are complete.
- [x] 8.3 Configure release signing and R8/ProGuard rules in `app/proguard-rules.pro` for Room, Coroutines, Serialization, and Keystore crypto; verify release build compilation with `./gradlew assembleRelease`.
- [x] 8.4 Execute end-to-end instrumented test suite validating full user journey from onboarding to task completion, workout timing, photo encryption, and sleep cutoff day transition.
