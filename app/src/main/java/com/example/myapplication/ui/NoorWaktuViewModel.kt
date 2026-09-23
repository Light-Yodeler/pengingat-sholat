package com.example.myapplication.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.core.alarm.AzanAlarmScheduler
import com.example.myapplication.core.audio.AzanAudioManager
import com.example.myapplication.core.location.CityLocation
import com.example.myapplication.core.location.LocationManagerHelper
import com.example.myapplication.core.location.LocationPresets
import com.example.myapplication.core.model.*
import com.example.myapplication.core.prayer.NextPrayerInfo
import com.example.myapplication.core.prayer.PrayerSchedule
import com.example.myapplication.core.prayer.PrayerTimeCalculator
import com.example.myapplication.core.prayer.PrayerType
import com.example.myapplication.core.qibla.QiblaCalculator
import com.example.myapplication.core.qibla.QiblaResult
import com.example.myapplication.core.preferences.AppPreferences
import android.widget.Toast
import com.example.myapplication.core.sensor.CompassSensorManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.abs

class NoorWaktuViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    val preferences = AppPreferences(context)
    val audioManager = AzanAudioManager(context)
    val compassManager = CompassSensorManager(context)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val initialLocation = preferences.loadLocation()
    private val initialSettings = preferences.loadSettings()
    private val initialSchedule = calculateCurrentSchedule(initialLocation)

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _currentLocation = MutableStateFlow(initialLocation)
    val currentLocation: StateFlow<CityLocation> = _currentLocation.asStateFlow()

    private val _isLocationPermissionGranted = MutableStateFlow(LocationManagerHelper.hasLocationPermission(context))
    val isLocationPermissionGranted: StateFlow<Boolean> = _isLocationPermissionGranted.asStateFlow()

    private val _isGpsEnabled = MutableStateFlow(LocationManagerHelper.isGpsEnabled(context))
    val isGpsEnabled: StateFlow<Boolean> = _isGpsEnabled.asStateFlow()

    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation.asStateFlow()

    private val _schedule = MutableStateFlow(initialSchedule)
    val schedule: StateFlow<PrayerSchedule> = _schedule.asStateFlow()

    private val _nextPrayer = MutableStateFlow(
        PrayerTimeCalculator.getNextPrayer(initialSchedule, LocalTime.now())
    )
    val nextPrayer: StateFlow<NextPrayerInfo> = _nextPrayer.asStateFlow()

    private val _qiblaResult = MutableStateFlow(
        QiblaCalculator.calculate(initialLocation.latitude, initialLocation.longitude)
    )
    val qiblaResult: StateFlow<QiblaResult> = _qiblaResult.asStateFlow()

    private val _isFacingQibla = MutableStateFlow(false)
    val isFacingQibla: StateFlow<Boolean> = _isFacingQibla.asStateFlow()

    private val _isQiblaLocked = MutableStateFlow(false)
    val isQiblaLocked: StateFlow<Boolean> = _isQiblaLocked.asStateFlow()

    private val _settings = MutableStateFlow(initialSettings)
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _isPreviewPlaying = MutableStateFlow(false)
    val isPreviewPlaying: StateFlow<Boolean> = _isPreviewPlaying.asStateFlow()

    private val _previewPlayingResId = MutableStateFlow<Int?>(null)
    val previewPlayingResId: StateFlow<Int?> = _previewPlayingResId.asStateFlow()

    private val _showProfileSheet = MutableStateFlow(false)
    val showProfileSheet: StateFlow<Boolean> = _showProfileSheet.asStateFlow()

    private val _showLocationPicker = MutableStateFlow(false)
    val showLocationPicker: StateFlow<Boolean> = _showLocationPicker.asStateFlow()

    // Dhikr State
    private val _dhikrCategory = MutableStateFlow(preferences.loadDhikrCategory())
    val dhikrCategory: StateFlow<String> = _dhikrCategory.asStateFlow()

    private val _activeDhikrIndex = MutableStateFlow(0)
    val activeDhikrIndex: StateFlow<Int> = _activeDhikrIndex.asStateFlow()

    // Map of dhikr item ID -> count
    private val _dhikrCounts = MutableStateFlow<Map<String, Int>>(preferences.loadDhikrCounts())
    val dhikrCounts: StateFlow<Map<String, Int>> = _dhikrCounts.asStateFlow()

    private val _tasbihCount = MutableStateFlow(0)
    val tasbihCount: StateFlow<Int> = _tasbihCount.asStateFlow()

    private val _vibrateFeedback = MutableStateFlow(true)
    val vibrateFeedback: StateFlow<Boolean> = _vibrateFeedback.asStateFlow()

    private var hasVibratedForCurrentAlignment = false

    init {
        val items = DhikrPresets.getItemsForCategory(_dhikrCategory.value)
        val firstItem = items.firstOrNull()
        if (firstItem != null) {
            _tasbihCount.value = _dhikrCounts.value[firstItem.id] ?: 0
        }
        startRealtimeTicker()
        observeCompass()
        checkAndFetchInitialGps()
        scheduleBackgroundAlarms()
    }


    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun toggleProfileSheet(show: Boolean) {
        _showProfileSheet.value = show
    }

    fun toggleLocationPicker(show: Boolean) {
        _showLocationPicker.value = show
    }

    fun toggleQiblaLock() {
        val newState = !_isQiblaLocked.value
        _isQiblaLocked.value = newState
        triggerHaptic(50L)
    }

    fun checkAndFetchInitialGps() {
        val hasPerm = LocationManagerHelper.hasLocationPermission(context)
        val gpsOn = LocationManagerHelper.isGpsEnabled(context)
        _isLocationPermissionGranted.value = hasPerm
        _isGpsEnabled.value = gpsOn

        if (hasPerm && gpsOn && _currentLocation.value.id == "gps") {
            refreshLocationFromGps()
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _isLocationPermissionGranted.value = granted
        if (granted) {
            _isGpsEnabled.value = LocationManagerHelper.isGpsEnabled(context)
            refreshLocationFromGps()
        }
    }

    fun refreshLocationFromGps() {
        viewModelScope.launch {
            _isLoadingLocation.value = true
            val detected = LocationManagerHelper.getCurrentLocation(context)
            if (detected != null) {
                selectCity(detected)
            }
            _isLoadingLocation.value = false
        }
    }

    fun selectCity(city: CityLocation) {
        _currentLocation.value = city
        preferences.saveLocation(city)
        val newSchedule = calculateCurrentSchedule(city)
        _schedule.value = newSchedule
        _nextPrayer.value = PrayerTimeCalculator.getNextPrayer(newSchedule, LocalTime.now())
        _qiblaResult.value = QiblaCalculator.calculate(city.latitude, city.longitude)
        scheduleBackgroundAlarms()
        Toast.makeText(context, "Lokasi: ${city.name}", Toast.LENGTH_SHORT).show()
    }

    private fun calculateCurrentSchedule(city: CityLocation): PrayerSchedule {
        return PrayerTimeCalculator.calculate(
            date = LocalDate.now(),
            latitude = city.latitude,
            longitude = city.longitude,
            altitudeMeters = city.altitudeMeters,
            timeZoneHours = city.timeZoneOffsetHours
        )
    }

    private fun startRealtimeTicker() {
        viewModelScope.launch {
            while (isActive) {
                val now = LocalTime.now()
                _nextPrayer.value = PrayerTimeCalculator.getNextPrayer(_schedule.value, now)
                delay(1000L)
            }
        }
    }

    private fun observeCompass() {
        viewModelScope.launch {
            compassManager.azimuthFlow.collect { deviceAzimuth ->
                if (_isQiblaLocked.value) return@collect

                val qiblaAzimuth = _qiblaResult.value.azimuthDegrees.toFloat()
                var diff = abs(deviceAzimuth - qiblaAzimuth)
                if (diff > 180f) diff = 360f - diff

                val aligned = diff <= 4.0f
                _isFacingQibla.value = aligned

                if (aligned && !hasVibratedForCurrentAlignment) {
                    hasVibratedForCurrentAlignment = true
                    triggerHaptic(120L, isImportant = true)
                } else if (!aligned) {
                    hasVibratedForCurrentAlignment = false
                }
            }
        }
    }

    fun scheduleBackgroundAlarms() {
        AzanAlarmScheduler.scheduleAllPrayers(context, _schedule.value, _settings.value)
    }

    fun playAudioPreview(rawResId: Int? = null) {
        val defaultResId = if (_settings.value.selectedRegularMuazzinId == 2) R.raw.azan_madinah else R.raw.azan_mekah
        val targetResId = rawResId ?: defaultResId
        if (_isPreviewPlaying.value && _previewPlayingResId.value == targetResId) {
            audioManager.stop()
            _isPreviewPlaying.value = false
            _previewPlayingResId.value = null
        } else {
            _isPreviewPlaying.value = true
            _previewPlayingResId.value = targetResId
            audioManager.playPreview(targetResId, _settings.value.azanVolumePercent) {
                _isPreviewPlaying.value = false
                _previewPlayingResId.value = null
            }
        }
    }

    fun toggleAutoSilent(enabled: Boolean) {
        _settings.value = _settings.value.copy(autoSilentMode = enabled)
        preferences.saveSettings(_settings.value)
    }

    fun setAzanVolume(volumePercent: Int) {
        _settings.value = _settings.value.copy(azanVolumePercent = volumePercent)
        preferences.saveSettings(_settings.value)
        audioManager.updateVolume(volumePercent)
    }

    fun selectSubuhMuazzin(id: Int) {
        _settings.value = _settings.value.copy(selectedSubuhMuazzinId = id)
        preferences.saveSettings(_settings.value)
        scheduleBackgroundAlarms()
        val muazzinName = MuazzinList.subuhOptions.find { it.id == id }?.title ?: "Azan Subuh"
        Toast.makeText(context, "$muazzinName berhasil disimpan", Toast.LENGTH_SHORT).show()
    }

    fun selectRegularMuazzin(id: Int) {
        _settings.value = _settings.value.copy(selectedRegularMuazzinId = id)
        preferences.saveSettings(_settings.value)
        scheduleBackgroundAlarms()
        val muazzinName = MuazzinList.regularOptions.find { it.id == id }?.title ?: "Azan Salat"
        Toast.makeText(context, "$muazzinName berhasil disimpan", Toast.LENGTH_SHORT).show()
    }

    fun setPrayerAlert(prayerType: PrayerType, alertType: AlertType) {
        val updated = _settings.value.prayerAlertTypes.toMutableMap()
        updated[prayerType] = alertType
        _settings.value = _settings.value.copy(prayerAlertTypes = updated)
        preferences.saveSettings(_settings.value)
        scheduleBackgroundAlarms()
        val modeLabel = when (alertType) {
            AlertType.AZAN -> "Bersuara"
            AlertType.SILENT -> "Senyap"
            AlertType.OFF -> "Mati"
        }
        Toast.makeText(context, "Notifikasi ${prayerType.displayName}: $modeLabel tersimpan", Toast.LENGTH_SHORT).show()
    }

    fun cyclePrayerAlert(prayerType: PrayerType) {
        val current = _settings.value.prayerAlertTypes[prayerType] ?: AlertType.AZAN
        val next = when (current) {
            AlertType.AZAN -> AlertType.SILENT
            AlertType.SILENT -> AlertType.OFF
            AlertType.OFF -> AlertType.AZAN
        }
        setPrayerAlert(prayerType, next)
        triggerHaptic(50L)
    }

    fun toggleSubuhEarlyReminder(enabled: Boolean) {
        _settings.value = _settings.value.copy(subuhEarlyReminder = enabled)
        preferences.saveSettings(_settings.value)
    }

    fun toggleMaghribIftarDua(enabled: Boolean) {
        _settings.value = _settings.value.copy(maghribIftarDuaReminder = enabled)
        preferences.saveSettings(_settings.value)
    }

    fun incrementTasbih() {
        val items = DhikrPresets.getItemsForCategory(_dhikrCategory.value)
        val currentItem = items.getOrElse(_activeDhikrIndex.value) { items[0] }
        val target = currentItem.targetCount

        val currentVal = _dhikrCounts.value[currentItem.id] ?: 0
        val nextVal = currentVal + 1

        val updatedMap = _dhikrCounts.value.toMutableMap()

        if (nextVal >= target) {
            updatedMap[currentItem.id] = target
            _dhikrCounts.value = updatedMap
            preferences.saveDhikrCounts(updatedMap)
            _tasbihCount.value = target
            triggerPatternHaptic()

            // Automatically move to next item if available
            if (_activeDhikrIndex.value < items.size - 1) {
                val nextIdx = _activeDhikrIndex.value + 1
                _activeDhikrIndex.value = nextIdx
                val nextItem = items[nextIdx]
                _tasbihCount.value = _dhikrCounts.value[nextItem.id] ?: 0
            }
        } else {
            updatedMap[currentItem.id] = nextVal
            _dhikrCounts.value = updatedMap
            preferences.saveDhikrCounts(updatedMap)
            _tasbihCount.value = nextVal
            if (_vibrateFeedback.value) {
                triggerHaptic(60L)
            }
        }
    }

    fun resetTasbih() {
        val items = DhikrPresets.getItemsForCategory(_dhikrCategory.value)
        val currentItem = items.getOrElse(_activeDhikrIndex.value) { items[0] }
        val updatedMap = _dhikrCounts.value.toMutableMap()
        updatedMap[currentItem.id] = 0
        _dhikrCounts.value = updatedMap
        preferences.saveDhikrCounts(updatedMap)
        _tasbihCount.value = 0
        triggerHaptic(80L)
    }

    fun resetAllDhikr() {
        val items = DhikrPresets.getItemsForCategory(_dhikrCategory.value)
        val updatedMap = _dhikrCounts.value.toMutableMap()
        items.forEach { item ->
            updatedMap[item.id] = 0
        }
        _dhikrCounts.value = updatedMap
        preferences.saveDhikrCounts(updatedMap)
        _activeDhikrIndex.value = 0
        _tasbihCount.value = 0
        triggerHaptic(100L)
    }

    fun selectDhikrItem(index: Int) {
        val items = DhikrPresets.getItemsForCategory(_dhikrCategory.value)
        if (index in items.indices) {
            _activeDhikrIndex.value = index
            val item = items[index]
            _tasbihCount.value = _dhikrCounts.value[item.id] ?: 0
        }
    }

    fun selectDhikrCategory(category: String) {
        _dhikrCategory.value = category
        preferences.saveDhikrCategory(category)
        _activeDhikrIndex.value = 0
        val items = DhikrPresets.getItemsForCategory(category)
        val firstItem = items.firstOrNull()
        if (firstItem != null) {
            _tasbihCount.value = _dhikrCounts.value[firstItem.id] ?: 0
        } else {
            _tasbihCount.value = 0
        }
    }

    fun toggleVibrationFeedback() {
        _vibrateFeedback.value = !_vibrateFeedback.value
        if (_vibrateFeedback.value) {
            triggerHaptic(70L)
        }
    }

    fun triggerHaptic(durationMs: Long, isImportant: Boolean = false) {
        try {
            val vibratorService = vibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            if (vibratorService == null || !vibratorService.hasVibrator()) return

            val duration = durationMs.coerceAtLeast(60L)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val usage = if (isImportant) VibrationAttributes.USAGE_ALARM else VibrationAttributes.USAGE_NOTIFICATION
                val attrs = VibrationAttributes.Builder()
                    .setUsage(usage)
                    .build()
                val effect = VibrationEffect.createOneShot(duration, 255)
                vibratorService.vibrate(effect, attrs)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(duration, 255)
                vibratorService.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibratorService.vibrate(duration)
            }
        } catch (_: Exception) {
        }
    }

    fun triggerPatternHaptic() {
        try {
            val vibratorService = vibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            if (vibratorService == null || !vibratorService.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 70, 120),
                    intArrayOf(0, 255, 0, 255),
                    -1
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attrs = VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_NOTIFICATION)
                        .build()
                    vibratorService.vibrate(effect, attrs)
                } else {
                    vibratorService.vibrate(effect)
                }
            } else {
                @Suppress("DEPRECATION")
                vibratorService.vibrate(longArrayOf(0, 100, 70, 120), -1)
            }
        } catch (_: Exception) {
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioManager.stop()
        compassManager.stop()
    }
}
