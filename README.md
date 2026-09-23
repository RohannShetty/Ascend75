# Ascend 75 — Discipline Operating System for Android

<div align="center">

![Ascend 75 Hero Banner](https://img.shields.io/badge/Platform-Android_8.0%2B_(API_26%2B)-34D399?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Security](https://img.shields.io/badge/Security-AES--256--GCM_Keystore-F2CA50?style=for-the-badge&logo=security&logoColor=black)
![Architecture](https://img.shields.io/badge/Architecture-Clean_MVVM_%2F_MVI-00A6E0?style=for-the-badge)
![Offline](https://img.shields.io/badge/Offline-100%25_Sovereign_Local-white?style=for-the-badge)

**An offline-first, executive-grade Android discipline and habit-transformation platform engineered around a structured 75-day personal challenge.**

[Features](#-core-pillars--features) • [Architecture](#-android-architecture--modular-design) • [Design System](#-design-system-zenith-fitness--growth) • [Security & Privacy](#-hardware-security--photo-vault) • [Building & Testing](#-building--testing) • [Download APK](#-testing--apk-downloads)

</div>

---

## ⚡ Executive Summary

**Ascend 75** rejects superficial gamification, toxic shaming, and intrusive cloud ad-trackers. Instead, it provides a calm, science-informed, privacy-first companion combining:
- **Customizable 75-Day Challenge Modes**: Strict 75, Flexible 75, 75 Soft, and Custom modes.
- **Intelligent Day Boundaries**: User-configurable sleep cutoff (default 3:00 AM) eliminating artificial midnight failures.
- **Encrypted Biometric Photo Vault**: Progress photos protected by Android Keystore hardware-backed AES-256-GCM encryption with zero leakage to public galleries.
- **Pre-Seeded 75-Day Science Curriculum**: Peer-reviewed educational cards derived from circadian biology, sleep architecture, dopamine dynamics, and implementation intentions with primary PubMed/DOI citations.
- **Sovereign Data Sovereignty**: 100% offline Room persistence with single-tap cryptographic zero-fill data erasure.
- **One Information Architecture, Two Clients**: Today · Trackers · Learn · Vault · More across both the Android app and the bundled web prototype.

---

## 🛡️ Core Pillars & Features

### 1. Challenge Modes & Empathetic Accountability
| Mode | Daily Requirements | Failure Policy |
| :--- | :--- | :--- |
| **Strict 75** | Two 45m workouts (1 outdoor), 3.8L water, 10 pages reading, strict diet, daily progress photo. | Incomplete day past sleep cutoff prompts an **Empathetic Reflection** modal, safely archives attempt history, and prompts restart or mode shift. |
| **Flexible 75** | Identical rigor to Strict 75 with customizable sleep cutoff up to 4:00 AM. | Incomplete days record reflection lessons and advance without resetting the cumulative streak. |
| **75 Soft** | One 45m workout daily, active recovery, 3.0L water, 10 pages reading, mindful balanced nutrition. | Sustainable gentle consistency for habit builders and active recovery cycles. |
| **Custom** | User-selected habits and thresholds. | Flexible personal experimentation. |

### 2. Specialized Tracking Engines
- **Dual 45-Minute Workout Timer**: Ongoing Android Foreground Service with persistent chronometer notification, CPU wake-lock safeguards, indoor/outdoor classification, minimum duration guardrails (<45 min alert), and a **3-hour physiological rest separation advisory**.
- **Smart Hydration Engine**: Quick-add increments (+250ml, +500ml, +750ml, +1000ml) with an hourly pacing visualizer and an automated **Hyponatremia Clinical Safety Warning** (>1,200 ml within a 60-minute window).
- **Cognitive Reading Tracker**: 10-page minimum session validation, active reading timer, and qualitative key takeaway reflection journal.
- **Progress Photo Vault**: In-app custom capture pipeline storing encrypted `.enc` files directly in sandbox with an interactive **Before-and-After split-slider comparison**.

### 3. Science-Backed Educational Library
- Pre-seeded offline catalog of **75 evidence-informed science cards**.
- Five scientific domains: `CIRCADIAN`, `FOCUS`, `DOPAMINE`, `RECOVERY`, and `HABITS`.
- Progressive daily unlock (Day $N$ unlocks Card $N$) with category filtering, keyword search, and bookmarking.

### 4. Hybrid Intelligent Notifications
- Android Notification Channels: `channel_briefing`, `channel_reminders`, `channel_workout_timer`, and `channel_milestones`.
- Quiet hours suppression (e.g. 10:00 PM to 7:00 AM) and **automatic cancellation of pending evening reminders** once 100% of daily habits are checked off.
- Resilient to device reboots (`BOOT_COMPLETED`) and daylight saving / timezone changes (`TIMEZONE_CHANGED`).

---

## 🏛️ Android Architecture & Modular Design

Built strictly adhering to modern Android Jetpack guidelines, Clean Architecture, and Unidirectional Data Flow (MVI/MVVM):

```
Ascend75/
├── app/                               # Root application, Hilt setup, type-safe navigation shell
├── core/
│   ├── domain/                        # JVM-only: domain models, HabitType, repository contracts
│   ├── data/                          # Room-backed repositories, entity↔model mappers, Hilt bindings
│   ├── common/                        # ChallengeRulesEngine, DayBoundaryEvaluator
│   ├── designsystem/                  # Material 3 theme, typography, shapes, Stitch tokens, bottom bar
│   ├── database/                      # Room database, entities, DAOs, ScienceCardSeeder (schema exported)
│   ├── datastore/                     # Jetpack Preferences DataStore for local state & cutoff schedules
│   ├── crypto/                        # Android Keystore manager, VaultFileStorage, BiometricAuthHelper
│   └── notifications/                 # Channels, WorkManager worker, AlarmManager scheduler, receivers
└── feature/
    ├── onboarding/                    # 6-step questionnaire, medical disclaimer, cutoff config, mode selection
    ├── dashboard/                     # Progress ring, habit checklist, Trackers hub, day/streak engine
    ├── workout/                       # Foreground timer service, workout screen, rest separation warning
    ├── water/                         # Water gauge, quick add, hyponatremia alert dialog
    ├── reading/                       # Book log, page interval counter, session timer, reflection notes
    ├── photos/                        # Biometric lock gate, encrypted capture, split comparison
    ├── learn/                         # 75-day science library, card detail, DOI links, bookmarks
    └── settings/                      # Export, sleep cutoff, protocol mode, cryptographic zero-wipe
```

### Persistence Boundary (single owner)

`Room` and `DataStore` are an implementation detail of **`:core:data`**. No `:feature:*` module declares
a dependency on `:core:database` or `:core:datastore`, and no ViewModel injects a DAO: UI state is built
from `:core:domain` models (`HabitType`, `TaskEntry`, `DailyRecord`, `TodayProtocol`) obtained from
repository contracts.

```
feature:*  ──▶  :core:domain  ◀──  :core:data  ──▶  :core:database / :core:datastore
   (UI)          (contracts)        (the only      (Room + DataStore)
                                  implementation)
```

This is enforced in CI, not by convention: the pipeline fails if a feature's `src/main` mentions
`com.ascend75.core.database` or `com.ascend75.core.datastore`. See
[`docs/architecture-persistence.md`](docs/architecture-persistence.md) for the rules and the rationale.

### Navigation & Information Architecture

Both clients expose the same five destinations, rendered by a fixed bottom bar
(`AscendBottomBar` on Android, `AscendTabBar` on web):

| Tab | Destinations it owns |
| :--- | :--- |
| **Today** | Dashboard, science-card highlight, daily guardrails checklist |
| **Trackers** | Trackers hub, Workout timer, Hydration, Reading |
| **Learn** | 75-day curriculum with in-place card detail and bookmarking |
| **Vault** | Biometric-gated encrypted progress photos |
| **More** | Sleep cutoff, protocol mode, JSON export, data wipe, about/disclaimer |

Pressing system back from a non-Today tab returns to Today before exiting. Deep links of the form
`ascend75://task/<taskId>` (used by the notification actions) resolve the task and open its tracker.

Navigation is **type-safe**: every destination is a `@Serializable` key declared in
`app/src/main/java/com/ascend75/app/navigation/Routes.kt`, registered in `AscendNavGraph.kt` with
`composable<DashboardRoute> { … }` and reached with `navController.navigate(WaterRoute(taskId))`.
There are no route strings left to mistype, and `MainActivity` is a 158-line shell that only picks the
start destination, drains the deep link and asks for `POST_NOTIFICATIONS`.

### Web Prototype (`web/`)

A presentational React 19 + Vite prototype mirrors the same five tabs and design tokens for review outside a
device. It keeps state in memory — no backend, no persistence — and is a companion to, not a replacement for,
the Android app. See [`web/README.md`](web/README.md).

---

## 🎨 Design System ("Zenith Fitness & Growth")

Directly integrated with Google Stitch Project `6273376942936009112` and [`DESIGN.md`](file:///D:/Ascend75/DESIGN.md):
- **Obsidian Dark Canvas**: `#0F131C` base surface with calibrated elevations (`#181B25`, `#1C1F29`, `#262A34`).
- **Brand Accents**: Champagne Gold (`#F2CA50`), Warm Muted Bronze (`#D4AF37`), Sky Cyan (`#7BD0FF`), Electric Azure (`#00A6E0`).
- **Typography Scale**: Editorial and high-contrast hierarchy using *Plus Jakarta Sans*.
- **Tactile Components**: `AscendButton` (48dp pill), `GlassCard` (translucent hairline border), `AscendProgressRing` (multi-segment animated sweep), and `HabitCheckCard` (haptic checkoff toggle).
- **Accessibility**: Enforces WCAG AA (4.5:1) and AAA (7:0:1) contrast compliance across all interactive surfaces.

---

## 🔒 Hardware Security & Photo Vault

```mermaid
flowchart LR
    Capture[Camera Capture] --> RAM[Raw Bytes in Memory]
    RAM --> Keystore[Android Keystore AES-256-GCM]
    Keystore --> Sandbox[(Private Sandbox /files/vault/*.enc)]
    RAM -- Memory Overwrite --> Clean[Zero Memory Leak]
    
    User[Open Photo Vault] --> Bio[BiometricPrompt / PIN]
    Bio -- Success --> Decrypt[Stream Direct to Compose Bitmap]
    Bio -- Failure --> Obscure[Obfuscated Gate Screen]
```

1. **Zero Public Gallery Leakage**: Photographs are never sent to `MediaStore` or external storage.
2. **Hardware Key Binding**: Master keys reside in `AndroidKeyStore` with GCM authentication tags.
3. **Cryptographic Zero-Wipe**: "Delete All Data" performs multi-pass zero-filling over encrypted photo files before unlinking from the file system.

---

## 🛠️ Building & Testing

### Prerequisites
- JDK 17
- Android SDK 34 (Android 14)
- Gradle 8.9+

### Build Debug APK Locally
```bash
./gradlew assembleDebug
```
The compiled APK will be located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Run Automated Unit & Integration Tests
```bash
./gradlew test
```
The suite runs on the JVM (Robolectric where a real `Context` or in-memory Room database is needed) and covers
the rules engine, day-boundary evaluation, the `:core:data` repositories' day advancement and streak derivation,
hydration rate limiting and restore, the reading 10-page gate and session timer, vault file framing,
photo-vault capture and vault gating, quiet-hours suppression, the science-card asset, the wipe/export
integration over a real schema, and an onboarding → day-2 journey driven entirely through repositories.

**47 unit tests, 0 failures** at the time of writing (`docs/refactor-baseline.md` records the pre-refactor
baseline of 41 so the comparison is auditable). CI runs the same command on every push and pull request, and
additionally fails the build when a feature module reaches for Room or DataStore directly:

```bash
# the persistence boundary check that CI enforces
grep -rn "com\.ascend75\.core\.database\|com\.ascend75\.core\.datastore" feature/*/src/main --include=*.kt
# expected output: no matches (exit code 1)
```

---

## 📦 Testing & APK Downloads

Every push to `main` (and every `v*` tag) triggers our GitHub Actions CI/CD pipeline, which runs the unit
suite and the persistence-boundary gate, then compiles a downloadable debug APK and attaches it to the release:

1. Navigate to the [Releases](https://github.com/RohannShetty/Ascend75/releases) tab.
2. Download `app-debug.apk` under the latest release (**v1.1.0**).
3. Install the APK on any device running Android 8.0+ (API 26+) or in Android Studio Emulator.

---

## 📄 License & Medical Disclaimer

**Health Notice**: Ascend 75 is an educational discipline and habit development tool. It is not a medical device. Always consult a qualified physician prior to initiating strenuous fitness regimens or major hydration shifts.
