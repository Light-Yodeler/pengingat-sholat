package com.example.myapplication.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.core.calendar.HijriCalendarHelper
import com.example.myapplication.ui.screens.DzikirScreen
import com.example.myapplication.ui.screens.JadwalScreen
import com.example.myapplication.ui.screens.KiblatScreen
import com.example.myapplication.ui.screens.PengingatScreen
import com.example.myapplication.ui.theme.*
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoorWaktuMainScreen(
    viewModel: NoorWaktuViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val showProfileSheet by viewModel.showProfileSheet.collectAsState()
    val today = remember { LocalDate.now() }
    val hijriStr = remember(today) { HijriCalendarHelper.formatHijri(today) }

    val showLocationPicker by viewModel.showLocationPicker.collectAsState()

    val tabTitles = listOf("Jadwal", "Kiblat", "Pengingat", "Dzikir")

    // Location Picker Dialog (accessible from any screen via TopAppBar chip)
    if (showLocationPicker) {
        com.example.myapplication.ui.screens.LocationPickerDialog(
            currentCity = currentLocation,
            onGpsSelected = {
                viewModel.toggleLocationPicker(false)
                viewModel.refreshLocationFromGps()
            },
            onCitySelected = { city ->
                viewModel.selectCity(city)
                viewModel.toggleLocationPicker(false)
            },
            onDismiss = { viewModel.toggleLocationPicker(false) }
        )
    }

    // Profile Bottom Sheet
    if (showProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleProfileSheet(false) },
            containerColor = SurfacePure,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Pengguna Noor Waktu",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Text(
                            text = "Menjaga Ibadah Tepat Waktu",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                    }
                }

                Divider(color = BorderDefault)

                // Stats Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldTint
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Salat Hari Ini", style = MaterialTheme.typography.labelSmall, color = EmeraldDeep)
                            Text(text = "5 Waktu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = GoldContainer
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "Target Dzikir", style = MaterialTheme.typography.labelSmall, color = OnGoldContainer)
                            Text(text = "33x / Sesi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = OnGoldContainer)
                        }
                    }
                }

                // Preferences & Settings info
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Preferensi Ibadah", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = SlateDark)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Metode Hisab", style = MaterialTheme.typography.bodyMedium, color = SlateDark)
                                Text(text = "Kemenag RI", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Mazhab Asar", style = MaterialTheme.typography.bodyMedium, color = SlateDark)
                                Text(text = "Syafi'i / Standar", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Koreksi Ikhtiyat", style = MaterialTheme.typography.bodyMedium, color = SlateDark)
                                Text(text = "+2 Menit", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }
                    }
                }

                // App Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Noor Waktu v1.0 • Edisi Emerald Serenity",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Copyright © light-yodeler",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = SlateMuted
                        )
                    }
                    Button(
                        onClick = { viewModel.toggleProfileSheet(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Tutup", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceParchment,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceParchment,
                    titleContentColor = SlateDark
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Logo Noor Waktu",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Column {
                            Text(
                                text = "Noor Waktu",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Text(
                                text = "$hijriStr • ${tabTitles[selectedTab]}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SlateMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    val displayLocationName = remember(currentLocation.name) {
                        when {
                            currentLocation.name.startsWith("Kabupaten ") -> "Kab. " + currentLocation.name.removePrefix("Kabupaten ")
                            currentLocation.name.startsWith("Kota Administrasi ") -> "Kota " + currentLocation.name.removePrefix("Kota Administrasi ")
                            else -> currentLocation.name
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfacePure,
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDefault)),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clickable { viewModel.toggleLocationPicker(true) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = displayLocationName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateDark,
                                modifier = Modifier.widthIn(max = 110.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleProfileSheet(true) },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(36.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(EmeraldDeep),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profil Pengguna",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfacePure.copy(alpha = 0.95f),
                shadowElevation = 8.dp
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier.navigationBarsPadding().height(64.dp)
                ) {
                    val tabs = listOf(
                        Triple(0, "Jadwal", Icons.Outlined.CalendarToday to Icons.Filled.CalendarToday),
                        Triple(1, "Kiblat", Icons.Outlined.Explore to Icons.Filled.Explore),
                        Triple(2, "Pengingat", Icons.Outlined.Notifications to Icons.Filled.NotificationsActive),
                        Triple(3, "Dzikir", Icons.Outlined.AllInclusive to Icons.Filled.AllInclusive)
                    )

                    tabs.forEach { (index, title, iconPair) ->
                        val isSelected = selectedTab == index
                        val (unselectedIcon, selectedIcon) = iconPair

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectTab(index) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                    contentDescription = title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldPrimary,
                                selectedTextColor = EmeraldPrimary,
                                unselectedIconColor = SlateMuted,
                                unselectedTextColor = SlateMuted,
                                indicatorColor = EmeraldTint
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> JadwalScreen(viewModel = viewModel)
                1 -> KiblatScreen(viewModel = viewModel)
                2 -> PengingatScreen(viewModel = viewModel)
                3 -> DzikirScreen(viewModel = viewModel)
            }
        }
    }
}
