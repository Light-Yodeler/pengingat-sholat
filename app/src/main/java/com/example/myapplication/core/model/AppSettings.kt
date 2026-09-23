package com.example.myapplication.core.model

import com.example.myapplication.R
import com.example.myapplication.core.prayer.PrayerType

enum class AlertType(val label: String) {
    AZAN("Bersuara"),
    SILENT("Senyap"),
    OFF("Mati")
}

data class MuazzinOption(
    val id: Int,
    val title: String,
    val name: String,
    val durationText: String,
    val rawResId: Int,
    val isFajrExclusive: Boolean = false
)

object MuazzinList {
    val subuhOptions = listOf(
        MuazzinOption(
            id = 1,
            title = "Azan Subuh Syeikh Muhammad Nafea",
            name = "Syeikh Muhammad Nafea",
            durationText = "03:16",
            rawResId = R.raw.azan_fajr_nafea,
            isFajrExclusive = true
        ),
        MuazzinOption(
            id = 2,
            title = "Azan Subuh Masjid Nabawi",
            name = "Syeikh Muhammad Marwan Qassas",
            durationText = "03:32",
            rawResId = R.raw.azan_fajr_madinah,
            isFajrExclusive = true
        )
    )

    val regularOptions = listOf(
        MuazzinOption(
            id = 1,
            title = "Azan Masjidil Haram",
            name = "Makkah Mukarramah",
            durationText = "03:42",
            rawResId = R.raw.azan_mekah
        ),
        MuazzinOption(
            id = 2,
            title = "Azan Masjid Nabawi",
            name = "Madinah Al-Munawwarah",
            durationText = "03:20",
            rawResId = R.raw.azan_madinah
        )
    )

    val allPreviewableItems = subuhOptions + regularOptions
}

data class AppSettings(
    val autoSilentMode: Boolean = true,
    val azanVolumePercent: Int = 85,
    val selectedSubuhMuazzinId: Int = 1, // 1: Muhammad Nafea, 2: Masjid Nabawi Subuh
    val selectedRegularMuazzinId: Int = 1, // 1: Masjidil Haram, 2: Masjid Nabawi
    val prayerAlertTypes: Map<PrayerType, AlertType> = mapOf(
        PrayerType.SUBUH to AlertType.AZAN,
        PrayerType.DZUHUR to AlertType.AZAN,
        PrayerType.ASAR to AlertType.AZAN,
        PrayerType.MAGHRIB to AlertType.AZAN,
        PrayerType.ISYA to AlertType.AZAN
    ),
    val subuhEarlyReminder: Boolean = true,
    val maghribIftarDuaReminder: Boolean = true,
    val calculationMethodName: String = "Kemenag RI (Subuh 20°, Isya 18°)",
    val ikhtiyatMinutes: Int = 2
)
