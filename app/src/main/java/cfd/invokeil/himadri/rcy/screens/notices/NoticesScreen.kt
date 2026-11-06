package cfd.invokeil.himadri.rcy.screens.notices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.Notice
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.BrandField
import cfd.invokeil.himadri.rcy.ui.components.EmptyState
import cfd.invokeil.himadri.rcy.ui.components.ErrorState
import cfd.invokeil.himadri.rcy.ui.components.ListSkeleton
import cfd.invokeil.himadri.rcy.ui.components.PillBadge
import cfd.invokeil.himadri.rcy.ui.components.lp
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.relTime
import cfd.invokeil.himadri.rcy.ui.components.severityColor
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel

/**
 * Notices list — mirrors website src/components/pages/notices-page.tsx:
 * centered heading, search field, category filter chips and the
 * severity-colored notice cards (pinned chip + relative-time footer).
 * Emergency/critical notices carry a soft red tint like the site's
 * emergency zone cards.
 */
@Composable
fun NoticesScreen(vm: AppViewModel, nav: NavHostController) {
    val state = rememberApiState("notices") { ApiClient.api.notices(null) }
    var tab by remember { mutableStateOf("all") }
    var query by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        // heading — site: centered title + subtitle
        Text(
            tl("not_title"),
            style = MaterialTheme.typography.headlineSmall,
            color = BrandRed,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            tl("not_sub"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        // search
        BrandField(
            value = query,
            onValueChange = { query = it },
            label = tl("not_search"),
            leading = AppIcon.of("search"),
            placeholder = tl("not_search"),
        )

        Spacer(Modifier.height(12.dp))

        // category filter chips
        NoticeCategoryTabs(selected = tab, onSelect = { tab = it })

        Spacer(Modifier.height(16.dp))

        when {
            state.loading -> ListSkeleton(4)
            state.error != null -> ErrorState(state.error) { state.retry() }
            else -> {
                val q = query.trim().lowercase()
                val filtered = (state.data?.notices ?: emptyList()).filter { n ->
                    val catOk = tab == "all" || n.category == tab
                    catOk && (
                        q.isEmpty() ||
                            lp(n.title).lowercase().contains(q) ||
                            lp(n.body).lowercase().contains(q)
                        )
                }
                if (filtered.isEmpty()) {
                    EmptyState(AppIcon.of("file-text"), tl("not_title"), subtitle = tl("not_sub"))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filtered.forEach { n ->
                            NoticeCard(n) { nav.navigate(Routes.notice(n.id)) }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── category chips (site TABS row) ─────────────────────────────────────
@Composable
private fun NoticeCategoryTabs(selected: String, onSelect: (String) -> Unit) {
    val tabs = listOf(
        "all" to tl("not_tab_all"),
        "general" to tl("not_tab_general"),
        "emergency" to tl("not_tab_emergency"),
        "event" to tl("not_tab_event"),
        "blood" to tl("not_tab_blood"),
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tabs, key = { it.first }) { (value, label) ->
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BrandRed.copy(alpha = 0.12f),
                    selectedLabelColor = BrandRed,
                ),
            )
        }
    }
}

// ── notice card — severity bar + tint, pinned chip, footer ─────────────
@Composable
private fun NoticeCard(n: Notice, onClick: () -> Unit) {
    val sev = severityColor(n.severity)
    val urgent = n.severity == "critical" || n.severity == "high"
    BrandCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // severity color left border (site CAT_STYLE bar)
            Box(Modifier.width(5.dp).fillMaxHeight().background(sev))
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (urgent) BrandRed.copy(alpha = 0.04f) else Color.Transparent)
                    .padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PillBadge(noticeCategoryLabel(n.category), color = sev)
                    if (n.is_pinned) PinnedChip()
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    lp(n.title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (lp(n.body).isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        lp(n.body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        relTime(n.created_at),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    PillBadge(n.severity.uppercase(), color = sev)
                }
            }
        }
    }
}

/** Category value → localized tab label (shared with the detail screen). */
@Composable
internal fun noticeCategoryLabel(category: String): String = when (category) {
    "general" -> tl("not_tab_general")
    "emergency" -> tl("not_tab_emergency")
    "event" -> tl("not_tab_event")
    "blood" -> tl("not_tab_blood")
    else -> category
}

/** Amber "pinned" chip — site: bg-amber-100 / text-amber-800. */
@Composable
internal fun PinnedChip() {
    Row(
        Modifier
            .background(Color(0xFFB45309).copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            AppIcon.of("pin"),
            contentDescription = null,
            tint = Color(0xFFB45309),
            modifier = Modifier.size(12.dp),
        )
        Text(
            tl("not_pinned"),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFB45309),
        )
    }
}
