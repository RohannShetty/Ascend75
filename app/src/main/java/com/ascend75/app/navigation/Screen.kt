package com.ascend75.app.navigation

/** Name of the navigation argument carrying a task entry id for the tracker destinations. */
const val ARG_TASK_ID = "taskId"

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object Trackers : Screen("trackers")
    data object Workout : Screen("workout/{$ARG_TASK_ID}") {
        fun createRoute(taskId: String) = "workout/$taskId"
    }
    data object Water : Screen("water/{$ARG_TASK_ID}") {
        fun createRoute(taskId: String) = "water/$taskId"
    }
    data object Reading : Screen("reading/{$ARG_TASK_ID}") {
        fun createRoute(taskId: String) = "reading/$taskId"
    }
    data object Photos : Screen("photos")
    data object ScienceLibrary : Screen("learn/library")
    data object Settings : Screen("settings")
}
