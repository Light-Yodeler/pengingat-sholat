package com.example.myapplication.ui.screens

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.calendar.HijriCalendarHelper
import com.example.myapplication.core.location.CityLocation
import com.example.myapplication.core.location.LocationPresets
import com.example.myapplication.core.model.AlertType
import com.example.myapplication.core.model.AppSettings
import com.example.myapplication.core.prayer.NextPrayerInfo
import com.example.myapplication.core.prayer.PrayerSchedule
import com.example.myapplication.core.prayer.PrayerType
import com.example.myapplication.ui.NoorWaktuViewModel
import com.example.myapplication.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun JadwalScreen(
    viewModel: NoorWaktuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLocation by viewModel.currentLocation.collectAsState()
    val schedule by viewModel.schedule.collectAsState()
    val nextPrayer by viewModel.nextPrayer.collectAsState()
    val qiblaResult by viewModel.qiblaResult.collectAsState()
    val tasbihCount by viewModel.tasbihCount.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isLocationPermissionGranted by viewModel.isLocationPermissionGranted.collectAsState()
    val isGpsEnabled by viewModel.isGpsEnabled.collectAsState()
    val isLoadingLocation by viewModel.isLoadingLocation.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        viewModel.onLocationPermissionResult(fineGranted || coarseGranted)
    }

    val today = remember { LocalDate.now() }
    val gregorianDateStr = remember(today) { HijriCalendarHelper.formatGregorian(today) }
    val hijriDateStr = remember(today) { HijriCalendarHelper.formatHijri(today) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceParchment),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location & GPS Status Banner
        if (!isLocationPermissionGranted) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldContainer.copy(alpha = 0.65f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldDark))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOff,
                                contentDescription = null,
                                tint = OnGoldContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Layanan Lokasi Belum Aktif",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnGoldContainer
                            )
                        }
                        Text(
                            text = "Aktifkan izin lokasi agar waktu salat dan arah kiblat diperbarui otomatis sesuai posisi Anda saat ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateDark,
                            lineHeight = 18.sp
                        )
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "Aktifkan Lokasi",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else if (!isGpsEnabled) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldContainer.copy(alpha = 0.5f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldDark))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.GpsFixed,
                                contentDescription = null,
                                tint = OnGoldContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "GPS Ponsel Nonaktif",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnGoldContainer
                            )
                        }
                        Text(
                            text = "Nyalakan GPS di ponsel Anda agar aplikasi dapat mendeteksi koordinat astronomis secara akurat.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateDark,
                            lineHeight = 18.sp
                        )
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = "Buka Pengaturan GPS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else if (isLoadingLocation) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldTint,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = "Mendeteksi posisi satelit GPS terkini...",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldDeep
                        )
                    }
                }
            }
        }

        // Location & Date Context Bar
        item {
            LocationContextBar(
                locationName = currentLocation.detail,
                gregorianDate = gregorianDateStr,
                hijriDate = hijriDateStr,
                onUbahLocationClick = { viewModel.toggleLocationPicker(true) }
            )
        }

        // Hero Card: Next Prayer Countdown
        item {
            NextPrayerHeroCard(
                nextPrayer = nextPrayer,
                azanEnabled = settings.autoSilentMode,
                onToggleAzan = { viewModel.toggleAutoSilent(!settings.autoSilentMode) }
            )
        }

        // Daily Prayer Times Section
        item {
            PrayerScheduleSection(
                schedule = schedule,
                nextPrayerType = nextPrayer.next,
                settings = settings,
                onCycleAlert = { type -> viewModel.cyclePrayerAlert(type) }
            )
        }

        // Quick Widgets Row (Qibla Direction & Mini Tasbih)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Qibla Widget
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectTab(1) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePure),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDefault))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ARAH KIBLAT",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Outlined.Explore,
                                contentDescription = null,
                                tint = GoldDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${qiblaResult.azimuthDegrees.toInt()}°",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = qiblaResult.formattedCompassDirection,
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }

                // Mini Tasbih Widget
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectTab(3) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePure),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDefault))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TASBIH HARIAN",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateMuted,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = Icons.Outlined.AllInclusive,
                                contentDescription = null,
                                tint = GoldDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "$tasbihCount / 33",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                        Text(
                            text = "Subhanallah",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                }
            }
        }

        // Hadits Hari Ini
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoStories,
                                contentDescription = null,
                                tint = GoldDark,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "HADITS HARI INI",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "\"Amalan yang paling dicintai oleh Allah adalah salat tepat pada waktunya, kemudian berbakti kepada orang tua, kemudian jihad di jalan Allah.\" (HR. Bukhari no. 527 & Muslim no. 85)"
                                    )
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Bagikan Hadits")
                                context.startActivity(shareIntent)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Bagikan Hadits",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = "\"Amalan yang paling dicintai oleh Allah adalah salat tepat pada waktunya, kemudian berbakti kepada orang tua, kemudian jihad di jalan Allah.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateDark,
                        lineHeight = 22.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "HR. Bukhari no. 527 & Muslim no. 85",
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Shahih",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationContextBar(
    locationName: String,
    gregorianDate: String,
    hijriDate: String,
    onUbahLocationClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onUbahLocationClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(EmeraldTint),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.titleSmall,
                        color = SlateDark,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "$gregorianDate • $hijriDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted,
                        maxLines = 1
                    )
                }
            }

            Surface(
                onClick = onUbahLocationClick,
                shape = RoundedCornerShape(20.dp),
                color = SurfacePure,
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDefault)),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Ubah",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NextPrayerHeroCard(
    nextPrayer: NextPrayerInfo,
    azanEnabled: Boolean,
    onToggleAzan: () -> Unit
) {
    val hours = nextPrayer.remainingSeconds / 3600
    val minutes = (nextPrayer.remainingSeconds % 3600) / 60
    val seconds = nextPrayer.remainingSeconds % 60
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row: Waktu Salat Berikutnya & Target Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GoldAccent)
                    )
                    Text(
                        text = "WAKTU SALAT BERIKUTNYA",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldContainer
                ) {
                    Text(
                        text = "${nextPrayer.nextTime.format(timeFormatter)} WIB",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Prayer Name & Arabic
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = nextPrayer.next.displayName.uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Sholat Wajib",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
                Text(
                    text = nextPrayer.next.arabicName,
                    fontSize = 32.sp,
                    color = GoldAccent,
                    fontFamily = FontFamily.Serif
                )
            }

            // Countdown Pod
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = EmeraldDeep
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CountdownUnit(value = String.format("%02d", hours), label = "JAM")
                        Text(text = ":", color = GoldAccent.copy(alpha = 0.7f), fontSize = 24.sp, fontWeight = FontWeight.Light)
                        CountdownUnit(value = String.format("%02d", minutes), label = "MENIT")
                        Text(text = ":", color = GoldAccent.copy(alpha = 0.7f), fontSize = 24.sp, fontWeight = FontWeight.Light)
                        CountdownUnit(value = String.format("%02d", seconds), label = "DETIK", isAccent = true)
                    }

                    // Progress Bar
                    val animatedProgress by animateFloatAsState(targetValue = nextPrayer.progressPercent, label = "Progress")
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = GoldAccent,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = nextPrayer.current.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                        Text(
                            text = "Tersisa ${(100 - (nextPrayer.progressPercent * 100).toInt())}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = nextPrayer.next.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            // Notification Azan Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Pengingat Azan Penuh",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Switch(
                    checked = azanEnabled,
                    onCheckedChange = { onToggleAzan() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EmeraldPrimary,
                        checkedTrackColor = GoldAccent,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

@Composable
private fun CountdownUnit(value: String, label: String, isAccent: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAccent) GoldAccent else Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PrayerScheduleSection(
    schedule: PrayerSchedule,
    nextPrayerType: PrayerType,
    settings: AppSettings,
    onCycleAlert: (PrayerType) -> Unit
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    data class PrayerRowItem(
        val type: PrayerType,
        val time: LocalTime,
        val note: String,
        val icon: androidx.compose.ui.graphics.vector.ImageVector
    )

    val items = listOf(
        PrayerRowItem(PrayerType.IMSAK, schedule.imsak, "Waktu Menahan", Icons.Outlined.Schedule),
        PrayerRowItem(PrayerType.SUBUH, schedule.subuh, "Sholat Wajib", Icons.Outlined.CheckCircle),
        PrayerRowItem(PrayerType.TERBIT, schedule.terbit, "Batas Waktu Subuh", Icons.Outlined.WbSunny),
        PrayerRowItem(PrayerType.DZUHUR, schedule.dzuhur, "Sholat Wajib", Icons.Outlined.CheckCircle),
        PrayerRowItem(PrayerType.ASAR, schedule.asar, "Sholat Wajib", Icons.Outlined.CheckCircle),
        PrayerRowItem(PrayerType.MAGHRIB, schedule.maghrib, "Waktu Buka Puasa", Icons.Outlined.CheckCircle),
        PrayerRowItem(PrayerType.ISYA, schedule.isya, "Dilanjutkan Witir", Icons.Outlined.NightsStay)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Jadwal Salat Hari Ini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
            }
            Text(
                text = "Kemenag RI",
                style = MaterialTheme.typography.labelSmall,
                color = SlateMuted
            )
        }

        items.forEach { item ->
            val isNext = item.type == nextPrayerType
            val isPassed = LocalTime.now().isAfter(item.time)
            val alertType = settings.prayerAlertTypes[item.type]

            PrayerRowCard(
                type = item.type,
                name = item.type.displayName,
                arabic = item.type.arabicName,
                timeStr = "${item.time.format(timeFormatter)} WIB",
                note = item.note,
                isNext = isNext,
                isPassed = isPassed,
                alertType = alertType,
                onCycleAlert = { onCycleAlert(item.type) }
            )
        }
    }
}

@Composable
private fun PrayerRowCard(
    type: PrayerType,
    name: String,
    arabic: String,
    timeStr: String,
    note: String,
    isNext: Boolean,
    isPassed: Boolean,
    alertType: AlertType?,
    onCycleAlert: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNext) GoldContainer.copy(alpha = 0.6f) else SurfacePure
        ),
        border = if (isNext) {
            CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldDark))
        } else {
            CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDefault))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isNext -> GoldAccent
                                isPassed -> SurfaceContainerLow
                                else -> EmeraldTint
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPassed) Icons.Default.Check else Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = when {
                            isNext -> EmeraldDeep
                            isPassed -> SlateMuted
                            else -> EmeraldPrimary
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = arabic,
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isNext) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = GoldAccent
                            ) {
                                Text(
                                    text = "Berikutnya",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDeep,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = note,
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                    }
                }

            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isNext) EmeraldPrimary else SlateDark
                )

                if (alertType != null) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCycleAlert() },
                        shape = RoundedCornerShape(8.dp),
                        color = when (alertType) {
                            AlertType.AZAN -> EmeraldTint
                            AlertType.SILENT -> GoldContainer.copy(alpha = 0.7f)
                            AlertType.OFF -> SurfaceContainerLow
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (alertType) {
                                AlertType.AZAN -> EmeraldPrimary.copy(alpha = 0.5f)
                                AlertType.SILENT -> GoldDark.copy(alpha = 0.5f)
                                AlertType.OFF -> BorderDefault
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = when (alertType) {
                                    AlertType.AZAN -> Icons.Default.NotificationsActive
                                    AlertType.SILENT -> Icons.Default.Vibration
                                    AlertType.OFF -> Icons.Default.NotificationsOff
                                },
                                contentDescription = "Mode ${alertType.label}",
                                tint = when (alertType) {
                                    AlertType.AZAN -> EmeraldPrimary
                                    AlertType.SILENT -> OnGoldContainer
                                    AlertType.OFF -> SlateMuted
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = alertType.label,
                                fontSize = 11.sp,
                                fontWeight = if (alertType == AlertType.OFF) FontWeight.Normal else FontWeight.Bold,
                                color = when (alertType) {
                                    AlertType.AZAN -> EmeraldPrimary
                                    AlertType.SILENT -> OnGoldContainer
                                    AlertType.OFF -> SlateMuted
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun LocationPickerDialog(
    currentCity: CityLocation,
    onGpsSelected: () -> Unit,
    onCitySelected: (CityLocation) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCities = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            LocationPresets.cities
        } else {
            LocationPresets.cities.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.detail.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pilih Lokasi Salat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
                Text(
                    text = "38 Provinsi",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Live GPS Detection Action
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGpsSelected() },
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldPrimary
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GoldAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = EmeraldDeep,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Gunakan Lokasi GPS Saya",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Deteksi koordinat otomatis via satelit",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                HorizontalDivider(color = BorderDefault, modifier = Modifier.padding(vertical = 2.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = SlateDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    placeholder = {
                        Text(text = "Cari kota atau provinsi...", style = MaterialTheme.typography.bodySmall, color = SlateMuted)
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Hapus", tint = SlateMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SlateDark,
                        unfocusedTextColor = SlateDark,
                        focusedPlaceholderColor = SlateMuted,
                        unfocusedPlaceholderColor = SlateMuted,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = BorderDefault,
                        focusedContainerColor = SurfacePure,
                        unfocusedContainerColor = SurfacePure,
                        cursorColor = EmeraldPrimary
                    )
                )

                Text(
                    text = "Daftar Kota / Provinsi (${filteredCities.size}):",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted,
                    fontWeight = FontWeight.SemiBold
                )

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredCities.size) { index ->
                        val city = filteredCities[index]
                        val isSelected = city.id == currentCity.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCitySelected(city) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) EmeraldTint else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = city.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) EmeraldPrimary else SlateDark
                                    )
                                    Text(
                                        text = city.detail,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SlateMuted
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Tutup", color = EmeraldPrimary, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = SurfacePure,
        shape = RoundedCornerShape(18.dp)
    )
}
