# Spec Delta

## Purpose

Governs the 75-day challenge lifecycle, operating modes, day boundary evaluation with configurable sleep cutoffs, and compassionate attempt archiving and reset flows.

## ADDED Requirements

### Requirement: Multi-Mode Challenge Execution
The system SHALL support four challenge operating modes: Strict 75, Flexible 75, 75 Soft, and Custom, each enforcing distinct daily criteria and failure consequences.

#### Scenario: Strict 75 mode selection
- **WHEN** the user selects Strict 75 during onboarding or setup
- **THEN** all 6 core daily habits are marked as mandatory and must be completed prior to the sleep cutoff each day

#### Scenario: Flexible 75 mode selection
- **WHEN** the user operates in Flexible 75 mode
- **THEN** missing a task records that day as incomplete, preserves the cumulative attempt history, and advances the challenge to the next calendar day without resetting to Day 1

### Requirement: Configurable Sleep Cutoff Day Boundary
The system SHALL evaluate day completion against a user-configurable sleep cutoff time (defaulting to 3:00 AM) rather than calendar midnight (12:00 AM).

#### Scenario: Activity logged past midnight before sleep cutoff
- **WHEN** the user completes a task at 1:30 AM before their configured 3:00 AM cutoff
- **THEN** the completion is credited toward the current challenge day rather than the subsequent day

#### Scenario: Evaluation past sleep cutoff
- **WHEN** the local time exceeds the sleep cutoff timestamp
- **THEN** the system finalizes the previous day's record and initializes the new challenge day

### Requirement: Strict Mode Failure and Empathetic Reset Flow
The system SHALL detect incomplete days in Strict 75 mode upon crossing the sleep cutoff, presenting an empathetic reflection interface and archiving the previous attempt.

#### Scenario: Day reset triggered after incomplete strict day
- **WHEN** the user opens the application following an incomplete day in Strict 75 mode
- **THEN** the system presents a dignified reflection dialog, preserves all past historical logs and photos, archives the attempt, and offers to restart Day 1 or transition to Flexible Mode

### Requirement: Medical and Health Disclaimer Acknowledgment
The system SHALL require explicit acknowledgment of medical and physical safety disclaimers prior to initializing any active challenge instance.

#### Scenario: Disclaimer gate before challenge initialization
- **WHEN** a user begins initial onboarding
- **THEN** the system displays the medical safety warning and prevents starting the challenge until the user explicitly checks the acknowledgment confirmation
