package cfd.invokeil.himadri.rcy.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Brand palette — mirrors website globals.css design tokens ──────────
val BrandRed = Color(0xFFE30613)
val BrandRedDark = Color(0xFFB00010)
val BrandRedSoft = Color(0xFFFDECEC)
val BrandGreen = Color(0xFF00734A)
val BrandGreenDark = Color(0xFF005234)
val BrandGreenSoft = Color(0xFFE7F5EE)

// light scheme
val LightBackground = Color(0xFFFFFFFF)
val LightForeground = Color(0xFF1B1B21)
val LightCard = Color(0xFFFFFFFF)
val LightSecondary = Color(0xFFF6F6F8)
val LightMutedFg = Color(0xFF62626E)
val LightBorder = Color(0xFFE8E8EE)
val LightDestructive = Color(0xFFD21C1C)

// dark scheme
val DarkBackground = Color(0xFF121217)
val DarkForeground = Color(0xFFF2F2F5)
val DarkCard = Color(0xFF1A1A21)
val DarkPrimary = Color(0xFFFF3341)
val DarkSecondary = Color(0xFF232330)
val DarkMutedFg = Color(0xFF9A9AA6)
val DarkAccent = Color(0xFF10281D)
val DarkAccentFg = Color(0xFF6FDCAE)
val DarkDestructive = Color(0xFFFF5B5B)
val DarkBorder = Color(0xFF2A2A35)

// misc
val Chart3 = Color(0xFFF5A623)
val Chart4 = Color(0xFF7C3AED)
val Chart5 = Color(0xFF0EA5E9)
val Scrim = Color(0x99000000)

/** Brand red gradient — 135°, #e30613 → #b00010 60% → #8f000c */
val BrandGradient = Brush.linearGradient(
    0f to BrandRed,
    0.6f to BrandRedDark,
    1f to Color(0xFF8F000C),
    start = androidx.compose.ui.geometry.Offset.Zero,
    end = androidx.compose.ui.geometry.Offset.Infinite,
)

/** Brand green gradient — 135°, #00734a → #005234 */
val GreenGradient = Brush.linearGradient(
    0f to BrandGreen,
    1f to BrandGreenDark,
    start = androidx.compose.ui.geometry.Offset.Zero,
    end = androidx.compose.ui.geometry.Offset.Infinite,
)

/** Text gradient — 100°, red → dark red → green */
fun textGradientBrush(): Brush = Brush.linearGradient(
    0.1f to BrandRed,
    0.55f to BrandRedDark,
    1f to BrandGreen,
    start = androidx.compose.ui.geometry.Offset.Zero,
    end = androidx.compose.ui.geometry.Offset.Infinite,
)
