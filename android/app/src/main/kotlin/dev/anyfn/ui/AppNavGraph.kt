/**
 * AppNavGraph
 *
 * Central navigation graph. Routes are declared as sealed [Destination] entries
 * so the compiler enforces exhaustiveness at every call site. Onboarding is
 * gated by [dev.anyfn.data.preferences.OnboardingPreferences] — once completed,
 * the start destination shifts to Home for the rest of the app's lifetime.
 */
package dev.anyfn.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.anyfn.ui.bridge.BridgeScreen
import dev.anyfn.ui.home.HomeScreen
import dev.anyfn.ui.onboarding.OnboardingScreen
import dev.anyfn.ui.playground.PlaygroundScreen
import dev.anyfn.ui.registry.RegistryScreen
import dev.anyfn.ui.scanner.ScannerScreen
import dev.anyfn.ui.settings.SettingsScreen

sealed class Destination(val route: String) {
    data object Onboarding : Destination("onboarding")
    data object Home : Destination("home")
    data object Scanner : Destination("scanner")
    data object Registry : Destination("registry")
    data object Playground : Destination("playground/{functionId}") {
        fun create(functionId: Long): String = "playground/$functionId"
    }
    data object Bridge : Destination("bridge")
    data object Settings : Destination("settings")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val rootVm: RootViewModel = hiltViewModel()
    val state = rootVm.state.collectAsState()
    val start = if (state.value.onboardingComplete) Destination.Home.route else Destination.Onboarding.route

    NavHost(navController = navController, startDestination = start) {
        composable(Destination.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    rootVm.markOnboardingComplete()
                    navController.navigate(Destination.Home.route) {
                        popUpTo(Destination.Onboarding.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Destination.Home.route) {
            HomeScreen(
                onScan = { navController.navigate(Destination.Scanner.route) },
                onRegistry = { navController.navigate(Destination.Registry.route) },
                onBridge = { navController.navigate(Destination.Bridge.route) },
                onSettings = { navController.navigate(Destination.Settings.route) },
            )
        }
        composable(Destination.Scanner.route) {
            ScannerScreen(onDone = { navController.popBackStack() })
        }
        composable(Destination.Registry.route) {
            RegistryScreen(
                onFunctionTap = { id -> navController.navigate(Destination.Playground.create(id)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable(Destination.Playground.route) { backStack ->
            val id = backStack.arguments?.getString("functionId")?.toLongOrNull() ?: 0L
            PlaygroundScreen(functionId = id, onBack = { navController.popBackStack() })
        }
        composable(Destination.Bridge.route) {
            BridgeScreen(onBack = { navController.popBackStack() })
        }
        composable(Destination.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
