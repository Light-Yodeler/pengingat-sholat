package com.example.myapplication.core.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.core.location.CityLocation
import com.example.myapplication.core.location.LocationPresets
import com.example.myapplication.core.model.AlertType
import com.example.myapplication.core.model.AppSettings
import com.example.myapplication.core.prayer.PrayerType
import org.json.JSONObject

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "noor_waktu_preferences"

        // Settings Keys
        private const val KEY_SUBUH_MUAZZIN_ID = "subuh_muazzin_id"
        private const val KEY_REGULAR_MUAZZIN_ID = "regular_muazzin_id"
        private const val KEY_AZAN_VOLUME = "azan_volume"
        private const val KEY_AUTO_SILENT = "auto_silent"
        private const val KEY_SUBUH_EARLY_REMINDER = "subuh_early_reminder"
        private const val KEY_MAGHRIB_IFTAR_REMINDER = "maghrib_iftar_reminder"

        // Prayer Alerts
        private const val KEY_ALERT_SUBUH = "alert_subuh"
        private const val KEY_ALERT_DZUHUR = "alert_dzuhur"
        private const val KEY_ALERT_ASAR = "alert_asar"
        private const val KEY_ALERT_MAGHRIB = "alert_maghrib"
        private const val KEY_ALERT_ISYA = "alert_isya"

        // Location Keys
        private const val KEY_LOC_ID = "loc_id"
        private const val KEY_LOC_NAME = "loc_name"
        private const val KEY_LOC_DETAIL = "loc_detail"
        private const val KEY_LOC_LAT = "loc_lat"
        private const val KEY_LOC_LON = "loc_lon"
        private const val KEY_LOC_ALT = "loc_alt"
        private const val KEY_LOC_TZ = "loc_tz"

        // Dhikr Keys
        private const val KEY_DHIKR_CATEGORY = "dhikr_category"
        private const val KEY_DHIKR_COUNTS_JSON = "dhikr_counts_json"
    }

    fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putInt(KEY_SUBUH_MUAZZIN_ID, settings.selectedSubuhMuazzinId)
            putInt(KEY_REGULAR_MUAZZIN_ID, settings.selectedRegularMuazzinId)
            putInt(KEY_AZAN_VOLUME, settings.azanVolumePercent)
            putBoolean(KEY_AUTO_SILENT, settings.autoSilentMode)
            putBoolean(KEY_SUBUH_EARLY_REMINDER, settings.subuhEarlyReminder)
            putBoolean(KEY_MAGHRIB_IFTAR_REMINDER, settings.maghribIftarDuaReminder)

            // Save individual prayer alert modes
            putString(KEY_ALERT_SUBUH, (settings.prayerAlertTypes[PrayerType.SUBUH] ?: AlertType.AZAN).name)
            putString(KEY_ALERT_DZUHUR, (settings.prayerAlertTypes[PrayerType.DZUHUR] ?: AlertType.AZAN).name)
            putString(KEY_ALERT_ASAR, (settings.prayerAlertTypes[PrayerType.ASAR] ?: AlertType.AZAN).name)
            putString(KEY_ALERT_MAGHRIB, (settings.prayerAlertTypes[PrayerType.MAGHRIB] ?: AlertType.AZAN).name)
            putString(KEY_ALERT_ISYA, (settings.prayerAlertTypes[PrayerType.ISYA] ?: AlertType.AZAN).name)
            apply()
        }
    }

    fun loadSettings(): AppSettings {
        val subuhMuazzinId = prefs.getInt(KEY_SUBUH_MUAZZIN_ID, 1)
        val regularMuazzinId = prefs.getInt(KEY_REGULAR_MUAZZIN_ID, 1)
        val volume = prefs.getInt(KEY_AZAN_VOLUME, 85)
        val autoSilent = prefs.getBoolean(KEY_AUTO_SILENT, true)
        val subuhEarly = prefs.getBoolean(KEY_SUBUH_EARLY_REMINDER, true)
        val maghribIftar = prefs.getBoolean(KEY_MAGHRIB_IFTAR_REMINDER, true)

        val prayerAlerts = mutableMapOf<PrayerType, AlertType>()
        prayerAlerts[PrayerType.SUBUH] = parseAlertType(prefs.getString(KEY_ALERT_SUBUH, null))
        prayerAlerts[PrayerType.DZUHUR] = parseAlertType(prefs.getString(KEY_ALERT_DZUHUR, null))
        prayerAlerts[PrayerType.ASAR] = parseAlertType(prefs.getString(KEY_ALERT_ASAR, null))
        prayerAlerts[PrayerType.MAGHRIB] = parseAlertType(prefs.getString(KEY_ALERT_MAGHRIB, null))
        prayerAlerts[PrayerType.ISYA] = parseAlertType(prefs.getString(KEY_ALERT_ISYA, null))

        return AppSettings(
            autoSilentMode = autoSilent,
            azanVolumePercent = volume,
            selectedSubuhMuazzinId = subuhMuazzinId,
            selectedRegularMuazzinId = regularMuazzinId,
            prayerAlertTypes = prayerAlerts,
            subuhEarlyReminder = subuhEarly,
            maghribIftarDuaReminder = maghribIftar
        )
    }

    private fun parseAlertType(name: String?): AlertType {
        if (name == null) return AlertType.AZAN
        return try {
            AlertType.valueOf(name)
        } catch (_: Exception) {
            AlertType.AZAN
        }
    }

    fun saveLocation(location: CityLocation) {
        prefs.edit().apply {
            putString(KEY_LOC_ID, location.id)
            putString(KEY_LOC_NAME, location.name)
            putString(KEY_LOC_DETAIL, location.detail)
            putFloat(KEY_LOC_LAT, location.latitude.toFloat())
            putFloat(KEY_LOC_LON, location.longitude.toFloat())
            putFloat(KEY_LOC_ALT, location.altitudeMeters.toFloat())
            putFloat(KEY_LOC_TZ, location.timeZoneOffsetHours.toFloat())
            apply()
        }
    }

    fun loadLocation(): CityLocation {
        val id = prefs.getString(KEY_LOC_ID, null) ?: return LocationPresets.defaultCity
        val name = prefs.getString(KEY_LOC_NAME, LocationPresets.defaultCity.name) ?: LocationPresets.defaultCity.name
        val detail = prefs.getString(KEY_LOC_DETAIL, LocationPresets.defaultCity.detail) ?: LocationPresets.defaultCity.detail
        val lat = prefs.getFloat(KEY_LOC_LAT, LocationPresets.defaultCity.latitude.toFloat()).toDouble()
        val lon = prefs.getFloat(KEY_LOC_LON, LocationPresets.defaultCity.longitude.toFloat()).toDouble()
        val alt = prefs.getFloat(KEY_LOC_ALT, LocationPresets.defaultCity.altitudeMeters.toFloat()).toDouble()
        val tz = prefs.getFloat(KEY_LOC_TZ, LocationPresets.defaultCity.timeZoneOffsetHours.toFloat()).toDouble()

        return CityLocation(
            id = id,
            name = name,
            detail = detail,
            latitude = lat,
            longitude = lon,
            altitudeMeters = alt,
            timeZoneOffsetHours = tz
        )
    }

    fun saveDhikrCounts(counts: Map<String, Int>) {
        val json = JSONObject()
        counts.forEach { (key, value) ->
            json.put(key, value)
        }
        prefs.edit().putString(KEY_DHIKR_COUNTS_JSON, json.toString()).apply()
    }

    fun loadDhikrCounts(): Map<String, Int> {
        val jsonStr = prefs.getString(KEY_DHIKR_COUNTS_JSON, null) ?: return emptyMap()
        val result = mutableMapOf<String, Int>()
        try {
            val json = JSONObject(jsonStr)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                result[key] = json.getInt(key)
            }
        } catch (_: Exception) {
            // return empty on error
        }
        return result
    }

    fun saveDhikrCategory(category: String) {
        prefs.edit().putString(KEY_DHIKR_CATEGORY, category).apply()
    }

    fun loadDhikrCategory(): String {
        return prefs.getString(KEY_DHIKR_CATEGORY, "Setelah Salat") ?: "Setelah Salat"
    }
}
