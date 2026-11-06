package cfd.invokeil.himadri.rcy.core.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Donor eligibility calculator — client-side mirror of the website's
 * computeEligibility (default gap 120 days, overridden by server config).
 */
object EligibilityCalc {

    data class Result(
        val eligible: Boolean,
        val lastDate: LocalDate?,
        val nextDate: LocalDate?,
        val daysLeft: Int,
        val count: Int,
    )

    fun compute(verifiedDates: List<String>, gapDays: Int = 120, today: LocalDate = LocalDate.now(ZoneId.of("Asia/Dhaka"))): Result {
        val clean = verifiedDates.mapNotNull { DateFmt.toLocalDate(it) }.sorted()
        val count = clean.size
        if (count == 0) return Result(true, null, null, 0, 0)
        val last = clean.last()
        val next = last.plusDays(gapDays.toLong())
        val daysLeft = maxOf(0L, ChronoUnit.DAYS.between(today, next)).toInt()
        return Result(!today.isBefore(next), last, next, daysLeft, count)
    }
}
