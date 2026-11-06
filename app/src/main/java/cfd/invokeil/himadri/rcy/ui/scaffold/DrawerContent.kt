package cfd.invokeil.himadri.rcy.ui.scaffold

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.LocalLang
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import cfd.invokeil.himadri.rcy.core.i18n.S
import cfd.invokeil.himadri.rcy.core.i18n.t
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.initials
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.vm.AppViewModel

/** Drawer — quick nav to everything (site header + member zone). */
@Composable
fun DrawerContent(vm: AppViewModel, nav: NavHostController, onClose: () -> Unit) {
    val session by vm.session.collectAsState()
    val lang = LocalLang.current

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
    ) {
        // member header (avatar initial, name, role/member no)
        Row(
            Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(52.dp).background(if (session != null) BrandRed else BrandRed.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (session != null) {
                    Text(
                        session!!.initials(), color = Color.White,
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    )
                } else {
                    Image(
                        painter = coil.compose.rememberAsyncImagePainter("file:///android_asset/logos/bdrcs.png"),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    session?.displayName ?: t(lang, S.nav_login),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                )
                Text(
                    when {
                        session == null -> t(lang, S.hero_badge)
                        session?.member_no != null -> "${t(lang, S.idcard_no)}: ${session?.member_no}"
                        else -> t(lang, S.prof_role_member)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.padding(6.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

        DrawerItem(t(lang, S.nav_home), AppIcon.home, BrandRed) { nav.navigate(Routes.HOME); onClose() }
        DrawerItem(t(lang, S.nav_notices), AppIcon.notices, BrandRed) { nav.navigate(Routes.NOTICES); onClose() }
        DrawerItem(t(lang, S.nav_events), AppIcon.events, BrandRed) { nav.navigate(Routes.EVENTS); onClose() }
        DrawerItem(t(lang, S.nav_leaderboard), AppIcon.leaderboard, BrandRed) { nav.navigate(Routes.LEADERBOARD); onClose() }
        DrawerItem(t(lang, S.nav_payments), AppIcon.payments, BrandRed) { nav.navigate(Routes.PAYMENTS); onClose() }
        DrawerItem(t(lang, S.nav_request), AppIcon.request, BrandRed) { nav.navigate(Routes.EMERGENCY_REQUEST); onClose() }

        HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

        if (session != null) {
            DrawerItem(t(lang, S.nav_member_zone), AppIcon.profile, BrandGreen) { nav.navigate(Routes.PROFILE); onClose() }
            DrawerItem(t(lang, S.idcard_title), AppIcon.of("id-card"), BrandGreen) { nav.navigate(Routes.ID_CARD); onClose() }
            DrawerItem(t(lang, S.my_certificates), AppIcon.of("certificate"), BrandGreen) { nav.navigate(Routes.CERTIFICATES); onClose() }
            DrawerItem(t(lang, S.notif_title), AppIcon.of("bell"), BrandGreen) { nav.navigate(Routes.NOTIFICATIONS); onClose() }
            DrawerItem(t(lang, S.nav_logout), AppIcon.of("logout"), MaterialTheme.colorScheme.error) {
                vm.logout(); onClose()
            }
        } else {
            DrawerItem(t(lang, S.nav_login), AppIcon.of("login"), BrandGreen) {
                nav.navigate(cfd.invokeil.himadri.rcy.ui.nav.LOGIN_ROUTE); onClose()
            }
        }

        HorizontalDivider(Modifier.padding(vertical = 6.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        DrawerItem(t(lang, S.foot_terms), AppIcon.of("file-text"), MaterialTheme.colorScheme.onSurfaceVariant) { nav.navigate(Routes.TERMS); onClose() }
        DrawerItem(mtl("about_app"), AppIcon.settings, MaterialTheme.colorScheme.onSurfaceVariant) { nav.navigate(Routes.SETTINGS); onClose() }
    }
}

@Composable
private fun DrawerItem(label: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
