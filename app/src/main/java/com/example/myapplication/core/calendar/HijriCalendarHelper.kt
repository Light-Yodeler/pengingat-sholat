package com.example.myapplication.core.calendar

import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

object HijriCalendarHelper {

    private val HIJRI_MONTH_NAMES = listOf(
        "Muharram",
        "Safar",
        "Rabi'ul Awwal",
        "Rabi'ul Akhir",
        "Jumadil Awwal",
        "Jumadil Akhir",
        "Rajab",
        "Sya'ban",
        "Ramadhan",
        "Syawwal",
        "Dzulqa'dah",
        "Dzulhijjah"
    )

    private val DAY_NAMES_ID = listOf(
        "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu"
    )

    private val MONTH_NAMES_ID = listOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agt", "Sep", "Okt", "Nov", "Des"
    )

    fun formatGregorian(date: LocalDate): String {
        val dayName = DAY_NAMES_ID[date.dayOfWeek.value - 1]
        val day = date.dayOfMonth
        val month = MONTH_NAMES_ID[date.monthValue - 1]
        val year = date.year
        return "$dayName, $day $month $year"
    }

    fun formatHijri(date: LocalDate): String {
        return try {
            val hijrahDate = HijrahDate.from(date)
            val hDay = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val hMonth = hijrahDate.get(ChronoField.MONTH_OF_YEAR)
            val hYear = hijrahDate.get(ChronoField.YEAR)
            val monthName = HIJRI_MONTH_NAMES.getOrElse(hMonth - 1) { "Bulan ke-$hMonth" }
            "$hDay $monthName $hYear H"
        } catch (_: Exception) {
            // Fallback approximation when java.time HijrahChronology is unavailable
            approximateHijri(date)
        }
    }

    private fun approximateHijri(date: LocalDate): String {
        val jd = toJulianDay(date.year, date.monthValue, date.dayOfMonth)
        val l = (jd - 1948440 + 10632).toInt()
        val n = ((l - 1) / 10631).toInt()
        val lRem = l - 10631 * n + 354
        val j = (((10985 - lRem) / 5316).toInt()) * (((50 * lRem) / 17719).toInt()) +
                ((lRem / 5670).toInt()) * (((43 * lRem) / 15238).toInt())
        val lNew = lRem - (((30 - j) / 15).toInt()) * (((17719 * j) / 50).toInt()) -
                ((j / 16).toInt()) * (((15238 * j) / 43).toInt()) + 29
        val m = ((24 * lNew) / 709).toInt()
        val d = lNew - ((709 * m) / 24).toInt()
        val y = 30 * n + j - 30

        val monthName = HIJRI_MONTH_NAMES.getOrElse(m - 1) { "Bulan ke-$m" }
        return "$d $monthName $y H"
    }

    private fun toJulianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = (y / 100)
        val b = 2 - a + (a / 4)
        return (365.25 * (y + 4716)).toInt() + (30.6001 * (m + 1)).toInt() + day + b - 1524.5
    }
}
