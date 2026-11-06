package cfd.invokeil.himadri.rcy.screens.leaderboard

// ============================================================
// Leaderboard — top donors / streaks / volunteer hours with
// filter tabs, medal styling, badges and the public donor
// eligibility calculator (mirrors website leaderboard-page.tsx
// + shared/eligibility-calculator.tsx).
// ============================================================

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import cfd.invokeil.himadri.rcy.core.i18n.S
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.LeaderRow
import cfd.invokeil.himadri.rcy.core.theme.BrandGreenDark
import cfd.invokeil.himadri.rcy.core.theme.BrandGreenSoft
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.BrandRedDark
import cfd.invokeil.himadri.rcy.core.theme.BrandRedSoft
import cfd.invokeil.himadri.rcy.core.util.DateFmt
import cfd.invokeil.himadri.rcy.core.util.EligibilityCalc
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.BrandField
import cfd.invokeil.himadri.rcy.ui.components.ChoiceChips
import cfd.invokeil.himadri.rcy.ui.components.EmptyState
import cfd.invokeil.himadri.rcy.ui.components.ErrorState
import cfd.invokeil.himadri.rcy.ui.components.ListSkeleton
import cfd.invokeil.himadri.rcy.ui.components.PillBadge
import cfd.invokeil.himadri.rcy.ui.components.SectionHeading
import cfd.invokeil.himadri.rcy.ui.components.currentLang
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

/** Leaderboard medal colors — 1 gold, 2 silver, 3 bronze */
private val MEDAL_COLORS = listOf(Color(0xFFF5A623), Color(0xFF9AA0A6), Color(0xFFB87333))

private fun medalColor(rank: Int): Color =
    if (rank in 1..3) MEDAL_COLORS[rank - 1] else Color(0xFF8A8A94)

private fun sortedRows(rows: List<LeaderRow>, tab: String): List<LeaderRow> = when (tab) {
    "streaks" -> rows.sortedByDescending { it.streak }
    "hours" -> rows.sortedByDescending { it.hours }
    else -> rows.sortedByDescending { it.blood_count }
}

private fun fmtLocalDate(d: LocalDate?, lang: Lang): String =
    d?.let { DateFmt.dayMonthYear(DateFmt.isoDate(it), lang) } ?: "—"

@Composable
fun LeaderboardScreen(vm: AppViewModel, nav: NavHostController) {
    val state = rememberApiState("leaderboard") { ApiClient.api.leaderboard().rows }
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("donors") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.padding(horizontal = 16.dp)) {
            SectionHeading(title = tl("lb_title"), subtitle = tl("lb_sub"))
        }

        // filter tabs — donors / streaks / hours
        ChoiceChips(
            options = listOf(
                "donors" to tl("lb_bloods"),
                "streaks" to tl("lb_streaks"),
                "hours" to tl("lb_hours"),
            ),
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        when {
            state.loading ->
                Box(Modifier.padding(horizontal = 16.dp)) { ListSkeleton(rows = 5) }
            state.error != null && state.data == null ->
                Box(Modifier.padding(horizontal = 16.dp)) {
                    ErrorState(state.error) { scope.launch { state.load() } }
                }
            else -> {
                val rows = state.data.orEmpty()
                if (rows.isEmpty()) {
                    EmptyState(
                        AppIcon.of("trophy"),
                        tl("lb_empty"),
                        Modifier.padding(horizontal = 16.dp),
                    )
                } else {
                    val sorted = remember(rows, tab) { sortedRows(rows, tab) }
                    Column(
                        Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        sorted.forEachIndexed { i, r -> LeaderRowCard(r, i + 1, tab) }
                    }
                }
            }
        }

        EligibilityCard(Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
    }
}

// ── one leaderboard row ────────────────────────────────────────────────

