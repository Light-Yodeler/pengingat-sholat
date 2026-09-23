package com.example.myapplication

import com.example.myapplication.core.calendar.HijriCalendarHelper
import com.example.myapplication.core.location.LocationPresets
import com.example.myapplication.core.prayer.PrayerTimeCalculator
import com.example.myapplication.core.prayer.PrayerType
import com.example.myapplication.core.qibla.QiblaCalculator
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs

class NoorWaktuCalculationTest {

    @Test
    fun qiblaCalculator_returnsAccurateAngleForJakarta() {
        val jakarta = LocationPresets.defaultCity
        val result = QiblaCalculator.calculate(jakarta.latitude, jakarta.longitude)

        // Jakarta Qibla is ~295.2 degrees from True North
        assertTrue("Azimuth should be around 295 degrees", abs(result.azimuthDegrees - 295.2) < 1.0)
        assertEquals("Barat Laut", result.formattedCompassDirection)
        // Distance is ~7900 km
        assertTrue("Distance to Kaaba should be around 7900 km", abs(result.distanceKm - 7900.0) < 150.0)
    }

    @Test
    fun prayerTimeCalculator_calculatesChronologicalTimes() {
        val jakarta = LocationPresets.defaultCity
        val testDate = LocalDate.of(2025, 2, 14)
        val schedule = PrayerTimeCalculator.calculate(
            date = testDate,
            latitude = jakarta.latitude,
            longitude = jakarta.longitude,
            altitudeMeters = jakarta.altitudeMeters,
            timeZoneHours = jakarta.timeZoneOffsetHours
        )

        // Verify logical chronological order of prayers
        assertTrue(schedule.imsak.isBefore(schedule.subuh))
        assertTrue(schedule.subuh.isBefore(schedule.terbit))
        assertTrue(schedule.terbit.isBefore(schedule.dzuhur))
        assertTrue(schedule.dzuhur.isBefore(schedule.asar))
        assertTrue(schedule.asar.isBefore(schedule.maghrib))
        assertTrue(schedule.maghrib.isBefore(schedule.isya))

        // Check realistic Indonesian prayer time windows (WIB)
        assertEquals(4, schedule.subuh.hour)
        assertEquals(12, schedule.dzuhur.hour)
        assertEquals(15, schedule.asar.hour)
        assertEquals(18, schedule.maghrib.hour)
        assertEquals(19, schedule.isya.hour)
    }

    @Test
    fun nextPrayerLogic_accuratelyIdentifiesRemainingTime() {
        val jakarta = LocationPresets.defaultCity
        val testDate = LocalDate.of(2025, 2, 14)
        val schedule = PrayerTimeCalculator.calculate(
            date = testDate,
            latitude = jakarta.latitude,
            longitude = jakarta.longitude
        )

        // At 14:00 WIB, the next prayer must be Asr
        val middayTime = LocalTime.of(14, 0)
        val nextInfo = PrayerTimeCalculator.getNextPrayer(schedule, middayTime)

        assertEquals(PrayerType.DZUHUR, nextInfo.current)
        assertEquals(PrayerType.ASAR, nextInfo.next)
        assertTrue(nextInfo.remainingSeconds > 0)
        assertTrue(nextInfo.progressPercent in 0f..1f)
    }

    @Test
    fun hijriCalendarHelper_formatsGregorianAndHijriCorrectly() {
        val testDate = LocalDate.of(2025, 2, 14)
        val gregorian = HijriCalendarHelper.formatGregorian(testDate)
        assertTrue(gregorian.contains("Jumat"))
        assertTrue(gregorian.contains("14"))
        assertTrue(gregorian.contains("2025"))

        val hijri = HijriCalendarHelper.formatHijri(testDate)
        assertTrue(hijri.endsWith("H"))
        assertTrue(hijri.contains("1446"))
    }
}
