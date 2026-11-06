package cfd.invokeil.himadri.rcy.screens.payments

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.CheckoutBody
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandGreenSoft
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.BrandRedSoft
import cfd.invokeil.himadri.rcy.core.theme.Chart3
import cfd.invokeil.himadri.rcy.core.util.ShareUtil
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.BrandGradientButton
import cfd.invokeil.himadri.rcy.ui.components.EmptyState
import cfd.invokeil.himadri.rcy.ui.components.ErrorState
import cfd.invokeil.himadri.rcy.ui.components.ListSkeleton
import cfd.invokeil.himadri.rcy.ui.components.PillBadge
import cfd.invokeil.himadri.rcy.ui.components.ShareLinks
import cfd.invokeil.himadri.rcy.ui.components.ShareRow
import cfd.invokeil.himadri.rcy.ui.components.errorText
import cfd.invokeil.himadri.rcy.ui.components.fullDate
import cfd.invokeil.himadri.rcy.ui.components.lp
import cfd.invokeil.himadri.rcy.ui.components.money
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Fund / gift detail + secure checkout — mirrors website pay-product-page.tsx.
 * Flow: POST api/payments/checkout → mock success screen or gateway URL.
 */
@Composable
fun FundDetailScreen(vm: AppViewModel, nav: NavHostController, id: String) {
    val context = LocalContext.current
    val session by vm.session.collectAsState()
    val scope = rememberCoroutineScope()

    val st = rememberApiState("fund_detail_$id") { ApiClient.api.items(null) }
    val item = st.data?.items?.firstOrNull { it.id == id && it.isFund }

    var paying by remember { mutableStateOf(false) }
    var payNote by remember { mutableStateOf<String?>(null) }
    var payError by remember { mutableStateOf<String?>(null) }

    // reset transient checkout state when navigating to another fund
    LaunchedEffect(id) { paying = false; payNote = null; payError = null }

    Box(Modifier.fillMaxSize()) {
        when {
            st.loading -> Column(Modifier.fillMaxSize().padding(16.dp)) {
                BackRow(nav)
                Spacer(Modifier.height(8.dp))
                ListSkeleton(2)
            }

            st.error != null -> Column(Modifier.fillMaxSize().padding(16.dp)) {
                BackRow(nav)
                ErrorState(st.error) { scope.launch { st.load() } }
            }

            item == null -> Column(
                Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BackRow(nav, tl("nav_payments"))
                EmptyState(AppIcon.of("alert-triangle"), tl("pay_not_found"), tint = BrandRed)
                BrandButton(tl("nav_payments"), { nav.navigate(Routes.PAYMENTS) { launchSingleTop = true } })
            }

            else -> Box(Modifier.fillMaxSize()) {
                // ── scrollable detail ─────────────────────────────────
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .padding(bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    BackRow(nav)

                    BrandCard(Modifier.fillMaxWidth()) {
                        Column {
                            // branded top band (site: thin brand-gradient strip)
                            Box(Modifier.fillMaxWidth().height(3.dp).background(BrandGradient))
                            Column(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(60.dp).background(BrandRedSoft, RoundedCornerShape(18.dp)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(AppIcon.of(item.icon), contentDescription = null, tint = BrandRed, modifier = Modifier.size(30.dp))
                                    }
                                    Spacer(Modifier.size(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            tl("pay_for"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold,
                                        )
                                        Text(
                                            lp(item.title),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                        )
                                    }
                                    if (!item.isActive) {
                                        if (item.status == "postponed") PillBadge(tl("ev_postponed"), Chart3)
                                        else PillBadge(tl("ev_cancelled"), BrandRed)
                                    }
                                }

                                Text(
                                    lp(item.description),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                // amount card
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        tl("ev_amount"),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        if (item.amount > 0) money(item.amount) else tl("ev_free"),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (item.amount > 0) BrandRed else BrandGreen,
                                    )
                                }

                                // meta rows
                                item.deadline?.let { MetaRow(AppIcon.of("clock"), tl("ev_deadline"), fullDate(it)) }
                                item.event_date?.let { MetaRow(AppIcon.of("calendar"), tl("ev_date"), fullDate(it)) }
                                item.location?.let { MetaRow(AppIcon.of("map-pin"), null, it) }

                                if (item.status == "postponed") {
                                    item.postpone_note?.let {
                                        Text(
                                            lp(it),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Chart3,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Chart3.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                                .padding(10.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    ShareRow(lp(item.title), ShareLinks.forEvent(id))
                }

                // ── sticky bottom CTA ─────────────────────────────────
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when {
                        !item.isActive -> {
                            BrandGradientButton(
                                tl("pay_paynow"),
                                {},
                                Modifier.fillMaxWidth(),
                                enabled = false,
                            )
                        }

                        item.amount <= 0.0 -> {
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
                                    tl("ev_free"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreen,
                                )
                            }
                        }

                        session == null -> {
                            Text(
                                tl("pay_login_first"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            BrandButton(tl("pay_login_btn"), { nav.navigate("ext_login") }, Modifier.fillMaxWidth())
                        }

                        else -> {
                            payError?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = BrandRed)
                            }
                            payNote?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = Chart3)
                            }
                            val payVerifyWaitMsg = tl("pay_verify_wait")
                            val payLoginFirstMsg = tl("pay_login_first")
                            BrandGradientButton(
                                if (paying) tl("pay_paying") else tl("pay_paynow"),
                                {
                                    payError = null
                                    payNote = null
                                    scope.launch {
                                        paying = true
                                        runCatching { ApiClient.api.checkout(CheckoutBody(item_id = id)) }
                                            .onSuccess { r ->
                                                paying = false
                                                if (r.mock == true) {
                                                    val tran = r.tran_id ?: ("MOCK-" + System.currentTimeMillis())
                                                    nav.navigate(
                                                        Routes.paySuccess(tran, id, item.amount.toString())
                                                    ) { launchSingleTop = true }
                                                } else {
                                                    val url = r.resolvedUrl
                                                    if (url != null) {
                                                        ShareUtil.openUrl(context, url)
                                                    } else {
                                                        payNote = payVerifyWaitMsg
                                                    }
                                                }
                                            }
                                            .onFailure { t ->
                                                paying = false
                                                payError = if (t is HttpException && t.code() == 401) {
                                                    payLoginFirstMsg
                                                } else {
                                                    (t.message ?: "")
                                                }
                                            }
                                    }
                                },
                                Modifier.fillMaxWidth(),
                                enabled = !paying,
                                loading = paying,
                            )
                        }
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            AppIcon.of("lock"),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            tl("pay_secure_note"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaRow(icon: ImageVector, label: String?, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = BrandRed, modifier = Modifier.size(16.dp))
        if (label != null) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun BackRow(nav: NavHostController, title: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { nav.popBackStack() }) {
            Icon(AppIcon.of("arrow-left"), contentDescription = null)
        }
        if (title != null) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
