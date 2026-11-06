package cfd.invokeil.himadri.rcy.screens.misc

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.Routes
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.network.AppNotification
import cfd.invokeil.himadri.rcy.core.network.MarkNotifBody
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.EmptyState
import cfd.invokeil.himadri.rcy.ui.components.relTime
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel
import kotlinx.coroutines.launch

/**
 * Notifications feed — mirrors website src/components/shared/notification-bell.tsx
 * as a full screen: back + title + "mark all read" header, kind-icon rows with
 * unread bold + red dot, tap navigates (notice / events) and marks the item read.
 * Guests get a login card (the notifications endpoint needs a session).
 */
@Composable
fun NotificationsScreen(vm: AppViewModel, nav: NavHostController) {
    val notifRes by vm.notifications.collectAsState()
    val session by vm.session.collectAsState()
    val scope = rememberCoroutineScope()

    // refresh on entry (VM no-ops for guests)
    LaunchedEffect(Unit) { vm.refreshNotifications() }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // header row — back + title + mark all read
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(AppIcon.of("arrow-left"), contentDescription = "back")
            }
            Spacer(Modifier.width(4.dp))
            Text(
                tl("notif_title"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = {
                vm.markAllRead()
                vm.refreshNotifications()
            }) {
                Text(tl("notif_mark_all"), style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(Modifier.height(8.dp))

        if (session == null) {
            // guest card — notifications require a session
            BrandCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        mtl("login_required_action"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(12.dp))
                    BrandButton(
                        tl("login_btn"),
                        { nav.navigate("ext_login") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } else {
            val items = notifRes.items
            if (items.isEmpty()) {
                EmptyState(AppIcon.of("bell"), tl("notif_empty"))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items.forEach { n ->
                        NotificationRow(n) {
                            // mark read (best effort) + navigate
                            if (!n.read) {
                                scope.launch {
                                    runCatching {
                                        ApiClient.api.markNotifications(
                                            MarkNotifBody(ids = listOf(n.id), action = "read"),
                                        )
                                    }
                                    vm.refreshNotifications()
                                }
                            }
                            val link = n.link
                            when {
                                link != null && link.startsWith("/n/") -> {
                                    val noticeId = link.removePrefix("/n/")
                                        .substringBefore('?').substringBefore('#')
                                    if (noticeId.isNotBlank()) nav.navigate(Routes.notice(noticeId))
                                }
                                link != null && link.contains("/events") -> nav.navigate(Routes.EVENTS)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ── row item — kind icon, title/body, relTime, unread dot ──────────────
@Composable
private fun NotificationRow(item: AppNotification, onClick: () -> Unit) {
    BrandCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                AppIcon.of(kindIconName(item.kind)),
                contentDescription = null,
                tint = BrandRed,
                modifier = Modifier.size(20.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    item.title.ifBlank { "—" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (item.read) FontWeight.Normal else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                item.body?.takeIf { it.isNotBlank() }?.let { body ->
                    Text(
                        body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    relTime(item.created_at),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!item.read) {
                Box(Modifier.size(8.dp).background(BrandRed, CircleShape))
            }
        }
    }
}

/** kind → lucide icon name (site KIND_ICON mapping). */
private fun kindIconName(kind: String): String = when (kind) {
    "certificate", "notice" -> "file-text"
    "event" -> "calendar"
    "broadcast" -> "megaphone"
    else -> "info"
}
