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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.LocalLang
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandGreenSoft
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.Chart3
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.ui.components.money
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel

/**
 * Payment success — mirrors website payment-success-page.tsx:
 * celebration check, receipt card, verify-wait note, actions.
 */
@Composable
fun PaymentSuccessScreen(vm: AppViewModel, nav: NavHostController, tran: String, item: String, amount: String) {
    val lang = LocalLang.current
    val amountValue = amount.toDoubleOrNull() ?: 0.0

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(32.dp))

        // ── big green check ───────────────────────────────────────────
        Box(
            Modifier.size(120.dp).background(BrandGreenSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                AppIcon.of("check-circle"),
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.size(72.dp),
            )
        }

        // ── thanks ────────────────────────────────────────────────────
        Text(
            tl("pay_success"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = BrandGreen,
            textAlign = TextAlign.Center,
        )
        Text(
            tl("pay_thanks_msg"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // ── receipt card ──────────────────────────────────────────────
        BrandCard(Modifier.fillMaxWidth()) {
            Column {
                Box(Modifier.fillMaxWidth().height(4.dp).background(BrandGreen))
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        tl("pay_receipt"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    ReceiptRow(tl("pay_txn")) {
                        Text(
                            tran,
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    ReceiptRow(tl("pay_reference")) {
                        Text(
                            tran,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    ReceiptRow(if (lang.isBn) "আইটেম" else "Item") {
                        Text(item, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                    ReceiptRow(tl("ev_amount")) {
                        Text(
                            money(amountValue),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandRed,
                        )
                    }
                }
            }
        }

        // ── verify-wait note ──────────────────────────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .background(Chart3.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(AppIcon.of("clock"), contentDescription = null, tint = Chart3, modifier = Modifier.size(18.dp))
            Text(
                tl("pay_verify_wait"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── actions ───────────────────────────────────────────────────
        BrandButton(
            mtl("back_home"),
            {
                nav.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            },
            Modifier.fillMaxWidth(),
        )
        BrandButton(
            tl("pay_title"),
            { nav.navigate(Routes.PAYMENTS) { launchSingleTop = true } },
            Modifier.fillMaxWidth(),
            style = BrandBtnStyle.OUTLINE,
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ReceiptRow(label: String, value: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.size(16.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { value() }
    }
}
