package com.ascend75.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ascend75.app.navigation.AscendNavGraph
import com.ascend75.app.navigation.DashboardRoute
import com.ascend75.app.navigation.OnboardingRoute
import com.ascend75.app.navigation.tabForDestination
import com.ascend75.app.navigation.navigateToTab
import com.ascend75.app.navigation.trackerRouteFor
import com.ascend75.core.designsystem.components.AscendBottomBar
import com.ascend75.core.designsystem.components.AscendLoadingGate
import com.ascend75.core.designsystem.components.AscendTab
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTheme
import com.ascend75.core.domain.repository.SettingsRepository
import com.ascend75.core.domain.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

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
                    settingsRepository = settingsRepository,
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

    /** Accepts only `ascend75://task/<id>`; anything else is ignored rather than guessed at. */
    private fun extractTaskId(intent: Intent?): String? {
        val data = intent?.data ?: return null
        if (!"ascend75".equals(data.scheme, ignoreCase = true)) return null
        if (!"task".equals(data.host, ignoreCase = true)) return null
        return data.lastPathSegment?.takeIf { it.isNotBlank() }
    }
}

@Composable
private fun AscendApp(
    settingsRepository: SettingsRepository,
    taskRepository: TaskRepository,
    pendingDeepLinkTaskId: String?,
    onDeepLinkResolved: () -> Unit
) {
    val navController = rememberNavController()
    val userPrefs by settingsRepository.preferences.collectAsStateWithLifecycle(initialValue = null)
    val prefs = userPrefs

    if (prefs == null) {
        AscendLoadingGate(modifier = Modifier.fillMaxSize())
        return
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = tabForDestination(backStackEntry?.destination)

    Scaffold(
        containerColor = AscendPalette.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // The bar owns "which section am I in"; anything unknown (onboarding) hides it.
            if (currentTab != null) {
                AscendBottomBar(
                    selected = currentTab,
                    onSelect = { tab -> navController.navigateToTab(tab) }
                )
            }
        }
    ) { padding ->
        AscendNavGraph(
            navController = navController,
            startDestination = if (prefs.isOnboardingCompleted) DashboardRoute else OnboardingRoute,
            onOnboardingFinished = {
                navController.navigate(DashboardRoute) {
                    popUpTo<OnboardingRoute> { inclusive = true }
                }
            },
            onDataWiped = {
                navController.navigate(OnboardingRoute) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                }
            }
        )

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
