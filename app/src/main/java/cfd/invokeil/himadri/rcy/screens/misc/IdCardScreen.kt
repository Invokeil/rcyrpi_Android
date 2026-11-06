package cfd.invokeil.himadri.rcy.screens.misc

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.util.QrCode
import cfd.invokeil.himadri.rcy.core.util.ShareUtil
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.PillBadge
import cfd.invokeil.himadri.rcy.ui.components.SectionHeading
import cfd.invokeil.himadri.rcy.ui.components.ShareLinks
import cfd.invokeil.himadri.rcy.ui.components.initials
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import androidx.navigation.NavHostController

/**
 * Digital member ID card — mirrors the donor-kit card on the website:
 * gradient header strip + white body (avatar, name, member no, blood,
 * team, active status) + verification QR linking to the public
 * /verify?code= page. Downloads/shares the QR bitmap; opens the web
 * verification page.
 */
@Composable
fun IdCardScreen(vm: AppViewModel, nav: NavHostController) {
    val sessionUser = vm.session.collectAsState().value
    if (sessionUser == null) {
        GuestGate { nav.navigate("ext_login") }
        return
    }

    val st = rememberApiState("idcard_me") { ApiClient.api.profile() }
    val p = st.data?.profile ?: sessionUser
    val memberNo = p.member_no?.takeIf { it.isNotBlank() } ?: p.username
    val verifyUrl = ShareLinks.forVerify(memberNo)
    val bmp = remember(verifyUrl) { QrCode.generate(verifyUrl, 400) }
    val context = LocalContext.current
    val brandName = tl("brand")

    // fixed ink colors — the card body is always paper-white, like a real card
    val ink = Color(0xFF1B1B21)
    val inkSoft = Color(0xFF62626E)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(AppIcon.of("arrow-left"), contentDescription = null)
            }
            SectionHeading(
                title = tl("idcard_title"),
                subtitle = tl("brand_sub"),
                modifier = Modifier.weight(1f),
            )
        }

        // ── the card (16:10) ───────────────────────────────────────────
        Column(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(18.dp)),
        ) {
            // gradient header strip with brand + logo
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(BrandGradient)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = "file:///android_asset/logos/bdrcs.png",
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        brandName,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            // white body
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(
                            p.displayName,
                            color = ink,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            tl("idcard_no") + ": " + memberNo,
                            color = inkSoft,
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!p.blood_group.isNullOrBlank()) {
                            // PillBadge on white — use solid for contrast
                            PillBadge(
                                text = tl("idcard_blood") + ": " + p.blood_group.orEmpty(),
                                color = BrandRed,
                                soft = false,
                            )
                        }
                        if (!p.team.isNullOrBlank()) {
                            Text(
                                tl("idcard_team") + ": " + p.team.orEmpty(),
                                color = inkSoft,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                AppIcon.of("verified"),
                                contentDescription = null,
                                tint = BrandGreen,
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                tl("idcard_status"),
                                color = BrandGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(110.dp),
                        )
                        Text(
                            tl("idcard_verify"),
                            color = inkSoft,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Text(
                    verifyUrl,
                    color = inkSoft,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // ── actions ────────────────────────────────────────────────────
        // (rendering the whole card to PNG is complex on Android — the
        // "download" action shares the verification QR bitmap instead)
        BrandButton(
            text = mtl("download_id_card"),
            onClick = {
                val uri = ShareUtil.bitmapUri(context, bmp, "rcy-id-$memberNo")
                ShareUtil.shareImage(context, uri, verifyUrl)
            },
            modifier = Modifier.fillMaxWidth(),
            style = BrandBtnStyle.OUTLINE,
            leading = AppIcon.of("download"),
        )
        BrandButton(
            text = mtl("share_id_card"),
            onClick = {
                ShareUtil.shareText(context, brandName, p.displayName + " — " + verifyUrl)
            },
            modifier = Modifier.fillMaxWidth(),
            leading = AppIcon.of("share"),
        )
        BrandButton(
            text = mtl("open_web"),
            onClick = { ShareUtil.openUrl(context, verifyUrl) },
            modifier = Modifier.fillMaxWidth(),
            style = BrandBtnStyle.GHOST,
            leading = AppIcon.of("globe"),
        )
    }
}

/** Centered login gate shared by member-only screens. */
@Composable
private fun GuestGate(onLogin: () -> Unit) {
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
            BrandButton(tl("login_btn"), onLogin, leading = AppIcon.of("login"))
        }
    }
}
