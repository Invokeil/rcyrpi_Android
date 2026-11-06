package cfd.invokeil.himadri.rcy.screens.payments

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import cfd.invokeil.himadri.rcy.LocalLang
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.Item
import cfd.invokeil.himadri.rcy.core.network.PaymentRecord
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.Chart3
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.EmptyState
import cfd.invokeil.himadri.rcy.ui.components.ErrorState
import cfd.invokeil.himadri.rcy.ui.components.ListSkeleton
import cfd.invokeil.himadri.rcy.ui.components.PillBadge
import cfd.invokeil.himadri.rcy.ui.components.fullDate
import cfd.invokeil.himadri.rcy.ui.components.lp
import cfd.invokeil.himadri.rcy.ui.components.money
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.relTime
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import kotlinx.coroutines.launch

/**
 * Payments & Funds — mirrors website payments-page.tsx.
 * Two tabs: Funds (donation/gift items) and My payments (member receipts).
 */
@Composable
fun PaymentsScreen(vm: AppViewModel, nav: NavHostController) {
    val lang = LocalLang.current
    val session by vm.session.collectAsState()
    val scope = rememberCoroutineScope()
    var tab by remember { mutableIntStateOf(0) } // 0 = Funds, 1 = My payments

    val funds = rememberApiState("payments_funds") { ApiClient.api.items(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ── header ────────────────────────────────────────────────────
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(56.dp).background(BrandGradient, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppIcon.of("wallet"), contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                tl("pay_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = BrandRed,
                textAlign = TextAlign.Center,
            )
            Text(
                tl("pay_sub"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        // ── tabs ──────────────────────────────────────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(14.dp))
                .padding(4.dp),
        ) {
            TabBtn(if (lang.isBn) "ফান্ড" else "Funds", tab == 0, Modifier.weight(1f)) { tab = 0 }
            TabBtn(tl("pay_receipt"), tab == 1, Modifier.weight(1f)) { tab = 1 }
        }

        if (tab == 0) {
            // ── Funds tab ─────────────────────────────────────────────
            when {
                funds.loading -> ListSkeleton(3)
                funds.error != null -> ErrorState(funds.error) { scope.launch { funds.load() } }
                else -> {
                    val items = funds.data?.items?.filter { it.isFund } ?: emptyList()
                    if (items.isEmpty()) {
                        EmptyState(
                            AppIcon.of("bell"),
                            if (lang.isBn) "এই ট্যাবে কোনো আইটেম নেই" else "No items in this tab yet",
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items.forEach { item ->
                                FundCard(item) { nav.navigate(Routes.fund(item.id)) }
                            }
                        }
                    }
                }
            }
        } else {
            // ── My payments tab (login-gated) ─────────────────────────
            if (session == null) {
                BrandCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier.size(56.dp).background(BrandRed.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(AppIcon.of("lock"), contentDescription = null, tint = BrandRed, modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            mtl("login_required_action"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(14.dp))
                        BrandButton(tl("login_btn")) { nav.navigate("ext_login") }
                    }
                }
            } else {
                val payments = rememberApiState("my_payments") { ApiClient.api.payments() }
                when {
                    payments.loading -> ListSkeleton(3)
                    payments.error != null -> ErrorState(payments.error) { scope.launch { payments.load() } }
                    else -> {
                        val rows = payments.data?.payments ?: emptyList()
                        if (rows.isEmpty()) {
                            EmptyState(AppIcon.of("wallet"), tl("paym_item_none"))
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                rows.forEach { p -> PaymentRow(p, lang) }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── segmented tab button ───────────────────────────────────────────────
@Composable
private fun TabBtn(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .background(
                if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) BrandRed else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── fund / gift card (Funds tab) ───────────────────────────────────────
@Composable
private fun FundCard(item: Item, onClick: () -> Unit) {
    BrandCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).background(iconTint(item).copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AppIcon.of(item.icon), contentDescription = null, tint = iconTint(item), modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        lp(item.title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        lp(item.description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                item.deadline?.let {
                    Row(
                        Modifier.background(BrandRed.copy(alpha = 0.08f), RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(AppIcon.of("calendar"), contentDescription = null, tint = BrandRed, modifier = Modifier.size(12.dp))
                        Text(fullDate(it), style = MaterialTheme.typography.labelSmall, color = BrandRed)
                    }
                }
                if (!item.isActive) {
                    if (item.status == "postponed") PillBadge(tl("ev_postponed"), Chart3)
                    else PillBadge(tl("ev_cancelled"), BrandRed)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (item.amount > 0) {
                    Text(
                        money(item.amount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = BrandRed,
                    )
                } else {
                    PillBadge(tl("ev_free"), BrandGreen)
                }
                Icon(AppIcon.of("chevron-down"), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
        }
    }
}

private fun iconTint(item: Item): Color = if (item.kind == "gift") Chart3 else BrandGreen

// ── payment receipt row (My payments tab) ──────────────────────────────
@Composable
private fun PaymentRow(p: PaymentRecord, lang: Lang) {
    BrandCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    AppIcon.of(if (p.method == "cash") "receipt" else "wallet"),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.size(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    p.item_title.pick(lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val sub = listOfNotNull(p.gateway_method, p.gateway_trxid).joinToString(" · ")
                if (sub.isNotBlank()) {
                    Text(
                        sub,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    relTime(p.created_at),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.size(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    money(p.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = BrandRed,
                )
                Spacer(Modifier.height(4.dp))
                PillBadge(payStatusLabel(p.status), payStatusColor(p.status))
            }
        }
    }
}

/** Site pay-status colors: success green, pending/due orange, failed red, refunded/cancelled gray. */
@Composable
private fun payStatusColor(status: String): Color = when (status) {
    "success" -> BrandGreen
    "pending", "submitted", "due" -> Chart3
    "failed" -> BrandRed
    else -> Color(0xFF8E8E96) // refunded / cancelled / unknown
}

@Composable
private fun payStatusLabel(status: String): String = when (status) {
    "success" -> tl("pay_success")
    "pending", "submitted" -> tl("pay_pending")
    "failed" -> tl("pay_failed")
    "refunded" -> tl("pay_refunded")
    "due" -> tl("paym_due")
    "cancelled" -> tl("paym_cancelled")
    else -> status
}
