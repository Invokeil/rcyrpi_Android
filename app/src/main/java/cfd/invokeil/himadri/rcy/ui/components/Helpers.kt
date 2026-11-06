package cfd.invokeil.himadri.rcy.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import cfd.invokeil.himadri.rcy.LocalAppVM
import cfd.invokeil.himadri.rcy.LocalLang
import cfd.invokeil.himadri.rcy.core.i18n.Lang
import cfd.invokeil.himadri.rcy.core.i18n.LText
import cfd.invokeil.himadri.rcy.core.i18n.S
import cfd.invokeil.himadri.rcy.core.i18n.t
import cfd.invokeil.himadri.rcy.core.util.AppError
import cfd.invokeil.himadri.rcy.core.util.DateFmt
import cfd.invokeil.himadri.rcy.core.theme.BrandGreen
import cfd.invokeil.himadri.rcy.core.theme.BrandRed
import cfd.invokeil.himadri.rcy.core.theme.Chart3
import cfd.invokeil.himadri.rcy.core.theme.Chart4
import cfd.invokeil.himadri.rcy.core.theme.Chart5

/** Translate a dictionary key with the ambient UI language. */
@Composable
fun tl(key: String): String = t(LocalLang.current, key)

/** Localized content string from an LText field. */
@Composable
fun lp(text: LText?): String = text?.pick(LocalLang.current) ?: ""

/** Current ambient language. */
@Composable
fun currentLang(): Lang = LocalLang.current

/** Server error code → friendly localized message. */
@Composable
fun errorText(code: String?): String = when (code) {
    null -> ""
    "network_unavailable" -> tl("error_generic")
    "cloudflare_challenge" -> tl("error_generic")
    "login_required" -> tl("login_btn")
    "invalid_credentials" -> tl("error_generic")
    "suspended" -> tl("error_generic")
    "not_admin" -> tl("error_generic")
    "too_many_requests", "too_many_attempts" -> tl("error_generic")
    else -> tl("error_generic")
}

/** One-line display text for an AppError (offline vs server vs challenge). */
@Composable
fun appErrorText(e: AppError?): String = when (e) {
    null -> ""
    AppError.Network -> tl("error_generic")
    AppError.CloudflareChallenge -> tl("error_generic")
    AppError.Unauthorized -> tl("login_btn")
    AppError.Server, is AppError.Api -> tl("error_generic")
}

/** Red/green severity color used across emergency UI (site convention). */
fun severityColor(severity: String): Color = when (severity) {
    "critical" -> BrandRed
    "high" -> Color(0xFFE85D04)
    "medium" -> Chart3
    else -> BrandGreen
}

/** Palette used for activity cards / icon chips (site: red|green pairs). */
val activityPalette = listOf(BrandRed, BrandGreen, Chart4, Chart5, Chart3)

/** ৳ amount formatting — "500" / "৳1,200" */
fun money(amount: Double, withSymbol: Boolean = true): String {
    val n = if (amount == amount.toLong().toDouble()) amount.toLong().toString()
    else String.format("%.2f", amount)
    val grouped = StringBuilder()
    var count = 0
    for (i in n.length - 1 downTo 0) {
        val c = n[i]
        grouped.append(c)
        if (c.isDigit()) {
            count++
            if (count % 3 == 0 && i > 0 && n[i - 1].isDigit()) grouped.append(',')
        }
    }
    val out = grouped.reverse().toString()
    return if (withSymbol) "৳$out" else out
}

/** relative time using ambient language */
@Composable
fun relTime(iso: String?): String = DateFmt.relative(iso, LocalLang.current)

/** date + time using ambient language */
@Composable
fun fullDate(iso: String?): String = DateFmt.dayMonthYearTime(iso, LocalLang.current)
