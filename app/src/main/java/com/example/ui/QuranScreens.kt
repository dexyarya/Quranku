package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.viewmodel.KhutbahViewModel
import com.example.ui.viewmodel.QuranUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranDashboardScreen(
    viewModel: KhutbahViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuranQuery by viewModel.quranSearchQuery.collectAsStateWithLifecycle()
    val filteredSurahs by viewModel.filteredSurahs.collectAsStateWithLifecycle()
    val selectedSurahId by viewModel.selectedSurahId.collectAsStateWithLifecycle()
    
    // States for Qari Selection & Bookmark
    val selectedReciter by viewModel.selectedReciter.collectAsStateWithLifecycle()
    val lastReadSurahId by viewModel.lastReadSurahId.collectAsStateWithLifecycle()
    val lastReadSurahName by viewModel.lastReadSurahName.collectAsStateWithLifecycle()
    val lastReadVerseNo by viewModel.lastReadVerseNo.collectAsStateWithLifecycle()

    // Quran Reminder states
    val isQuranReminderEnabled by viewModel.isQuranReminderEnabled.collectAsStateWithLifecycle()
    val quranReminderHour by viewModel.quranReminderHour.collectAsStateWithLifecycle()
    val quranReminderMinute by viewModel.quranReminderMinute.collectAsStateWithLifecycle()
    val quranReminderTargetDays by viewModel.quranReminderTargetDays.collectAsStateWithLifecycle()
    val quranReadStreak by viewModel.quranReadStreak.collectAsStateWithLifecycle()
    val isQuranReadToday by viewModel.isQuranReadToday.collectAsStateWithLifecycle()
    
    var showTajwidGuide by remember { mutableStateOf(false) }
    var showQariDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (selectedSurahId != null) {
            SurahDetailView(
                viewModel = viewModel,
                surahId = selectedSurahId!!,
                onBack = { viewModel.selectSurah(null) }
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Al-Qur'an Header (Sacred Typography Accent)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.02f)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Al-Qur'anul Karim",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Membaca dengan tartil & mempelajari hukum bacaan",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }

                        // Tajwid Button
                        Button(
                            onClick = { showTajwidGuide = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.tertiary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tajwid", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Bookmark Widget (Terakhir Dibaca)
                if (lastReadSurahId > 0) {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { viewModel.selectSurah(lastReadSurahId) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TERAKHIR DIBACA",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "QS. $lastReadSurahName : Ayat $lastReadVerseNo",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Quran Auto-Sync Progress Banner
                val isSyncRunning by viewModel.isAutoSyncRunning.collectAsStateWithLifecycle()
                val syncProgress by viewModel.autoSyncProgress.collectAsStateWithLifecycle()
                val syncStatusText by viewModel.autoSyncStatusText.collectAsStateWithLifecycle()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (syncProgress == 1f) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) 
                        else 
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (syncProgress == 1f) 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else 
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), 
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSyncRunning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            } else {
                                Icon(
                                    imageVector = if (syncProgress == 1f) 
                                        Icons.Default.CloudDone 
                                    else 
                                        Icons.Default.CloudQueue,
                                    contentDescription = null,
                                    tint = if (syncProgress == 1f) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sinkronisasi Al-Qur'an Offline",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Text(
                                    text = "${(syncProgress * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (syncProgress == 1f) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.tertiary
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = syncStatusText,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            LinearProgressIndicator(
                                progress = syncProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (syncProgress == 1f) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.tertiary,
                                trackColor = if (syncProgress == 1f) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) 
                                else 
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                            )
                        }

                        // Play/Resume Sync action
                        if (!isSyncRunning && syncProgress < 1f) {
                            IconButton(
                                onClick = { viewModel.startQuranAutoSync() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Mulai Ulang",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Qari / Syekh Murottal Selection Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardVoice,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Syekh Murottal:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    SuggestionChip(
                        onClick = { showQariDialog = true },
                        label = { Text(selectedReciter.name, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }

                // Beautiful Minimalist Elevated Search Bar (Pill design)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("search_quran_surface"),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 2.dp,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                ) {
                    TextField(
                        value = searchQuranQuery,
                        onValueChange = { viewModel.updateQuranSearchQuery(it) },
                        placeholder = { 
                            Text(
                                text = "Cari Surat (misal: Yasin, Al-Mulk...)", 
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), 
                                fontSize = 14.sp
                            ) 
                        },
                        leadingIcon = { 
                            Icon(
                                imageVector = Icons.Default.Search, 
                                contentDescription = "Cari Surat", 
                                tint = MaterialTheme.colorScheme.primary
                            ) 
                        },
                        trailingIcon = {
                            if (searchQuranQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateQuranSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close, 
                                        contentDescription = "Bersihkan", 
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_quran_input"),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true
                    )
                }

                // List of Surahs
                if (filteredSurahs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentPasteSearch,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            )
                            Text(
                                "Surat tidak ditemukan",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredSurahs, key = { it.nomor }) { surah ->
                            val downloadedSurahs by viewModel.downloadedSurahIds.collectAsStateWithLifecycle()
                            val downloadingSurahId by viewModel.downloadingSurahId.collectAsStateWithLifecycle()
                            val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
                            val currentPlayingSurah by viewModel.currentPlayingSurah.collectAsStateWithLifecycle()
                            val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
                            val audioLoading by viewModel.audioLoading.collectAsStateWithLifecycle()
                            
                            val isDownloaded = downloadedSurahs.contains(surah.nomor)
                            val isDownloading = downloadingSurahId == surah.nomor
                            val isPlayingThis = isPlaying && currentPlayingSurah?.nomor == surah.nomor
                            val isPlayLoading = audioLoading && currentPlayingSurah?.nomor == surah.nomor

                            SurahCard(
                                surah = surah,
                                isDownloaded = isDownloaded,
                                isDownloading = isDownloading,
                                downloadProgress = downloadProgress,
                                isPlayingThis = isPlayingThis,
                                isPlayLoading = isPlayLoading,
                                onClick = { viewModel.selectSurah(surah.nomor) },
                                onPlayClick = { viewModel.playMurottal(surah) },
                                onDownloadClick = { viewModel.downloadSurah(surah) },
                                onDeleteClick = { viewModel.deleteDownloadedSurah(surah.nomor) }
                            )
                        }
                    }
                }
            }
        }

        if (showTajwidGuide) {
            TajwidGuideDialog(onDismiss = { showTajwidGuide = false })
        }

        if (showQariDialog) {
            QariChoiceDialog(viewModel = viewModel, onDismiss = { showQariDialog = false })
        }
    }
}

@Composable
fun QariChoiceDialog(
    viewModel: KhutbahViewModel,
    onDismiss: () -> Unit
) {
    val activeQariKey by viewModel.selectedReciterKey.collectAsStateWithLifecycle()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Syekh Murottal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.qariList.forEach { qari ->
                    val isSelected = qari.id == activeQariKey
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectReciter(qari.id)
                                onDismiss()
                            }
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                viewModel.selectReciter(qari.id)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = qari.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun SurahCard(
    surah: SurahModel,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    isPlayingThis: Boolean = false,
    isPlayLoading: Boolean = false,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("surah_card_${surah.nomor}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clickable section containing all details except actions
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Number Ornament
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = surah.nomor.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text Details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = surah.namaLatin,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "(${surah.arti})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = surah.tempatTurun,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${surah.jumlahAyat} Ayat",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Arabic text right-aligned + Actions
            Column(
                modifier = Modifier.padding(start = 12.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = surah.nama,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.clickable(onClick = onClick)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Sleek, Custom Styled Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPlayingThis) MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                                else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                            )
                            .clickable { onPlayClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isPlayLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = if (isPlayingThis) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlayingThis) "Pause Murottal" else "Mainkan Murottal",
                                tint = if (isPlayingThis) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Sleek, Custom Styled Download Button
                    if (isDownloading) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (isDownloaded) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                                .clickable { onDeleteClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Hapus Offline",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                .clickable { onDownloadClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Simpan Offline",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahDetailView(
    viewModel: KhutbahViewModel,
    surahId: Int,
    onBack: () -> Unit
) {
    val surahDetailState by viewModel.surahDetailState.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPlayingSurah by viewModel.currentPlayingSurah.collectAsStateWithLifecycle()
    val audioLoading by viewModel.audioLoading.collectAsStateWithLifecycle()
    val downloadingSurahId by viewModel.downloadingSurahId.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()

    val isDownloading = downloadingSurahId == surahId
    val currentSurah = remember(surahId) { QuranStaticData.surahs.find { it.nomor == surahId } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Appbar
        TopAppBarHeader(
            title = currentSurah?.namaLatin ?: "Surat Detail",
            arabicTitle = currentSurah?.nama ?: "",
            onBack = onBack
        )

        when (val state = surahDetailState) {
            is QuranUiState.Idle, is QuranUiState.Loading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Mengambil ayat-ayat Qur'an dari Kemenag...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            is QuranUiState.Success -> {
                val detail = state.surahDetail
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card
                    item {
                        SurahDetailHeaderCard(
                            detail = detail,
                            onPlayFull = { viewModel.playMurottal(currentSurah!!) },
                            isPlayingFull = isPlaying && currentPlayingSurah?.nomor == surahId,
                            isPlayLoading = audioLoading,
                            isDownloading = isDownloading,
                            downloadProgress = downloadProgress
                        )
                    }

                    // Basmalah except for At-Taubah
                    if (surahId != 9 && surahId != 1) {
                        item {
                            BasmalahRow()
                        }
                    }

                    // List representation of verses
                    itemsIndexed(detail.ayat, key = { _, item -> item.nomorAyat }) { index, verse ->
                        val lastReadSurahId by viewModel.lastReadSurahId.collectAsStateWithLifecycle()
                        val lastReadVerseNo by viewModel.lastReadVerseNo.collectAsStateWithLifecycle()
                        
                        val isBookmarked = lastReadSurahId == surahId && lastReadVerseNo == verse.nomorAyat

                        VerseItemCard(
                            verse = verse,
                            isBookmarked = isBookmarked,
                            onBookmarkVerse = { viewModel.updateLastRead(surahId, detail.namaLatin, verse.nomorAyat) },
                            onPlayVerse = { viewModel.playVerseAudio(currentSurah!!, verse) }
                        )
                    }
                }
            }
            is QuranUiState.Error -> {
                // If API fails, offline reading is supported!
                // Let's offer a polished offline fallback screen with details.
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SignalWifiStatusbarConnectedNoInternet4,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Koneksi Terputus",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Dibutuhkan internet untuk mengambil teks ayat lengkap 30 Juz secara real-time. Anda tetap dapat mendengarkan audio murottal atau melihat isi petunjuk tajwid secara gratis.",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                lineHeight = 18.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onBack,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Kembali")
                                }
                                Button(
                                    onClick = { viewModel.selectSurah(surahId) },
                                    modifier = Modifier.weight(1.5f)
                                ) {
                                    Text("Cobalah Lagi")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopAppBarHeader(
    title: String,
    arabicTitle: String,
    onBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Surat ke-$title",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
            }

            Text(
                text = arabicTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp),
                fontFamily = FontFamily.Serif
            )
        }
    }
}

@Composable
fun SurahDetailHeaderCard(
    detail: SurahDetailModel,
    onPlayFull: () -> Unit,
    isPlayingFull: Boolean,
    isPlayLoading: Boolean,
    isDownloading: Boolean = false,
    downloadProgress: Float = 0f
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = detail.namaLatin,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "(${detail.arti})",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                modifier = Modifier.width(180.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = detail.tempatTurun.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f), CircleShape)
                )
                Text(
                    text = "${detail.jumlahAyat} AYAT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Audio Stream Button
            Button(
                onClick = { if (!isDownloading) onPlayFull() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDownloading) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    disabledContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
                enabled = !isDownloading
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mengunduh Murottal... (${(downloadProgress * 100).toInt()}%)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    if (isPlayLoading && isPlayingFull) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    } else {
                        Icon(
                            imageVector = if (isPlayingFull) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isPlayingFull) "Pause Murottal" else "Putar Full Murottal",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BasmalahRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun VerseItemCard(
    verse: VerseModel,
    isBookmarked: Boolean,
    onBookmarkVerse: () -> Unit,
    onPlayVerse: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Verse Header (Actions + Number)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = verse.nomorAyat.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Penanda Bacaan Terakhir Button
                    IconButton(
                        onClick = onBookmarkVerse,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Tandai Terakhir Dibaca",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Play Audio button
                    IconButton(
                        onClick = onPlayVerse,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Dengarkan Ayat",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Arabic text (Right-aligned, custom line heights, pristine contrast)
            Text(
                text = verse.teksArab,
                fontFamily = FontFamily.Serif,
                fontSize = 25.sp,
                lineHeight = 44.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Right,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            // Latin Phonetics (Transliteration)
            Text(
                text = verse.teksLatin,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )

            // Indonesian Meaning (Translation)
            Text(
                text = verse.teksIndonesia,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                textAlign = TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoaDashboardScreen(
    viewModel: KhutbahViewModel,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true
) {
    val searchDoaQuery by viewModel.doaSearchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedDoaCategory.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteDoaIds.collectAsStateWithLifecycle()
    val filteredDoas by viewModel.filteredDoas.collectAsStateWithLifecycle()

    val categories = listOf("Semua", "Favorit", "Harian", "Ibadah", "Perjalanan", "Keluarga", "Pilihan")

    Column(modifier = modifier.fillMaxSize()) {
        // Prayers Header View
        if (showHeader) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.01f)
                            )
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        text = "Buku Doa Pilihan",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Koleksi doa sehari-hari terlengkap disertai arti, arab, & cara membaca",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Beautiful Minimalist Elevated Search Bar (Pill design)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("search_doa_surface"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 2.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )
        ) {
            TextField(
                value = searchDoaQuery,
                onValueChange = { viewModel.updateDoaSearchQuery(it) },
                placeholder = { 
                    Text(
                        text = "Cari doa (misal: tidur, makan, sakit...)", 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), 
                        fontSize = 14.sp
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Search, 
                        contentDescription = "Cari Doa", 
                        tint = MaterialTheme.colorScheme.secondary
                    ) 
                },
                trailingIcon = {
                    if (searchDoaQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateDoaSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Close, 
                                contentDescription = "Bersihkan", 
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_doa_input"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )
        }

        // Filters Horizontal Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateDoaCategory(category) },
                    label = { Text(category) },
                    leadingIcon = {
                        if (category == "Favorit") {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // List representation of prayers
        if (filteredDoas.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (selectedCategory == "Favorit") Icons.Default.FavoriteBorder else Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Text(
                        text = if (selectedCategory == "Favorit") "Belum ada doa favorit tersimpan" else "Doa tidak ditemukan",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredDoas, key = { it.id }) { doa ->
                    val isFavorite = favoriteIds.contains(doa.id)
                    DoaItemCard(
                        doa = doa,
                        isFavorite = isFavorite,
                        onToggleFavorite = { viewModel.toggleFavoriteDoa(doa.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DoaItemCard(
    doa: DoaModel,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("doa_card_${doa.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                        contentColor = MaterialTheme.colorScheme.secondary
                    ) {
                        Text(
                            text = doa.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = doa.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp).testTag("btn_fav_doa_${doa.id}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // centered Arabic font
            Text(
                text = doa.arabic,
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Transliteration
            Text(
                text = doa.latin,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Meaning / Translation
            Text(
                text = doa.translation,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
                modifier = Modifier.fillMaxWidth()
            )

            // Fadhilah / Virtue (Expandable details)
            if (doa.fadhilah.isNotEmpty()) {
                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(14.dp).padding(top = 2.dp)
                            )
                            Column {
                                Text(
                                    text = "Keutamaan & Hikmah:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = doa.fadhilah,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Sembunyikan Hikmah" else "Lihat Fadhilah / Hikmah Doa",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TajwidGuideDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
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
                    Column {
                        Text(
                            text = "Panduan Tajwid & Tasydid",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Belajar melafalkan & menahan bacaan dengan benar",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable explanations
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. CARA BACA TASYDID (TASJDID)
                    TajwidRuleSection(
                        title = "1. Cara Membaca Tasydid (Tashdid / ّ )",
                        description = "Tashdid melambangkan penekanan atau konsonan kembar (geminasi). Saat membaca huruf bertasydid, satukan dan tekan suara Anda pada huruf tersebut sebelum meneruskan aliran kata. Huruf dibaca ganda.",
                        examples = listOf(
                            TajwidExample("Tasydid Biasa", "إِيَّاك نَعْبُدُ", "Iyyaaka na'budu", "Suara 'y' ditekan (didobel), ditahan sejenak tanpa mendengung."),
                            TajwidExample("Tasydid Ghunnah", "النَّاسِّ", "An-Naas(i)", "Khusus untuk M-Mim (ّ م) atau N-Nun (ّ n) bertasydid, WAJIB mendengung (Ghunnah) selama 2 harakat (ketukan) sebelum melepas lafal.")
                        )
                    )

                    // 2. NUN MATI DAN TANWIN
                    TajwidRuleSection(
                        title = "2. Hukum Nun Mati ( نْ ) & Tanwin",
                        description = "Terjadi bila nun mati atau tanwin bertemu huruf-huruf hijaiyah tertentu:",
                        examples = listOf(
                            TajwidExample("Izhar (Jelas)", "مَنْ عَمِلَ", "Man 'amila", "Dibaca jelas tanpa mendengung jika bertemu huruf halqi (ء, هـ, ع, ح, غ, kh)."),
                            TajwidExample("Idgham Bighunnah", "مَنْ يَقُولُ", "May-yaquulu", "Suara dimasukkan ke huruf berikutnya sambil mendengung jika bertemu (ي, n, m, w)."),
                            TajwidExample("Ikhfa (Samar)", "مِنْ قَبْلِ", "Ming-qabli", "Suara disamarkan ke makhraj huruf berikutnya jika bertemu 15 huruf khsusus (ت, ث, j, d...)."),
                            TajwidExample("Iqlab (Tukar ke M)", "مِنْ بَعْدِ", "Mim-ba'di", "Bunyi 'N' diganti menjadi 'M' disertai dengungan samar jika bertemu huruf ب.")
                        )
                    )

                    // 3. QALQALAH (HURUF MEMANTUL)
                    TajwidRuleSection(
                        title = "3. Hukum Qalqalah (Huruf Memantul)",
                        description = "Pantulan bunyi suara pada huruf mati (sukun) atau diwakafkan. Huruf qalqalah ada 5: ق, ط, ب, ج, d (Baju Di Toko).",
                        examples = listOf(
                            TajwidExample("Qalqalah Sughra", "يَقْطَعُونَ", "Yaq-ta'uuna", "Suara memantul tipis/ringan di tengah kata karena sukun asli."),
                            TajwidExample("Qalqalah Kubra", "الْفَلَقِ ۗ", "Al-Falaq", "Suara memantul kuat/tebal di akhir kalimat yang diwakafkan.")
                        )
                    )

                    // 4. HUKUM MAD (PEMBACAAN PANJANG)
                    TajwidRuleSection(
                        title = "4. Hukum Mad (Membaca Panjang)",
                        description = "Memanjangkan suara vokal pada huruf mad (Alif, Wawu, Ya).",
                        examples = listOf(
                            TajwidExample("Mad Thabi'i", "قُوْلُوْا", "Quuluu", "Dibaca panjang standar 2 harakat (ketukan)."),
                            TajwidExample("Mad Wajib Muttashil", "جَاءَ", "Jaaa-a", "Dibaca panjang 4-5 harakat karena bertemu hamzah dalam satu kata.")
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Saya Paham")
                }
            }
        }
    }
}

@Composable
fun TajwidRuleSection(
    title: String,
    description: String,
    examples: List<TajwidExample>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = description,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                examples.forEach { example ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = example.ruleName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = example.arabic,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontFamily = FontFamily.Serif
                            )
                        }
                        Text(
                            text = example.latin,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = example.notes,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                    if (example != examples.last()) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.06f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

data class TajwidExample(
    val ruleName: String,
    val arabic: String,
    val latin: String,
    val notes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MurottalMiniPlayer(
    viewModel: KhutbahViewModel,
    modifier: Modifier = Modifier
) {
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val playingSurah by viewModel.currentPlayingSurah.collectAsStateWithLifecycle()
    val playingVerse by viewModel.currentPlayingVerse.collectAsStateWithLifecycle()
    val audioLoading by viewModel.audioLoading.collectAsStateWithLifecycle()
    val progress by viewModel.playbackProgress.collectAsStateWithLifecycle()
    val positionText by viewModel.playbackPositionText.collectAsStateWithLifecycle()
    val selectedReciter by viewModel.selectedReciter.collectAsStateWithLifecycle()

    if (playingSurah != null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .navigationBarsPadding()
                .testTag("murottal_mini_player"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.96f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Main controls info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Spiritual Icon animation or spin
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mendengarkan Murottal",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (playingVerse != null) {
                                "QS. ${playingSurah!!.namaLatin} : Ayat $playingVerse"
                            } else {
                                "QS. ${playingSurah!!.namaLatin} (Satu Surat Penuh)"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Qari: ${selectedReciter.name}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }

                    // Action buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { viewModel.togglePlayPause() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            if (audioLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.stopAudio() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Progress row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                    Text(
                        text = positionText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
