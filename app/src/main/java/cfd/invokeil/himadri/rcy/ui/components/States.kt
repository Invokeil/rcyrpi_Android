package cfd.invokeil.himadri.rcy.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.util.AppError
import kotlinx.coroutines.launch

// ── Skeleton shimmer ───────────────────────────────────────────────────
@Composable
fun SkeletonBox(modifier: Modifier = Modifier, radius: Int = 12) {
    val alpha = rememberInfiniteTransition(label = "shimmer").animateFloat(
        initialValue = 0.35f, targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "a",
    )
    Box(
        modifier
            .alpha(alpha.value)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(radius.dp))
    )
}

@Composable
fun CardSkeleton() {
    BrandCard {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(Modifier.size(42.dp), radius = 21)
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    SkeletonBox(Modifier.fillMaxWidth(0.6f).height(14.dp))
                    Spacer(Modifier.height(6.dp))
                    SkeletonBox(Modifier.fillMaxWidth(0.4f).height(11.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            SkeletonBox(Modifier.fillMaxWidth().height(12.dp))
            Spacer(Modifier.height(6.dp))
            SkeletonBox(Modifier.fillMaxWidth(0.8f).height(12.dp))
        }
    }
}

@Composable
fun ListSkeleton(rows: Int = 3) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(rows) { CardSkeleton() }
    }
}

// ── Small centered spinner row ─────────────────────────────────────────
@Composable
fun LoadingRow(text: String = tl("loading")) {
    Row(
        Modifier.fillMaxWidth().padding(18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = BrandRed)
        Spacer(Modifier.size(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── EmptyState ─────────────────────────────────────────────────────────
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    tint: Color? = null,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 36.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(64.dp).background((tint ?: BrandGreen).copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = tint ?: BrandGreen, modifier = Modifier.size(30.dp)) }
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
            )
        }
    }
}

// ── ErrorState (network / server / cloudflare challenge) ──────────────
@Composable
fun ErrorState(
    error: AppError?,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 30.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(64.dp).background(BrandRed.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center,
        ) { Icon(AppIcon.of("alert-triangle"), null, tint = BrandRed, modifier = Modifier.size(30.dp)) }
        Spacer(Modifier.height(12.dp))
        Text(appErrorText(error), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (error == AppError.CloudflareChallenge) {
            Spacer(Modifier.height(4.dp))
            Text(
                cfd.invokeil.himadri.rcy.core.i18n.M.t(cfd.invokeil.himadri.rcy.LocalLang.current, "cf_challenge_hint"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        if (onRetry != null) {
            Spacer(Modifier.height(14.dp))
            BrandButton(cfd.invokeil.himadri.rcy.core.i18n.M.t(cfd.invokeil.himadri.rcy.LocalLang.current, "retry"), onRetry, style = BrandBtnStyle.OUTLINE, leading = AppIcon.of("refresh"))
        }
    }
}

// ── rememberApiState — simple network-state holder for screens ────────
class ApiState<T> internal constructor(
    initial: T?,
    private val fetcher: suspend () -> T,
) {
    var data by mutableStateOf(initial)
        private set
    var loading by mutableStateOf(true)
        private set
    var refreshing by mutableStateOf(false)
        private set
    var error by mutableStateOf<AppError?>(null)
        private set
    var offlineUsed by mutableStateOf(false)
        private set

    suspend fun load(showSpinner: Boolean = true) {
        if (showSpinner) loading = true else refreshing = true
        error = null
        try {
            data = fetcher()
            offlineUsed = false
        } catch (t: Throwable) {
            error = when (t) {
                is cfd.invokeil.himadri.rcy.core.network.CfChallengeException -> AppError.CloudflareChallenge
                is cfd.invokeil.himadri.rcy.core.network.NetworkUnavailableException -> AppError.Network
                is retrofit2.HttpException ->
                    if (t.code() == 401) AppError.Unauthorized else AppError.Server
                else -> AppError.Server
            }
        }
        loading = false
        refreshing = false
    }

    /** Mutate local data after an action (RSVP, mark-read…) without refetch. */
    internal fun setLocal(v: T) { data = v }

    // non-suspend retry wrapper for onClick callbacks
    private val retryScope = kotlinx.coroutines.CoroutineScope(
        kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main
    )

    fun retry() { retryScope.launch { load(showSpinner = false) } }
}

@Composable
fun <T> rememberApiState(key: Any?, initial: T? = null, fetcher: suspend () -> T): ApiState<T> {
    val state = remember(key) { ApiState(initial, fetcher) }
    androidx.compose.runtime.LaunchedEffect(key) { state.load() }
    return state
}
