package cfd.invokeil.himadri.rcy.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.FeedAlert
import cfd.invokeil.himadri.rcy.core.network.NotificationsResponse
import cfd.invokeil.himadri.rcy.core.network.SessionUser
import cfd.invokeil.himadri.rcy.core.storage.CacheStore
import cfd.invokeil.himadri.rcy.core.storage.SettingsStore
import cfd.invokeil.himadri.rcy.core.theme.ThemeMode
import cfd.invokeil.himadri.rcy.core.util.AppError
import cfd.invokeil.himadri.rcy.core.util.Repo
import cfd.invokeil.himadri.rcy.core.util.Resource
import cfd.invokeil.himadri.rcy.core.network.NoticesResponse
import cfd.invokeil.himadri.rcy.core.network.Notice
import cfd.invokeil.himadri.rcy.core.util.appJson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer

/**
 * App-wide state: settings, session, LIVE emergency ticker + popup,
 * notification badge. Mirrors the website's providers (session,
 * emergency-popup, emergency-strip) and zustand stores.
 */
class AppViewModel(
    private val settingsStore: SettingsStore,
    cache: CacheStore,
) : ViewModel() {

    private val repo = Repo(cache)

    // ── settings ───────────────────────────────────────────────────────
    val settings: StateFlow<SettingsStore.Settings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, SettingsStore.Settings())

    private val _lang = MutableStateFlow(Lang.BN)
    val lang: StateFlow<Lang> = _lang.asStateFlow()

    private val _theme = MutableStateFlow(ThemeMode.SYSTEM)
    val theme: StateFlow<ThemeMode> = _theme.asStateFlow()

    init {
        viewModelScope.launch {
            settingsStore.settings.collect {
                _lang.value = it.lang
                _theme.value = it.themeMode
            }
        }
    }

    fun setLang(lang: Lang) = viewModelScope.launch { settingsStore.setLang(lang) }
    fun toggleLang() = setLang(if (lang.value == Lang.BN) Lang.EN else Lang.BN)
    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settingsStore.setTheme(mode) }
    fun setOnboarded() = viewModelScope.launch { settingsStore.setOnboarded(true) }
    fun setServerUrl(url: String?) = viewModelScope.launch {
        settingsStore.setServerUrl(url)
        if (url.isNullOrBlank()) ApiClient.setBaseUrl(ApiClient.MAIN_URL)
        else ApiClient.setBaseUrl(url)
        refreshSession()
        refreshTicker()
    }

    // ── session ────────────────────────────────────────────────────────
    private val _session = MutableStateFlow<SessionUser?>(null)
    val session: StateFlow<SessionUser?> = _session.asStateFlow()

    private val _sessionLoading = MutableStateFlow(true)
    val sessionLoading: StateFlow<Boolean> = _sessionLoading.asStateFlow()

    private val _errors = MutableSharedFlow<AppError>(extraBufferCapacity = 4)
    val errors: SharedFlow<AppError> = _errors.asSharedFlow()

    fun refreshSession() {
        viewModelScope.launch {
            _sessionLoading.value = true
            if (!ApiClient.hasSession()) {
                _session.value = null
                _sessionLoading.value = false
                return@launch
            }
            try {
                val me = ApiClient.api.me()
                _session.value = me.user
                if (me.user != null) refreshNotifications()
            } catch (t: Throwable) {
                // 401 = stale cookie → drop session; network errors keep nothing
                _session.value = null
            }
            _sessionLoading.value = false
        }
    }

    fun onLoginSuccess(user: SessionUser) {
        _session.value = user
        refreshNotifications()
    }

    fun logout() {
        viewModelScope.launch {
            runCatching { ApiClient.api.logout() }
            ApiClient.clearSession()
            _session.value = null
            _notifications.value = NotificationsResponse()
        }
    }

    // ── notifications badge ────────────────────────────────────────────
    private val _notifications = MutableStateFlow(NotificationsResponse())
    val notifications: StateFlow<NotificationsResponse> = _notifications.asStateFlow()

    fun refreshNotifications() {
        if (_session.value == null) return
        viewModelScope.launch {
            _notifications.value = runCatching { ApiClient.api.notifications() }
                .getOrDefault(_notifications.value)
        }
    }

    fun markAllRead() {
        val items = _notifications.value.items.filter { !it.read }.map { it.id }
        if (items.isEmpty()) return
        // website POST /api/notifications expects raw ids split by store:
        //   "notice:<id>" → notice_ids · "app:<id>" → app_ids  (ev:/bc: are read-only)
        val noticeIds = items.filter { it.startsWith("notice:") }.map { it.removePrefix("notice:") }
        val appIds = items.filter { it.startsWith("app:") }.map { it.removePrefix("app:") }
        if (noticeIds.isEmpty() && appIds.isEmpty()) return
        viewModelScope.launch {
            runCatching {
                ApiClient.api.markNotifications(
                    cfd.invokeil.himadri.rcy.core.network.MarkNotifsBody(
                        notice_ids = noticeIds.ifEmpty { null },
                        app_ids = appIds.ifEmpty { null },
                    )
                )
            }
            refreshNotifications()
        }
    }

    // ── LIVE emergency ticker + popup (site emergency-strip/popup) ────
    data class TickerState(
        val alerts: List<FeedAlert> = emptyList(),
        val urgentNotices: List<Notice> = emptyList(),
        val loading: Boolean = true,
        val offline: Boolean = false,
    )

    private val _ticker = MutableStateFlow(TickerState())
    val ticker: StateFlow<TickerState> = _ticker.asStateFlow()

    private val _popupAlert = MutableStateFlow<FeedAlert?>(null)
    val popupAlert: StateFlow<FeedAlert?> = _popupAlert.asStateFlow()

    fun dismissPopup() { _popupAlert.value = null }

    private var tickerJob: Job? = null

    fun startTickerPolling() {
        if (tickerJob != null) return
        tickerJob = viewModelScope.launch {
            while (isActive) {
                refreshTicker()
                delay(45_000L) // site polls the feed every ~45s
            }
        }
    }

    fun refreshTicker() {
        viewModelScope.launch {
            // 1) live feed (broadcasts + auto-detected disasters + requests)
            when (val r = repo.fetch(
                "feed", FeedAlert.serializer().let { ListSerializer(it) },
                call = { ApiClient.api.emergencyFeed().alerts },
            )) {
                is Resource.Fresh -> {
                    val alerts = r.data
                    _ticker.value = _ticker.value.copy(alerts = alerts, loading = false, offline = false)
                    maybePopup(alerts)
                }
                is Resource.Stale -> _ticker.value = _ticker.value.copy(alerts = r.data, loading = false, offline = true)
                is Resource.Failure -> {
                    _ticker.value = _ticker.value.copy(loading = false, offline = true)
                    _errors.tryEmit(r.error)
                }
            }
            // 2) urgent notices for the ticker strip (category emergency/blood)
            runCatching {
                val res = ApiClient.api.notices()
                val urgent = res.notices.filter {
                    it.severity == "critical" || it.severity == "high" ||
                        it.category == "emergency" || it.category == "blood"
                }
                _ticker.value = _ticker.value.copy(urgentNotices = urgent.take(8))
                runCatching {
                    repo.cache().put(
                        "urgent_notices",
                        appJson.encodeToString(NoticesResponse.serializer(), NoticesResponse(notices = _ticker.value.urgentNotices))
                    )
                }
            }
        }
    }

    private val seenPopupIds = mutableSetOf<String>()

    private fun maybePopup(alerts: List<FeedAlert>) {
        val latest = alerts.firstOrNull() ?: return
        if (latest.id in seenPopupIds) return
        seenPopupIds.add(latest.id)
        // popup only for fresh alerts (< 2h old), like the site's provider
        val fresh = runCatching {
            java.time.OffsetDateTime.parse(latest.created_at).toInstant()
                .isAfter(java.time.Instant.now().minus(java.time.Duration.ofHours(2)))
        }.getOrDefault(false)
        if (fresh && _session.value != null) _popupAlert.value = latest
    }

    // ── misc ───────────────────────────────────────────────────────────
    val cacheStore: CacheStore get() = repo.cache()

    class Factory(
        private val settingsStore: SettingsStore,
        private val cache: CacheStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AppViewModel(settingsStore, cache) as T
    }
}
