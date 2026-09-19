# Spec Delta

## Purpose

Delivers a pre-seeded, evidence-informed 75-card educational curriculum rooted in circadian biology, dopamine dynamics, recovery, and habit formation with primary scientific citations.

## ADDED Requirements

### Requirement: Pre-Seeded 75-Day Scientific Curriculum
The system SHALL provide a pre-seeded library of 75 educational science cards, each containing a title, category, evidence summary, physiological mechanism explanation, actionable recommendation, and primary peer-reviewed citation with DOI.

#### Scenario: Displaying daily science card content
- **WHEN** the user views the daily science card for their active challenge day
- **THEN** the system displays the full evidence summary, mechanism, action step, and clickable peer-reviewed citation link

### Requirement: Progressive Daily Unlock Mechanism
The system SHALL unlock science cards progressively in alignment with the active challenge day counter while maintaining access to all previously unlocked cards.

#### Scenario: Day progression card unlock
- **WHEN** the challenge advances from Day N to Day N+1
- **THEN** science card N+1 transitions from locked to unlocked status and becomes readable on the home dashboard

#### Scenario: Browsing historical unlocked cards
- **WHEN** the user navigates to the science library tab
- **THEN** all cards corresponding to days up to and including the current day are visible and readable

### Requirement: Categorized Filtering, Search, and Bookmarking
The system SHALL enable users to search unlocked science cards by text query, filter by scientific categories (Circadian, Focus, Dopamine, Recovery, Habits), and save bookmarked cards.

#### Scenario: Filtering cards by category
- **WHEN** the user selects the "Circadian" category chip in the library
- **THEN** only unlocked cards classified under circadian biology are displayed

#### Scenario: Bookmarking an unlocked card
- **WHEN** the user toggles the bookmark icon on any unlocked science card
- **THEN** the card is saved to the user's bookmarked collection for offline review
