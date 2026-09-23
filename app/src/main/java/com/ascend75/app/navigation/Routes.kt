package com.ascend75.app.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation keys. Each destination is a serializable type, so a route argument can no
 * longer be silently mistyped the way the old `"workout/{taskId}"` strings allowed.
 */
@Serializable
data object OnboardingRoute

@Serializable
data object DashboardRoute

@Serializable
data object TrackersHubRoute

@Serializable
data class WorkoutRoute(val taskId: String)

@Serializable
data class WaterRoute(val taskId: String)

@Serializable
data class ReadingRoute(val taskId: String)

@Serializable
data object PhotoVaultRoute

@Serializable
data object ScienceLibraryRoute

@Serializable
data object SettingsRoute
