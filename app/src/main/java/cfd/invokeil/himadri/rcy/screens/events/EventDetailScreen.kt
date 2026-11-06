package cfd.invokeil.himadri.rcy.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.RsvpBody
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.util.AppError
import cfd.invokeil.himadri.rcy.core.util.DateFmt
import cfd.invokeil.himadri.rcy.core.util.ShareUtil
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.ErrorState
import cfd.invokeil.himadri.rcy.ui.components.ListSkeleton
import cfd.invokeil.himadri.rcy.ui.components.PillBadge
import cfd.invokeil.himadri.rcy.ui.components.ShareLinks
import cfd.invokeil.himadri.rcy.ui.components.ShareRow
import cfd.invokeil.himadri.rcy.ui.components.currentLang
import cfd.invokeil.himadri.rcy.ui.components.fullDate
import cfd.invokeil.himadri.rcy.ui.components.lp
import cfd.invokeil.himadri.rcy.ui.components.money
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Event detail — back row, status badge, title, full description, an
 * info card (date / location+map / deadline / volunteer hours / amount),
 * the payment CTA (routes to the fund detail screen) and the RSVP block
 * ("আমি যাবো" toggle, guests get a login card). ShareRow at the bottom.
 */
@Composable
fun EventDetailScreen(vm: AppViewModel, nav: NavHostController, id: String) {
    val state = rememberApiState("event-$id") {
        ApiClient.api.items("event").items.firstOrNull { it.id == id }
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val session by vm.session.collectAsState()
    val lang = currentLang()

    // RSVP toggle state: null = untouched, true = going, false = cancelled
    var going by remember(id) { mutableStateOf<Boolean?>(null) }
    var rsvpBusy by remember(id) { mutableStateOf(false) }
    var rsvpNeedsLogin by remember(id) { mutableStateOf(false) }
    var rsvpFailed by remember(id) { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // back row
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(AppIcon.of("arrow-left"), contentDescription = "back")
            }
        }

        when {
            state.loading -> ListSkeleton(3)
            state.error != null -> ErrorState(state.error) { state.retry() }
            else -> {
                val event = state.data
                if (event == null) {
                    ErrorState(AppError.Server) { state.retry() }
                } else {
                    PillBadge(eventStatusLabel(event.status), color = eventStatusColor(event.status))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        lp(event.title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    // postpone note — orange card like the site
                    if (event.status == "postponed" && lp(event.postpone_note).isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        PostponeNote(lp(event.postpone_note))
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(lp(event.description), style = MaterialTheme.typography.bodyLarge)

                    Spacer(Modifier.height(16.dp))

                    // info card
                    BrandCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            DetailInfoRow(AppIcon.of("calendar"), tl("ev_date"), fullDate(event.event_date))
                            val loc = event.location
                            if (!loc.isNullOrBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(
                                        AppIcon.of("map-pin"),
                                        contentDescription = null,
                                        tint = BrandRed,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            tl("ev_location"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            loc,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                    BrandButton(
                                        tl("ev_open_map"),
                                        {
                                            val link = event.map_link
                                            if (link.isNullOrBlank()) ShareUtil.openMap(context, loc)
                                            else ShareUtil.openUrl(context, link)
                                        },
                                        style = BrandBtnStyle.OUTLINE,
                                    )
                                }
                            }
                            if (!event.deadline.isNullOrBlank()) {
                                DetailInfoRow(AppIcon.of("clock"), tl("ev_deadline"), fullDate(event.deadline))
                            }
                            val hours = event.volunteer_hours ?: 0
                            if (hours > 0) {
                                DetailInfoRow(
                                    AppIcon.of("volunteer"),
                                    tl("ev_hours"),
                                    "${DateFmt.num(hours, lang)} ${mtl("hours_short")}",
                                )
                            }
                            if (event.amount > 0) {
                                DetailInfoRow(AppIcon.of("wallet"), tl("ev_amount"), money(event.amount))
                            }
                        }
                    }

                    // payment CTA — the fund detail screen handles the actual payment
                    if (event.payment_required && event.amount > 0) {
                        Spacer(Modifier.height(12.dp))
                        BrandButton(
                            "${tl("ev_pay")} · ${money(event.amount)}",
                            { nav.navigate(Routes.fund(event.id)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // ── RSVP block ─────────────────────────────────────
                    if (event.rsvp_enabled == true) {
                        Spacer(Modifier.height(16.dp))
                        if (session == null) {
                            // guest — login first
                            BrandCard(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                                    Text(
                                        tl("ev_rsvp_login"),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Spacer(Modifier.height(10.dp))
                                    BrandButton(
                                        tl("login_btn"),
                                        { nav.navigate("ext_login") },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        } else {
                            BrandCard(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                                    if (going == true) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            Icon(
                                                AppIcon.of("check"),
                                                contentDescription = null,
                                                tint = BrandGreen,
                                                modifier = Modifier.size(18.dp),
                                            )
                                            Text(
                                                mtl("rsvp_toggle_on"),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = BrandGreen,
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            tl("ev_going_done"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        BrandButton(
                                            mtl("rsvp_toggle_off"),
                                            {
                                                scope.launch {
                                                    rsvpBusy = true
                                                    runCatching {
                                                        ApiClient.api.rsvp(event.id, RsvpBody(going = false))
                                                    }
                                                        .onSuccess { r ->
                                                            if (r.ok) {
                                                                going = false
                                                                rsvpNeedsLogin = false
                                                                rsvpFailed = false
                                                            } else {
                                                                rsvpFailed = true
                                                            }
                                                        }
                                                        .onFailure { t ->
                                                            rsvpNeedsLogin = t is HttpException && t.code() == 401
                                                            if (!rsvpNeedsLogin) rsvpFailed = true
                                                        }
                                                    rsvpBusy = false
                                                }
                                            },
                                            style = BrandBtnStyle.OUTLINE,
                                            enabled = !rsvpBusy,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    } else {
                                        BrandButton(
                                            event.rsvp_label?.pick(lang)?.takeIf { it.isNotBlank() }
                                                ?: tl("ev_going_btn"),
                                            {
                                                scope.launch {
                                                    rsvpBusy = true
                                                    runCatching {
                                                        ApiClient.api.rsvp(event.id, RsvpBody(going = true))
                                                    }
                                                        .onSuccess { r ->
                                                            if (r.ok) {
                                                                going = true
                                                                rsvpNeedsLogin = false
                                                                rsvpFailed = false
                                                            } else {
                                                                rsvpFailed = true
                                                            }
                                                        }
                                                        .onFailure { t ->
                                                            rsvpNeedsLogin = t is HttpException && t.code() == 401
                                                            if (!rsvpNeedsLogin) rsvpFailed = true
                                                        }
                                                    rsvpBusy = false
                                                }
                                            },
                                            style = BrandBtnStyle.GREEN,
                                            enabled = !rsvpBusy,
                                            loading = rsvpBusy,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                    if (rsvpNeedsLogin) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            tl("ev_rsvp_login"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BrandRed,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    } else if (rsvpFailed) {
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            tl("error_generic"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BrandRed,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))
                    ShareRow(title = lp(event.title), url = ShareLinks.forEvent(event.id))
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

// ── small detail pieces ────────────────────────────────────────────────

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = BrandRed, modifier = Modifier.size(18.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PostponeNote(note: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFE85D04).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            AppIcon.of("clock"),
            contentDescription = null,
            tint = Color(0xFFE85D04),
            modifier = Modifier.size(18.dp),
        )
        Text(
            note,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF9A3412),
            fontWeight = FontWeight.Medium,
        )
    }
}
