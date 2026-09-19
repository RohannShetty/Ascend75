# Spec Delta

## Purpose

Delivers adaptive, context-aware reminders for incomplete daily habits while strictly honoring quiet hours, preventing spam, and surviving device reboots and timezone shifts.

## ADDED Requirements

### Requirement: Android Notification Channels Configuration
The system SHALL organize notifications into dedicated, descriptive notification channels corresponding to briefings, habit reminders, ongoing workout timers, and milestone achievements.

#### Scenario: Channel segregation
- **WHEN** the application posts a notification to the system tray
- **THEN** the notification is routed to its designated channel with appropriate importance, sound, and vibration settings

### Requirement: Quiet Hours and Completion-Driven Alarm Cancellation
The system SHALL suppress reminder notifications during user-configured quiet hours and cancel remaining evening reminders once all daily tasks are accomplished.

#### Scenario: All daily tasks completed before evening
- **WHEN** the user marks their final remaining daily habit complete
- **THEN** any pending scheduled reminder alarms for the remainder of that day are cancelled

#### Scenario: Scheduled trigger during quiet hours
- **WHEN** a background evaluation calculates an alert time falling within configured quiet hours
- **THEN** the system suppresses the alert or defers delivery until the morning wake window

### Requirement: Device Reboot and Timezone Resilience
The system SHALL re-evaluate and restore all scheduled reminders upon device reboot and when the system detects a timezone or daylight saving time transition.

#### Scenario: System reboot recovery
- **WHEN** the host device finishes booting
- **THEN** the system re-registers all appropriate reminder triggers for the current challenge day

#### Scenario: Timezone change detection
- **WHEN** the device transitions across time zones or adjusts for daylight saving time
- **THEN** the system recalculates trigger timestamps relative to local time and updates scheduled alarms

### Requirement: Actionable Interactive Notification Controls
The system SHALL include contextual action buttons within habit reminder notifications enabling direct actions without opening the full application.

#### Scenario: Direct logging from notification action
- **WHEN** the user taps an action button such as logging water or snoozing an alert
- **THEN** the background service records the action or updates the schedule without launching the main application window
