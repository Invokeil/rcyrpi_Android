package cfd.invokeil.himadri.rcy.core.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import cfd.invokeil.himadri.rcy.core.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "rcy_settings")

/** App-wide persisted settings (DataStore Preferences). */
class SettingsStore(private val context: Context) {

    private object K {
        val LANG = stringPreferencesKey("lang")
        val THEME = stringPreferencesKey("theme")
        val SERVER_URL = stringPreferencesKey("server_url")
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val LAST_USERNAME = stringPreferencesKey("last_username")
        val SESSION_COOKIE = stringPreferencesKey("session_cookie")
        val SESSION_HOST = stringPreferencesKey("session_host")
        val APP_LOCK = booleanPreferencesKey("app_lock")
    }

    data class Settings(
        val lang: Lang = Lang.BN,
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
        val serverUrl: String? = null,
        val onboarded: Boolean = false,
        val lastUsername: String = "",
        val sessionCookie: String? = null,
        val sessionHost: String? = null,
        val appLock: Boolean = false,
    )

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            lang = Lang.from(p[K.LANG]),
            themeMode = ThemeMode.from(p[K.THEME]),
            serverUrl = p[K.SERVER_URL],
            onboarded = p[K.ONBOARDED] ?: false,
            lastUsername = p[K.LAST_USERNAME] ?: "",
            sessionCookie = p[K.SESSION_COOKIE],
            sessionHost = p[K.SESSION_HOST],
            appLock = p[K.APP_LOCK] ?: false,
        )
    }

    suspend fun current(): Settings = settings.first()

    suspend fun setLang(lang: Lang) = context.dataStore.edit { it[K.LANG] = lang.code }
    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[K.THEME] = mode.storage }
    suspend fun setServerUrl(url: String?) = context.dataStore.edit {
        if (url.isNullOrBlank()) it.remove(K.SERVER_URL) else it[K.SERVER_URL] = url
    }
    suspend fun setOnboarded(done: Boolean) = context.dataStore.edit { it[K.ONBOARDED] = done }
    suspend fun setLastUsername(name: String) = context.dataStore.edit { it[K.LAST_USERNAME] = name }
    suspend fun setAppLock(enabled: Boolean) = context.dataStore.edit { it[K.APP_LOCK] = enabled }

    suspend fun setSessionCookie(cookie: String?, host: String?) = context.dataStore.edit { p ->
        if (cookie.isNullOrBlank() || host.isNullOrBlank()) {
            p.remove(K.SESSION_COOKIE); p.remove(K.SESSION_HOST)
        } else {
            p[K.SESSION_COOKIE] = cookie; p[K.SESSION_HOST] = host
        }
    }
}
