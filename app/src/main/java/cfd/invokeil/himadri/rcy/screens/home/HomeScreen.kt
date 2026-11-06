package cfd.invokeil.himadri.rcy.screens.home

// ============================================================
// Home — mirrors the website's 10 home sections stacked in a
// single scroll column (hero, stats, emergency strip, activities,
// events, leaderboard, schedule, principles, about, join CTA).
// ============================================================

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import cfd.invokeil.himadri.rcy.core.i18n.LText
import cfd.invokeil.himadri.rcy.core.i18n.S
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.Item
import cfd.invokeil.himadri.rcy.core.network.ScheduleRow
import cfd.invokeil.himadri.rcy.core.network.Stats
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandGreenDark
import cfd.invokeil.himadri.rcy.core.theme.BrandGreenSoft
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.BrandRedSoft
import cfd.invokeil.himadri.rcy.core.util.DateFmt
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BlinkDot
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.BrandGradientButton
import cfd.invokeil.himadri.rcy.ui.components.EmptyState
import cfd.invokeil.himadri.rcy.ui.components.ErrorState
import cfd.invokeil.himadri.rcy.ui.components.ListSkeleton
import cfd.invokeil.himadri.rcy.ui.components.SectionHeading
import cfd.invokeil.himadri.rcy.ui.components.StatCard
import cfd.invokeil.himadri.rcy.ui.components.currentLang
import cfd.invokeil.himadri.rcy.ui.components.lp
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.relTime
import cfd.invokeil.himadri.rcy.ui.components.severityColor
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.launch

// ── static bilingual content (copied from website src/data/static.ts) ──

/** (icon, bnTitle, enTitle, bnDesc, enDesc, red?) — first 6 site ACTIVITIES */
private data class ActivityCell(
    val icon: String,
    val bnTitle: String,
    val enTitle: String,
    val bnDesc: String,
    val enDesc: String,
    val red: Boolean,
)

private val ACTIVITIES = listOf(
    ActivityCell(
        "droplets", "রক্তদান কর্মসূচি", "Blood Donation",
        "স্বেচ্ছাসেবী রক্তদান ক্যাম্প এবং ইমার্জেন্সির জন্য ২৪/৭ ডোনার পুল।",
        "Voluntary blood donation camps & 24/7 donor pool for emergencies.", true,
    ),
    ActivityCell(
        "stethoscope", "প্রাথমিক চিকিৎসা প্রশিক্ষণ", "First Aid Training",
        "সদস্য ও কমিউনিটির জন্য সার্টিফায়েড প্রাথমিক চিকিৎসা ও সিপিআর প্রশিক্ষণ।",
        "Certified first aid & CPR training for members and the community.", false,
    ),
    ActivityCell(
        "waves", "দুর্যোগ মোকাবিলা", "Disaster Response",
        "বন্যা, ঝড় ও শীতে ত্রাণ — উদ্ধার সহায়তা ও রিলিফ বিতরণ।",
        "Flood, storm & cold-wave relief — rescue support and relief distribution.", true,
    ),
    ActivityCell(
        "sun", "স্বাস্থ্যবিধি প্রচার", "Hygiene Promotion",
        "হ্যান্ডওয়াশ ক্যাম্পেইন, স্কুল ও কমিউনিটিতে স্যানিটেশন সচেতনতা।",
        "Handwash campaigns, sanitation awareness in schools & communities.", false,
    ),
    ActivityCell(
        "handshake", "কমিউনিটি সেবা", "Community Service",
        "শীতবস্ত্র বিতরণ, বয়স্ক আশ্রয়ণ পরিদর্শন, হাসপাতালের শিশুদের জন্য গিফট।",
        "Winter blanket drives, elderly home visits, gifts for hospitalized children.", true,
    ),
    ActivityCell(
        "trophy", "যুব নেতৃত্ব উন্নয়ন", "Youth Leadership",
        "বিতর্ক, কর্মশালা ও এক্সচেঞ্জ প্রোগ্রাম — আগামীর হিউম্যানিটেরিয়ান লিডার।",
        "Debates, workshops & exchange programs building tomorrow's humanitarian leaders.", false,
    ),
)

/** (icon, bn, en, bnDesc, enDesc) — the 7 fundamental principles */
private data class Principle(val icon: String, val bn: String, val en: String, val bnDesc: String, val enDesc: String)

