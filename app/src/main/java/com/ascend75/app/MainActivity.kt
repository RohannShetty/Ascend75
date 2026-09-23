package com.ascend75.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ascend75.app.navigation.ARG_TASK_ID
import com.ascend75.app.navigation.Screen
import com.ascend75.core.domain.model.HabitType
import com.ascend75.core.domain.repository.TaskRepository
import com.ascend75.core.datastore.AscendPreferencesDataSource
import com.ascend75.core.designsystem.components.AscendBottomBar
import com.ascend75.core.designsystem.components.AscendLoadingGate
import com.ascend75.core.designsystem.components.AscendTab
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTheme
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var preferencesDataSource: AscendPreferencesDataSource

    @Inject
    lateinit var taskRepository: TaskRepository

    /** Task id delivered by an `ascend75://task/<id>` deep link, drained once the shell is up. */
    private val pendingDeepLinkTaskId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingDeepLinkTaskId.value = extractTaskId(intent)

        setContent {
            AscendTheme {
                AscendApp(
                    preferencesDataSource = preferencesDataSource,
                    taskRepository = taskRepository,
                    pendingDeepLinkTaskId = pendingDeepLinkTaskId.value,
                    onDeepLinkResolved = { pendingDeepLinkTaskId.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractTaskId(intent)?.let { pendingDeepLinkTaskId.value = it }
    }

    private fun extractTaskId(intent: Intent?): String? {
        val data = intent?.data ?: return null
        if (!"ascend75".equals(data.scheme, ignoreCase = true)) return null
        if (!"task".equals(data.host, ignoreCase = true)) return null
        return data.lastPathSegment?.takeIf { it.isNotBlank() }
    }
}

@Composable
private fun AscendApp(
    preferencesDataSource: AscendPreferencesDataSource,
    taskRepository: TaskRepository,
    pendingDeepLinkTaskId: String?,
    onDeepLinkResolved: () -> Unit
) {
    val navController = rememberNavController()
    val userPrefs by preferencesDataSource.userPreferencesFlow.collectAsStateWithLifecycle(initialValue = null)
    val prefs = userPrefs

    if (prefs == null) {
        AscendLoadingGate(modifier = Modifier.fillMaxSize())
    } else {
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route

        Scaffold(
            containerColor = AscendPalette.Background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (currentRoute != Screen.Onboarding.route) {
                    AscendBottomBar(
                        selected = routeToTab(currentRoute) ?: AscendTab.TODAY,
                        onSelect = { tab -> navController.navigateToTab(tab) }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = if (prefs.isOnboardingCompleted) {
                    Screen.Dashboard.route
                } else {
                    Screen.Onboarding.route
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
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
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        viewModel = hiltViewModel<OnboardingViewModel>(),
                        onFinish = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = hiltViewModel<DashboardViewModel>(),
                        onNavigateToScienceLibrary = { navController.navigateToTab(AscendTab.LEARN) },
                        onOpenTracker = { task ->
                            trackerRouteFor(task.habitType, task.id)?.let { route -> navController.navigate(route) }
                        }
                    )
                }

                composable(Screen.Trackers.route) {
                    TrackersHubScreen(
                        viewModel = hiltViewModel<TrackersHubViewModel>(),
                        onOpenTracker = { task ->
                            trackerRouteFor(task.habitType, task.id)?.let { route -> navController.navigate(route) }
                        },
                        onOpenVault = { navController.navigateToTab(AscendTab.VAULT) }
                    )
                }

                composable(
                    route = Screen.Workout.route,
                    arguments = listOf(navArgument(ARG_TASK_ID) { type = NavType.StringType })
                ) { entry ->
                    WorkoutScreen(
                        taskId = entry.arguments?.getString(ARG_TASK_ID).orEmpty(),
                        viewModel = hiltViewModel<WorkoutViewModel>(),
                        onFinished = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.Water.route,
                    arguments = listOf(navArgument(ARG_TASK_ID) { type = NavType.StringType })
                ) { entry ->
                    WaterScreen(
                        taskId = entry.arguments?.getString(ARG_TASK_ID).orEmpty(),
                        viewModel = hiltViewModel<WaterViewModel>(),
                        onFinished = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.Reading.route,
                    arguments = listOf(navArgument(ARG_TASK_ID) { type = NavType.StringType })
                ) { entry ->
                    ReadingScreen(
                        taskId = entry.arguments?.getString(ARG_TASK_ID).orEmpty(),
                        viewModel = hiltViewModel<ReadingViewModel>(),
                        onFinished = { navController.popBackStack() }
                    )
                }

                composable(Screen.Photos.route) {
                    PhotoVaultScreen(
                        viewModel = hiltViewModel<PhotoVaultViewModel>(),
                        onFinished = { navController.popBackStack() }
                    )
                }

                composable(Screen.ScienceLibrary.route) {
                    ScienceLibraryScreen(
                        viewModel = hiltViewModel<ScienceLibraryViewModel>()
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = hiltViewModel<SettingsViewModel>(),
                        onNavigateToOnboardingAfterWipe = {
                            navController.navigate(Screen.Onboarding.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }

            NotificationPermissionRequest()

            LaunchedEffect(pendingDeepLinkTaskId, prefs.isOnboardingCompleted) {
                val taskId = pendingDeepLinkTaskId ?: return@LaunchedEffect
                if (!prefs.isOnboardingCompleted) return@LaunchedEffect
                onDeepLinkResolved()
                val task = taskRepository.getTask(taskId) ?: return@LaunchedEffect
                trackerRouteFor(task.habitType, task.id)?.let { route -> navController.navigate(route) }
            }
        }
    }
}

/** Asks for notification permission once on API 33+, where the platform drops every post without it. */
@Composable
private fun NotificationPermissionRequest() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

private fun routeToTab(route: String?): AscendTab? = when {
    route == null -> null
    route.startsWith("dashboard") -> AscendTab.TODAY
    route.startsWith("trackers") ||
        route.startsWith("workout") ||
        route.startsWith("water") ||
        route.startsWith("reading") -> AscendTab.TRACKERS
    route.startsWith("learn") -> AscendTab.LEARN
    route.startsWith("photos") -> AscendTab.VAULT
    route.startsWith("settings") -> AscendTab.MORE
    else -> null
}

/**
 * Maps a habit type to its dedicated tracker destination. `DIET` has no tracker screen and returns
 * null so callers can omit the affordance entirely.
 */
private fun trackerRouteFor(habitType: HabitType, taskId: String): String? = when (habitType) {
    HabitType.WORKOUT_1, HabitType.WORKOUT_2 -> Screen.Workout.createRoute(taskId)
    HabitType.WATER -> Screen.Water.createRoute(taskId)
    HabitType.READING -> Screen.Reading.createRoute(taskId)
    HabitType.PHOTO -> Screen.Photos.route
    HabitType.DIET, HabitType.CUSTOM -> null
}

private fun NavHostController.navigateToTab(tab: AscendTab) {
    val route = when (tab) {
        AscendTab.TODAY -> Screen.Dashboard.route
        AscendTab.TRACKERS -> Screen.Trackers.route
        AscendTab.LEARN -> Screen.ScienceLibrary.route
        AscendTab.VAULT -> Screen.Photos.route
        AscendTab.MORE -> Screen.Settings.route
    }
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
