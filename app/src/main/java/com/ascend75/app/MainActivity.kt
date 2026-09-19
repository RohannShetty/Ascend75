package com.ascend75.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ascend75.app.navigation.Screen
import com.ascend75.core.datastore.AscendPreferencesDataSource
import com.ascend75.core.designsystem.theme.AscendPalette
import com.ascend75.core.designsystem.theme.AscendTheme
import com.ascend75.feature.dashboard.DashboardScreen
import com.ascend75.feature.dashboard.DashboardViewModel
import com.ascend75.feature.learn.ScienceLibraryScreen
import com.ascend75.feature.learn.ScienceLibraryViewModel
import com.ascend75.feature.onboarding.OnboardingScreen
import com.ascend75.feature.onboarding.OnboardingViewModel
import com.ascend75.feature.settings.SettingsScreen
import com.ascend75.feature.settings.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesDataSource: AscendPreferencesDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AscendTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AscendPalette.Background
                ) {
                    val navController = rememberNavController()
                    val userPrefs by preferencesDataSource.userPreferencesFlow.collectAsState(initial = null)

                    if (userPrefs != null) {
                        val startDestination = if (userPrefs?.isOnboardingCompleted == true) {
                            Screen.Dashboard.route
                        } else {
                            Screen.Onboarding.route
                        }

                        NavHost(
                            navController = navController,
                            startDestination = startDestination
                        ) {
                            composable(Screen.Onboarding.route) {
                                val viewModel = hiltViewModel<OnboardingViewModel>()
                                OnboardingScreen(
                                    viewModel = viewModel,
                                    onFinish = {
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable(Screen.Dashboard.route) {
                                val viewModel = hiltViewModel<DashboardViewModel>()
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToScienceLibrary = {
                                        navController.navigate(Screen.ScienceLibrary.route)
                                    }
                                )
                            }

                            composable(Screen.ScienceLibrary.route) {
                                val viewModel = hiltViewModel<ScienceLibraryViewModel>()
                                ScienceLibraryScreen(
                                    viewModel = viewModel,
                                    onCardClick = {}
                                )
                            }

                            composable(Screen.Settings.route) {
                                val viewModel = hiltViewModel<SettingsViewModel>()
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateToOnboardingAfterWipe = {
                                        navController.navigate(Screen.Onboarding.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
