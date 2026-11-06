package cfd.invokeil.himadri.rcy.screens.misc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.Certificate
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.BrandRedDark
import cfd.invokeil.himadri.rcy.core.util.QrCode
import cfd.invokeil.himadri.rcy.core.util.ShareUtil
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.EmptyState
import cfd.invokeil.himadri.rcy.ui.components.ErrorState
import cfd.invokeil.himadri.rcy.ui.components.ListSkeleton
import cfd.invokeil.himadri.rcy.ui.components.PillBadge
import cfd.invokeil.himadri.rcy.ui.components.ShareLinks
import cfd.invokeil.himadri.rcy.ui.components.fullDate
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import kotlinx.coroutines.launch

/**
 * Ornamental certificate detail — mirrors website certificate-page.tsx:
 * double red border, presented-to name, field rows, serial, QR verify link.
 */
@Composable
fun CertificateDetailScreen(vm: AppViewModel, nav: NavHostController, id: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val st = rememberApiState("cert_detail_$id") { ApiClient.api.certificates(mine = "1") }
    val cert = st.data?.certificates?.firstOrNull { it.id == id }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(AppIcon.of("arrow-left"), contentDescription = null)
            }
            Text(tl("cert_view"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }

        when {
            st.loading -> ListSkeleton(2)

            st.error != null -> ErrorState(st.error) { scope.launch { st.load() } }

            cert == null -> EmptyState(AppIcon.of("file-text"), tl("cert_invalid"), tint = BrandRed)

            else -> {
                val verifyUrl = ShareLinks.forVerify(cert.serial)
                val qr = remember(cert.serial) { QrCode.generate(verifyUrl, 256) }

                // ── ornamental sheet ──────────────────────────────────
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(18.dp))
                        .border(3.dp, BrandRed, RoundedCornerShape(18.dp))
                        .padding(5.dp),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .border(1.dp, BrandRed.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // green accent + serial
                        Box(Modifier.size(width = 48.dp, height = 4.dp).background(BrandGreen, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.height(10.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column {
                                Text(
                                    tl("brand"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandRed,
                                )
                                Text(
                                    tl("brand_sub"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF62626E),
                                )
                            }
                            PillBadge(tl("cert_serial") + " " + cert.serial, BrandRed)
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            cert.template.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF62626E),
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            tl("cert_issued_to"),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF62626E),
                        )
                        Text(
                            cert.fields["name"] ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = BrandRedDark,
                            textAlign = TextAlign.Center,
                        )
                        if (cert.title.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                cert.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B1B21),
                                textAlign = TextAlign.Center,
                            )
                        }

                        // field rows (all certificate fields)
                        if (cert.fields.size > 1 || !cert.fields.containsKey("name")) {
                            Spacer(Modifier.height(12.dp))
                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                cert.fields.forEach { (key, value) ->
                                    if (key != "name") {
                                        CertFieldRow(key, value)
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // QR verify
                        Image(
                            bitmap = qr.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(90.dp).background(Color.White, CircleShape),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            tl("cert_verify_note"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF62626E),
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(14.dp))

                        // bottom row: serial / issued on / issued by
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    cert.serial,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B1B21),
                                )
                                Text(
                                    tl("cert_issued_on") + " " + fullDate(cert.created_at),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF62626E),
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(AppIcon.of("verified"), contentDescription = null, tint = BrandGreen, modifier = Modifier.size(18.dp))
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
                                Text(
                                    cert.issued_by?.takeIf { it.isNotBlank() && it != "system:auto" } ?: tl("brand"),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1B1B21),
                                    textAlign = TextAlign.End,
                                )
                            }
                        }
                    }
                }

                // ── actions ───────────────────────────────────────────
                val brandLabel = tl("brand")
                val serialLabel = tl("cert_serial")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BrandButton(
                        mtl("cert_view_share"),
                        {
                            val title = cert.title.ifBlank { brandLabel }
                            ShareUtil.shareText(
                                context,
                                title,
                                title + "\n" + serialLabel + ": " + cert.serial + "\n" + verifyUrl,
                            )
                        },
                        Modifier.weight(1f),
                    )
                    BrandButton(
                        mtl("open_web"),
                        { ShareUtil.openUrl(context, verifyUrl) },
                        Modifier.weight(1f),
                        style = BrandBtnStyle.OUTLINE,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CertFieldRow(key: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            key,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF62626E),
        )
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B1B21),
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}
