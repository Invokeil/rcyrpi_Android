package cfd.invokeil.himadri.rcy.core.network

import cfd.invokeil.himadri.rcy.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Thrown when Cloudflare's browser challenge blocks a native request
 * (rcy.invokeil.bond enables it for non-browser clients). The UI maps
 * this to a friendly "try the mirror" message.
 */
class CfChallengeException(message: String = "cloudflare_challenge") : Exception(message)

/** Thrown on network failures (offline, DNS, timeout) — no response body. */
class NetworkUnavailableException(cause: Throwable) : Exception("network_unavailable", cause)

/**
 * Singleton HTTP stack.
 *  - session cookie `rcy_session` persisted across launches (matches the
 *    website's HMAC-signed httpOnly cookie auth)
 *  - Cloudflare challenge sniffing → [CfChallengeException]
 *  - base URL switchable from Settings (main site ↔ pages.dev mirror)
 */
object ApiClient {

    const val MAIN_URL = BuildConfig.DEFAULT_SERVER_URL   // https://rcy.invokeil.bond
    const val MIRROR_URL = BuildConfig.MIRROR_SERVER_URL  // https://rcyrpi.pages.dev

    @Volatile
    var baseUrl: String = MAIN_URL
        private set

    lateinit var api: Api
        private set

    private lateinit var client: OkHttpClient
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        encodeDefaults = true
    }

    // cookie persistence — wired to DataStore by init()
    private var cookieSaver: (suspend (String?, String?) -> Unit)? = null
    private val cookies = ConcurrentHashMap<String, Cookie>() // host -> rcy_session cookie

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * @param persistedCookie previously stored `rcy_session` value (nullable)
     * @param persistedHost   host the cookie belongs to
     * @param onCookieChanged persistence callback: (cookieValue, host) — both null = cleared
     */
    fun init(
        persistedCookie: String?,
        persistedHost: String?,
        savedBaseUrl: String?,
        onCookieChanged: suspend (cookie: String?, host: String?) -> Unit,
    ) {
        cookieSaver = onCookieChanged
        if (!savedBaseUrl.isNullOrBlank()) baseUrl = savedBaseUrl.normalize()
        if (!persistedCookie.isNullOrBlank() && !persistedHost.isNullOrBlank()) {
            runCatching {
                Cookie.Builder().name("rcy_session").value(persistedCookie)
                    .domain(persistedHost).path("/").build()?.let { cookies[persistedHost] = it }
            }
        }

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        client = OkHttpClient.Builder()
            .cookieJar(SessionCookieJar)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .addInterceptor(ChallengeInterceptor)
            .addInterceptor(logging)
            .build()

        api = Retrofit.Builder()
            .baseUrl(baseUrl.ensureSlash())
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(Api::class.java)
    }

    /** Switch server (Settings). Clears the session cookie of the old host. */
    fun setBaseUrl(url: String) {
        val normalized = url.trim().normalize()
        if (normalized == baseUrl) return
        baseUrl = normalized
        cookies.clear()
        scope.launch { cookieSaver?.invoke(null, null) }
        if (this::api.isInitialized) rebuildRetrofit()
    }

    private fun rebuildRetrofit() {
        api = Retrofit.Builder()
            .baseUrl(baseUrl.ensureSlash())
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(Api::class.java)
    }

    val sessionCookieValue: String?
        get() = cookies.entries.firstOrNull()?.value?.value

    val sessionCookieHost: String?
        get() = cookies.entries.firstOrNull()?.key

    fun hasSession(): Boolean = cookies.isNotEmpty()

    fun clearSession() {
        cookies.clear()
        scope.launch { cookieSaver?.invoke(null, null) }
    }

    private object SessionCookieJar : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            for (c in cookies) {
                if (c.name == "rcy_session") {
                    ApiClient.cookies[url.host] = c
                    val host = url.host
                    val value = c.value
                    scope.launch { cookieSaver?.invoke(value, host) }
                }
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val c = ApiClient.cookies[url.host] ?: return emptyList()
            return if (c.matches(url)) listOf(c) else emptyList()
        }
    }

    private object ChallengeInterceptor : okhttp3.Interceptor {
        override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
            val request = chain.request()
            val response = try {
                chain.proceed(request)
            } catch (e: java.io.IOException) {
                throw NetworkUnavailableException(e)
            }
            if (response.code == 403 || response.code == 503) {
                val mitigated = response.header("cf-mitigated")?.contains("challenge", true) == true
                val peeked = runCatching { response.peekBody(1024).string() }.getOrDefault("")
                if (mitigated || peeked.contains("Just a moment", true) || peeked.contains("cf-challenge")) {
                    response.close()
                    throw CfChallengeException()
                }
            }
            return response
        }
    }

    private fun String.normalize(): String {
        val withScheme = if (startsWith("http")) this else "https://$this"
        return withScheme.trimEnd('/')
    }

    private fun String.ensureSlash() = if (endsWith("/")) this else "$this/"
}
