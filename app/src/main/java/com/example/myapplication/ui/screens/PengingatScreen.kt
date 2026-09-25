package com.example.myapplication.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.model.AlertType
import com.example.myapplication.core.model.MuazzinList
import com.example.myapplication.core.prayer.PrayerType
import com.example.myapplication.ui.NoorWaktuViewModel
import com.example.myapplication.ui.theme.*

@Composable
fun PengingatScreen(
    viewModel: NoorWaktuViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val isPreviewPlaying by viewModel.isPreviewPlaying.collectAsState()
    val previewPlayingResId by viewModel.previewPlayingResId.collectAsState()
    val schedule by viewModel.schedule.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceParchment),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Keandalan Notifikasi & Izin HP Card
        item {
            val isNotificationGranted by viewModel.isNotificationGranted.collectAsState()
            val isExactAlarmGranted by viewModel.isExactAlarmGranted.collectAsState()
            val isBatteryIgnored by viewModel.isBatteryOptimizedIgnored.collectAsState()
            val isReady = isNotificationGranted && isExactAlarmGranted && isBatteryIgnored

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleReliabilityDialog(true) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isReady) EmeraldTint else GoldContainer.copy(alpha = 0.65f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isReady) EmeraldPrimary.copy(alpha = 0.25f) else GoldAccent
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isReady) EmeraldPrimary else OnGoldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isReady) Icons.Default.Verified else Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Keandalan Azan & Izin Sistem HP",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isReady) EmeraldPrimary else OnGoldContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isReady) {
                                "Semua izin aktif. Azan akan berkumandang tepat waktu saat layar mati atau HP direstart."
                            } else {
                                "Perlu izin agar azan tetap bunyi saat HP restart / layar mati. Ketuk untuk mengatur."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isReady) EmeraldDeep else SlateDark,
                            lineHeight = 16.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Buka",
                        tint = if (isReady) EmeraldPrimary else OnGoldContainer
                    )
                }
            }
        }

        // Main Presets Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
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
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "PENGATURAN UTAMA",
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldContainer
                        ) {
                            Text(
                                text = "Aktif Otomatis",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Silent Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "Mode Senyap Otomatis",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Meredam dering ponsel selama 15 menit ketika azan berkumandang agar ibadah khusyuk.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f),
                                lineHeight = 16.sp
                            )
                        }
                        Switch(
                            checked = settings.autoSilentMode,
                            onCheckedChange = { viewModel.toggleAutoSilent(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = EmeraldPrimary,
                                checkedTrackColor = GoldAccent,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                    }

                    // Volume Slider Pod
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldDeep
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Volume Notifikasi Azan",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${settings.azanVolumePercent}%",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Button(
                                        onClick = { viewModel.playAudioPreview() },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isPreviewPlaying) GoldDark else Color.White.copy(alpha = 0.2f)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isPreviewPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (isPreviewPlaying) "Berhenti" else "Tes",
                                                fontSize = 11.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Slider(
                                value = settings.azanVolumePercent.toFloat(),
                                onValueChange = { viewModel.setAzanVolume(it.toInt()) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldAccent,
                                    activeTrackColor = GoldAccent,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Muazzin Selection Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Lantunan Suara Azan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "Pilih suara azan untuk mengingatkan waktu salat",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLow
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Audio Asli",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 1. Pilihan Azan Subuh
                Text(
                    text = "Pilihan Azan Subuh (Dengan Lafaz As-Salatu Khayrum Minan-Nawm)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep
                )

                MuazzinList.subuhOptions.forEach { muazzin ->
                    val isSelected = muazzin.id == settings.selectedSubuhMuazzinId
                    val isPlayingThis = isPreviewPlaying && previewPlayingResId == muazzin.rawResId

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectSubuhMuazzin(muazzin.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) EmeraldTint.copy(alpha = 0.7f) else SurfacePure
                        ),
                        border = if (isSelected) {
                            CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldPrimary))
                        } else {
                            CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDefault))
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) EmeraldPrimary else SurfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.NightsStay,
                                            contentDescription = null,
                                            tint = SlateMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = muazzin.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) EmeraldPrimary else SlateDark
                                    )
                                    Text(
                                        text = muazzin.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateMuted
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = muazzin.durationText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateMuted
                                )
                                IconButton(
                                    onClick = { viewModel.playAudioPreview(muazzin.rawResId) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "Putar Cuplikan ${muazzin.title}",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // 2. Azan Dzuhur, Asar, Maghrib, Isya
                Text(
                    text = "Pilihan Azan Salat Lainnya (Dzuhur, Asar, Maghrib, Isya)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDeep
                )

                MuazzinList.regularOptions.forEach { muazzin ->
                    val isSelected = muazzin.id == settings.selectedRegularMuazzinId
                    val isPlayingThis = isPreviewPlaying && previewPlayingResId == muazzin.rawResId

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectRegularMuazzin(muazzin.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) EmeraldTint.copy(alpha = 0.7f) else SurfacePure
                        ),
                        border = if (isSelected) {
                            CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(EmeraldPrimary))
                        } else {
                            CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDefault))
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) EmeraldPrimary else SurfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Mosque,
                                            contentDescription = null,
                                            tint = SlateMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = muazzin.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) EmeraldPrimary else SlateDark
                                    )
                                    Text(
                                        text = muazzin.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateMuted
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = muazzin.durationText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateMuted
                                )
                                IconButton(
                                    onClick = { viewModel.playAudioPreview(muazzin.rawResId) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "Putar Cuplikan ${muazzin.title}",
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Specific Prayer Alarms Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Jadwal Pengingat Spesifik",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )

                val timeFormatter = remember { java.time.format.DateTimeFormatter.ofPattern("HH:mm") }
                val prayers = listOf(
                    PrayerType.SUBUH to "${schedule.subuh.format(timeFormatter)} WIB",
                    PrayerType.DZUHUR to "${schedule.dzuhur.format(timeFormatter)} WIB",
                    PrayerType.ASAR to "${schedule.asar.format(timeFormatter)} WIB",
                    PrayerType.MAGHRIB to "${schedule.maghrib.format(timeFormatter)} WIB",
                    PrayerType.ISYA to "${schedule.isya.format(timeFormatter)} WIB"
                )


                prayers.forEach { (prayer, timeStr) ->
                    val selectedAlert = settings.prayerAlertTypes[prayer] ?: AlertType.AZAN

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfacePure),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDefault))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = prayer.displayName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SlateDark
                                        )
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SlateMuted
                                        )
                                    }
                                }
                            }

                            // Alert Type Selector Pills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AlertType.values().forEach { alertType ->
                                    val isPicked = selectedAlert == alertType
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { viewModel.setPrayerAlert(prayer, alertType) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isPicked) EmeraldPrimary else SurfaceContainerLow
                                    ) {
                                        Text(
                                            text = alertType.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isPicked) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isPicked) Color.White else SlateDark,
                                            modifier = Modifier
                                                .padding(vertical = 8.dp)
                                                .wrapContentWidth(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
                            }

                            // Subuh / Maghrib Extra Checkbox options
                            if (prayer == PrayerType.SUBUH) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleSubuhEarlyReminder(!settings.subuhEarlyReminder) },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = settings.subuhEarlyReminder,
                                        onCheckedChange = { viewModel.toggleSubuhEarlyReminder(it) },
                                        colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                                    )
                                    Text(
                                        text = "Pengingat Bangun 15 Menit Sebelum Subuh",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateDark
                                    )
                                }
                            } else if (prayer == PrayerType.MAGHRIB) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.toggleMaghribIftarDua(!settings.maghribIftarDuaReminder) },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = settings.maghribIftarDuaReminder,
                                        onCheckedChange = { viewModel.toggleMaghribIftarDua(it) },
                                        colors = CheckboxDefaults.colors(checkedColor = EmeraldPrimary)
                                    )
                                    Text(
                                        text = "Notifikasi Doa Berbuka Puasa saat Azan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SlateDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Calculation Authority Card
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Otoritas & Metode Perhitungan",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                    }

                    Text(
                        text = "Metode Resmi: ${settings.calculationMethodName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = EmeraldPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Faktor Kehati-hatian (Ikhtiyat)", style = MaterialTheme.typography.labelSmall, color = SlateMuted)
                        Text(text = "+${settings.ikhtiyatMinutes} Menit (Standar Kemenag)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SlateDark)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Ketinggian Tempat", style = MaterialTheme.typography.labelSmall, color = SlateMuted)
                        Text(text = "25 mdpl (Jakarta Pusat)", style = MaterialTheme.typography.labelSmall, color = SlateDark)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Noor Waktu • Versi 1.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Copyright © light-yodeler",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = SlateMuted
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
