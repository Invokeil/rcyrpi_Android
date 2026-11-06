package cfd.invokeil.himadri.rcy.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.Item
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.util.DateFmt
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.EmptyState
import cfd.invokeil.himadri.rcy.ui.components.ErrorState
import cfd.invokeil.himadri.rcy.ui.components.ListSkeleton
import cfd.invokeil.himadri.rcy.ui.components.PillBadge
import cfd.invokeil.himadri.rcy.ui.components.SectionHeading
import cfd.invokeil.himadri.rcy.ui.components.currentLang
import cfd.invokeil.himadri.rcy.ui.components.lp
import cfd.invokeil.himadri.rcy.ui.components.money
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel

/**
 * Events list — mirrors website src/components/pages/events-page.tsx
 * (list part; the month calendar is a desktop nicety kept web-only):
 * heading, upcoming/past tabs and event cards with a date block,
 * status badge, location, fee / volunteer-hours / going chips.
 */
@Composable
fun EventsScreen(vm: AppViewModel, nav: NavHostController) {
    val state = rememberApiState("events") { ApiClient.api.items("event") }
    var tab by remember { mutableStateOf("upcoming") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        SectionHeading(title = tl("ev_title"), subtitle = tl("ev_sub"))

        Spacer(Modifier.height(14.dp))

        // upcoming / past tabs
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EventTabChip(tl("ev_upcoming"), selected = tab == "upcoming") { tab = "upcoming" }
            EventTabChip(tl("ev_past"), selected = tab == "past") { tab = "past" }
        }

        Spacer(Modifier.height(14.dp))

        when {
            state.loading -> ListSkeleton(3)
            state.error != null -> ErrorState(state.error) { state.retry() }
            else -> {
                val items = state.data?.items ?: emptyList()
                val upcoming = items.filter { it.status == "active" }
                val past = items.filter {
                    it.status == "completed" || it.status == "postponed" || it.status == "cancelled"
                }
                val list = if (tab == "upcoming") upcoming else past
                if (list.isEmpty()) {
                    EmptyState(AppIcon.of("calendar"), tl("ev_no_events"))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        list.forEach { item ->
                            EventCard(item) { nav.navigate(Routes.event(item.id)) }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EventTabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BrandRed.copy(alpha = 0.12f),
            selectedLabelColor = BrandRed,
        ),
    )
}

// ── event card ─────────────────────────────────────────────────────────
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EventCard(item: Item, onClick: () -> Unit) {
    val lang = currentLang()
    val date = DateFmt.parse(item.event_date)
    BrandCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // left date block — big day number + short month
            Column(
                Modifier
                    .width(58.dp)
                    .background(BrandRed.copy(alpha = 0.07f), RoundedCornerShape(10.dp))
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (date != null) {
                    Text(
                        DateFmt.num(date.dayOfMonth, lang),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BrandRed,
                    )
                    Text(
                        monthShort(date.monthValue, lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Icon(
                        AppIcon.of("calendar"),
                        contentDescription = null,
                        tint = BrandRed,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Column(Modifier.weight(1f)) {
                Text(
                    lp(item.title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                PillBadge(eventStatusLabel(item.status), color = eventStatusColor(item.status))
                item.location?.takeIf { it.isNotBlank() }?.let { loc ->
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            AppIcon.of("map-pin"),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            loc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // fee — amount or free
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            AppIcon.of("wallet"),
                            contentDescription = null,
                            tint = if (item.amount > 0) BrandRed else BrandGreen,
                            modifier = Modifier.size(13.dp),
                        )
                        Text(
                            if (item.amount > 0) money(item.amount) else tl("ev_free"),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.amount > 0) BrandRed else BrandGreen,
                        )
                    }
                    val hours = item.volunteer_hours ?: 0
                    if (hours > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                AppIcon.of("clock"),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                "${DateFmt.num(hours, lang)} ${tl("ev_hours")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (item.rsvp_enabled == true) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                AppIcon.of("group"),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                tl("ev_going_count"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── status helpers (shared with the detail screen) ─────────────────────

/** Dictionary has no `ev_open` / `ev_completed` — closest keys used. */
@Composable
internal fun eventStatusLabel(status: String): String = when (status) {
    "active" -> mtl("status_open")
    "completed" -> tl("ev_done")
    "postponed" -> tl("ev_postponed")
    "cancelled" -> tl("ev_cancelled")
    else -> status
}

/** active green / cancelled red / postponed orange / completed gray. */
internal fun eventStatusColor(status: String): Color = when (status) {
    "active" -> BrandGreen
    "cancelled" -> BrandRed
    "postponed" -> Color(0xFFE85D04)
    else -> Color(0xFF6B7280)
}

private fun monthShort(month: Int, lang: Lang): String =
    if (lang == Lang.BN) DateFmt.BN_MONTHS[month - 1] else DateFmt.EN_MONTHS_SHORT[month - 1]
