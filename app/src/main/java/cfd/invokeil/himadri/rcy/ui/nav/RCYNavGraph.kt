package cfd.invokeil.himadri.rcy.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.screens.auth.ForgotScreen
import cfd.invokeil.himadri.rcy.screens.auth.LoginScreen
import cfd.invokeil.himadri.rcy.screens.auth.ResetScreen
import cfd.invokeil.himadri.rcy.screens.auth.SignupScreen
import cfd.invokeil.himadri.rcy.screens.onboarding.OnboardingScreen
import cfd.invokeil.himadri.rcy.screens.onboarding.SplashScreen
import cfd.invokeil.himadri.rcy.ui.scaffold.MainScaffold
import cfd.invokeil.himadri.rcy.vm.AppViewModel

/**
 * Top-level graph:
 *  splash → (onboarding) → main shell | login/signup/forgot/reset fullscreen.
 * All member content lives inside [MainScaffold] (header + ticker + drawer
 * + bottom bar) with its own child graph.
 */
@Composable
fun RCYNavGraph(nav: NavHostController, vm: AppViewModel) {
    NavHost(navController = nav, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onDone = {
                    val target = if (!vm.settings.value.onboarded) Routes.ONBOARDING
                    else if (vm.session.value != null) Routes.MAIN else Routes.MAIN
                    nav.navigate(target) { popUpTo(Routes.SPLASH) { inclusive = true } }
                }
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    vm.setOnboarded()
                    nav.navigate(Routes.MAIN) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                }
            )
        }
        composable(Routes.MAIN) {
            MainScaffold(vm = vm)
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onBack = { nav.popBackStack() },
                onSignup = { nav.navigate(Routes.SIGNUP) },
                onForgot = { nav.navigate(Routes.FORGOT) },
                onSuccess = { nav.navigate(Routes.MAIN) { popUpTo(Routes.LOGIN) { inclusive = true } } },
            )
        }
        composable(Routes.SIGNUP) {
            SignupScreen(
                onBack = { nav.popBackStack() },
                onLogin = { nav.navigate(Routes.LOGIN) { popUpTo(Routes.SIGNUP) { inclusive = true } } },
                onSuccess = { nav.navigate(Routes.MAIN) { popUpTo(Routes.SIGNUP) { inclusive = true } } },
            )
        }
        composable(Routes.FORGOT) {
            ForgotScreen(onBack = { nav.popBackStack() }, onSent = { nav.navigate(Routes.RESET_TOKENLESS) })
        }
        composable(Routes.RESET_TOKENLESS) {
            ResetScreen(token = null, onBack = { nav.popBackStack() }, onDone = {
                nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
            })
        }
        composable(Routes.RESET) { entry ->
            val token = entry.arguments?.getString("token")
            ResetScreen(token = token, onBack = { nav.popBackStack() }, onDone = {
                nav.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
            })
        }
    }

    // Deep links (/verify?code=…, /n/[id]) are consumed by MainScaffold,
    // because notice/verify live in the shell's CHILD graph.
}

// (reset_tokenless lives in Routes)

/** Maps website URLs to in-app routes. */
object DeepLinks {
    fun routeFor(url: String): String? {
        val clean = url.substringAfter("://")
        return when {
            clean.contains("/verify") -> {
                val code = clean.substringAfter("code=", "").substringBefore("&")
                Routes.verify(code.takeIf { it.isNotBlank() && it != url })
            }
            clean.contains("/n/") -> {
                val id = clean.substringAfter("/n/").substringBefore("?").substringBefore("/")
                if (id.isNotBlank()) Routes.notice(id) else null
            }
            clean.contains("/certificate/") -> {
                val id = clean.substringAfter("/certificate/").substringBefore("?").substringBefore("/")
                if (id.isNotBlank()) "certificate/$id" else null
            }
            else -> null
        }
    }
}
