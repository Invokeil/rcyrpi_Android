package cfd.invokeil.himadri.rcy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.rememberNavController
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import cfd.invokeil.himadri.rcy.core.theme.RCYTheme
import cfd.invokeil.himadri.rcy.core.theme.isDarkTheme
import cfd.invokeil.himadri.rcy.ui.nav.RCYNavGraph
import cfd.invokeil.himadri.rcy.vm.AppViewModel

/** CompositionLocals — language + app VM reach every composable without drilling. */
val LocalLang = staticCompositionLocalOf { Lang.BN }
val LocalAppVM = staticCompositionLocalOf<AppViewModel> { error("AppViewModel not provided") }

class MainActivity : ComponentActivity() {

    private val vm: AppViewModel by viewModels {
        val app = application as RCYApp
        AppViewModel.Factory(app.settingsStore, app.cacheStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by vm.theme.collectAsState()
            val lang by vm.lang.collectAsState()
            val dark = isDarkTheme(themeMode)

            RCYTheme(themeMode = themeMode, useBengali = lang.isBn) {
                // edge-to-edge status bar icons follow the resolved theme
                DisposableEffect(dark) {
                    enableEdgeToEdge(
                        statusBarStyle = androidx.activity.SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                        ) { dark },
                        navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                        ) { dark },
                    )
                    onDispose { }
                }

                CompositionLocalProvider(LocalLang provides lang, LocalAppVM provides vm) {
                    val nav = rememberNavController()
                    RCYNavGraph(nav = nav, vm = vm)
                }
            }

            // global error toasts are handled per-screen; start the LIVE ticker here
            LaunchedEffect(Unit) { vm.startTickerPolling() }
        }

        // Deep link: /verify?code=..., /n/[id]
        handleDeepLink(intent?.data?.toString())
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent.data?.toString())
    }

    private fun handleDeepLink(url: String?) {
        if (url.isNullOrEmpty()) return
        pendingDeepLink = url
    }

    companion object {
        @Volatile var pendingDeepLink: String? = null
    }
}
