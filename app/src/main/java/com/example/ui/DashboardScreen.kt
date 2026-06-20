package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.KhutbahViewModel
import com.example.ui.viewmodel.PrayerTimesUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: KhutbahViewModel,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Quran Reminder states
    val isQuranReminderEnabled by viewModel.isQuranReminderEnabled.collectAsStateWithLifecycle()
    val quranReminderHour by viewModel.quranReminderHour.collectAsStateWithLifecycle()
    val quranReminderMinute by viewModel.quranReminderMinute.collectAsStateWithLifecycle()
    val quranReminderTargetDays by viewModel.quranReminderTargetDays.collectAsStateWithLifecycle()
    val quranReadStreak by viewModel.quranReadStreak.collectAsStateWithLifecycle()
    val isQuranReadToday by viewModel.isQuranReadToday.collectAsStateWithLifecycle()
    
    // Shalat times state for quick glance
    val prayerTimesState by viewModel.prayerTimesState.collectAsStateWithLifecycle()
    val locationName by viewModel.userLocationName.collectAsStateWithLifecycle()

    // Last read states
    val lastReadSurahId by viewModel.lastReadSurahId.collectAsStateWithLifecycle()
    val lastReadSurahName by viewModel.lastReadSurahName.collectAsStateWithLifecycle()
    val lastReadVerseNo by viewModel.lastReadVerseNo.collectAsStateWithLifecycle()

    var showQuranReminderDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Header banner with beautiful gradient
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Assalamu'alaikum,",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "Halo, Sahabat Muslim",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(
                            onClick = { onNavigateToTab(5) }, // settings/setelan
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Setelan",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Location and dynamic date strip
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
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = locationName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // Display Hijri Date if success
                        val hijriStr = when (val state = prayerTimesState) {
                            is PrayerTimesUiState.Success -> state.hijriDate
                            else -> "Indonesia"
                        }
                        Text(
                            text = hijriStr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // QURAN DAILY STREAK MONITORING WIDGET (移步到主页: As requested!)
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_quran_streak_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (isQuranReadToday) {
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    }
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Dynamic Duolingo style flame ignition!
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        if (quranReadStreak > 0) Color(0xFFFF9800).copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (quranReadStreak > 0) "🔥" else "💤",
                                    fontSize = 26.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = if (quranReadStreak > 0) "$quranReadStreak Hari Berturut-turut!" else "Mulai Streak Tilawah",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (quranReadStreak > 0) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isQuranReadToday) "Selesai hari ini! Masya Allah ✨" else "Membaca Al-Qur'an harian untuk melatih konsistensi.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Streak circular indicator goal
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(54.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = {
                                    if (quranReminderTargetDays > 0) {
                                        (quranReadStreak.coerceAtMost(quranReminderTargetDays).toFloat() / quranReminderTargetDays)
                                    } else 0f
                                },
                                color = if (isQuranReadToday) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeWidth = 5.dp,
                                modifier = Modifier.matchParentSize()
                            )
                            Text(
                                text = "$quranReadStreak/$quranReminderTargetDays",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Alarm set state details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (isQuranReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isQuranReminderEnabled) {
                                    "Alarm Remind: %02d:%02d".format(quranReminderHour, quranReminderMinute)
                                } else {
                                    "Ingatkan berbuat kebaikan (Remind Off)"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isQuranReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }

                        // Action button set
                        TextButton(
                            onClick = { showQuranReminderDialog = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("btn_atur_target_quran")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atur Target & Pengingat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // LAST READ SHORTCUT (Lanjut Membaca Widget)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(1) }, // Takes to Quran
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.outlineVariant())
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Terakhir Dibaca",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (lastReadSurahName.isNotEmpty()) "Surah $lastReadSurahName (Ayat $lastReadVerseNo)" else "Belum mulai membaca",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Button(
                        onNavigateToTab = { onNavigateToTab(1) },
                        text = if (lastReadSurahName.isNotEmpty()) "Lanjut" else "Buka Alquran",
                        modifier = Modifier.testTag("btn_continue_reading")
                    )
                }
            }
        }

        // PRAYER TIMES AUTOMATIC WIDGET (Quick prayer details)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTab(3) }, // Shalat Tab
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.outlineVariant())
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Jadwal Shalat Hari Ini",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Lihat Semua",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (prayerTimesState is PrayerTimesUiState.Success) {
                        val times = (prayerTimesState as PrayerTimesUiState.Success).timings
                        val nextPrayer = getNextPrayerMap(times)
                        val remaining = getRemainingTime(times)
                        if (nextPrayer != null && remaining.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 12.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Berikutnya: ${nextPrayer.first} (${nextPrayer.second}) • Mundur: -$remaining",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    when (val state = prayerTimesState) {
                        is PrayerTimesUiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
                            }
                        }
                        is PrayerTimesUiState.Error -> {
                            Text(
                                text = state.message,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                            )
                        }
                        is PrayerTimesUiState.Success -> {
                            val times = state.timings
                            // Display Fajr, Dhuhr, Asr, Maghrib, Isha
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val keys = listOf(
                                    "Subuh" to (times["Fajr"] ?: "--:--"),
                                    "Zuhur" to (times["Dhuhr"] ?: "--:--"),
                                    "Asar" to (times["Asr"] ?: "--:--"),
                                    "Maghrib" to (times["Maghrib"] ?: "--:--"),
                                    "Isya" to (times["Isha"] ?: "--:--")
                                )
                                keys.forEach { (name, time) ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = time,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
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

        // MAIN NAVIGATION DASHBOARD shortcuts grid!
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Layanan Fitur Islami",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                // Layout grid items beautifully
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureShortcutCard(
                        icon = Icons.Outlined.AutoStories,
                        title = "Al-Qur'an",
                        subtitle = "Kaji mushaf digital & audio qari",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onNavigateToTab(1) }
                    )
                    FeatureShortcutCard(
                        icon = Icons.Outlined.MenuBook,
                        title = "Doa & Shalat",
                        subtitle = "Buku doa pilhan & panduan",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onNavigateToTab(2) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureShortcutCard(
                        icon = Icons.Outlined.AccessTime,
                        title = "Jadwal Shalat",
                        subtitle = "Waktu shalat & adzan presisi",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onNavigateToTab(3) }
                    )
                    FeatureShortcutCard(
                        icon = Icons.Default.Explore,
                        title = "Kompas Kiblat",
                        subtitle = "Deteksi arah Ka'bah presisi",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onNavigateToTab(6) }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureShortcutCard(
                        icon = Icons.Outlined.BookOpener,
                        title = "Khutbah Masjid",
                        subtitle = "Petugas khutbah masjid terdaftar",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onNavigateToTab(4) }
                    )
                    FeatureShortcutCard(
                        icon = Icons.Outlined.AutoStories,
                        title = "Yasin & Tahlil",
                        subtitle = "Surat Yasin & bacaan doa tahlil lengkap",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = {
                            viewModel.setActiveDoaSubTab(3)
                            onNavigateToTab(2)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureShortcutCard(
                        icon = Icons.Default.VerifiedUser,
                        title = "Dzikir Ratib",
                        subtitle = "Ratib Al-Haddad & Al-Athos lengkap",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onNavigateToTab(7) }
                    )
                    FeatureShortcutCard(
                        icon = Icons.Default.Stars,
                        title = "Maulid Nabi",
                        subtitle = "Simtudduror & Al-Barzanji lengkap",
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onNavigateToTab(8) }
                    )
                }
            }
        }
    }

    // Embed the popup dialog exactly so it is editable right on the main panel!
    if (showQuranReminderDialog) {
        var isEnabledLocal by remember { mutableStateOf(isQuranReminderEnabled) }
        var hourLocal by remember { mutableIntStateOf(quranReminderHour) }
        var minuteLocal by remember { mutableIntStateOf(quranReminderMinute) }
        var targetDaysLocal by remember { mutableIntStateOf(quranReminderTargetDays) }

        Dialog(onDismissRequest = { showQuranReminderDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Target & Pengingat Tilawah",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Atur waktu harian agar konsisten membaca Al-Qur'an layaknya Duolingo Streak!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Aktifkan Pengingat",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = isEnabledLocal,
                            onCheckedChange = { isEnabledLocal = it },
                            modifier = Modifier.testTag("switch_quran_reminder")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Target Days Title
                    Text(
                        text = "Target Konsistensi: $targetDaysLocal Hari",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Styled target selectors
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val targets = listOf(3, 7, 15, 30)
                        targets.forEach { t ->
                            val isSelected = targetDaysLocal == t
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary 
                                        else Color.Transparent
                                    )
                                    .clickable { targetDaysLocal = t }
                                    .padding(vertical = 10.dp)
                                    .testTag("chip_target_day_$t"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$t Hari",
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                            else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time Picker Title
                    Text(
                        text = "Jadwal Pengingat Harian (Jam: Menit)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Clock simulation sliders / buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Jam: %02d".format(hourLocal), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = hourLocal.toFloat(),
                                onValueChange = { hourLocal = it.toInt() },
                                valueRange = 0f..23f,
                                steps = 23,
                                enabled = isEnabledLocal,
                                modifier = Modifier.testTag("slider_quran_hour")
                            )
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Menit: %02d".format(minuteLocal), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Slider(
                                value = minuteLocal.toFloat(),
                                onValueChange = { minuteLocal = it.toInt() },
                                valueRange = 0f..59f,
                                steps = 59,
                                enabled = isEnabledLocal,
                                modifier = Modifier.testTag("slider_quran_minute")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showQuranReminderDialog = false }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.setQuranReminderEnabled(isEnabledLocal)
                                viewModel.setQuranReminderTime(hourLocal, minuteLocal)
                                viewModel.setQuranReminderTargetDays(targetDaysLocal)
                                showQuranReminderDialog = false
                            },
                            modifier = Modifier.testTag("btn_save_quran_reminder")
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Button(onNavigateToTab: () -> Unit, text: String, modifier: Modifier = Modifier) {
    Button(
        onClick = onNavigateToTab,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FeatureShortcutCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.outlineVariant())
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

// Extension to safely generate outline variants on legacy materials
@Composable
fun MaterialTheme.outlineVariant(): Color {
    return this.colorScheme.outlineVariant.copy(alpha = 0.35f)
}

// Add a spacer icon for back compatibility with old designs of icons
val Icons.Outlined.BookOpener: androidx.compose.ui.graphics.vector.ImageVector
    get() = Icons.Default.Mosque
