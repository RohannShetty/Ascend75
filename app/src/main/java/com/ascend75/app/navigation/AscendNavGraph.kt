package com.ascend75.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.ascend75.core.designsystem.components.AscendTab
import com.ascend75.core.domain.model.HabitType
import com.ascend75.feature.dashboard.DashboardScreen
import com.ascend75.feature.dashboard.DashboardViewModel
import com.ascend75.feature.dashboard.TrackersHubScreen
import com.ascend75.feature.dashboard.TrackersHubViewModel
import com.ascend75.feature.learn.ScienceLibraryScreen
import com.ascend75.feature.learn.ScienceLibraryViewModel
import com.ascend75.feature.onboarding.OnboardingScreen
import com.ascend75.feature.onboarding.OnboardingViewModel
import com.ascend75.feature.photos.PhotoVaultScreen
import com.ascend75.feature.photos.PhotoVaultViewModel
import com.ascend75.feature.reading.ReadingScreen
import com.ascend75.feature.reading.ReadingViewModel
import com.ascend75.feature.settings.SettingsScreen
import com.ascend75.feature.settings.SettingsViewModel
import com.ascend75.feature.water.WaterScreen
import com.ascend75.feature.water.WaterViewModel
import com.ascend75.feature.workout.WorkoutScreen
import com.ascend75.feature.workout.WorkoutViewModel
import androidx.compose.runtime.Composable

/**
 * The whole destination table, in one place and typed. The activity shell only decides *where* the
 * user starts and *how* tabs are switched; everything else lives here.
 */
@Composable
fun AscendNavGraph(
    navController: NavHostController,
    startDestination: Any,
    onOnboardingFinished: () -> Unit,
    onDataWiped: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it / 10 }, animationSpec = tween(220)) +
                fadeIn(animationSpec = tween(220))
        },
        exitTransition = { fadeOut(animationSpec = tween(180)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it / 10 }, animationSpec = tween(180)) +
                fadeOut(animationSpec = tween(180))
        }
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                viewModel = hiltViewModel<OnboardingViewModel>(),
                onFinish = onOnboardingFinished
            )
        }

        composable<DashboardRoute> {
            DashboardScreen(
                viewModel = hiltViewModel<DashboardViewModel>(),
                onNavigateToScienceLibrary = { navController.navigateToTab(AscendTab.LEARN) },
                onOpenTracker = { task ->
                    trackerRouteFor(task.habitType, task.id)?.let(navController::navigate)
                }
            )
        }

        composable<TrackersHubRoute> {
            TrackersHubScreen(
                viewModel = hiltViewModel<TrackersHubViewModel>(),
                onOpenTracker = { task ->
                    trackerRouteFor(task.habitType, task.id)?.let(navController::navigate)
                },
                onOpenVault = { navController.navigateToTab(AscendTab.VAULT) }
            )
        }

        composable<WorkoutRoute> { entry ->
            WorkoutScreen(
                taskId = entry.toRoute<WorkoutRoute>().taskId,
                viewModel = hiltViewModel<WorkoutViewModel>(),
                onFinished = { navController.popBackStack() }
            )
        }

        composable<WaterRoute> { entry ->
            WaterScreen(
                taskId = entry.toRoute<WaterRoute>().taskId,
                viewModel = hiltViewModel<WaterViewModel>(),
                onFinished = { navController.popBackStack() }
            )
        }

        composable<ReadingRoute> { entry ->
            ReadingScreen(
                taskId = entry.toRoute<ReadingRoute>().taskId,
                viewModel = hiltViewModel<ReadingViewModel>(),
                onFinished = { navController.popBackStack() }
            )
        }

        composable<PhotoVaultRoute> {
            PhotoVaultScreen(
                viewModel = hiltViewModel<PhotoVaultViewModel>(),
                onFinished = { navController.popBackStack() }
            )
        }

        composable<ScienceLibraryRoute> {
            ScienceLibraryScreen(
                viewModel = hiltViewModel<ScienceLibraryViewModel>()
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                viewModel = hiltViewModel<SettingsViewModel>(),
                onNavigateToOnboardingAfterWipe = onDataWiped
            )
        }
    }
}

/**
 * Maps a habit type to its dedicated tracker destination. `DIET` and `CUSTOM` have no tracker screen
 * and return null so callers can omit the affordance entirely.
 */
internal fun trackerRouteFor(habitType: HabitType, taskId: String): Any? = when (habitType) {
    HabitType.WORKOUT_1, HabitType.WORKOUT_2 -> WorkoutRoute(taskId)
    HabitType.WATER -> WaterRoute(taskId)
    HabitType.READING -> ReadingRoute(taskId)
    HabitType.PHOTO -> PhotoVaultRoute
    HabitType.DIET, HabitType.CUSTOM -> null
}

/** Keeps the bottom bar honest: a destination belongs to exactly one tab. */
internal fun tabForDestination(destination: NavDestination?): AscendTab? = when {
    destination == null -> null
    destination.hasRoute<DashboardRoute>() -> AscendTab.TODAY
    destination.hasRoute<TrackersHubRoute>() ||
        destination.hasRoute<WorkoutRoute>() ||
        destination.hasRoute<WaterRoute>() ||
        destination.hasRoute<ReadingRoute>() -> AscendTab.TRACKERS
    destination.hasRoute<ScienceLibraryRoute>() -> AscendTab.LEARN
    destination.hasRoute<PhotoVaultRoute>() -> AscendTab.VAULT
    destination.hasRoute<SettingsRoute>() -> AscendTab.MORE
    else -> null
}

internal fun NavHostController.navigateToTab(tab: AscendTab) {
    val route: Any = when (tab) {
        AscendTab.TODAY -> DashboardRoute
        AscendTab.TRACKERS -> TrackersHubRoute
        AscendTab.LEARN -> ScienceLibraryRoute
        AscendTab.VAULT -> PhotoVaultRoute
        AscendTab.MORE -> SettingsRoute
    }
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}


