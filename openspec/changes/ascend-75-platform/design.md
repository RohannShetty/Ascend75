# Design

## Context

Ascend 75 is an offline-first Android discipline platform engineered around a 75-day personal challenge (see `proposal.md` for motivation and background). The workspace is currently initialized with design tokens in `DESIGN.md` linked to Google Stitch Project `6273376942936009112` ("Zenith Fitness & Growth"). The platform requires an architectural design that ensures zero data loss, rock-solid background scheduling resistant to Android Doze mode and timezone shifts, hardware-level encryption for sensitive body transformation photos, and a modular, unidirectional UI architecture.

## Goals / Non-Goals

**Goals:**
- Architect a clean, scalable Gradle multi-module Android project separating core infrastructure (`:core:common`, `:core:designsystem`, `:core:database`, `:core:datastore`, `:core:crypto`, `:core:notifications`) from feature modules (`:feature:onboarding`, `:feature:dashboard`, `:feature:workout`, `:feature:water`, `:feature:reading`, `:feature:photos`, `:feature:learn`, `:feature:settings`).
- Establish an offline-first Room persistence model with foreign-key cascading and reactive `Flow` queries as the single source of truth.
- Implement the "Zenith Fitness & Growth" Material 3 design system in Jetpack Compose, directly ingesting semantic tokens from `DESIGN.md` (Obsidian canvas `#0F131C`, Bronze CTA `#D4AF37`, Gold `#F2CA50`, Cyan `#7BD0FF`).
- Enforce hardware-backed AES-256-GCM encryption with Android Keystore and BiometricPrompt authentication, ensuring zero plain-text photo exposure to external storage or public galleries.
- Build a resilient hybrid notification scheduler combining WorkManager (periodic evaluation, surviving process kill) and AlarmManager (`setAndAllowWhileIdle()` for exact alerts and Doze survival).
- Pre-seed 75 peer-reviewed behavioral science cards with DOI citations into Room on initial database creation.

**Non-Goals:**
- Cloud-based synchronization, external backend authentication, or multi-device sync (V1 is 100% offline and local).
- Calorie counting, barcode scanning, or macronutrient database integration.
- Public social media sharing or punitive gamification mechanics (leaderboards, public shaming).
- Bluetooth hardware scale integration.

## Decisions

### 1. Multi-Module Architecture with Unidirectional Data Flow (MVI/Clean MVVM)
- **Decision**: Divide the project into domain-focused core libraries and feature modules. ViewModels expose a single immutable `StateFlow<UiState>` and a `SharedFlow<UiEffect>`.
- **Rationale**: Isolates domain logic from Android framework dependencies, enables parallel Gradle compilation caching, prevents accidental leakage between features, and ensures predictable UI state.
- **Alternatives Considered**: 
  - *Single Monolithic App Module*: Faster initial setup, but leads to tight coupling, high build times, and blurred boundaries between crypto, background tasks, and UI.

### 2. Room Persistence with Foreign Key Cascades as Single Source of Truth
- **Decision**: Use Room with SQLite foreign keys (`ON DELETE CASCADE`) across `challenge_instances`, `daily_records`, `task_entries`, `workout_sessions`, `water_logs`, `reading_sessions`, `progress_photos`, and `science_cards`.
- **Rationale**: Guarantees relational integrity (e.g., archiving or deleting a challenge cleanly manages associated child records). Kotlin Coroutines and reactive `Flow` emissions allow UI to update automatically without polling.
- **Alternatives Considered**: 
  - *Realm / ObjectBox*: Faster for some graph queries, but proprietary, larger binary size, and lack of seamless integration with Android Jetpack paging/testing libraries.
  - *Raw SQLite*: High boilerplate and prone to compile-time syntax errors.

### 3. Jetpack Security Crypto + Android Keystore for Photo Vault
- **Decision**: Encrypt progress photos using `EncryptedFile` backed by a 256-bit AES-GCM master key generated in `AndroidKeyStore`. Gate decryption behind `BiometricPrompt` with `BIOMETRIC_STRONG | DEVICE_CREDENTIAL`.
- **Rationale**: Protects sensitive photos against unauthorized extraction even on rooted devices or file system inspection. Decrypted image bytes stream directly into Compose `Bitmap` objects in memory and are discarded immediately.
- **Alternatives Considered**:
  - *SQLCipher*: Encrypts the entire database, but causes performance overhead for high-frequency habit queries and bloats the SQLite file when storing image blobs. Storing encrypted files in sandbox with metadata in Room is cleaner and lighter.
  - *Unencrypted private files*: Still readable by root or adb backup without hardware key protection.

