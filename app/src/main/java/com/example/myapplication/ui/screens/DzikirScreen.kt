package com.example.myapplication.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.core.model.DhikrPresets
import com.example.myapplication.ui.NoorWaktuViewModel
import com.example.myapplication.ui.theme.*

@Composable
fun DzikirScreen(
    viewModel: NoorWaktuViewModel,
    modifier: Modifier = Modifier
) {
    val dhikrCategory by viewModel.dhikrCategory.collectAsState()
    val activeIndex by viewModel.activeDhikrIndex.collectAsState()
    val tasbihCount by viewModel.tasbihCount.collectAsState()
    val vibrateFeedback by viewModel.vibrateFeedback.collectAsState()

    val dhikrCounts by viewModel.dhikrCounts.collectAsState()
    val dhikrItems = remember(dhikrCategory) { DhikrPresets.getItemsForCategory(dhikrCategory) }
    val currentDhikr = dhikrItems.getOrElse(activeIndex) { dhikrItems.firstOrNull() ?: DhikrPresets.postPrayerDhikr[0] }
    val progress = (tasbihCount.toFloat() / currentDhikr.targetCount.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceParchment),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Segmented Tabs Row
        item {
            val categories = listOf("Setelah Salat", "Dzikir Pagi", "Dzikir Petang", "Dzikir Bebas")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = dhikrCategory == category
                    Surface(
                        modifier = Modifier.clickable { viewModel.selectDhikrCategory(category) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) EmeraldPrimary else SurfaceContainerLow
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else SlateDark
                            )
                        }
                    }
                }
            }
        }

        // Digital Tasbih Counter Hero Card
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
                    // Sequence & Target Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldTint
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Urutan ${currentDhikr.sequenceNumber} dari ${dhikrItems.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GoldContainer
                        ) {
                            Text(
                                text = "Target: ${currentDhikr.targetCount}x",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OnGoldContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Arabic Dhikr & Transliteration
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = currentDhikr.arabicText,
                            fontSize = 30.sp,
                            color = EmeraldPrimary,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentDhikr.transliteration,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "\"${currentDhikr.translation}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                    }

                    // Large Circular Tap Target Pod
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Animated Circular Progress Ring
                        val animatedProgress by animateFloatAsState(targetValue = progress, label = "TasbihProgress")
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 8.dp.toPx()
                            drawCircle(
                                color = SurfaceContainerHigh,
                                style = Stroke(width = strokeWidth)
                            )
                            drawArc(
                                color = GoldAccent,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                            )
                        }

                        // Central Tap Button
                        Surface(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.incrementTasbih() },
                            shape = CircleShape,
                            color = EmeraldPrimary,
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "HITUNGAN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$tasbihCount",
                                        fontSize = 44.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "/${currentDhikr.targetCount}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }
                                Text(
                                    text = "Ketuk Untuk Menambah",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Control Buttons Row 1: Vibrate + Reset Active
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.toggleVibrationFeedback() },
                            shape = RoundedCornerShape(12.dp),
                            color = if (vibrateFeedback) EmeraldTint else SurfaceContainerLow
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (vibrateFeedback) Icons.Default.Vibration else Icons.Outlined.Vibration,
                                    contentDescription = null,
                                    tint = if (vibrateFeedback) EmeraldPrimary else SlateMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (vibrateFeedback) "Getar Aktif" else "Getar Mati",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (vibrateFeedback) EmeraldPrimary else SlateDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.resetTasbih() },
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceContainerLow
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = SlateDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reset Item",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = SlateDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Control Buttons Row 2: Reset Semua
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.resetAllDhikr() },
                        shape = RoundedCornerShape(12.dp),
                        color = GoldContainer.copy(alpha = 0.5f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldDark.copy(alpha = 0.4f)))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = OnGoldContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reset Semua Hitungan ($dhikrCategory)",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnGoldContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Dhikr Sequence List
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Bacaan: $dhikrCategory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    Text(
                        text = "${activeIndex + 1} dari ${dhikrItems.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted
                    )
                }

                dhikrItems.forEachIndexed { index, item ->
                    val isActive = index == activeIndex
                    val count = dhikrCounts[item.id] ?: 0
                    val isCompleted = count >= item.targetCount

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectDhikrItem(index) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) EmeraldPrimary else SurfacePure
                        ),
                        border = if (isActive) {
                            CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GoldAccent))
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
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCompleted -> EmeraldTint
                                                isActive -> GoldAccent
                                                else -> SurfaceContainerLow
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${item.sequenceNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) EmeraldDeep else SlateMuted
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "${item.title} (${item.targetCount}x)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isActive) Color.White else SlateDark
                                    )
                                    Text(
                                        text = item.transliteration,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isActive) Color.White.copy(alpha = 0.8f) else SlateMuted,
                                        maxLines = 1
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    isCompleted -> EmeraldTint
                                    isActive -> GoldAccent
                                    else -> SurfaceContainerLow
                                }
                            ) {
                                Text(
                                    text = when {
                                        isCompleted -> "Selesai (${item.targetCount}x)"
                                        isActive -> "$tasbihCount/${item.targetCount}"
                                        count > 0 -> "$count/${item.targetCount}"
                                        else -> "Belum"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isCompleted -> EmeraldPrimary
                                        isActive -> EmeraldDeep
                                        else -> SlateMuted
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Renungan Dzikir Card
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
                        Text(
                            text = "RENUNGAN DZIKIR HARI INI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "HR. Bukhari",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                    }

                    Text(
                        text = "\"Dua kalimat yang ringan di lisan, berat di timbangan, dan dicintai Ar-Rahman: Subhanallah wa bihamdihi, Subhanallahil 'Azhim.\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateDark,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
