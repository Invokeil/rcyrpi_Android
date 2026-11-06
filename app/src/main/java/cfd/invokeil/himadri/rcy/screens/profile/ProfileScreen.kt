package cfd.invokeil.himadri.rcy.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cfd.invokeil.himadri.rcy.LocalLang
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.SessionUser
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.Chart3
import cfd.invokeil.himadri.rcy.core.util.DateFmt
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.ErrorState
import cfd.invokeil.himadri.rcy.ui.components.ListSkeleton
import cfd.invokeil.himadri.rcy.ui.components.PillBadge
import cfd.invokeil.himadri.rcy.ui.components.SectionHeading
import cfd.invokeil.himadri.rcy.ui.components.fullDate
import cfd.invokeil.himadri.rcy.ui.components.initials
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.tl
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import kotlinx.coroutines.launch

/**
 * My Profile — mirrors website profile-page.tsx + profile-donor-kit.tsx:
 * gradient-strip header card (avatar, name, @username, role chip,
 * member no, member since), 2x2 quick actions, donor eligibility card,
 * badges row, donation log, account/logout and the admin zone card.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(vm: AppViewModel, nav: NavHostController) {
    val sessionUser = vm.session.collectAsState().value
    val scope = rememberCoroutineScope()
    val lang = LocalLang.current

    // ── guest gate ─────────────────────────────────────────────────────
    if (sessionUser == null) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(64.dp).background(BrandRed.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcon.of("lock"), null, tint = BrandRed, modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    tl("prof_login_gate"),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                BrandButton(tl("login_btn"), { nav.navigate("ext_login") }, leading = AppIcon.of("login"))
            }
        }
        return
    }

    val st = rememberApiState("profile_me") { ApiClient.api.profile() }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SectionHeading(title = tl("prof_title"), subtitle = tl("prof_sub"))

        when {
            st.loading -> ListSkeleton(3)
            st.error != null -> ErrorState(st.error) { scope.launch { st.load() } }
            else -> {
                val data = st.data
                val p = data?.profile ?: sessionUser
                val donations = data?.donations ?: emptyList()
                val el = data?.eligibility
                val verifiedCount = donations.count { it.verified }
                val totalUnits = donations.sumOf { it.units }

                // ── profile header card ────────────────────────────────
                BrandCard(Modifier.fillMaxWidth()) {
                    Column {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .background(BrandGradient),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AsyncImage(
                                    model = "file:///android_asset/logos/bdrcs.png",
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp),
                                    contentScale = ContentScale.Fit,
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    tl("brand"),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(56.dp)
                                        .background(BrandRed.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    val avatar = p.avatar_url
                                    if (avatar.isNullOrBlank()) {
                                        Text(
                                            p.initials(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = BrandRed,
                                        )
                                    } else {
                                        AsyncImage(
                                            model = avatar,
                                            contentDescription = null,
                                            modifier = Modifier.size(56.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop,
                                        )
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        p.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        "@" + p.username,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    PillBadge(
                                        text = if (p.isAdmin) tl("prof_role_admin") else tl("prof_role_member"),
                                        color = if (p.isAdmin) BrandRed else BrandGreen,
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            InfoRow(
                                label = tl("prof_member_no"),
                                value = p.member_no ?: "—",
                                mono = true,
                            )
                            InfoRow(
                                label = tl("prof_member_since"),
                                value = fullDate(p.created_at).ifBlank { "—" },
                            )
                        }
                    }
                }

                // ── quick actions 2x2 ──────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickAction(
                            label = tl("idcard_title"), icon = "id-card",
                            modifier = Modifier.weight(1f),
                        ) { nav.navigate(Routes.ID_CARD) }
                        QuickAction(
                            label = tl("my_certificates"), icon = "trophy",
                            modifier = Modifier.weight(1f),
                        ) { nav.navigate(Routes.CERTIFICATES) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        QuickAction(
                            label = tl("prof_title"), icon = "edit",
                            modifier = Modifier.weight(1f),
                        ) { nav.navigate(Routes.PROFILE_EDIT) }
                        QuickAction(
                            label = tl("prof_password"), icon = "lock",
                            modifier = Modifier.weight(1f),
                        ) { nav.navigate(Routes.CHANGE_PASSWORD) }
                    }
                }

                // ── donor eligibility card ─────────────────────────────
                BrandCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(AppIcon.of("blood"), null, tint = BrandRed, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                tl("elig_calc_title"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        if (el == null) {
                            Text(
                                tl("elig_never"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            if (el.eligible) {
                                StatusPill(
                                    text = tl("elig_result_ok"),
                                    icon = "check",
                                    color = BrandGreen,
                                )
                            } else {
                                StatusPill(
                                    text = tl("elig_wait"),
                                    icon = "hourglass",
                                    color = BrandRed,
                                )
                                Text(
                                    tl("elig_next") + ": " + fullDate(el.next_date).ifBlank { "—" } +
                                        " (" + DateFmt.num(el.days_left, lang) + " " + tl("elig_result_wait") + ")",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            InfoRow(
                                label = tl("elig_last"),
                                value = el.last_date?.let { fullDate(it) }?.takeIf { it.isNotBlank() }
                                    ?: tl("elig_never"),
                            )
                            InfoRow(
                                label = tl("lb_bloods"),
                                value = DateFmt.num(el.count, lang),
                            )
                            InfoRow(
                                label = tl("adm_units"),
                                value = DateFmt.num(totalUnits, lang),
                            )
                            InfoRow(
                                label = tl("adm_verify"),
                                value = DateFmt.num(verifiedCount, lang),
                            )
                        }
                    }
                }

                // ── badges row ─────────────────────────────────────────
                BrandCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(AppIcon.of("trophy"), null, tint = Chart3, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                tl("badges_title"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        val badgeKeys = buildList {
                            val count = el?.count ?: 0
                            if (count >= 1) add("badge_first_drop")
                            if (count >= 3) add("badge_rising_donor")
                            if (count >= 10) add("badge_life_saver")
                            if (count >= 2) add("badge_streak") // site heuristic: >=2 donations
                        }
                        if (badgeKeys.isEmpty()) {
                            Text(
                                tl("badge_none"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                badgeKeys.forEach { key -> PillBadge(text = tl(key), color = Chart3) }
                            }
                        }
                    }
                }

                // ── donation log ───────────────────────────────────────
                BrandCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(AppIcon.of("calendar"), null, tint = BrandRed, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                tl("adm_donations"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        if (donations.isEmpty()) {
                            Text(
                                tl("lb_empty"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        } else {
                            donations.forEach { d ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceContainerHigh,
                                            RoundedCornerShape(10.dp),
                                        )
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            fullDate(d.donated_on).ifBlank { d.donated_on },
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        if (!d.place.isNullOrBlank()) {
                                            Text(
                                                d.place.orEmpty(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                    Text(
                                        DateFmt.num(d.units, lang) + " " + tl("adm_units"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        AppIcon.of(if (d.verified) "verified" else "close"),
                                        contentDescription = null,
                                        tint = if (d.verified) BrandGreen else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // ── admin zone ─────────────────────────────────────────
                if (p.isAdmin) {
                    BrandCard(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(AppIcon.of("shield"), null, tint = BrandRed, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    tl("prof_admin_zone"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandRed,
                                )
                            }
                            Text(
                                tl("prof_admin_zone_hint"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                cfd.invokeil.himadri.rcy.core.i18n.mtl("web_only_note"),
                                style = MaterialTheme.typography.labelSmall,
                                color = BrandRed,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                // ── account / logout ───────────────────────────────────
                SectionHeading(title = tl("prof_account"))
                BrandCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(10.dp)) {
                        BrandButton(
                            text = tl("nav_logout"),
                            onClick = { vm.logout() },
                            style = BrandBtnStyle.GHOST,
                            leading = AppIcon.of("logout"),
                        )
                    }
                }
            }
        }
    }
}

// ── file-private helpers ───────────────────────────────────────────────

@Composable
private fun InfoRow(label: String, value: String, mono: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp),
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

@Composable
private fun StatusPill(text: String, icon: String, color: Color) {
    Row(
        Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppIcon.of(icon), null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    BrandCard(modifier = modifier, onClick = onClick) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(36.dp).background(BrandRed.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcon.of(icon), null, tint = BrandRed, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