private val PRINCIPLES = listOf(
    Principle("heart", "মানবতা", "Humanity", "যেখানেই মানুষের দুঃখ-কষ্ট, সেখানেই রক্ষা ও উপশমের চেষ্টা।", "Preventing and easing human suffering wherever it is found."),
    Principle("shield-check", "নিরপেক্ষতা", "Impartiality", "কোনো বৈষম্য নয় — প্রয়োজনই প্রথম, অন্য কিছু নয়।", "No discrimination — need comes first, nothing else."),
    Principle("eye", "স্বেচ্ছানিরপেক্ষতা", "Neutrality", "কোনো বিরোধ বা রাজনৈতিক বিতর্কে পক্ষ নেওয়া নয়।", "Taking no sides in hostilities or political disputes."),
    Principle("building", "স্বাধীনতা", "Independence", "মানবিক কাজে যেকোনো কর্তৃপক্ষ থেকে স্বাধীনতা।", "Autonomy from any authority in humanitarian work."),
    Principle("handshake", "স্বেচ্ছাসেবী সেবা", "Voluntary Service", "সেবাই আমাদের উদ্দেশ্য — কোনো লাভ নয়।", "Relief is our motive — not gain of any kind."),
    Principle("users", "ঐক্য", "Unity", "দেশে একটিই সোসাইটি — সবার জন্য উন্মুক্ত, সবাই এক হয়ে।", "One society per country — open to all, working as one."),
    Principle("globe", "সর্বজনীনতা", "Universality", "সমান মর্যাদা ও দায়িত্বের এক বিশ্বব্যাপী আন্দোলন।", "A worldwide movement with equal status and duties."),
)

/** Site weekly-schedule fallback (used while /api/schedule is empty/error). */
private val STATIC_SCHEDULE = listOf(
    ScheduleRow(day = "sat", time_text = "4:00 PM", activity = LText(en = "Unit General Meeting", bn = "ইউনিট সাধারণ সভা"), place = LText(en = "Unit Room", bn = "ইউনিট রুম"), icon = "users", sort = 0),
    ScheduleRow(day = "sun", time_text = "4:30 PM", activity = LText(en = "First Aid Practice", bn = "প্রাথমিক চিকিৎসা অনুশীলন"), place = LText(en = "Auditorium", bn = "অডিটোরিয়াম"), icon = "stethoscope", sort = 1),
    ScheduleRow(day = "mon", time_text = "5:00 PM", activity = LText(en = "Blood Grouping Desk", bn = "ব্লাড গ্রুপিং ডেস্ক"), place = LText(en = "Campus Gate", bn = "ক্যাম্পাস গেট"), icon = "droplets", sort = 2),
    ScheduleRow(day = "tue", time_text = "4:00 PM", activity = LText(en = "Disaster Drill", bn = "দুর্যোগ মহড়া"), place = LText(en = "Open Field", bn = "খোলা মাঠ"), icon = "siren", sort = 3),
    ScheduleRow(day = "wed", time_text = "4:30 PM", activity = LText(en = "Community Visit", bn = "কমিউনিটি পরিদর্শন"), place = LText(en = "Nearby Area", bn = "আশপাশের এলাকা"), icon = "handshake", sort = 4),
    ScheduleRow(day = "thu", time_text = "10:00 AM", activity = LText(en = "Training & Workshop", bn = "ট্রেনিং ও কর্মশালা"), place = LText(en = "Seminar Hall", bn = "সেমিনার হল"), icon = "school", sort = 5),
)

private val DAY_ORDER = listOf("sat", "sun", "mon", "tue", "wed", "thu")

/** Leaderboard medal colors — 1 gold, 2 silver, 3 bronze */
private val MEDAL_COLORS = listOf(Color(0xFFF5A623), Color(0xFF9AA0A6), Color(0xFFB87333))

private fun medalColor(rank: Int): Color =
    if (rank in 1..3) MEDAL_COLORS[rank - 1] else Color(0xFF8A8A94)

private fun dayOrder(day: String): Int = DAY_ORDER.indexOf(day).let { if (it >= 0) it else 99 }

private fun todayScheduleKey(): String? = when (LocalDate.now().dayOfWeek) {
    DayOfWeek.SATURDAY -> "sat"
    DayOfWeek.SUNDAY -> "sun"
    DayOfWeek.MONDAY -> "mon"
    DayOfWeek.TUESDAY -> "tue"
    DayOfWeek.WEDNESDAY -> "wed"
    DayOfWeek.THURSDAY -> "thu"
    else -> null
}

private fun dayLabel(day: String): String {
    val key = "day_$day"
    return if (key in S.keys) key else day
}

