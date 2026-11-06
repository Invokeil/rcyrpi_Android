package cfd.invokeil.himadri.rcy.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import cfd.invokeil.himadri.rcy.R

/** Inter (Latin) + Noto Sans Bengali — matching the website pairing. */
val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

val BengaliFamily = FontFamily(
    Font(R.font.notosansbengali_regular, FontWeight.Normal),
    Font(R.font.notosansbengali_medium, FontWeight.Medium),
    Font(R.font.notosansbengali_semibold, FontWeight.SemiBold),
    Font(R.font.notosansbengali_bold, FontWeight.Bold),
)

private val LineTrim = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

/**
 * Site typography: Inter for Latin, Noto Sans Bengali when the UI language
 * is Bangla (headlines carry the site's tight tracking).
 */
fun appTypography(useBengali: Boolean): Typography {
    val display = if (useBengali) BengaliFamily else InterFamily
    val body = if (useBengali) BengaliFamily else InterFamily
    return Typography(
        displayLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-0.5).sp, lineHeightStyle = LineTrim),
        displaySmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.4).sp, lineHeightStyle = LineTrim),
        headlineLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.3).sp, lineHeightStyle = LineTrim),
        headlineMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp, lineHeightStyle = LineTrim),
        headlineSmall = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp, lineHeightStyle = LineTrim),
        titleLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-0.1).sp, lineHeightStyle = LineTrim),
        titleMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, lineHeightStyle = LineTrim),
        titleSmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, lineHeightStyle = LineTrim),
        bodyLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, lineHeightStyle = LineTrim),
        bodyMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp, lineHeightStyle = LineTrim),
        bodySmall = TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp, lineHeightStyle = LineTrim),
        labelLarge = TextStyle(fontFamily = display, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp, lineHeightStyle = LineTrim),
        labelMedium = TextStyle(fontFamily = display, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, lineHeightStyle = LineTrim),
        labelSmall = TextStyle(fontFamily = display, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 15.sp, lineHeightStyle = LineTrim),
    )
}
