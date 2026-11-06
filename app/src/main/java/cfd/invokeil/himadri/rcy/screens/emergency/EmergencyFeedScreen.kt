package cfd.invokeil.himadri.rcy.screens.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.EmergencyRequest
import cfd.invokeil.himadri.rcy.core.network.FeedAlert
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandGreenSoft
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.Chart3
import cfd.invokeil.himadri.rcy.core.util.ShareUtil
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BlinkDot
import cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.EmptyState
import cfd.invokeil.himadri.rcy.ui.components.ErrorState
import cfd.invokeil.himadri.rcy.ui.components.LoadingRow
import cfd.invokeil.himadri.rcy.ui.components.PillBadge
import cfd.invokeil.himadri.rcy.ui.components.SectionHeading
import cfd.invokeil.himadri.rcy.ui.components.lp
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.relTime
import cfd.invokeil.himadri.rcy.ui.components.severityColor
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import kotlinx.coroutines.launch

/**
 * Emergency Notice Board — mirrors the website's live emergency strip/feed:
 * Section A: severity-colored LIVE alerts (one-tap Call/SMS/Map, member-gated),
 * Section B: open emergency requests with tap-to-call.
 */
@Composable
fun EmergencyFeedScreen(vm: AppViewModel, nav: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ticker by vm.ticker.collectAsState()
    val session by vm.session.collectAsState()

    val requests = rememberApiState("open_requests") { ApiClient.api.openRequests() }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ── header + refresh ──────────────────────────────────────────
        SectionHeading(
            title = tl("strip_title"),
            subtitle = tl("strip_sub"),
            action = {
                IconButton(onClick = { vm.refreshTicker() }) {
                    Icon(AppIcon.of("refresh"), contentDescription = mtl("pull_refresh"))
                }
            },
        )

        // ── Section A: LIVE alerts ────────────────────────────────────
        when {
            ticker.loading && ticker.alerts.isEmpty() -> LoadingRow()
            ticker.alerts.isEmpty() -> GreenStrip(tl("strip_empty"))
            else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ticker.alerts.forEach { alert -> AlertCard(alert, member = session != null) }
            }
        }

        // ── Section B: open requests ──────────────────────────────────
        SectionHeading(title = tl("req_title"))
        when {
            requests.loading -> LoadingRow()
            requests.error != null -> ErrorState(requests.error) { scope.launch { requests.load() } }
            else -> {
                val rows = requests.data?.requests ?: emptyList()
                if (rows.isEmpty()) {
                    EmptyState(AppIcon.of("check-circle"), tl("strip_empty"))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        rows.forEach { req -> RequestRow(req, member = session != null) }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── green all-clear strip ──────────────────────────────────────────────
@Composable
private fun GreenStrip(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(BrandGreenSoft, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppIcon.of("check-circle"), contentDescription = null, tint = BrandGreen, modifier = Modifier.size(18.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = BrandGreen,
        )
    }
}

// ── Section A: severity-colored alert card ─────────────────────────────
@Composable
private fun AlertCard(alert: FeedAlert, member: Boolean) {
    val context = LocalContext.current
    val color = severityColor(alert.severity)
    BrandCard(Modifier.fillMaxWidth()) {
        Column {
            Box(Modifier.fillMaxWidth().height(4.dp).background(color))
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (alert.severity == "critical") {
                        BlinkDot(color, 8)
                        Spacer(Modifier.size(8.dp))
                    }
                    Text(
                        lp(alert.title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        alert.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (lp(alert.body).isNotBlank()) {
                    Text(
                        lp(alert.body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        relTime(alert.created_at),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    alert.hospital?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                alert.location?.let { loc ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(AppIcon.of("map-pin"), contentDescription = null, tint = BrandRed, modifier = Modifier.size(12.dp))
                        Text(
                            loc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // one-tap emergency response (site: member-gated)
                val phone = alert.phone
                if (!phone.isNullOrBlank()) {
                    if (!member) {
                        Text(
                            mtl("login_required_action"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        val otapMsg = tl("otap_default_msg")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BrandButton(tl("otap_call"), { ShareUtil.dial(context, phone) })
                            BrandButton(
                                tl("otap_sms"),
                                { ShareUtil.sms(context, phone, otapMsg) },
                                style = BrandBtnStyle.OUTLINE,
                            )
                            alert.location?.let { loc ->
                                BrandButton(
                                    tl("otap_map"),
                                    { ShareUtil.openMap(context, loc) },
                                    style = BrandBtnStyle.OUTLINE,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Section B: open emergency request row ──────────────────────────────
@Composable
private fun RequestRow(req: EmergencyRequest, member: Boolean) {
    val context = LocalContext.current
    BrandCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).background(kindColor(req.kind).copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcon.of(kindIcon(req.kind)), contentDescription = null, tint = kindColor(req.kind), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.size(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        req.patient_name?.ifBlank { null } ?: (if (req.kind == "blood") mtl("patient") else req.kind),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        relTime(req.created_at),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                req.blood_group?.let { PillBadge(it, BrandRed) }
                Spacer(Modifier.size(6.dp))
                PillBadge(requestStatusLabel(req.status), requestStatusColor(req.status))
            }

            req.hospital?.let {
                MetaLine(AppIcon.of("hospital"), it)
            }
            MetaLine(AppIcon.of("map-pin"), GeoText(req))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PillBadge(urgLabel(req.urgency), Chart3)
            }

            // tap-to-call (member-gated, same as the site's one-tap actions)
            if (req.phone.isNotBlank()) {
                if (!member) {
                    Text(
                        mtl("login_required_action"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(10.dp))
                            .clickable { ShareUtil.dial(context, req.phone) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(AppIcon.of("phone"), contentDescription = null, tint = BrandRed, modifier = Modifier.size(14.dp))
                        Text(
                            req.phone,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = BrandRed,
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            tl("otap_call"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    if (text.isBlank()) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = BrandRed, modifier = Modifier.size(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GeoText(req: EmergencyRequest): String {
    val loc = req.location
    val detail = req.detail_location
    return if (detail.isNullOrBlank()) loc else "$loc · $detail"
}

@Composable
private fun requestStatusLabel(status: String): String = when (status) {
    "fulfilled" -> mtl("status_fulfilled")
    "cancelled" -> mtl("status_cancelled")
    else -> mtl("status_open")
}

private fun requestStatusColor(status: String): androidx.compose.ui.graphics.Color = when (status) {
    "fulfilled" -> BrandGreen
    "cancelled" -> androidx.compose.ui.graphics.Color(0xFF8E8E96)
    else -> BrandRed
}

private fun kindIcon(kind: String): String = when (kind) {
    "blood" -> "blood"
    "accident" -> "zap"
    "fire" -> "fire"
    "flood" -> "waves"
    "medical" -> "stethoscope"
    "missing" -> "search"
    else -> "help"
}

private fun kindColor(kind: String): androidx.compose.ui.graphics.Color = when (kind) {
    "blood", "fire" -> BrandRed
    "accident" -> Chart3
    else -> BrandGreen
}
