package cfd.invokeil.himadri.rcy.screens.misc

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.core.network.VerifyResult
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.util.ShareUtil
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle
import cfd.invokeil.himadri.rcy.ui.components.BrandField
import cfd.invokeil.himadri.rcy.ui.components.LoadingRow
import cfd.invokeil.himadri.rcy.ui.components.SectionHeading
import cfd.invokeil.himadri.rcy.ui.components.ShareLinks
import cfd.invokeil.himadri.rcy.ui.components.errorText
import cfd.invokeil.himadri.rcy.ui.components.fullDate
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import kotlinx.coroutines.launch

/**
 * Public member verification — mirrors website verify-page.tsx.
 * No login required: enter (or deep-link) a member code, GET /api/verify/{code}
 * → green verified card (name, @username, member no, blood, team, member
 * since) / red not-found or not-verified card. Plus an "open in browser"
 * link to the website's verify page.
 */
@Composable
fun VerifyScreen(vm: AppViewModel, nav: NavHostController, code: String?) {
    var query by remember { mutableStateOf(code.orEmpty()) }
    var result by remember { mutableStateOf<VerifyResult?>(null) }
    var notFound by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun search(q: String) {
        val c = q.trim()
        if (c.isEmpty()) return
        loading = true
        notFound = false
        result = null
        err = null
        scope.launch {
            runCatching { ApiClient.api.verify(c) }
                .onSuccess { r -> result = r }
                .onFailure { e ->
                    if (e is retrofit2.HttpException && e.code() == 404) notFound = true
                    else err = if (e is retrofit2.HttpException) "http_${e.code()}"
                    else (e.message ?: "network_error")
                }
            loading = false
        }
    }

    // deep link / prefilled code → auto-search once
    LaunchedEffect(code) {
        if (!code.isNullOrBlank()) search(code)
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(AppIcon.of("arrow-left"), contentDescription = null)
            }
            SectionHeading(
                title = tl("verify_title"),
                subtitle = tl("brand_sub"),
                modifier = Modifier.weight(1f),
            )
        }

        BrandField(
            value = query,
            onValueChange = { query = it },
            label = tl("idcard_no"),
            leading = AppIcon.of("search"),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BrandButton(
                text = tl("adm_verify"),
                onClick = { search(query) },
                loading = loading,
                leading = AppIcon.of("search"),
            )
            BrandButton(
                text = mtl("open_web"),
                onClick = {
                    if (query.isNotBlank()) {
                        ShareUtil.openUrl(context, ShareLinks.forVerify(query.trim()))
                    }
                },
                style = BrandBtnStyle.GHOST,
                leading = AppIcon.of("globe"),
            )
        }

        if (err != null) {
            Text(
                errorText(err),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )
        }

        when {
            loading -> LoadingRow(tl("loading"))
            notFound -> ResultCard(
                text = tl("verify_notfound"),
                icon = "close",
                color = BrandRed,
            )
            result != null -> {
                val r: VerifyResult = result ?: VerifyResult()
                val ok = r.verified && r.status == "active"
                if (ok) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandGreen.copy(alpha = 0.12f))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                AppIcon.of("verified"),
                                contentDescription = null,
                                tint = BrandGreen,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                tl("verify_ok"),
                                color = BrandGreen,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            r.full_name ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "@" + (r.username ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        InfoRow(tl("idcard_blood"), r.blood_group ?: "—")
                        InfoRow(tl("idcard_no"), r.member_no ?: "—", mono = true)
                        if (!r.team.isNullOrBlank()) {
                            InfoRow(tl("idcard_team"), r.team.orEmpty())
                        }
                        InfoRow(tl("verify_since"), fullDate(r.member_since).ifBlank { "—" })
                    }
                } else {
                    ResultCard(
                        text = tl("verify_bad"),
                        icon = "close",
                        color = BrandRed,
                    )
                }
            }
        }
    }
}

// ── file-private helpers ───────────────────────────────────────────────

@Composable
private fun ResultCard(text: String, icon: String, color: Color) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppIcon.of(icon), contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String, mono: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(110.dp),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            fontFamily = if (mono) FontFamily.Monospace else null,
            modifier = Modifier.weight(1f),
        )
    }
}
