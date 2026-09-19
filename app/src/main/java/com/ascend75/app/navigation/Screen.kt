package com.ascend75.app.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object Workout : Screen("workout/{taskId}") {
        fun createRoute(taskId: String) = "workout/$taskId"
    }
    data object Water : Screen("water/{taskId}") {
        fun createRoute(taskId: String) = "water/$taskId"
    }
    data object Reading : Screen("reading/{taskId}") {
        fun createRoute(taskId: String) = "reading/$taskId"
    }
    data object Photos : Screen("photos/{taskId}") {
        fun createRoute(taskId: String) = "photos/$taskId"
    }
    data object ScienceLibrary : Screen("learn/library")
    data object Settings : Screen("settings")
}
