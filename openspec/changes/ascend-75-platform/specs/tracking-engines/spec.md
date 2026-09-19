# Spec Delta

## Purpose

Provides specialized habit tracking engines for dual 45-minute workouts, paced hydration with health safety guardrails, and non-fiction reading sessions.

## ADDED Requirements

### Requirement: Dual 45-Minute Workout Tracking with Separation Warning
The system SHALL provide an active workout tracking timer enforcing a minimum 45-minute duration, indoor/outdoor classification, and a resting separation safety warning between daily sessions.

#### Scenario: Workout session completion
- **WHEN** the 45-minute timer completes and the user designates the session as indoor or outdoor
- **THEN** the system logs the session duration and marks the corresponding daily workout habit as completed

#### Scenario: Close workout session separation warning
- **WHEN** the user initiates the second daily workout less than 3 hours after completing the first session
- **THEN** the system displays a rest and recovery advisory warning recommending adequate physiological separation before beginning

### Requirement: Hydration Tracking with Hyponatremia Safety Threshold
The system SHALL track incremental daily fluid intake against customizable targets and display an explicit clinical safety warning if water is logged at an excessively rapid pace.

#### Scenario: Rapid fluid intake detection
- **WHEN** a user logs more than 1,200 ml of fluid intake within a rolling 60-minute window
- **THEN** the system displays an amber hyponatremia safety warning advising the user to pace water consumption to avoid water intoxication

#### Scenario: Incremental water logging
- **WHEN** the user logs water intake using incremental quick-add increments
- **THEN** the daily accumulated volume increments immediately and recalculates remaining target volume

### Requirement: Reading Session Tracking with Reflections
The system SHALL track daily non-fiction reading sessions, recording page intervals, elapsed reading time, and qualitative takeaway reflections.

#### Scenario: Completing reading requirement
- **WHEN** the user logs a session recording at least 10 pages read along with optional reflections
- **THEN** the system marks the daily reading habit completed and preserves the session entry in the book log
