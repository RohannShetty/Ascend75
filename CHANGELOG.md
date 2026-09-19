# Changelog

All notable changes to the **Ascend 75** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

[1.0.0]: https://github.com/RohannShetty/Ascend75/releases/tag/v1.0.0-preview
