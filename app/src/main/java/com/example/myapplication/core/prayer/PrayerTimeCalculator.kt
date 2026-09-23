package com.example.myapplication.core.prayer

import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.*

data class PrayerSchedule(
    val date: LocalDate,
    val imsak: LocalTime,
    val subuh: LocalTime,
    val terbit: LocalTime,
    val dzuhur: LocalTime,
    val asar: LocalTime,
    val maghrib: LocalTime,
    val isya: LocalTime
)

enum class PrayerType(val displayName: String, val arabicName: String) {
    IMSAK("Imsak", "الإمساك"),
    SUBUH("Subuh", "الفجر"),
    TERBIT("Terbit", "الشروق"),
    DZUHUR("Dzuhur", "الظهر"),
    ASAR("Asar", "العصر"),
    MAGHRIB("Maghrib", "المغرب"),
    ISYA("Isya", "العشاء")
}

data class NextPrayerInfo(
    val current: PrayerType,
    val next: PrayerType,
    val nextTime: LocalTime,
    val remainingSeconds: Long,
    val totalIntervalSeconds: Long,
    val progressPercent: Float
)

object PrayerTimeCalculator {

    // Kemenag RI standard angles in degrees
    private const val FAJR_ANGLE = 20.0
    private const val ISHA_ANGLE = 18.0
    private const val IKHTIYAT_MINUTES = 2L

    fun calculate(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        altitudeMeters: Double = 25.0,
        timeZoneHours: Double = 7.0
    ): PrayerSchedule {
        val julianDate = toJulianDate(date.year, date.monthValue, date.dayOfMonth)
        val d = julianDate - 2451545.0

        val q = fixAngle(280.459 + 0.98564736 * d)
        val g = fixAngle(357.529 + 0.98560028 * d)
        val gRad = Math.toRadians(g)

        val l = fixAngle(q + 1.915 * sin(gRad) + 0.020 * sin(2 * gRad))
        val lRad = Math.toRadians(l)

        val e = 23.439 - 0.00000036 * d
        val eRad = Math.toRadians(e)

        val ra = fixAngle(Math.toDegrees(atan2(cos(eRad) * sin(lRad), cos(lRad)))) / 15.0
        val dSun = Math.toDegrees(asin(sin(eRad) * sin(lRad)))
        val eqT = (q / 15.0) - fixHour(ra)

        val noon = fixHour(12.0 + timeZoneHours - (longitude / 15.0) - eqT)

        // Refraction and elevation angle adjustment
        val dipAngle = 0.0347 * sqrt(max(0.0, altitudeMeters))
        val sunriseSunAlt = -(0.833 + dipAngle)

        val sunriseHours = calculateSunAngleHour(sunriseSunAlt, latitude, dSun)
        val fajrHours = calculateSunAngleHour(-FAJR_ANGLE, latitude, dSun)
        val ishaHours = calculateSunAngleHour(-ISHA_ANGLE, latitude, dSun)

        // Asr angle using Shafi'i shadow multiplier (shadow length = object + noon shadow)
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(dSun)
        val noonAltRad = Math.PI / 2.0 - abs(latRad - decRad)
        val asrAltRad = atan(1.0 / (1.0 + tan(Math.PI / 2.0 - noonAltRad)))
        val asrSunAlt = Math.toDegrees(asrAltRad)
        val asrHours = calculateSunAngleHour(asrSunAlt, latitude, dSun)

        val fajrTime = toLocalTime(noon - fajrHours).plusMinutes(IKHTIYAT_MINUTES)
        val sunriseTime = toLocalTime(noon - sunriseHours)
        val dhuhrTime = toLocalTime(noon).plusMinutes(IKHTIYAT_MINUTES)
        val asrTime = toLocalTime(noon + asrHours).plusMinutes(IKHTIYAT_MINUTES)
        val maghribTime = toLocalTime(noon + sunriseHours).plusMinutes(IKHTIYAT_MINUTES)
        val ishaTime = toLocalTime(noon + ishaHours).plusMinutes(IKHTIYAT_MINUTES)
        val imsakTime = fajrTime.minusMinutes(10)

        return PrayerSchedule(
            date = date,
            imsak = imsakTime,
            subuh = fajrTime,
            terbit = sunriseTime,
            dzuhur = dhuhrTime,
            asar = asrTime,
            maghrib = maghribTime,
            isya = ishaTime
        )
    }

    fun getNextPrayer(schedule: PrayerSchedule, now: LocalTime): NextPrayerInfo {
        val nowSec = now.toSecondOfDay()

        val times = listOf(
            PrayerType.SUBUH to schedule.subuh.toSecondOfDay(),
            PrayerType.DZUHUR to schedule.dzuhur.toSecondOfDay(),
            PrayerType.ASAR to schedule.asar.toSecondOfDay(),
            PrayerType.MAGHRIB to schedule.maghrib.toSecondOfDay(),
            PrayerType.ISYA to schedule.isya.toSecondOfDay()
        )

        for (i in times.indices) {
            val (type, sec) = times[i]
            if (nowSec < sec) {
                val prevType = if (i == 0) PrayerType.ISYA else times[i - 1].first
                val prevSec = if (i == 0) times.last().second - 86400 else times[i - 1].second
                val total = sec - prevSec
                val remaining = sec - nowSec
                val progress = 1f - (remaining.toFloat() / max(1, total))
                val targetTime = when (type) {
                    PrayerType.SUBUH -> schedule.subuh
                    PrayerType.DZUHUR -> schedule.dzuhur
                    PrayerType.ASAR -> schedule.asar
                    PrayerType.MAGHRIB -> schedule.maghrib
                    PrayerType.ISYA -> schedule.isya
                    else -> schedule.subuh
                }
                return NextPrayerInfo(
                    current = prevType,
                    next = type,
                    nextTime = targetTime,
                    remainingSeconds = remaining.toLong(),
                    totalIntervalSeconds = total.toLong(),
                    progressPercent = progress.coerceIn(0f, 1f)
                )
            }
        }

        // Past Isha, next is tomorrow Fajr
        val tomorrowFajrSec = times.first().second + 86400
        val ishaSec = times.last().second
        val total = tomorrowFajrSec - ishaSec
        val remaining = tomorrowFajrSec - nowSec
        val progress = 1f - (remaining.toFloat() / max(1, total))
        return NextPrayerInfo(
            current = PrayerType.ISYA,
            next = PrayerType.SUBUH,
            nextTime = schedule.subuh,
            remainingSeconds = remaining.toLong(),
            totalIntervalSeconds = total.toLong(),
            progressPercent = progress.coerceIn(0f, 1f)
        )
    }

    private fun calculateSunAngleHour(targetAlt: Double, latitude: Double, declination: Double): Double {
        val latRad = Math.toRadians(latitude)
        val decRad = Math.toRadians(declination)
        val altRad = Math.toRadians(targetAlt)

        val cosH = (sin(altRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val clampedCosH = cosH.coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(clampedCosH)) / 15.0
    }

    private fun toJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixHour(h: Double): Double {
        var a = h - 24.0 * floor(h / 24.0)
        if (a < 0.0) a += 24.0
        return a
    }

    private fun fixAngle(a: Double): Double {
        var r = a - 360.0 * floor(a / 360.0)
        if (r < 0.0) r += 360.0
        return r
    }

    private fun toLocalTime(decimalHours: Double): LocalTime {
        val fixed = fixHour(decimalHours)
        val totalSeconds = (fixed * 3600.0).roundToInt() % 86400
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return LocalTime.of(hours, minutes, seconds)
    }
}
