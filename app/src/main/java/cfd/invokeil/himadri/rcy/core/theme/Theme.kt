package cfd.invokeil.himadri.rcy.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** User-selectable theme mode (Settings). */
enum class ThemeMode(val storage: String) {
    SYSTEM("system"), LIGHT("light"), DARK("dark");
    companion object {
        fun from(s: String?) = entries.firstOrNull { it.storage == s } ?: SYSTEM
    }
}

private val LightScheme = lightColorScheme(
    primary = BrandRed,
    onPrimary = Color.White,
    primaryContainer = BrandRedSoft,
    onPrimaryContainer = BrandRedDark,
    secondary = BrandGreen,
    onSecondary = Color.White,
    secondaryContainer = BrandGreenSoft,
    onSecondaryContainer = BrandGreenDark,
    tertiary = BrandGreenDark,
    background = LightBackground,
    onBackground = LightForeground,
    surface = LightCard,
    onSurface = LightForeground,
    surfaceVariant = LightSecondary,
    onSurfaceVariant = LightMutedFg,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightDestructive,
    scrim = Scrim,
    surfaceContainerLowest = LightBackground,
    surfaceContainerLow = Color(0xFFFAFAFC),
    surfaceContainer = LightSecondary,
    surfaceContainerHigh = Color(0xFFF1F1F5),
    surfaceContainerHighest = Color(0xFFE8E8EE),
)

private val DarkScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3D060C),
    onPrimaryContainer = Color(0xFFFFB3B9),
    secondary = DarkAccentFg,
    onSecondary = Color(0xFF003924),
    secondaryContainer = DarkAccent,
    onSecondaryContainer = DarkAccentFg,
    tertiary = DarkAccentFg,
    background = DarkBackground,
    onBackground = DarkForeground,
    surface = DarkCard,
    onSurface = DarkForeground,
    surfaceVariant = DarkSecondary,
    onSurfaceVariant = DarkMutedFg,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = DarkDestructive,
    scrim = Scrim,
    surfaceContainerLowest = Color(0xFF17171D),
    surfaceContainerLow = DarkCard,
    surfaceContainer = DarkCard,
    surfaceContainerHigh = DarkSecondary,
    surfaceContainerHighest = Color(0xFF2A2A35),
)

/** Site radius token: 0.75rem = 12px; pills use 999dp. */
val RCYShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun RCYTheme(
    themeMode: ThemeMode,
    useBengali: Boolean,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = appTypography(useBengali),
        shapes = RCYShapes,
        content = content,
    )
}

/** true when the resolved scheme is dark (for status-bar icons etc). */
@Composable
fun isDarkTheme(themeMode: ThemeMode): Boolean = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
