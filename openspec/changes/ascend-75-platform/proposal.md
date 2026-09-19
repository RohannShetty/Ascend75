# Proposal

## Why

Modern habit tracking applications frequently suffer from aggressive gamification, superficial streak mechanics, invasive third-party cloud tracking, and unverified motivational claims. Individuals undertaking rigorous physical and mental discipline programs like 75 Hard require an offline-first, executive-grade Android platform that delivers sovereign data privacy (hardware-encrypted biometric photo vault), scientifically grounded circadian and habit guidance, robust day-boundary handling (customizable sleep cutoff without artificial midnight resets), and compassionate accountability instead of punitive shaming.

## What Changes

Establish the core architecture, persistence models, security layers, background schedulers, and user-facing modules for the Ascend 75 Android platform based on the master product blueprint:
- Scaffold modern Android Jetpack multi-module architecture with Gradle version catalog (`libs.versions.toml`), Clean MVVM/MVI unidirectional data flow, and Hilt dependency injection.
- Implement the "Zenith Fitness & Growth" Material 3 design system with semantic design tokens, WCAG AA/AAA contrast ratios, fluid motion curves, and custom components (`AscendProgressRing`, `GlassCard`, `HabitCheckCard`).
- Deliver the core challenge rules engine supporting 4 modes (Strict 75, Flexible 75, 75 Soft, Custom), configurable sleep cutoff timestamps, and compassionate reset/archive flows.
- Implement offline-first Room database persistence covering challenge instances, daily records, task entries, workout sessions, water logs, reading logs, progress photos, and science cards.
- Build dedicated habit tracking engines: dual 45-minute workout timer with foreground service, smart paced hydration tracking with rapid-intake safety warnings (>1.2L/hr), and reading tracker with notes.
- Build the Android Keystore AES-256-GCM encrypted photo vault with BiometricPrompt / PIN authentication, ensuring zero leakage to public galleries, alongside secure single-tap data erasure.
- Build a hybrid WorkManager and AlarmManager intelligent notification engine that respects quiet hours, adapts to timezone changes, and cancels evening nudges when daily tasks are complete.
- Populate and unlock a 75-card behavioral science library pre-seeded with peer-reviewed research citations (circadian biology, dopamine regulation, sleep architecture).

## Capabilities

### New Capabilities
- `design-system`: Stitch-integrated Material 3 design system ("Zenith Fitness & Growth"), semantic color/typography tokens, high-contrast themes, and reusable tactile UI components.
- `challenge-engine`: Rules engine supporting 4 challenge modes, customizable sleep cutoffs, daily record state machine, and compassionate attempt archiving and reset flows.
- `tracking-engines`: Specialized habit loggers for dual 45-minute workouts (with foreground timer service), hydration tracking with rate-limit warnings, and reading sessions.
- `crypto-vault`: Hardware-backed AES-256-GCM encrypted photo vault, BiometricPrompt / PIN security gate, before/after split slider, and cryptographic wipe mechanism.
- `intelligent-notifications`: Hybrid WorkManager + AlarmManager notification engine with quiet hours suppression, boot/timezone receivers, and dynamic cancellation upon task completion.
- `science-library`: Pre-seeded 75-day progressive behavioral science catalog with primary research citations, offline search, and category filtering.

### Modified Capabilities

## Impact
- **Architecture & Build**: New Android Gradle multi-module project structure (`:core:common`, `:core:designsystem`, `:core:database`, `:core:datastore`, `:core:crypto`, `:core:notifications`, and feature modules `:feature:onboarding`, `:feature:dashboard`, `:feature:workout`, `:feature:water`, `:feature:reading`, `:feature:photos`, `:feature:learn`, `:feature:settings`).
- **Dependencies**: AndroidX Core, Compose BOM, Material 3, Navigation Compose, Hilt, Room, WorkManager, AndroidX Security Crypto, Biometric, Kotlinx Coroutines, and Serialization.
- **Hardware & Security**: Requires Android Keystore AES-256-GCM crypto and optional BiometricPrompt hardware abstraction.
- **System Services**: Interacts with AlarmManager, WorkManager, NotificationManager, and Foreground Service lifecycle.
