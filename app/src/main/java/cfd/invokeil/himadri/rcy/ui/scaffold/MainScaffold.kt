package cfd.invokeil.himadri.rcy.ui.scaffold

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cfd.invokeil.himadri.rcy.LocalAppVM
import cfd.invokeil.himadri.rcy.LocalLang
import cfd.invokeil.himadri.rcy.MainActivity
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.i18n.S
import cfd.invokeil.himadri.rcy.core.i18n.t
import cfd.invokeil.himadri.rcy.core.network.SessionUser
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.DarkBackground
import cfd.invokeil.himadri.rcy.core.theme.LightBackground
import cfd.invokeil.himadri.rcy.core.theme.isDarkTheme
import cfd.invokeil.himadri.rcy.core.theme.ThemeMode
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BlinkDot
import cfd.invokeil.himadri.rcy.ui.nav.DeepLinks
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * App shell — website header (logo + brand + lang toggle + bell + menu),
 * LIVE emergency ticker strip, navigation drawer, bottom bar
 * (Home / Notices / Events / Profile) and the child content graph.
 */
@Composable
fun MainScaffold(vm: cfd.invokeil.himadri.rcy.vm.AppViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val childNav = rememberNavController()
    val theme by vm.theme.collectAsState()
    val dark = isDarkTheme(theme)

    // deep links (verify / shared notice) once the shell is up
    LaunchedEffect(Unit) {
        MainActivity.pendingDeepLink?.let { link ->
            MainActivity.pendingDeepLink = null
            DeepLinks.routeFor(link)?.let { route -> childNav.navigate(route) }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { DrawerContent(vm, childNav, onClose = { scope.launch { drawerState.close() } }) },
    ) {
        val backStack by childNav.currentBackStackEntryAsState()
        val route = backStack?.destination?.route ?: Routes.HOME
        val showBottomBar = route in setOf(Routes.HOME, Routes.NOTICES, Routes.EVENTS, Routes.PROFILE)

        Scaffold(
            containerColor = if (dark) DarkBackground else LightBackground,
            topBar = {
                Column {
                    HeaderBar(vm = vm, onMenu = { scope.launch { drawerState.open() } }, onBell = {
                        childNav.navigate(Routes.NOTIFICATIONS)
                    })
                    LiveTickerStrip(vm, onOpenFeed = { childNav.navigate(Routes.EMERGENCY_FEED) })
                }
            },
            bottomBar = { if (showBottomBar) BottomBar(childNav, route) },
        ) { padding ->
            ChildGraph(
                vm = vm,
                nav = childNav,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }

        EmergencyPopupHost(vm, childNav)
    }
}

// ── Header — sticky, mirrors site header ───────────────────────────────
@Composable
private fun HeaderBar(vm: cfd.invokeil.himadri.rcy.vm.AppViewModel, onMenu: () -> Unit, onBell: () -> Unit) {
    val lang by vm.lang.collectAsState()
    val session by vm.session.collectAsState()
    val unread by vm.notifications.collectAsState()

    Row(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // brand logo (from site assets) + two-line brand text
        Box(
            Modifier.size(38.dp).background(BrandRed.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = coil.compose.rememberAsyncImagePainter("file:///android_asset/logos/bdrcs.png"),
                contentDescription = null,
                modifier = Modifier.size(30.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                t(lang, S.brand), style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Text(
                t(lang, S.brand_sub), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        // language toggle — site style: "EN | বাংলা"
        Row(
            Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(999.dp))
                .clickable { vm.toggleLang() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (lang == cfd.invokeil.himadri.rcy.core.i18n.Lang.BN) "বাংলা" else "EN",
                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = BrandRed,
            )
        }
        Spacer(Modifier.width(6.dp))
        BadgedBox(badge = {
            if (unread.unread > 0) Badge(containerColor = BrandRed) {
                Text(unread.unread.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
        }) {
            IconButton(onClick = onBell) {
                Icon(AppIcon.of("bell"), contentDescription = "notifications")
            }
        }
        IconButton(onClick = onMenu) { Icon(Icons.Filled.Menu, contentDescription = "menu") }
    }
}

// ── LIVE ticker — red strip, blinking dot, rotating urgent headlines ───
@Composable
private fun LiveTickerStrip(vm: cfd.invokeil.himadri.rcy.vm.AppViewModel, onOpenFeed: () -> Unit) {
    val ticker by vm.ticker.collectAsState()
    val lang by vm.lang.collectAsState()

    val headlines = remember(ticker) {
        buildList {
            ticker.alerts.forEach { a ->
                add(a.title.pick(lang).ifBlank { a.body.pick(lang) })
            }
            ticker.urgentNotices.forEach { n -> add(n.title.pick(lang)) }
        }.filter { it.isNotBlank() }.distinct().take(6)
    }

    var idx by remember { mutableIntStateOf(0) }
    LaunchedEffect(headlines.size) {
        while (headlines.size > 1) {
            delay(4200)
            idx = (idx + 1) % headlines.size
        }
    }

    val hasAlerts = headlines.isNotEmpty()
    Box(
        Modifier.fillMaxWidth()
            .background(if (hasAlerts) BrandRed else Color(0xFF0F7B54))
            .clickable(enabled = hasAlerts) { onOpenFeed() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BlinkDot(Color.White, 7)
            Spacer(Modifier.width(8.dp))
            Text(
                if (hasAlerts) t(lang, S.ticker_label) else t(lang, S.strip_empty),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
            )
            if (hasAlerts) {
                Spacer(Modifier.width(10.dp))
                Box(Modifier.width(1.dp).height(14.dp).background(Color.White.copy(alpha = 0.4f)))
                Spacer(Modifier.width(10.dp))
                Box(Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = if (headlines.isEmpty()) 0 else idx % headlines.size,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "ticker",
                    ) { i ->
                        Text(
                            headlines.getOrNull(i) ?: "",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.95f),
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

// ── Bottom bar — Home / Notices / Events / Profile ─────────────────────
@Composable
private fun BottomBar(nav: NavHostController, current: String) {
    val lang = LocalLang.current
    val tabs = listOf(
        Triple(Routes.HOME, t(lang, S.nav_home), AppIcon.home),
        Triple(Routes.NOTICES, t(lang, S.nav_notices), AppIcon.notices),
        Triple(Routes.EVENTS, t(lang, S.nav_events), AppIcon.events),
        Triple(Routes.PROFILE, t(lang, S.nav_profile).let { t(lang, S.nav_member_zone) }, AppIcon.profile),
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
        tabs.forEach { (r, label, icon) ->
            NavigationBarItem(
                selected = current == r,
                onClick = {
                    nav.navigate(r) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(icon, null) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandRed,
                    selectedTextColor = BrandRed,
                    indicatorColor = BrandRed.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
