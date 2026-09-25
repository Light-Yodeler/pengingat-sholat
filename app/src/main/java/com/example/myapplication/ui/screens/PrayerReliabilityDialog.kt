package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.core.permission.AppPermissionManager
import com.example.myapplication.ui.NoorWaktuViewModel
import com.example.myapplication.ui.theme.*

@Composable
fun PrayerReliabilityDialog(
    viewModel: NoorWaktuViewModel,
    onRequestNotificationPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isNotificationGranted by viewModel.isNotificationGranted.collectAsState()
    val isExactAlarmGranted by viewModel.isExactAlarmGranted.collectAsState()
    val isBatteryIgnored by viewModel.isBatteryOptimizedIgnored.collectAsState()

    val brandName = remember { AppPermissionManager.getDeviceBrandDisplayName() }
    val isOemAutoStart = remember { AppPermissionManager.isOemWithAutoStart() }

    val totalItems = if (isOemAutoStart) 4 else 3
    var activeCount = 0
    if (isNotificationGranted) activeCount++
    if (isExactAlarmGranted) activeCount++
    if (isBatteryIgnored) activeCount++
    // Auto-start cannot be programmatically checked on Android, so treat as guide item

    val isAllReady = isNotificationGranted && isExactAlarmGranted && isBatteryIgnored

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfacePure)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isAllReady) EmeraldTint else GoldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAllReady) Icons.Default.CheckCircle else Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isAllReady) EmeraldPrimary else OnGoldContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Keandalan Waktu Azan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Text(
                                text = "Izin & Optimasi Sistem HP",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = SlateMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isAllReady) EmeraldTint else GoldContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isAllReady) Icons.Default.Verified else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isAllReady) EmeraldPrimary else OnGoldContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isAllReady) "Kesiapan Sistem: Sangat Baik" else "Perlu Pengaturan Tambahan",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isAllReady) EmeraldPrimary else OnGoldContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isAllReady) {
                                "Semua izin utama sistem telah aktif. Notifikasi dan suara azan siap berbunyi tepat waktu saat layar mati maupun setelah HP direstart."
                            } else {
                                "Agar suara azan tidak terhenti oleh sistem Android dan tetap bersuara setelah HP direstart tanpa harus membuka aplikasi, silakan aktifkan izin berikut:"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isAllReady) EmeraldDeep else OnGoldContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable items
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Item 1: Notifikasi
                    PermissionItemCard(
                        icon = Icons.Outlined.Notifications,
                        title = "1. Izin Notifikasi Aplikasi",
                        description = "Wajib agar pesan azan dan hitung mundur waktu salat dapat muncul di layar HP Anda.",
                        isGranted = isNotificationGranted,
                        actionLabel = "Izinkan Notifikasi",
                        onAction = {
                            onRequestNotificationPermission()
                            AppPermissionManager.openNotificationSettings(context)
                        }
                    )

                    // Item 2: Exact Alarm
                    PermissionItemCard(
                        icon = Icons.Outlined.Alarm,
                        title = "2. Izin Alarm Tepat Waktu",
                        description = "Menjamin kumandang azan berbunyi pas di detik dan menitnya, tanpa ditunda oleh Doze Mode Android.",
                        isGranted = isExactAlarmGranted,
                        actionLabel = "Buka Izin Alarm",
                        onAction = {
                            AppPermissionManager.openExactAlarmSettings(context)
                        }
                    )

                    // Item 3: Battery Optimization
                    PermissionItemCard(
                        icon = Icons.Outlined.BatteryChargingFull,
                        title = "3. Abaikan Penghemat Baterai",
                        description = "Mencegah sistem mematikan atau membekukan proses azan saat HP dibiarkan lama dalam keadaan siaga/saku.",
                        isGranted = isBatteryIgnored,
                        actionLabel = "Bebaskan Aplikasi",
                        onAction = {
                            AppPermissionManager.requestIgnoreBatteryOptimization(context)
                        }
                    )

                    // Item 4: Auto-Start (Mulai Otomatis)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderDefault)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.RestartAlt,
                                        contentDescription = null,
                                        tint = GoldDark,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "4. Mulai Otomatis (Auto-start)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SlateDark
                                        )
                                        Text(
                                            text = "Khusus ponsel $brandName",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldDark,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "PENTING: Di HP $brandName, aplikasi diblokir setelah HP direstart jika Mulai Otomatis mati. Aktifkan agar azan berbunyi setelah reboot tanpa perlu membuka aplikasi.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { AppPermissionManager.openAutoStartSettings(context) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Launch,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Buka Menu Mulai Otomatis $brandName",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(
                        text = "Selesai & Tutup",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionItemCard(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) EmeraldPrimary.copy(alpha = 0.25f) else BorderDefault
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) EmeraldPrimary else SlateMuted,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isGranted) EmeraldTint else GoldContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isGranted) EmeraldPrimary else OnGoldContainer,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isGranted) "Aktif" else "Belum",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isGranted) EmeraldPrimary else OnGoldContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = SlateMuted
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = onAction,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = EmeraldTint,
                        contentColor = EmeraldPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
