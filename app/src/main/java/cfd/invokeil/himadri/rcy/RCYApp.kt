package cfd.invokeil.himadri.rcy

import android.app.Application
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.storage.CacheStore
import cfd.invokeil.himadri.rcy.core.storage.SettingsStore
import cfd.invokeil.himadri.rcy.core.util.Geo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RCYApp : Application() {

    lateinit var settingsStore: SettingsStore
        private set
    lateinit var cacheStore: CacheStore
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        settingsStore = SettingsStore(this)
        cacheStore = CacheStore(this)
        Geo.load(this)

        appScope.launch {
            val s = settingsStore.settings.first()
            ApiClient.init(
                persistedCookie = s.sessionCookie,
                persistedHost = s.sessionHost,
                savedBaseUrl = s.serverUrl,
            ) { cookie, host ->
                settingsStore.setSessionCookie(cookie, host)
            }
        }

        // FCM (enable later):
        // FirebaseApp.initializeApp(this)
        // FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
        //     // TODO: register device token with the server once an endpoint exists
        // }
    }
}
