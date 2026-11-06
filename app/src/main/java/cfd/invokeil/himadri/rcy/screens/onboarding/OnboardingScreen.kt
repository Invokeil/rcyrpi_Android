package cfd.invokeil.himadri.rcy.screens.onboarding

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cfd.invokeil.himadri.rcy.LocalLang
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.ui.components.AppIcon
import cfd.invokeil.himadri.rcy.ui.components.BrandButton
import cfd.invokeil.himadri.rcy.ui.components.BrandGradientButton

private data class Page(val icon: androidx.compose.ui.graphics.vector.ImageVector, val title: Map<Lang, String>, val body: Map<Lang, String>)

private val PAGES = listOf(
    Page(
        AppIcon.of("blood"),
        mapOf(Lang.BN to "রক্তদানে প্রাণ বাঁচান", Lang.EN to "Save lives with blood"),
        mapOf(
            Lang.BN to "জরুরি রক্তের প্রয়োজনে দ্রুত ডোনার খুঁজুন ও নিজে ডোনার হিসেবে নিবন্ধন করুন।",
            Lang.EN to "Find donors fast in emergencies and register yourself as a voluntary donor.",
        ),
    ),
    Page(
        AppIcon.of("siren"),
        mapOf(Lang.BN to "লাইভ ইমার্জেন্সি অ্যালার্ট", Lang.EN to "Live emergency alerts"),
        mapOf(
            Lang.BN to "দুর্যোগ ও জরুরি রিকোয়েস্ট সাথে সাথেই নোটিফিকেশন — এক ট্যাপে কল/এসএমএস/ম্যাপ।",
            Lang.EN to "Disasters & urgent requests reach you instantly — one tap for Call/SMS/Map.",
        ),
    ),
    Page(
        AppIcon.of("calendar"),
        mapOf(Lang.BN to "ইভেন্ট ও স্বেচ্ছাসেবা", Lang.EN to "Events & volunteering"),
        mapOf(
            Lang.BN to "ক্যালেন্ডারে ইভেন্ট, RSVP, ঘণ্টা ট্র্যাকিং, ব্যাজ ও লিডারবোর্ড।",
            Lang.EN to "Event calendar, RSVP, hour tracking, badges & leaderboard.",
        ),
    ),
)

/** First-launch onboarding — 3 pages, Bangla-first copy. */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val p = PAGES[page]
    val lang = LocalLang.current

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(60.dp))
        Box(
            Modifier.size(110.dp).background(BrandRed.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier.size(84.dp).background(BrandRed, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(p.icon, null, tint = Color.White, modifier = Modifier.size(44.dp))
            }
        }
        Spacer(Modifier.height(28.dp))
        Text(p.title[lang].orEmpty(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(
            p.body[lang].orEmpty(),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PAGES.forEachIndexed { i, _ ->
                Box(
                    Modifier.size(if (i == page) 22.dp else 8.dp, 8.dp)
                        .background(if (i == page) BrandRed else MaterialTheme.colorScheme.outline, CircleShape)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (page > 0) {
                BrandButton("←", { page-- }, style = cfd.invokeil.himadri.rcy.ui.components.BrandBtnStyle.OUTLINE)
            }
            Box(Modifier.weight(1f)) {
                if (page < PAGES.size - 1) {
                    BrandGradientButton(
                        if (lang == Lang.BN) "পরের" else "Next",
                        { page++ }, modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    BrandGradientButton(
                        if (lang == Lang.BN) "শুরু করুন" else "Get Started",
                        onFinish, modifier = Modifier.fillMaxWidth(), green = true,
                    )
                }
            }
        }
        Spacer(Modifier.height(18.dp))
    }
}
