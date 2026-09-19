# Spec Delta

## Purpose

Safeguards sensitive transformation photographs and personal journal logs using hardware-backed cryptography, biometric authentication, and sovereign data controls.

## ADDED Requirements

### Requirement: Hardware-Backed Photo Encryption and Zero Gallery Leakage
The system SHALL encrypt all progress photographs immediately upon capture using hardware-backed AES-256-GCM encryption and store them exclusively within app-private storage with zero exposure to public media galleries.

#### Scenario: Progress photo capture and encryption
- **WHEN** the user captures a daily progress photo via the in-app camera
- **THEN** image bytes are encrypted and written to an app-private file, and no media records are created in public device directories

### Requirement: Biometric and PIN Security Gate
The system SHALL require biometric authentication (fingerprint or face) or device lockscreen PIN fallback before displaying encrypted progress photos or sensitive reflections.

#### Scenario: Accessing protected photo vault
- **WHEN** the user opens the progress photo tab or detail view
- **THEN** an authentication prompt appears and conceals photo content until identity is successfully verified

#### Scenario: Authentication failure or cancellation
- **WHEN** biometric authentication fails or is cancelled by the user
- **THEN** photo thumbnails and comparison sliders remain obscured and inaccessible

### Requirement: Before-and-After Visual Comparison
The system SHALL provide an interactive split-slider comparison tool that allows users to compare their Day 1 baseline progress photo against their most recent photo.

#### Scenario: Split-slider interaction
- **WHEN** an authenticated user opens the transformation comparison view
- **THEN** the system renders an interactive vertical divider allowing dragging across Day 1 and current progress photos in real time

### Requirement: Secure Cryptographic Data Erasure
The system SHALL provide a single-action data wipe that cryptographically overwrites encrypted photo files with zeros before unlinking them, wiping all local databases and preferences.

#### Scenario: Executing complete data wipe
- **WHEN** the user confirms the data erasure request in security settings
- **THEN** all encrypted files are zero-filled and deleted, all database tables are truncated, and application state resets to fresh onboarding
