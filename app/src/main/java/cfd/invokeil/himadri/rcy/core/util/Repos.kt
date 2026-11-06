package cfd.invokeil.himadri.rcy.core.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository pattern: network-first with offline fallback.
 *  - online  → fetch, cache JSON snapshot, return fresh data + source flag
 *  - offline → parse cached snapshot, return stale data
 */
sealed class Resource<out T> {
    data class Fresh<T>(val data: T) : Resource<T>()
    data class Stale<T>(val data: T) : Resource<T>()
    data class Failure(val error: AppError) : Resource<Nothing>()
}

/** Normalized error surfaced to the UI. */
sealed class AppError(open val key: String) {
    data object Network : AppError("network_unavailable")
    data object CloudflareChallenge : AppError("cloudflare_challenge")
    data object Unauthorized : AppError("login_required")
    data object Server : AppError("server_error")
    data class Api(override val key: String) : AppError(key)
}

val appJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
}

class Repo(private val cache: cfd.invokeil.himadri.rcy.core.storage.CacheStore) {

    /** Exposed cache (AppViewModel writes ad-hoc snapshots like urgent_notices). */
    fun cache(): cfd.invokeil.himadri.rcy.core.storage.CacheStore = cache

    private fun errorOf(t: Throwable): AppError = when (t) {
        is cfd.invokeil.himadri.rcy.core.network.CfChallengeException -> AppError.CloudflareChallenge
        is cfd.invokeil.himadri.rcy.core.network.NetworkUnavailableException -> AppError.Network
        is retrofit2.HttpException -> when (t.code()) {
            401 -> AppError.Unauthorized
            else -> AppError.Server
        }
        else -> AppError.Server
    }

    /** Network-first fetch with offline cache fallback. */
    suspend fun <T> fetch(
        key: String,
        serializer: KSerializer<T>,
        call: suspend () -> T,
        takeIfValid: (T) -> Boolean = { true },
    ): Resource<T> = try {
        val fresh = call()
        if (takeIfValid(fresh)) {
            cache.put(key, appJson.encodeToString(serializer, fresh))
            Resource.Fresh(fresh)
        } else {
            staleOrFailure(key, serializer) ?: Resource.Failure(AppError.Server)
        }
    } catch (t: Throwable) {
        staleOrFailure(key, serializer) ?: Resource.Failure(errorOf(t))
    }

    private suspend fun <T> staleOrFailure(
        key: String,
        serializer: KSerializer<T>,
    ): Resource<T>? = cache.get(key) { appJson.decodeFromString(serializer, it) }?.let { Resource.Stale(it) }

    /** Cache-first read (instant render), then refresh in background. */
    suspend fun <T> cachedFirst(
        key: String,
        serializer: KSerializer<T>,
    ): T? = cache.get(key) { appJson.decodeFromString(serializer, it) }
}
