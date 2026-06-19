package com.example.ui

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.KhutbahViewModel
import com.example.ui.viewmodel.PrayerTimesUiState
import java.util.*

// Preset major Indonesian regions for manual overrides
data class PresetCity(val name: String, val latitude: Double, val longitude: Double)

val PRESET_CITIES = listOf(
    PresetCity("Jakarta", -6.2088, 106.8456),
    PresetCity("Surabaya", -7.2575, 112.7521),
    PresetCity("Bandung", -6.9175, 107.6191),
    PresetCity("Medan", 3.5952, 98.6722),
    PresetCity("Makassar", -5.1477, 119.4327),
    PresetCity("Yogyakarta", -7.7956, 110.3695),
    PresetCity("Banjarmasin", -3.3186, 114.5944),
    PresetCity("Banda Aceh", 5.5483, 95.3238),
    PresetCity("Jayapura", -2.5413, 140.7100),
    PresetCity("Denpasar", -8.6705, 115.2126),
    PresetCity("Balikpapan", -1.2654, 116.8312)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesDashboardScreen(
    viewModel: KhutbahViewModel,
    modifier: Modifier = Modifier,
    onNavigateToQibla: () -> Unit = {}
) {
    val context = LocalContext.current
    val userLocationName by viewModel.userLocationName.collectAsStateWithLifecycle()
    val userLatitude by viewModel.userLatitude.collectAsStateWithLifecycle()
    val userLongitude by viewModel.userLongitude.collectAsStateWithLifecycle()
    val prayerState by viewModel.prayerTimesState.collectAsStateWithLifecycle()

    var showCitySelectorDialog by remember { mutableStateOf(false) }

    // Dynamic Permission requesting launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (coarseGranted || fineGranted) {
            viewModel.detectLocationAndFetchPrayerTimes(context)
        } else {
            ScaffoldMessengerState.showToast(context, "Akses gps ditolak, memuat lokasi default.")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper Emerald Gradient Header Block for Prayer Page
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "JADWAL SHALAT 5 WAKTU",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Location indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f))
                        .clickable { showCitySelectorDialog = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Region Location",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = userLocationName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Ganti Daerah",
                        tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Subtly print coordinates
                Text(
                    text = String.format(Locale.getDefault(), "Lat: %.4f • Lng: %.4f • Kemenag RI", userLatitude, userLongitude),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // GPS detect button
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Search GPS Location",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ambil Lokasi Otomatis (GPS)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Layout below header
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (val state = prayerState) {
                is PrayerTimesUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Mengambil jadwal shalat...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                        }
                    }
                }
                is PrayerTimesUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error icon",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = state.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.fetchPrayerTimes(userLatitude, userLongitude) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Coba Lagi", color = Color.White)
                                }
                            }
                        }
                    }
                }
                is PrayerTimesUiState.Success -> {
                    val timings = state.timings
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 90.dp)
                    ) {
                        if (state.isOffline) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 0.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WifiOff,
                                        contentDescription = "Offline Mode Indicator",
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mode Offline: Menampilkan Jadwal Lokal Tersimpan",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                        
                        // Date strip card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Tanggal Hari Ini:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    Text(state.readableDate, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Kalender Hijriah:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    Text(state.hijriDate, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        // Countdown to next prayer card
                        NextPrayerCountdownCard(timings = timings)

                        // Elegant Qibla locator action card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable { onNavigateToQibla() }
                                .testTag("qibla_finder_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Explore,
                                        contentDescription = "Kompas",
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Cari Arah Kiblat",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "Kompas presisi untuk melacak arah Ka'bah",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Arahkan ke Pencari Kiblat",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Full Prayer Times list (including Imsak and Terbit/Sunrise)
                        val items = listOf(
                            PrayerTimeItem("Imsak", timings["Imsak"] ?: "--:--", Icons.Outlined.AccessAlarm),
                            PrayerTimeItem("Subuh (Fajr)", timings["Fajr"] ?: "--:--", Icons.Outlined.Brightness2),
                            PrayerTimeItem("Terbit (Sunrise)", timings["Sunrise"] ?: "--:--", Icons.Outlined.WbTwilight),
                            PrayerTimeItem("Dzuhur (Dhuhr)", timings["Dhuhr"] ?: "--:--", Icons.Outlined.WbSunny),
                            PrayerTimeItem("Ashar (Asr)", timings["Asr"] ?: "--:--", Icons.Outlined.LightMode),
                            PrayerTimeItem("Maghrib", timings["Maghrib"] ?: "--:--", Icons.Outlined.NightsStay),
                            PrayerTimeItem("Isya (Isha)", timings["Isha"] ?: "--:--", Icons.Outlined.Bedtime)
                        )

                        Text(
                            text = "Jadwal Shalat Hari Ini",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )

                        items.forEach { item ->
                            val isNext = checkIsNext(item.name, timings)
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isNext) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ),
                                border = if (isNext) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null,
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 2.dp else 0.5.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(
                                                    color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.name,
                                                tint = if (isNext) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(
                                            text = item.name,
                                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isNext) {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.padding(end = 8.dp)
                                            ) {
                                                Text(
                                                    text = "BERIKUTNYA",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        
                                        Text(
                                            text = item.time,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCitySelectorDialog) {
        CitySelectorDialog(
            viewModel = viewModel,
            onDismiss = { showCitySelectorDialog = false }
        )
    }
}

data class PrayerTimeItem(
    val name: String,
    val time: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun NextPrayerCountdownCard(timings: Map<String, String>) {
    val next = getNextPrayerMap(timings) ?: return
    
    // Live update of countdown timer
    var ticks by remember { mutableStateOf(0) }
    LaunchedEffect(timings) {
        while (true) {
            kotlinx.coroutines.delay(10000) // Update every 10 seconds
            ticks++
        }
    }
    
    val countdownText = remember(ticks, timings) {
        getRemainingTime(timings)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Waktu Shalat Berikutnya",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Next Prayer Clock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = next.first,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                if (countdownText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Mundur: -$countdownText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Pukul",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                Text(
                    text = next.second,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

fun getRemainingTime(timings: Map<String, String>): String {
    val keys = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
    val now = Calendar.getInstance()
    val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    
    for (key in keys) {
        val timeStr = timings[key] ?: continue
        val parts = timeStr.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].trim().toIntOrNull() ?: 0
            val minute = parts[1].split(" ")[0].trim().toIntOrNull() ?: 0
            val prayerMinutes = hour * 60 + minute
            if (prayerMinutes > currentMinutes) {
                val diff = prayerMinutes - currentMinutes
                val hoursLeft = diff / 60
                val minutesLeft = diff % 60
                return if (hoursLeft > 0) {
                    "${hoursLeft}j ${minutesLeft}m"
                } else {
                    "${minutesLeft}m"
                }
            }
        }
    }
    
    // Fallback to Fajr (Subuh) tomorrow
    val fajrStr = timings["Fajr"] ?: return ""
    val parts = fajrStr.split(":")
    if (parts.size >= 2) {
        val hour = parts[0].trim().toIntOrNull() ?: 0
        val minute = parts[1].split(" ")[0].trim().toIntOrNull() ?: 0
        val prayerMinutes = hour * 60 + minute
        // fajr is tomorrow, so it's (24 * 60 - currentMinutes) + prayerMinutes
        val diff = (24 * 60 - currentMinutes) + prayerMinutes
        val hoursLeft = diff / 60
        val minutesLeft = diff % 60
        return if (hoursLeft > 0) {
            "${hoursLeft}j ${minutesLeft}m"
        } else {
            "${minutesLeft}m"
        }
    }
    
    return ""
}

fun checkIsNext(prayerName: String, timings: Map<String, String>): Boolean {
    val nextPair = getNextPrayerMap(timings) ?: return false
    val sanitizedNext = nextPair.first.lowercase()
    val sanitizedPrayer = prayerName.lowercase()
    return sanitizedPrayer.contains(sanitizedNext) || sanitizedNext.contains(sanitizedPrayer)
}

fun getNextPrayerMap(timings: Map<String, String>): Pair<String, String>? {
    val keys = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")
    val indonesianNames = mapOf(
        "Fajr" to "Subuh",
        "Sunrise" to "Terbit",
        "Dhuhr" to "Dzuhur",
        "Asr" to "Ashar",
        "Maghrib" to "Maghrib",
        "Isha" to "Isya"
    )
    val now = Calendar.getInstance()
    val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    
    for (key in keys) {
        val timeStr = timings[key] ?: continue
        val parts = timeStr.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].trim().toIntOrNull() ?: 0
            val minute = parts[1].split(" ")[0].trim().toIntOrNull() ?: 0
            val prayerMinutes = hour * 60 + minute
            if (prayerMinutes > currentMinutes) {
                return Pair(indonesianNames[key] ?: key, timeStr)
            }
        }
    }
    // Fallback to Fajr (Subuh) if all passed
    return Pair("Subuh (Fajr)", timings["Fajr"] ?: "--:--")
}

@Composable
fun CitySelectorDialog(
    viewModel: KhutbahViewModel,
    onDismiss: () -> Unit
) {
    var customLat by remember { mutableStateOf("") }
    var customLng by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Wilayah Shalat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = "Daftar Kota Populer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable city grids
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PRESET_CITIES.forEach { city ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectManualCity(city.name, city.latitude, city.longitude)
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = city.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "Lat: %.2f Lng: %.2f", city.latitude, city.longitude),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Koordinat Kustom Maps (Manual)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Nama Daerah (misal: Bekasi)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customLat,
                        onValueChange = { customLat = it },
                        label = { Text("Latitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = customLng,
                        onValueChange = { customLng = it },
                        label = { Text("Longitude") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        val latVal = customLat.toDoubleOrNull()
                        val lngVal = customLng.toDoubleOrNull()
                        val label = customName.trim().ifEmpty { "Koordinat Kustom" }
                        
                        if (latVal != null && lngVal != null) {
                            viewModel.selectManualCity(label, latVal, lngVal)
                            onDismiss()
                        } else {
                            ScaffoldMessengerState.showToast(context, "Input koordinat tidak valid!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Ganti Koordinat", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Simple Toast Helper class to prevent duplicating contexts
object ScaffoldMessengerState {
    fun showToast(context: Context, msg: String) {
        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
    }
}