private fun initials(name: String, username: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
        .ifBlank { username.take(2).uppercase() }

// ────────────────────────────────────────────────────────────────────────

@Composable
fun HomeScreen(vm: AppViewModel, nav: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        HeroSection(nav)
        StatsSection()
        EmergencyStripSection(vm, nav)
        ActivitiesSection()
        EventsPreviewSection(nav)
        LeaderboardPreviewSection(nav)
        ScheduleSection()
        PrinciplesSection()
        AboutSection(nav)
        JoinCtaSection(nav)
        Spacer(Modifier.height(8.dp))
    }
}

// ── 1. Hero — brand-red gradient card ──────────────────────────────────

@Composable
private fun HeroSection(nav: NavHostController) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(BrandGradient, RoundedCornerShape(20.dp)),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // badge chip
            Row(
                Modifier
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(AppIcon.of("shield"), null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    tl("hero_badge"),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                tl("hero_title_1"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Text(
                tl("hero_title_2"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = BrandGreenSoft,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                tl("hero_desc"),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { nav.navigate(Routes.EMERGENCY_REQUEST) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BrandRed),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
            ) {
                Icon(AppIcon.of("siren"), null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(tl("hero_cta_blood"), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(10.dp))
            BrandGradientButton(
                text = tl("hero_cta_join"),
                onClick = { nav.navigate("ext_signup") },
                modifier = Modifier.fillMaxWidth(),
                green = true,
            )
        }
    }
}

// ── 2. Stats — 2x2 grid of live counters ───────────────────────────────

private data class StatCell(val icon: ImageVector, val value: String, val label: String, val tint: Color)

@Composable
private fun StatsSection() {
    val state = rememberApiState("home_stats") { ApiClient.api.publicStats().stats }
    val scope = rememberCoroutineScope()
    val lang = currentLang()

    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        when {
            state.loading -> ListSkeleton(rows = 2)
            state.error != null && state.data == null ->
                ErrorState(state.error) { scope.launch { state.load() } }
            else -> {
                val s = state.data ?: Stats()
                val cells = listOf(
                    StatCell(AppIcon.of("users"), DateFmt.num(s.active_members, lang) + "+", tl("stats_members"), BrandRed),
                    StatCell(AppIcon.of("calendar"), DateFmt.num(s.events_completed, lang) + "+", tl("stats_events"), BrandGreen),
                    StatCell(AppIcon.of("blood"), DateFmt.num(s.blood_bags, lang) + "+", tl("stats_blood"), BrandRed),
                    StatCell(AppIcon.of("clock"), DateFmt.num(s.volunteer_hours, lang) + "+", tl("stats_hours"), BrandGreen),
                )
                cells.chunked(2).forEach { pair ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { c ->
                            StatCard(
                                icon = c.icon,
                                value = c.value,
                                label = c.label,
                                tint = c.tint,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── 3. Emergency strip — LIVE urgent items (ticker + urgent notices) ──

private data class StripRow(val id: String, val title: LText, val severity: String, val time: String, val isNotice: Boolean)

@Composable
private fun EmergencyStripSection(vm: AppViewModel, nav: NavHostController) {
    val ticker by vm.ticker.collectAsState()
    val rows = remember(ticker) {
        (
            ticker.alerts.map { StripRow(it.id, it.title, it.severity, it.created_at, isNotice = false) } +
                ticker.urgentNotices.map { StripRow(it.id, it.title, it.severity, it.created_at, isNotice = true) }
            ).take(4)
    }

    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeading(title = tl("strip_title"), subtitle = tl("strip_sub"))

        when {
            ticker.loading && rows.isEmpty() -> ListSkeleton(rows = 2)
            rows.isEmpty() -> {
                // green "all safe" card (site: bg-brand-green-soft)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(BrandGreenSoft, RoundedCornerShape(16.dp))
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(AppIcon.of("check"), null, tint = BrandGreenDark, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        tl("strip_empty"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = BrandGreenDark,
                    )
                }
            }
            else -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(BrandRed.copy(alpha = 0.06f), RoundedCornerShape(16.dp))
                        .padding(10.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rows.forEach { r ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (r.isNotice) nav.navigate(Routes.notice(r.id)) else nav.navigate(Routes.EMERGENCY_FEED)
                                    }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                BlinkDot(severityColor(r.severity))
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        lp(r.title),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandRed,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        relTime(r.time),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            tl("strip_view_all"),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = BrandRed,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { nav.navigate(Routes.EMERGENCY_FEED) }
                .padding(vertical = 4.dp),
        )
    }
}

// ── 4. Activities — static bilingual 2-column grid ─────────────────────

@Composable
private fun ActivitiesSection() {
    val lang = currentLang()

    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeading(title = tl("act_title"), subtitle = tl("act_sub"))

        ACTIVITIES.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { a ->
                    val tint = if (a.red) BrandRed else BrandGreenDark
                    val soft = if (a.red) BrandRedSoft else BrandGreenSoft
                    BrandCard(Modifier.weight(1f)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Box(
                                Modifier.size(40.dp).background(soft, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(AppIcon.of(a.icon), null, tint = tint, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                if (lang == Lang.BN) a.bnTitle else a.enTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (lang == Lang.BN) a.bnDesc else a.enDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

// ── 5. Events preview — next 2 upcoming events ─────────────────────────

@Composable
private fun EventsPreviewSection(nav: NavHostController) {
    val state = rememberApiState("home_events") { ApiClient.api.items("event").items }
    val scope = rememberCoroutineScope()
    val lang = currentLang()

    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeading(title = tl("ev_title"), subtitle = tl("ev_sub"))

        when {
            state.loading -> ListSkeleton(rows = 2)
            state.error != null && state.data == null ->
                ErrorState(state.error) { scope.launch { state.load() } }
            else -> {
                val events = state.data.orEmpty()
                    .filter { it.isActive }
                    .sortedBy { it.event_date ?: it.deadline ?: it.created_at }
                    .take(2)
                if (events.isEmpty()) {
                    EmptyState(AppIcon.of("calendar"), tl("ev_no_events"))
                } else {
                    events.forEach { item -> EventPreviewCard(item, lang, nav) }
                }
            }
        }

        Text(
            mtl("see_all"),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = BrandRed,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { nav.navigate(Routes.EVENTS) }
                .padding(vertical = 4.dp),
        )
    }
}

@Composable
private fun EventPreviewCard(item: Item, lang: Lang, nav: NavHostController) {
    val dateText = DateFmt.dayNameDate(item.event_date ?: item.deadline, lang)
    val loc = item.location

    BrandCard(modifier = Modifier.fillMaxWidth(), onClick = { nav.navigate(Routes.event(item.id)) }) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (dateText.isNotBlank()) {
                Column(
                    Modifier.background(BrandRedSoft, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        dateText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandRed,
                    )
                }
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    lp(item.title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!loc.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            AppIcon.of("map-pin"), null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            loc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

// ── 6. Leaderboard preview — top 3 donors ──────────────────────────────

@Composable
private fun LeaderboardPreviewSection(nav: NavHostController) {
    val state = rememberApiState("home_leaderboard") { ApiClient.api.leaderboard().rows }
    val scope = rememberCoroutineScope()
    val lang = currentLang()

    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeading(title = tl("lb_title"))

        when {
            state.loading -> ListSkeleton(rows = 3)
            state.error != null && state.data == null ->
                ErrorState(state.error) { scope.launch { state.load() } }
            else -> {
                val top = state.data.orEmpty().sortedByDescending { it.blood_count }.take(3)
                if (top.isEmpty()) {
                    EmptyState(AppIcon.of("trophy"), tl("lb_empty"))
                } else {
                    top.forEachIndexed { i, r ->
                        BrandCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    (i + 1).toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = medalColor(i + 1),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(26.dp),
                                )
                                Spacer(Modifier.width(10.dp))
                                Box(
                                    Modifier.size(42.dp).background(BrandGradient, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        initials(r.full_name, r.username),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        r.full_name.ifBlank { r.username },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        "${DateFmt.num(r.blood_count, lang)} ${tl("lb_bloods")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    DateFmt.num(r.blood_count, lang),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandRed,
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            tl("lb_view_all"),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = BrandRed,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { nav.navigate(Routes.LEADERBOARD) }
                .padding(vertical = 4.dp),
        )
    }
}

// ── 7. Weekly schedule — API rows grouped by day, static fallback ─────

@Composable
private fun ScheduleSection() {
    val state = rememberApiState("home_schedule") { ApiClient.api.schedule().schedule.filter { it.active } }

    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeading(title = tl("sch_title"), subtitle = tl("sch_sub"))

        when {
            state.loading -> ListSkeleton(rows = 4)
            state.error != null && state.data == null -> ScheduleList(STATIC_SCHEDULE)
            else -> {
                val rows = state.data.orEmpty()
                    .sortedWith(compareBy({ dayOrder(it.day) }, { it.sort }))
                    .ifEmpty { STATIC_SCHEDULE }
                ScheduleList(rows)
            }
        }
    }
}

@Composable
private fun ScheduleList(rows: List<ScheduleRow>) {
    val today = remember { todayScheduleKey() }
    BrandCard(Modifier.fillMaxWidth()) {
        Column {
            rows.forEachIndexed { i, row ->
                if (i > 0) HorizontalDivider()
                ScheduleRowItem(row, today)
            }
        }
    }
}

@Composable
private fun ScheduleRowItem(row: ScheduleRow, today: String?) {
    val isToday = row.day == today
    val label = dayLabel(row.day)

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .background(if (isToday) BrandRed else BrandGreenSoft, RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                tl(label),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isToday) Color.White else BrandGreenDark,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    AppIcon.of("clock"), null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    row.time_text,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(26.dp).background(if (isToday) BrandRed else BrandRedSoft, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        AppIcon.of(row.icon), null,
                        tint = if (isToday) Color.White else BrandRed,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    lp(row.activity),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    AppIcon.of("map-pin"), null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    lp(row.place),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ── 8. The 7 fundamental principles ────────────────────────────────────

@Composable
private fun PrinciplesSection() {
    val lang = currentLang()

    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeading(title = tl("pr_title"), subtitle = tl("pr_sub"))

        PRINCIPLES.mapIndexed { i, p -> i to p }.chunked(2).forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { (index, p) ->
                    val cellModifier = if (pair.size == 1) Modifier.fillMaxWidth() else Modifier.weight(1f)
                    BrandCard(cellModifier) {
                        Box(Modifier.fillMaxWidth()) {
                            Text(
                                "%02d".format(index + 1),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                modifier = Modifier.align(Alignment.TopEnd),
                            )
                            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                                Box(
                                    Modifier.size(38.dp).background(BrandRedSoft, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(AppIcon.of(p.icon), null, tint = BrandRed, modifier = Modifier.size(19.dp))
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    if (lang == Lang.BN) p.bn else p.en,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (lang == Lang.BN) p.bnDesc else p.enDesc,
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
}

// ── 9. About — history + decorative brand card ─────────────────────────

@Composable
private fun AboutSection(nav: NavHostController) {
    val lang = currentLang()

    Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SectionHeading(title = tl("about_title"))
        Text(
            tl("about_body1"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            tl("about_body2"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            Modifier
                .background(BrandGreenSoft, RoundedCornerShape(999.dp))
                .clickable { nav.navigate(Routes.TERMS) }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(AppIcon.of("link"), null, tint = BrandGreenDark, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                tl("about_learn"),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = BrandGreenDark,
            )
        }

        // decorative brand-gradient card (site: right column of the About grid)
        Box(
            Modifier
                .fillMaxWidth()
                .background(BrandGradient, RoundedCornerShape(20.dp)),
        ) {
            Column(
                Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    Modifier.size(64.dp).background(Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcon.of("heart-handshake"), null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    if (lang == Lang.BN) "১৯৭৩ সাল থেকে" else "SINCE 1973",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 2.sp,
                )
                Text(
                    "BDRCS",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                )
                Text(
                    if (lang == Lang.BN) "বাংলাদেশ রেড ক্রিসেন্ট সোসাইটি" else "Bangladesh Red Crescent Society",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val chips = if (lang == Lang.BN) {
                        listOf("১২০+ সদস্য", "৪৫+ ইভেন্ট", "প্রাথমিক চিকিৎসা")
                    } else {
                        listOf("120+ Members", "45+ Events", "First Aid")
                    }
                    chips.forEach { c ->
                        Box(
                            Modifier
                                .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Text(
                                c,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── 10. Join CTA — green gradient card ─────────────────────────────────

@Composable
private fun JoinCtaSection(nav: NavHostController) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                Brush.verticalGradient(listOf(BrandGreenSoft, Color.White)),
                RoundedCornerShape(20.dp),
            )
            .border(1.dp, BrandGreen.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(52.dp).background(Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcon.of("handshake"), null, tint = BrandGreen, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                tl("join_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = BrandGreenDark,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                tl("join_sub"),
                style = MaterialTheme.typography.bodyMedium,
                color = BrandGreenDark.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            BrandGradientButton(
                text = tl("join_btn"),
                onClick = { nav.navigate("ext_signup") },
                modifier = Modifier.fillMaxWidth(),
                green = true,
            )
        }
    }
}
