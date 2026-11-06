package cfd.invokeil.himadri.rcy.core.util

import cfd.invokeil.himadri.rcy.core.i18n.Lang
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Date helpers — mirrors the site's date formatting in BN/EN
 * (e.g. "৬ নভেম্বর ২০২৬" / "6 Nov 2026", relative "২ মিনিট আগে").
 */
object DateFmt {

    val BN_MONTHS = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val EN_MONTHS_SHORT = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val BN_DAYS = listOf("রবি", "সোম", "মঙ্গল", "বুধ", "বৃহঃ", "শুক্র", "শনি")
    val EN_DAYS = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    private val bnDigits = "০১২৩৪৫৬৭৮৯"

    fun toBnDigits(s: String): String = s.map { if (it in '0'..'9') bnDigits[it - '0'] else it }
        .joinToString("")

    fun num(n: Int, lang: Lang): String = if (lang == Lang.BN) toBnDigits(n.toString()) else n.toString()

    fun parse(iso: String?): OffsetDateTime? {
        if (iso.isNullOrBlank()) return null
        return runCatching { OffsetDateTime.parse(iso) }.getOrElse {
            runCatching { Instant.parse(iso).atZone(ZoneId.systemDefault()).toOffsetDateTime() }.getOrElse {
                runCatching {
                    LocalDate.parse(iso.take(10)).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime()
                }.getOrNull()
            }
        }
    }

    /** "6 Nov 2026" / "৬ নভেম্বর ২০২৬" */
    fun dayMonthYear(iso: String?, lang: Lang): String {
        val d = parse(iso) ?: return ""
        val day = d.dayOfMonth
        val month = d.monthValue
        val year = d.year
        return if (lang == Lang.BN) "${toBnDigits(day.toString())} ${BN_MONTHS[month - 1]} ${toBnDigits(year.toString())}"
        else "$day ${EN_MONTHS_SHORT[month - 1]} $year"
    }

    /** "Sat, 6 Nov" / "শনি, ৬ নভেম্বর" */
    fun dayNameDate(iso: String?, lang: Lang): String {
        val d = parse(iso) ?: return ""
        val dow = d.dayOfWeek.value % 7 // java: Mon=1..Sun=7 → Sun=0
        val day = d.dayOfMonth
        val month = d.monthValue
        return if (lang == Lang.BN) "${BN_DAYS[dow]}, ${toBnDigits(day.toString())} ${BN_MONTHS[month - 1]}"
        else "${EN_DAYS[dow]}, $day ${EN_MONTHS_SHORT[month - 1]}"
    }

    /** "4:00 PM" / "বিকাল ৪:০০" (approximate Bangla day-part naming) */
    fun time(iso: String?, lang: Lang): String {
        val d = parse(iso) ?: return ""
        val h24 = d.hour
        val m = d.minute
        return if (lang == Lang.BN) {
            val part = when {
                h24 < 6 -> "রাত"
                h24 < 12 -> "সকাল"
                h24 < 16 -> "দুপুর"
                h24 < 18 -> "বিকাল"
                else -> "রাত"
            }
            val h12 = when {
                h24 == 0 -> 12
                h24 > 12 -> h24 - 12
                else -> h24
            }
            "$part ${toBnDigits(h12.toString())}:${toBnDigits(m.toString().padStart(2, '0'))}"
        } else {
            val ampm = if (h24 < 12) "AM" else "PM"
            val h12 = when {
                h24 == 0 -> 12
                h24 > 12 -> h24 - 12
                else -> h24
            }
            "$h12:${m.toString().padStart(2, '0')} $ampm"
        }
    }

    fun dayMonthYearTime(iso: String?, lang: Lang): String {
        val date = dayMonthYear(iso, lang)
        val t = time(iso, lang)
        return if (date.isBlank()) "" else "$date${if (t.isBlank()) "" else ", $t"}"
    }

    /** Relative time — "just now / 5 min ago / 3 hr ago / 2 d ago" + BN variants. */
    fun relative(iso: String?, lang: Lang): String {
        val d = parse(iso) ?: return ""
        val mins = ChronoUnit.MINUTES.between(d, OffsetDateTime.now(ZoneId.systemDefault()))
        return when {
            mins < 1 -> if (lang == Lang.BN) "এইমাত্র" else "just now"
            mins < 60 -> if (lang == Lang.BN) "${toBnDigits(mins.toString())} মিনিট আগে" else "$mins min ago"
            mins < 60 * 24 -> {
                val h = mins / 60
                if (lang == Lang.BN) "${toBnDigits(h.toString())} ঘণ্টা আগে" else "$h hr ago"
            }
            mins < 60 * 24 * 30 -> {
                val days = mins / (60 * 24)
                if (lang == Lang.BN) "${toBnDigits(days.toString())} দিন আগে" else "$days d ago"
            }
            else -> dayMonthYear(iso, lang)
        }
    }

    /** yyyy-MM-dd → LocalDate */
    fun toLocalDate(iso: String?): LocalDate? {
        if (iso.isNullOrBlank()) return null
        return runCatching { LocalDate.parse(iso.take(10)) }.getOrNull()
    }

    fun isoDate(date: LocalDate): String = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
}
