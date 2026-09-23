package com.example.myapplication.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.NoorWaktuViewModel
import com.example.myapplication.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun KiblatScreen(
    viewModel: NoorWaktuViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val qiblaResult by viewModel.qiblaResult.collectAsState()
    val isFacingQibla by viewModel.isFacingQibla.collectAsState()
    val deviceAzimuth by viewModel.compassManager.azimuthFlow.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isLocked by viewModel.isQiblaLocked.collectAsState()

    DisposableEffect(Unit) {
        viewModel.compassManager.start()
        onDispose {
            viewModel.compassManager.stop()
        }
    }

    val animatedAzimuth by animateFloatAsState(
        targetValue = if (isLocked) qiblaResult.azimuthDegrees.toFloat() else deviceAzimuth,
        label = "CompassRotation"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceParchment),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sensor Status Strip
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
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
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                            )
                            Text(
                                text = "Akurasi Sensor: Sangat Baik (GPS Aktif)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldPrimary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = GoldDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Kalibrasi",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ScreenRotation,
                            contentDescription = null,
                            tint = GoldDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Gerakkan ponsel dengan pola angka 8 jika kompas bergeser atau berada di dekat medan magnetik.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Main Compass Stage
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDefault))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Alignment Badge
                    val badgeColor by animateColorAsState(
                        targetValue = if (isFacingQibla || isLocked) GoldAccent else SurfaceContainerLow,
                        label = "BadgeColor"
                    )
                    val badgeTextColor by animateColorAsState(
                        targetValue = if (isFacingQibla || isLocked) EmeraldDeep else SlateMuted,
                        label = "BadgeTextColor"
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = badgeColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isFacingQibla || isLocked) Icons.Default.CheckCircle else Icons.Outlined.NearMe,
                                contentDescription = null,
                                tint = badgeTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isFacingQibla || isLocked) "Tepat Menghadap Ka'bah" else "Arahkan Ponsel ke Arah Kiblat",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor
                            )
                        }
                    }

                    // Heading Degrees Readout
                    val displayDegrees = if (isLocked) qiblaResult.azimuthDegrees.toInt() else animatedAzimuth.toInt()
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$displayDegrees°",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFacingQibla || isLocked) EmeraldPrimary else SlateDark
                            )
                            Text(
                                text = qiblaResult.formattedCompassDirection,
                                style = MaterialTheme.typography.titleMedium,
                                color = SlateMuted,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Text(
                            text = "Sudut Kiblat ${currentLocation.name}: ${String.format("%.1f", qiblaResult.azimuthDegrees)}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }

                    // Rotating Compass Dial
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CompassCanvas(
                            rotation = -animatedAzimuth,
                            qiblaAngle = qiblaResult.azimuthDegrees.toFloat(),
                            isAligned = isFacingQibla || isLocked
                        )

                        // Center Kaaba Pointer Arrow
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isFacingQibla || isLocked) EmeraldPrimary else SurfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = if (isFacingQibla || isLocked) GoldAccent else SlateMuted,
                                modifier = Modifier
                                    .size(26.dp)
                                    .graphicsLayer {
                                        rotationZ = qiblaResult.azimuthDegrees.toFloat() - animatedAzimuth
                                    }
                            )
                        }
                    }

                    // Sub-info Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLow
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Vibration,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Getar Otomatis",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SlateDark
                                )
                            }
                            Text(
                                text = "Jarak ke Ka'bah: ${qiblaResult.formattedDistance}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Solar Validation Card (Waktu Istiwa)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfacePure),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDefault))
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WbSunny,
                                contentDescription = null,
                                tint = GoldDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Validasi Posisi Matahari",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceContainerLow
                        ) {
                            Text(
                                text = "Waktu Istiwa",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Di luar ruangan, bayangan tongkat tegak lurus pada siang hari dapat memverifikasi arah kiblat secara optik tanpa interferensi magnetik.",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateMuted,
                        lineHeight = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceContainerLow
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Deklinasi Magnetik", style = MaterialTheme.typography.labelSmall, color = SlateMuted)
                                Text(text = "+0.8° E", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = SurfaceContainerLow
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "Koordinat Mekkah", style = MaterialTheme.typography.labelSmall, color = SlateMuted)
                                Text(text = "21.42° N, 39.82° E", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Mode Sajadah & Lock Qibla Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLocked) GoldContainer.copy(alpha = 0.5f) else SurfacePure
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (isLocked) GoldDark else BorderDefault)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                                imageVector = if (isLocked) Icons.Default.Lock else Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = if (isLocked) GoldDark else EmeraldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (isLocked) "Mode Sajadah Aktif" else "Kunci Arah Kiblat (Mode Sajadah)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isLocked) OnGoldContainer else SlateDark
                            )
                        }
                        if (isLocked) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GoldAccent
                            ) {
                                Text(
                                    text = "TERKUNCI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDeep,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (isLocked) {
                        // Sajadah Alignment Guideline visual
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfacePure, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderDefault, RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Straight,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Arah Garis Sajadah (${qiblaResult.azimuthDegrees.toInt()}°)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                            Text(
                                text = "Letakkan ponsel mendatar di atas sajadah Anda. Sisi atas ponsel menghadap lurus ke depan mengikuti arah Ka'bah.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SlateMuted,
                                lineHeight = 18.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Fungsi ini membekukan sudut kompas agar ponsel bisa diletakkan mendatar di atas sajadah saat salat tanpa terganggu goyangan sensor.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted,
                            lineHeight = 18.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.toggleQiblaLock() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLocked) GoldDark else EmeraldPrimary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Text(
                                text = if (isLocked) "Buka Kunci Kompas" else "Kunci Sudut Kiblat Ini",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompassCanvas(
    rotation: Float,
    qiblaAngle: Float,
    isAligned: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 12.dp.toPx()

        rotate(rotation, center) {
            // Outer ring
            drawCircle(
                color = BorderDefault,
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Inner circle
            drawCircle(
                color = SurfaceContainerLow,
                radius = radius * 0.85f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Degree Ticks
            for (i in 0 until 360 step 30) {
                val tickRad = Math.toRadians(i.toDouble())
                val isCard = i % 90 == 0
                val tickLen = if (isCard) 16.dp.toPx() else 8.dp.toPx()

                val startX = (center.x + (radius - tickLen) * sin(tickRad)).toFloat()
                val startY = (center.y - (radius - tickLen) * cos(tickRad)).toFloat()
                val endX = (center.x + radius * sin(tickRad)).toFloat()
                val endY = (center.y - radius * cos(tickRad)).toFloat()

                drawLine(
                    color = if (i == 0) Color.Red else SlateSubdued,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = if (isCard) 2.dp.toPx() else 1.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw Kaaba Marker Ring on dial
            val qiblaRad = Math.toRadians(qiblaAngle.toDouble())
            val kaabaX = (center.x + radius * 0.85f * sin(qiblaRad)).toFloat()
            val kaabaY = (center.y - radius * 0.85f * cos(qiblaRad)).toFloat()

            drawCircle(
                color = if (isAligned) GoldAccent else EmeraldPrimary,
                radius = 10.dp.toPx(),
                center = Offset(kaabaX, kaabaY)
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(kaabaX, kaabaY)
            )
        }
    }
}
