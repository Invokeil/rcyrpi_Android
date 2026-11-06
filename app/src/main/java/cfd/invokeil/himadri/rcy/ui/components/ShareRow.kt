package cfd.invokeil.himadri.rcy.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.i18n.mtl
import cfd.invokeil.himadri.rcy.core.util.ShareUtil

/**
 * Share targets row — mirrors the site's ShareMenu
 * (WhatsApp / Facebook / Messenger / Telegram / copy).
 */
object ShareLinks {
    /** Builds the canonical website URL for a notice/event. */
    fun forNotice(id: String, base: String = "https://rcy.invokeil.bond") = "$base/n/$id"
    fun forEvent(id: String, base: String = "https://rcy.invokeil.bond") = "$base/events?id=$id"
    fun forVerify(code: String, base: String = "https://rcy.invokeil.bond") = "$base/verify?code=$code"

    fun whatsapp(context: android.content.Context, text: String, url: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/?text=${android.net.Uri.encode("$text\n$url")}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun facebook(context: android.content.Context, url: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.facebook.com/sharer/sharer.php?u=${android.net.Uri.encode(url)}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun messenger(context: android.content.Context, url: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, android.net.Uri.parse("fb-messenger://share?link=${android.net.Uri.encode(url)}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ).let {}
        }.onFailure {
            // fallback: plain share (Messenger not installed)
            ShareUtil.shareText(context, url, url)
        }
    }

    fun telegram(context: android.content.Context, text: String, url: String) {
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://t.me/share/url?url=${android.net.Uri.encode(url)}&text=${android.net.Uri.encode(text)}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}

private data class Target(val label: String, val color: Color, val emoji: String, val action: () -> Unit)

@Composable
fun ShareRow(title: String, url: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier.fillMaxWidth()) {
        Text(tl("share_title"), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.size(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            val targets = listOf(
                Target(mtl("share_whatsapp"), Color(0xFF25D366), "WA") { ShareLinks.whatsapp(context, title, url) },
                Target(mtl("share_facebook"), Color(0xFF1877F2), "FB") { ShareLinks.facebook(context, url) },
                Target(mtl("share_messenger"), Color(0xFFA334FA), "MS") { ShareLinks.messenger(context, url) },
                Target(mtl("share_telegram"), Color(0xFF0088CC), "TG") { ShareLinks.telegram(context, title, url) },
            )
            targets.forEach { t ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.material3.Surface(
                        shape = CircleShape,
                        color = t.color.copy(alpha = 0.14f),
                        modifier = Modifier.size(44.dp).clickable { t.action() },
                    ) {
                        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                            Text(
                                t.emoji, style = MaterialTheme.typography.labelMedium,
                                color = t.color, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            )
                        }
                    }
                    Spacer(Modifier.size(4.dp))
                    Text(t.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(44.dp).clickable { ShareUtil.copy(context, mtl("copied"), url) },
                ) {
                    androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Icon(
                            AppIcon.of("link"), null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(Modifier.size(4.dp))
                Text(mtl("share_copy"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