@Composable
private fun LeaderRowCard(r: LeaderRow, rank: Int, tab: String) {
    val medaled = rank in 1..3

    BrandCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                rank.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (medaled) medalColor(rank) else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(30.dp),
            )
            Spacer(Modifier.width(10.dp))
            AvatarCircle(r, medaled, rank)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    r.full_name.ifBlank { r.username },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "@${r.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!r.blood_group.isNullOrBlank()) {
                        Spacer(Modifier.width(6.dp))
                        PillBadge(r.blood_group, color = BrandRed)
                    }
                }
                if (r.badges.isEmpty()) {
                    PillBadge(tl("badge_none"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        r.badges.take(3).forEach { b ->
                            val key = "badge_${b.key}"
                            PillBadge(
                                if (key in S.keys) tl(key) else b.key,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            // the 3 stat values — selected tab metric highlighted
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatValue(r.blood_count, tl("lb_bloods"), tab == "donors")
                StatValue(r.streak, mtl("streak_short"), tab == "streaks")
                StatValue(r.hours, tl("lb_hours_short"), tab == "hours")
            }
        }
    }
}

@Composable
private fun StatValue(value: Int, label: String, highlighted: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            DateFmt.num(value, currentLang()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (highlighted) BrandRed else MaterialTheme.colorScheme.onSurface,
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun AvatarCircle(r: LeaderRow, medaled: Boolean, rank: Int) {
    val text: @Composable () -> Unit = {
        Box(
            Modifier
                .size(if (medaled) 40.dp else 44.dp)
                .background(BrandGradient, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                initialsOf(r),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
    if (medaled) {
        Box(
            Modifier
                .size(50.dp)
                .background(medalColor(rank).copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center,
        ) { text() }
    } else {
        text()
    }
}

private fun initialsOf(r: LeaderRow): String =
    r.full_name.split(" ").filter { it.isNotBlank() }.take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
        .ifBlank { r.username.take(2).uppercase() }

// ── donor eligibility calculator (public, client-side) ────────────────

@Composable
private fun EligibilityCard(modifier: Modifier = Modifier) {
    var dateInput by remember { mutableStateOf("") }
    var female by remember { mutableStateOf(false) }
    val lang = currentLang()

    val parsed = remember(dateInput) { DateFmt.toLocalDate(dateInput) }
    val result = if (parsed != null) {
        // male gap 120 days / female 180 days (site: female ? 180 : 120)
        EligibilityCalc.compute(listOf(dateInput.take(10)), if (female) 180 else 120)
    } else {
        null
    }

    BrandCard(modifier = modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(AppIcon.of("blood"), null, tint = BrandRed, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    tl("elig_calc_title"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Text(
                tl("elig_calc_desc"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BrandField(
                value = dateInput,
                onValueChange = { dateInput = it },
                label = tl("elig_date_label"),
                placeholder = "yyyy-mm-dd",
                isError = dateInput.isNotBlank() && parsed == null,
            )
            ChoiceChips(
                options = listOf(
                    "m" to tl("elig_male"),
                    "f" to tl("elig_female"),
                ),
                selected = if (female) "f" else "m",
                onSelect = { female = it == "f" },
            )

            when {
                dateInput.isBlank() -> Text(
                    tl("elig_never"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                result != null -> {
                    val bg = if (result.eligible) BrandGreenSoft else BrandRedSoft
                    val fg = if (result.eligible) BrandGreenDark else BrandRedDark
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(bg, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (result.eligible) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(AppIcon.of("check"), null, tint = fg, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    tl("elig_result_ok"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = fg,
                                )
                            }
                            Text(
                                tl("elig_ok"),
                                style = MaterialTheme.typography.bodySmall,
                                color = fg,
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(AppIcon.of("hourglass"), null, tint = fg, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    tl("elig_wait"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = fg,
                                )
                                Spacer(Modifier.width(8.dp))
                                PillBadge(
                                    "${DateFmt.num(result.daysLeft, lang)} ${tl("elig_result_wait")}",
                                    color = BrandRed,
                                )
                            }
                            Text(
                                "${tl("elig_next")}: ${fmtLocalDate(result.nextDate, lang)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = fg,
                            )
                        }
                        result.lastDate?.let {
                            Text(
                                "${tl("elig_last")}: ${fmtLocalDate(it, lang)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = fg.copy(alpha = 0.8f),
                            )
                        }
                    }
                }
            }
        }
    }
}
