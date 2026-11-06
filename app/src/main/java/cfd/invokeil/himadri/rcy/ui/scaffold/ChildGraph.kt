package cfd.invokeil.himadri.rcy.ui.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cfd.invokeil.himadri.rcy.LocalAppVM
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.screens.auth.LoginScreen
import cfd.invokeil.himadri.rcy.screens.emergency.EmergencyFeedScreen
import cfd.invokeil.himadri.rcy.screens.emergency.EmergencyRequestScreen
import cfd.invokeil.himadri.rcy.screens.events.EventDetailScreen
import cfd.invokeil.himadri.rcy.screens.events.EventsScreen
import cfd.invokeil.himadri.rcy.screens.home.HomeScreen
import cfd.invokeil.himadri.rcy.screens.leaderboard.LeaderboardScreen
import cfd.invokeil.himadri.rcy.screens.misc.CertificateDetailScreen
import cfd.invokeil.himadri.rcy.screens.misc.CertificatesScreen
import cfd.invokeil.himadri.rcy.screens.misc.ChangePasswordScreen
import cfd.invokeil.himadri.rcy.screens.misc.IdCardScreen
import cfd.invokeil.himadri.rcy.screens.misc.NotificationsScreen
import cfd.invokeil.himadri.rcy.screens.misc.ProfileEditScreen
import cfd.invokeil.himadri.rcy.screens.misc.SettingsScreen
import cfd.invokeil.himadri.rcy.screens.misc.TermsScreen
import cfd.invokeil.himadri.rcy.screens.misc.VerifyScreen
import cfd.invokeil.himadri.rcy.screens.notices.NoticeDetailScreen
import cfd.invokeil.himadri.rcy.screens.notices.NoticesScreen
import cfd.invokeil.himadri.rcy.screens.payments.FundDetailScreen
import cfd.invokeil.himadri.rcy.screens.payments.PaymentSuccessScreen
import cfd.invokeil.himadri.rcy.screens.payments.PaymentsScreen
import cfd.invokeil.himadri.rcy.screens.profile.ProfileScreen
import cfd.invokeil.himadri.rcy.vm.AppViewModel

/** LOGIN route for drawer (outer-graph navigation via child is avoided). */
const val LOGIN_ROUTE = "ext_login"

/**
 * Child content graph inside the shell.
 * Every screen receives (vm, nav) — the two integration points.
 */
@Composable
fun ChildGraph(vm: AppViewModel, nav: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = nav, startDestination = Routes.HOME, modifier = modifier) {
        composable(Routes.HOME) { HomeScreen(vm, nav) }
        composable(Routes.NOTICES) { NoticesScreen(vm, nav) }
        composable(Routes.NOTICE_DETAIL) { e ->
            NoticeDetailScreen(vm, nav, e.arguments?.getString("id").orEmpty())
        }
        composable(Routes.EVENTS) { EventsScreen(vm, nav) }
        composable(Routes.EVENT_DETAIL) { e ->
            EventDetailScreen(vm, nav, e.arguments?.getString("id").orEmpty())
        }
        composable(Routes.FUND_DETAIL) { e ->
            FundDetailScreen(vm, nav, e.arguments?.getString("id").orEmpty())
        }
        composable(Routes.LEADERBOARD) { LeaderboardScreen(vm, nav) }
        composable(Routes.PAYMENTS) { PaymentsScreen(vm, nav) }
        composable(
            Routes.PAYMENT_SUCCESS,
            arguments = listOf(
                navArgument("tran") { type = NavType.StringType; defaultValue = "" },
                navArgument("item") { type = NavType.StringType; defaultValue = "" },
                navArgument("amount") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { e ->
            PaymentSuccessScreen(
                vm, nav,
                tran = e.arguments?.getString("tran").orEmpty(),
                item = e.arguments?.getString("item").orEmpty(),
                amount = e.arguments?.getString("amount").orEmpty(),
            )
        }
        composable(Routes.EMERGENCY_REQUEST) { EmergencyRequestScreen(vm, nav) }
        composable(Routes.EMERGENCY_FEED) { EmergencyFeedScreen(vm, nav) }
        composable(Routes.PROFILE) { ProfileScreen(vm, nav) }
        composable(Routes.PROFILE_EDIT) { ProfileEditScreen(vm, nav) }
        composable(Routes.ID_CARD) { IdCardScreen(vm, nav) }
        composable(Routes.CERTIFICATES) { CertificatesScreen(vm, nav) }
        composable(Routes.CERTIFICATE_DETAIL) { e ->
            CertificateDetailScreen(vm, nav, e.arguments?.getString("id").orEmpty())
        }
        composable(Routes.CHANGE_PASSWORD) { ChangePasswordScreen(vm, nav) }
        composable(Routes.NOTIFICATIONS) { NotificationsScreen(vm, nav) }
        composable(
            Routes.VERIFY,
            arguments = listOf(navArgument("code") { type = NavType.StringType; defaultValue = "" }),
        ) { e ->
            VerifyScreen(vm, nav, e.arguments?.getString("code")?.takeIf { it.isNotBlank() })
        }
        composable(Routes.TERMS) { TermsScreen(vm, nav) }
        composable(Routes.SETTINGS) { SettingsScreen(vm, nav) }
        // login inside the shell (drawer entry for guests)
        composable(LOGIN_ROUTE) {
            LoginScreen(
                onBack = { nav.popBackStack() },
                onSignup = { nav.navigate(SIGNUP_ROUTE) },
                onForgot = { nav.navigate(FORGOT_ROUTE) },
                onSuccess = { nav.popBackStack() },
            )
        }
        composable(SIGNUP_ROUTE) {
            cfd.invokeil.himadri.rcy.screens.auth.SignupScreen(
                onBack = { nav.popBackStack() },
                onLogin = { nav.popBackStack() },
                onSuccess = { nav.popBackStack() },
                onTerms = { nav.navigate(Routes.TERMS) },
            )
        }
        composable(FORGOT_ROUTE) {
            cfd.invokeil.himadri.rcy.screens.auth.ForgotScreen(onBack = { nav.popBackStack() }, onSent = { nav.popBackStack() })
        }
    }
}

const val SIGNUP_ROUTE = "ext_signup"
const val FORGOT_ROUTE = "ext_forgot"
