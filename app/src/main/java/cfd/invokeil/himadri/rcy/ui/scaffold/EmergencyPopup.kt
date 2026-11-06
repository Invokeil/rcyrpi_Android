package cfd.invokeil.himadri.rcy.ui.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.LocalAppVM
import cfd.invokeil.himadri.rcy.LocalLang
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.i18n.S
import cfd.invokeil.himadri.rcy.core.i18n.t
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.BrandRedDark
import cfd.invokeil.himadri.rcy.core.util.ShareUtil
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.ui.components.tl

/**
 * Global emergency popup — mirrors the site's EmergencyPopupProvider.
 * Mobile rule: instant cancel (the 3s protected lock is desktop-only).
 */
@Composable
fun EmergencyPopupHost(vm: cfd.invokeil.himadri.rcy.vm.AppViewModel, nav: NavHostController) {
    val alert by vm.popupAlert.collectAsState()
    val lang = LocalLang.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val current = alert ?: return

    androidx.compose.material3.AlertDialog(
        onDismissRequest = { vm.dismissPopup() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(34.dp).background(BrandRed, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(AppIcon.of("siren"), null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(10.dp))
                    Text(t(lang, S.pop_live), color = BrandRed, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        text = {
            Column {
                Text(
                    current.title.pick(lang),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    current.body.pick(lang),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // one-tap response row (site: Call / WhatsApp / SMS / Map)
                if (!current.phone.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                        cfd.invokeil.himadri.rcy.ui.components.BrandButton(tl("otap_call"), {
                            ShareUtil.dial(context, current.phone!!)
                        }, style = cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle.PRIMARY)
                        cfd.invokeil.himadri.rcy.ui.components.BrandButton(tl("otap_sms"), {
                            ShareUtil.sms(context, current.phone!!, null)
                        }, style = cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle.OUTLINE)
                    }
                }
            }
        },
        confirmButton = {
            cfd.invokeil.himadri.rcy.ui.components.BrandButton(t(lang, S.pop_details), {
                vm.dismissPopup()
                nav.navigate(Routes.EMERGENCY_FEED)
            }, style = cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle.GREEN)
        },
        dismissButton = {
            IconButton(onClick = { vm.dismissPopup() }) {
                Icon(Icons.Filled.Close, contentDescription = t(lang, S.pop_cancel))
            }
        },
    )
}