### 4. Hybrid Background Scheduling (WorkManager + AlarmManager + BroadcastReceivers)
- **Decision**: Use `WorkManager` for periodic state evaluation (every 6 hours and on task checkoff) and `AlarmManager.setAndAllowWhileIdle()` for exact notification firing and quiet hour boundaries. Register `BootReceiver` (`BOOT_COMPLETED`) and `TimezoneReceiver` (`TIMEZONE_CHANGED`, `TIME_SET`).
- **Rationale**: WorkManager alone cannot guarantee exact-minute delivery in Doze mode. AlarmManager alone does not provide constraint-based rescheduling. Combining them ensures alarms survive reboots, time-zone hops, and device battery optimization.
- **Alternatives Considered**:
  - *Pure WorkManager*: Fails to fire exact morning briefings or sleep cutoff reminders due to opportunistic OS batching.
  - *Pure AlarmManager*: Requires manual persistence and retry logic if execution fails during low-power states.

### 5. Pre-Seeded Room Database via Bundled JSON Asset
- **Decision**: Package `science_cards.json` containing 75 peer-reviewed educational cards in `:core:database` assets and populate Room via `RoomDatabase.Callback.onCreate()`.
- **Rationale**: Guarantees instant offline availability without requiring network connectivity on first launch. Allows standard Room FTS / queries for category filtering and search.
- **Alternatives Considered**:
  - *Hardcoded Kotlin constants*: Excessive bytecode bloat and difficult to update.
  - *Remote API download*: Violates the 100% offline-first principle.

## Risks / Trade-offs

- **[OEM Battery Optimization Killing Background Alarms]** → Mitigate by using `AlarmManager.setAndAllowWhileIdle()`, running the active workout timer as an ongoing Foreground Service with persistent chronometer notification, and surfacing battery optimization guidance in Settings.
- **[Timezone Shifts and Daylight Saving Cutoff Glitches]** → Mitigate by storing timestamps in UTC/epoch milliseconds and evaluating day boundaries relative to local calendar day plus user's sleep cutoff offset. Register `ACTION_TIMEZONE_CHANGED` to recalculate triggers immediately.
- **[Excessive Memory Usage during Photo Decryption]** → Mitigate by decoding downsampled bitmaps sized to the display target (`BitmapFactory.Options.inSampleSize`) rather than loading full-resolution raw photos into memory.
- **[Hyponatremia Risk from Rapid Water Logging]** → Mitigate by tracking timestamps of water logs in `water_logs` table and triggering a safety warning dialog if logged volume exceeds 1,200 ml in a rolling 60-minute window.
- **[Device Without Biometric Sensor]** → Mitigate by configuring `BiometricPrompt` with `DEVICE_CREDENTIAL` fallback, enabling standard device PIN/Pattern authentication.

## Migration Plan

The platform is rolled out across 8 structured milestones:
1. **Milestone 1**: Project foundation, Gradle version catalog (`libs.versions.toml`), and `:core:designsystem` with Stitch tokens.
2. **Milestone 2**: `:core:database` entities, DAOs, `ChallengeRulesEngine`, and `:feature:onboarding` flow.
3. **Milestone 3**: `:feature:dashboard` with real-time `AscendProgressRing`, task checkoff, and day reset dialogs.
4. **Milestone 4**: Dedicated habit tracking engines (`:feature:workout` foreground service, `:feature:water` with hyponatremia warnings, `:feature:reading`).
5. **Milestone 5**: `:core:crypto` hardware vault, Biometric gate, and `:feature:photos` split comparison.
6. **Milestone 6**: `:core:notifications` hybrid scheduling engine, channels, and receivers.
7. **Milestone 7**: Science card JSON seeder, `:feature:learn` library, and milestone celebration dialogs.
8. **Milestone 8**: Release hardening, accessibility audit (WCAG AA/AAA), ProGuard rules, data wipe, and APK verification.
