package cfd.invokeil.himadri.rcy.screens.notices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import cfd.invokeil.himadri.rcy.core.network.ApiClient
import cfd.invokeil.himadri.rcy.core.util.AppError
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandCard
import cfd.invokeil.himadri.rcy.ui.components.ErrorState
import cfd.invokeil.himadri.rcy.ui.components.ListSkeleton
import cfd.invokeil.himadri.rcy.ui.components.PillBadge
import cfd.invokeil.himadri.rcy.ui.components.ShareLinks
import cfd.invokeil.himadri.rcy.ui.components.ShareRow
import cfd.invokeil.himadri.rcy.ui.components.fullDate
import cfd.invokeil.himadri.rcy.ui.components.lp
import cfd.invokeil.himadri.rcy.ui.components.rememberApiState
import cfd.invokeil.himadri.rcy.ui.components.severityColor
import cfd.invokeil.himadri.rcy.ui.components.tl
import cfd.invokeil.himadri.rcy.vm.AppViewModel

/**
 * Notice detail — mirrors website src/components/pages/notice-detail-page.tsx:
 * back row, category + severity badges (+ pinned chip), title, full body,
 * created date, then the share row. The single notice is resolved from the
 * same notices list call the list screen uses (the site's detail endpoint
 * is not part of the mobile Api surface).
 */
@Composable
fun NoticeDetailScreen(vm: AppViewModel, nav: NavHostController, id: String) {
    val state = rememberApiState("notice-$id") {
        ApiClient.api.notices(null).notices.firstOrNull { it.id == id }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // back row
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(AppIcon.of("arrow-left"), contentDescription = "back")
            }
        }

        when {
            state.loading -> ListSkeleton(3)
            state.error != null -> ErrorState(state.error) { state.retry() }
            else -> {
                val notice = state.data
                if (notice == null) {
                    // notice not found (or dropped from the list)
                    ErrorState(AppError.Server) { state.retry() }
                } else {
                    BrandCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                PillBadge(
                                    noticeCategoryLabel(notice.category),
                                    color = severityColor(notice.severity),
                                )
                                PillBadge(
                                    notice.severity.uppercase(),
                                    color = severityColor(notice.severity),
                                )
                                if (notice.is_pinned) PinnedChip()
                            }
                            Text(
                                lp(notice.title),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                fullDate(notice.created_at),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            HorizontalDivider()
                            Text(lp(notice.body), style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    ShareRow(title = lp(notice.title), url = ShareLinks.forNotice(notice.id))
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
