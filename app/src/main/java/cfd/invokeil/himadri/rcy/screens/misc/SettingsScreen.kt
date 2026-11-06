package cfd.invokeil.himadri.rcy.screens.misc

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.BuildConfig
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.theme.BrandGradient
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.ThemeMode
import cfd.invokeil.himadri.rcy.core.util.ShareUtil
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.BrandField
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import kotlinx.coroutines.launch

/**
 * Settings — theme / language / server / security / cache / about.
 * Mirrors the mobile-only preferences (MobileStrings keys).
 */
@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val theme by vm.theme.collectAsState()
    val lang by vm.lang.collectAsState()

    // biometric lock — AppViewModel exposes no setter yet, so the switch
    // keeps a session-local state (see task notes: missing VM API)
    var appLock by remember { mutableStateOf(false) }

    var currentBase by remember { mutableStateOf(ApiClient.baseUrl) }
    var customOpen by remember { mutableStateOf(false) }
    var customUrl by remember { mutableStateOf("") }

    val clearedMsg = mtl("cache_cleared")

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // back row + title
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(AppIcon.of("arrow-left"), contentDescription = null)
            }
            Text(mtl("about_app"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }

        // ── 1. theme ──────────────────────────────────────────────────
        BrandCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(mtl("theme"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                RadioRow(mtl("theme_system"), theme == ThemeMode.SYSTEM) { vm.setTheme(ThemeMode.SYSTEM) }
                RadioRow(mtl("theme_light"), theme == ThemeMode.LIGHT) { vm.setTheme(ThemeMode.LIGHT) }
                RadioRow(mtl("theme_dark"), theme == ThemeMode.DARK) { vm.setTheme(ThemeMode.DARK) }
            }
        }

        // ── 2. language ───────────────────────────────────────────────
        BrandCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(mtl("language"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "(${lang.code.uppercase()})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LangChip("বাংলা", lang == Lang.BN) { vm.setLang(Lang.BN) }
                    LangChip("English", lang == Lang.EN) { vm.setLang(Lang.EN) }
                }
            }
        }

        // ── 3. server ─────────────────────────────────────────────────
        BrandCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(mtl("server_url"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    currentBase,
                    style = MaterialTheme.typography.labelSmall,
                    color = BrandRed,
                    fontWeight = FontWeight.SemiBold,
                )
                RadioRow(mtl("server_main"), currentBase == ApiClient.MAIN_URL) {
                    customOpen = false
                    currentBase = ApiClient.MAIN_URL
                    vm.setServerUrl(null)
                }
                RadioRow(mtl("server_mirror"), currentBase == ApiClient.MIRROR_URL) {
                    customOpen = false
                    currentBase = ApiClient.MIRROR_URL
                    vm.setServerUrl(ApiClient.MIRROR_URL)
                }
                RadioRow(
                    if (lang.isBn) "নিজের URL" else "Custom URL",
                    !customOpen && currentBase != ApiClient.MAIN_URL && currentBase != ApiClient.MIRROR_URL,
                    expandable = true,
                    expanded = customOpen,
                ) {
                    customOpen = !customOpen
                }
                if (customOpen) {
                    BrandField(customUrl, { customUrl = it }, mtl("server_url"))
                    BrandButton(tl("pay_done"), {
                        val u = customUrl.trim()
                        if (u.isNotBlank()) {
                            vm.setServerUrl(u)
                        } else {
                            vm.setServerUrl(null)
                        }
                        currentBase = ApiClient.baseUrl
                    })
                }
                Text(
                    mtl("server_url_hint"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    mtl("cf_challenge_hint"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // ── 4. security ───────────────────────────────────────────────
        BrandCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(mtl("app_lock"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        mtl("coming_soon"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = appLock, onCheckedChange = { appLock = it })
            }
        }

        // ── 5. data ───────────────────────────────────────────────────
        BrandCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            vm.cacheStore.clear()
                            Toast.makeText(context, clearedMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(AppIcon.of("trash"), contentDescription = null, tint = BrandRed, modifier = Modifier.size(20.dp))
                Text(mtl("clear_cache"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }

        // ── 6. about ──────────────────────────────────────────────────
        BrandCard(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(52.dp).background(BrandGradient, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(AppIcon.of("heart"), contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text("RCY-RPI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Text(
                            tl("brand_sub"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        mtl("app_version"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    mtl("open_source"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // GitHub link row
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { ShareUtil.openUrl(context, "https://github.com/Invokeil/rcyrpi") }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(AppIcon.of("link"), contentDescription = null, tint = BrandRed, modifier = Modifier.size(16.dp))
                    Text(
                        "github.com/Invokeil/rcyrpi",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandRed,
                    )
                }
                // terms row
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { nav.navigate(Routes.TERMS) }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(AppIcon.of("shield"), contentDescription = null, tint = BrandRed, modifier = Modifier.size(16.dp))
                    Text(
                        tl("foot_terms"),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandRed,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, expandable: Boolean = false, expanded: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        if (expandable) {
            Spacer(Modifier.size(6.dp))
            Icon(
                AppIcon.of(if (expanded) "chevron-up" else "chevron-down"),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun LangChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .background(
                if (selected) BrandRed else MaterialTheme.colorScheme.surfaceContainerHigh,
                RoundedCornerShape(999.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
