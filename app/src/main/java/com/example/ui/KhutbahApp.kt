package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.Manifest
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.KhutbahRecord
import com.example.data.model.KhutbahWithMasjid
import com.example.data.model.Masjid
import com.example.data.model.JadwalPetugas
import com.example.data.model.JadwalWithMasjid
import com.example.ui.viewmodel.KhutbahViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhutbahApp(viewModel: KhutbahViewModel) {
    val context = LocalContext.current
    val masjids by viewModel.allMasjids.collectAsStateWithLifecycle()
    val khutbahs by viewModel.khutbahListState.collectAsStateWithLifecycle()
    val jadwals by viewModel.jadwalListState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var currentBottomTab by remember { mutableIntStateOf(0) } // 0 = Beranda, 1 = Al-Qur'an, 2 = Doa & Guide, 3 = Shalat, 4 = Khutbah, 5 = Settings
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Catatan Khutbah, 1 = Masjid Terdaftar, 2 = Jadwal Petugas (under Khutbah)

    var showSplashScreen by remember { mutableStateOf(true) }

    if (showSplashScreen) {
        QuranKuSplashScreen(onDismiss = { showSplashScreen = false })
        return
    }

    val activity = context as? androidx.activity.ComponentActivity
    LaunchedEffect(activity) {
        if (activity != null) {
            val target = activity.intent?.getStringExtra("target_screen")
            if (target != null) {
                when (target) {
                    "quran" -> currentBottomTab = 1
                    "shalat" -> currentBottomTab = 3
                }
                activity.intent?.removeExtra("target_screen")
            }
        }
    }

    // Dialog state
    var showAddMasjidDialog by remember { mutableStateOf(false) }
    var showAddKhutbahDialog by remember { mutableStateOf(false) }
    var showAddJadwalDialog by remember { mutableStateOf(false) }

    var masjidToEdit by remember { mutableStateOf<Masjid?>(null) }
    var khutbahToEdit by remember { mutableStateOf<KhutbahWithMasjid?>(null) }
    var jadwalToEdit by remember { mutableStateOf<JadwalWithMasjid?>(null) }

    var khutbahToDelete by remember { mutableStateOf<KhutbahWithMasjid?>(null) }
    var masjidToDelete by remember { mutableStateOf<Masjid?>(null) }
    var jadwalToDelete by remember { mutableStateOf<JadwalWithMasjid?>(null) }

    var selectedFilterMasjidId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (currentBottomTab == 4 && activeTab in 0..2) {
                FloatingActionButton(
                    onClick = {
                        if (activeTab == 0) {
                            showAddKhutbahDialog = true
                        } else if (activeTab == 1) {
                            showAddMasjidDialog = true
                        } else {
                            showAddJadwalDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("main_fab")
                ) {
                    Icon(
                        imageVector = when (activeTab) {
                            0 -> Icons.Default.PostAdd
                            1 -> Icons.Default.AddHome
                            else -> Icons.Default.EditCalendar
                        },
                        contentDescription = when (activeTab) {
                            0 -> "Tambah Khutbah"
                            1 -> "Daftar Masjid"
                            else -> "Tambah Jadwal Petugas"
                        }
                    )
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 4.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(24.dp),
                            clip = false,
                            ambientColor = Color.Black.copy(alpha = 0.06f),
                            spotColor = Color.Black.copy(alpha = 0.1f)
                        )
                        .testTag("bottom_navigation_bar"),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                    )
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0, 0, 0, 0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                    ) {
                        val navItemColors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = Color.Transparent, // Menghilangkan pil kaku standar
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                        )

                        @Composable
                        fun CustomNavItem(
                            selected: Boolean,
                            onClick: () -> Unit,
                            selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
                            unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
                            label: String,
                            testTag: String
                        ) {
                            NavigationBarItem(
                                selected = selected,
                                onClick = onClick,
                                icon = {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (selected) selectedIcon else unselectedIcon,
                                            contentDescription = label,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = navItemColors,
                                modifier = Modifier.testTag(testTag)
                            )
                        }

                        CustomNavItem(
                            selected = currentBottomTab == 0,
                            onClick = { currentBottomTab = 0 },
                            selectedIcon = Icons.Filled.Home,
                            unselectedIcon = Icons.Outlined.Home,
                            label = "Beranda",
                            testTag = "bottom_tab_beranda"
                        )
                        CustomNavItem(
                            selected = currentBottomTab == 1,
                            onClick = { currentBottomTab = 1 },
                            selectedIcon = Icons.Filled.AutoStories,
                            unselectedIcon = Icons.Outlined.AutoStories,
                            label = "Qur'an",
                            testTag = "bottom_tab_quran"
                        )
                        CustomNavItem(
                            selected = currentBottomTab == 2,
                            onClick = { currentBottomTab = 2 },
                            selectedIcon = Icons.Filled.MenuBook,
                            unselectedIcon = Icons.Outlined.MenuBook,
                            label = "Doa & Shalat",
                            testTag = "bottom_tab_doa"
                        )
                        CustomNavItem(
                            selected = currentBottomTab == 3,
                            onClick = { currentBottomTab = 3 },
                            selectedIcon = Icons.Filled.AccessTime,
                            unselectedIcon = Icons.Outlined.AccessTime,
                            label = "Shalat",
                            testTag = "bottom_tab_shalat"
                        )
                        CustomNavItem(
                            selected = currentBottomTab == 4,
                            onClick = { currentBottomTab = 4 },
                            selectedIcon = Icons.Filled.Mosque,
                            unselectedIcon = Icons.Outlined.Mosque,
                            label = "Khutbah",
                            testTag = "bottom_tab_khutbah"
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (currentBottomTab == 4) {
                    // Elegant Emerald Gradient Header with compact size and no integrated stat badges
                    HeaderView(
                        khutbahCount = khutbahs.size,
                        masjidCount = masjids.size,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.updateSearchQuery(it) },
                        onClearFilter = { selectedFilterMasjidId = null },
                        selectedFilterMasjidId = selectedFilterMasjidId,
                        masjids = masjids
                    )

                    // Neat & Scrollable sub-navigation tabs under Khutbah
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            if (activeTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Outlined.Book, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("Khutbah", fontSize = 13.sp, fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal)
                                }
                            },
                            modifier = Modifier.testTag("tab_khutbah")
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Outlined.Mosque, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("Masjid", fontSize = 13.sp, fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal)
                                }
                            },
                            modifier = Modifier.testTag("tab_masjid")
                        )
                        Tab(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text("Petugas", fontSize = 13.sp, fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal)
                                }
                            },
                            modifier = Modifier.testTag("tab_jadwal")
                        )
                    }
                }

                // Smooth tabs content dispatcher
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentBottomTab) {
                        0 -> {
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { index -> currentBottomTab = index },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        1 -> {
                            QuranDashboardScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        2 -> {
                            DoaAndShalatGuideTabScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        3 -> {
                            PrayerTimesDashboardScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize(),
                                onNavigateToQibla = { currentBottomTab = 6 }
                            )
                        }
                        4 -> { // Khutbah features
                            when (activeTab) {
                                0 -> {
                                    val filteredKhutbahs = if (selectedFilterMasjidId != null) {
                                        khutbahs.filter { it.masjidId == selectedFilterMasjidId }
                                    } else {
                                        khutbahs
                                    }

                                    if (filteredKhutbahs.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            Column(modifier = Modifier.fillMaxSize()) {
                                                // Even if list is empty, display the stats badge here under the tab
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    StatBadge(
                                                        icon = Icons.Default.Mosque,
                                                        label = "Masjid",
                                                        value = "${masjids.size}",
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    StatBadge(
                                                        icon = Icons.Default.ImportContacts,
                                                        label = "Sermon Logs",
                                                        value = "${khutbahs.size}",
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                EmptyStateView(
                                                    title = if (searchQuery.isNotEmpty()) "Catatan Tidak Ditemukan" else "Belum Ada Catatan Khutbah",
                                                    subtitle = if (searchQuery.isNotEmpty()) "Coba kata kunci pencarian yang lain." else "Mulai mencatat khutbah Jumat pertama di masjid terdaftar Anda.",
                                                    icon = Icons.Outlined.LibraryBooks
                                                )
                                            }
                                        }
                                    } else {
                                        LazyColumn(
                                            contentPadding = PaddingValues(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .testTag("khutbah_list")
                                        ) {
                                            // Prepend local Stats badges inside the list so they are spacious and beautiful!
                                            item {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    StatBadge(
                                                        icon = Icons.Default.Mosque,
                                                        label = "Masjid",
                                                        value = "${masjids.size}",
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    StatBadge(
                                                        icon = Icons.Default.ImportContacts,
                                                        label = "Sermon Logs",
                                                        value = "${khutbahs.size}",
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                            items(filteredKhutbahs, key = { it.id }) { record ->
                                                KhutbahCard(
                                                    record = record,
                                                    onEdit = { khutbahToEdit = record },
                                                    onDelete = { khutbahToDelete = record }
                                                )
                                            }
                                        }
                                    }
                                }
                                1 -> {
                                    if (masjids.isEmpty()) {
                                        EmptyStateView(
                                            title = "Belum Ada Masjid Terdaftar",
                                            subtitle = "Silakan daftarkan masjid di sekitar Anda terlebih dahulu untuk memulai mencatat khutbah.",
                                            icon = Icons.Outlined.AddHomeWork
                                        )
                                    } else {
                                        LazyColumn(
                                            contentPadding = PaddingValues(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .testTag("masjid_list")
                                        ) {
                                            items(masjids, key = { it.id }) { masjid ->
                                                MasjidCard(
                                                    masjid = masjid,
                                                    isFiltering = selectedFilterMasjidId == masjid.id,
                                                    onEdit = { masjidToEdit = masjid },
                                                    onDelete = { masjidToDelete = masjid },
                                                    onFilterToggle = {
                                                        selectedFilterMasjidId = if (selectedFilterMasjidId == masjid.id) null else masjid.id
                                                        activeTab = 0 // Switch to Khutbah list to see filters
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                2 -> {
                                    val filteredJadwals = if (selectedFilterMasjidId != null) {
                                        jadwals.filter { it.masjidId == selectedFilterMasjidId }
                                    } else {
                                        jadwals
                                    }

                                    if (filteredJadwals.isEmpty()) {
                                        EmptyStateView(
                                            title = if (searchQuery.isNotEmpty()) "Jadwal Tidak Ditemukan" else "Belum Ada Jadwal Petugas",
                                            subtitle = if (searchQuery.isNotEmpty()) "Coba kata kunci pencarian yang lain." else "Mulai menjadwalkan petugas khatib, imam, muadzin, dan bilal di masjid pilihan Anda.",
                                            icon = Icons.Outlined.CalendarToday
                                        )
                                    } else {
                                        LazyColumn(
                                            contentPadding = PaddingValues(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .testTag("jadwal_list")
                                        ) {
                                            items(filteredJadwals, key = { it.id }) { jadwal ->
                                                JadwalCard(
                                                    jadwal = jadwal,
                                                    onEdit = { jadwalToEdit = jadwal },
                                                    onDelete = { jadwalToDelete = jadwal }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        5 -> {
                            SettingsScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        6 -> {
                            QiblaFinderScreen(
                                viewModel = viewModel,
                                onBack = { currentBottomTab = 0 },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Floating mini media player for Murottal recitations overlayed beautifully
            MurottalMiniPlayer(
                viewModel = viewModel,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }

        // Add/Edit Masjid Dialogs
        if (showAddMasjidDialog) {
            MasjidFormDialog(
                onDismiss = { showAddMasjidDialog = false },
                onSave = { nama, alamat, alamatLengkap, kontak ->
                    viewModel.addMasjid(nama, alamat, alamatLengkap, kontak)
                    showAddMasjidDialog = false
                }
            )
        }

        masjidToEdit?.let { masjid ->
            MasjidFormDialog(
                masjid = masjid,
                onDismiss = { masjidToEdit = null },
                onSave = { nama, alamat, alamatLengkap, kontak ->
                    viewModel.updateMasjid(masjid.copy(nama = nama, alamat = alamat, alamatLengkap = alamatLengkap, kontak = kontak))
                    masjidToEdit = null
                }
            )
        }

        // Add/Edit Khutbah Dialogs
        if (showAddKhutbahDialog) {
            if (masjids.isEmpty()) {
                AlertDialog(
                    onDismissRequest = { showAddKhutbahDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showAddKhutbahDialog = false
                            activeTab = 1 // Redirect to Masjid tab
                            showAddMasjidDialog = true
                        }) {
                            Text("Daftarkan Masjid Sekarang")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddKhutbahDialog = false }) {
                            Text("Batal")
                        }
                    },
                    title = { Text("Masjid Belum Terdaftar") },
                    text = { Text("Untuk mencatat khutbah, Anda harus mendaftarkan setidaknya satu masjid terlebih dahulu.") },
                    modifier = Modifier.testTag("no_masjid_alert")
                )
            } else {
                KhutbahFormDialog(
                    masjids = masjids,
                    onDismiss = { showAddKhutbahDialog = false },
                    onSave = { masjidId, tanggal, khatib, judul, summary, durasi ->
                        viewModel.addKhutbah(masjidId, tanggal, khatib, judul, summary, durasi)
                        showAddKhutbahDialog = false
                    }
                )
            }
        }

        khutbahToEdit?.let { record ->
            KhutbahFormDialog(
                record = record,
                masjids = masjids,
                onDismiss = { khutbahToEdit = null },
                onSave = { masjidId, tanggal, khatib, judul, summary, durasi ->
                    viewModel.updateKhutbah(
                        KhutbahRecord(
                            id = record.id,
                            masjidId = masjidId,
                            tanggal = tanggal,
                            khatib = khatib,
                            judul = judul,
                            summary = summary,
                            durasiMenit = durasi,
                            createdAt = record.createdAt
                        )
                    )
                    khutbahToEdit = null
                }
            )
        }

        // Deletion Confirmations
        khutbahToDelete?.let { record ->
            AlertDialog(
                onDismissRequest = { khutbahToDelete = null },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteKhutbah(record)
                            khutbahToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { khutbahToDelete = null }) {
                        Text("Batal")
                    }
                },
                title = { Text("Hapus Catatan Khutbah") },
                text = { Text("Apakah Anda yakin ingin menghapus catatan khutbah dari \"${record.khatib}\" di masjid ${record.namaMasjid}?") },
                modifier = Modifier.testTag("delete_khutbah_confirm")
            )
        }

        masjidToDelete?.let { masjid ->
            AlertDialog(
                onDismissRequest = { masjidToDelete = null },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteMasjid(masjid)
                            if (selectedFilterMasjidId == masjid.id) {
                                selectedFilterMasjidId = null
                            }
                            masjidToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { masjidToDelete = null }) {
                        Text("Batal")
                    }
                },
                title = { Text("Hapus Masjid") },
                text = { Text("Apakah Anda yakin ingin menghapus masjid \"${masjid.nama}\"? Menghapus masjid juga akan menghapus semua catatan khutbah yang berkaitan dengannya.") },
                modifier = Modifier.testTag("delete_masjid_confirm")
            )
        }

        // Add/Edit schedule Dialogs
        if (showAddJadwalDialog) {
            if (masjids.isEmpty()) {
                AlertDialog(
                    onDismissRequest = { showAddJadwalDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showAddJadwalDialog = false
                            activeTab = 1 // Redirect to Masjid tab
                            showAddMasjidDialog = true
                        }) {
                            Text("Daftarkan Masjid Sekarang")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddJadwalDialog = false }) {
                            Text("Batal")
                        }
                    },
                    title = { Text("Masjid Belum Terdaftar") },
                    text = { Text("Untuk membuat jadwal petugas khutbah, Anda harus mendaftarkan setidaknya satu masjid terlebih dahulu.") },
                    modifier = Modifier.testTag("no_masjid_jadwal_alert")
                )
            } else {
                JadwalFormDialog(
                    masjids = masjids,
                    onDismiss = { showAddJadwalDialog = false },
                    onSave = { masjidId, tanggal, khatib, imam, muadzin, bilal, keterangan ->
                        viewModel.addJadwal(masjidId, tanggal, khatib, imam, muadzin, bilal, keterangan)
                        showAddJadwalDialog = false
                    }
                )
            }
        }

        jadwalToEdit?.let { record ->
            JadwalFormDialog(
                jadwal = record,
                masjids = masjids,
                onDismiss = { jadwalToEdit = null },
                onSave = { masjidId, tanggal, khatib, imam, muadzin, bilal, keterangan ->
                    viewModel.updateJadwal(
                        com.example.data.model.JadwalPetugas(
                            id = record.id,
                            masjidId = masjidId,
                            tanggal = tanggal,
                            khatib = khatib,
                            imam = imam,
                            muadzin = muadzin,
                            bilal = bilal,
                            keterangan = keterangan,
                            createdAt = record.createdAt
                        )
                    )
                    jadwalToEdit = null
                }
            )
        }

        jadwalToDelete?.let { record ->
            AlertDialog(
                onDismissRequest = { jadwalToDelete = null },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteJadwal(record)
                            jadwalToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Hapus")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { jadwalToDelete = null }) {
                        Text("Batal")
                    }
                },
                title = { Text("Hapus Jadwal Petugas") },
                text = { Text("Apakah Anda yakin ingin menghapus jadwal petugas Jum'at di masjid \"${record.namaMasjid}\" tanggal ${record.tanggal}?") },
                modifier = Modifier.testTag("delete_jadwal_confirm")
            )
        }
    }
}

// Procedural visual components
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderView(
    khutbahCount: Int,
    masjidCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClearFilter: () -> Unit,
    selectedFilterMasjidId: Int?,
    masjids: List<Masjid>
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val goldAccent = MaterialTheme.colorScheme.tertiary
    val darkBackground = MaterialTheme.colorScheme.background
    val darkSurface = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val brush = Brush.verticalGradient(
                    colors = listOf(
                        darkSurface,
                        darkBackground
                    )
                )
                drawRect(brush = brush)
            }
            .statusBarsPadding()
            .padding(top = 20.dp, bottom = 24.dp)
            .testTag("brand_header")
    ) {
        // Overlay stylish vector artwork - Arabic architecture dome & crescent
        Canvas(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(160.dp)
                .offset(x = 20.dp, y = (-20).dp)
        ) {
            // Draw dome archways procedurally with semi-transparency
            val path = Path().apply {
                moveTo(size.width * 0.3f, size.height)
                cubicTo(
                    size.width * 0.3f, size.height * 0.4f,
                    size.width * 0.5f, size.height * 0.1f,
                    size.width * 0.7f, size.height * 0.4f
                )
                cubicTo(
                    size.width * 0.8f, size.height * 0.5f,
                    size.width * 0.85f, size.height * 0.8f,
                    size.width * 0.85f, size.height
                )
                close()
            }
            drawPath(
                path = path,
                color = primaryColor.copy(alpha = 0.08f)
            )

            // Draw beautiful golden crescent
            drawCircle(
                color = goldAccent.copy(alpha = 0.12f),
                radius = 28.dp.toPx(),
                center = Offset(size.width * 0.45f, size.height * 0.35f)
            )
            drawCircle(
                color = Color.Transparent, // acts as punchout conceptually
                radius = 26.dp.toPx(),
                center = Offset(size.width * 0.40f, size.height * 0.31f),
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Catatan Khutbah",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Jurnal Pencatatan & Jadwal Masjid",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Small elegant badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = goldAccent,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "Jumat",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Beautiful Minimalist Elevated Search Bar (Pill design)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_surface"),
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
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { 
                        Text(
                            text = "Cari judul, khatib, isi khutbah...", 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), 
                            fontSize = 14.sp
                        ) 
                    },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Search, 
                            contentDescription = "Cari", 
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
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
                        .testTag("search_field"),
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

            // Show active location filter indicators
            if (selectedFilterMasjidId != null) {
                val filterName = masjids.find { it.id == selectedFilterMasjidId }?.nama ?: "Masjid"
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable { onClearFilter() }
                        .testTag("clear_filter_badge")
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Masjid: $filterName",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(Icons.Default.Cancel, contentDescription = "Clear", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun StatBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}


@Composable
fun EmptyStateView(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag("empty_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun KhutbahCard(
    record: KhutbahWithMasjid,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("khutbah_card_${record.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp))
                        Text(text = record.tanggal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_edit_khutbah_${record.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_delete_khutbah_${record.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sermon Title
            Text(
                text = record.judul,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.primary,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Preacher detail
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = record.khatib,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Mosque detail
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mosque,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = record.namaMasjid,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            }

            // Summary notes block (expandable)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catatan Khutbah",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${record.durasiMenit} menit",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = record.summary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .testTag("khutbah_summary_full_${record.id}")
                )
            }

            if (!isExpanded) {
                Text(
                    text = record.summary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isExpanded) "Tampilkan lebih sedikit" else "Tampilkan selengkapnya...",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun MasjidCard(
    masjid: Masjid,
    isFiltering: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFilterToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("masjid_card_${masjid.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFiltering) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isFiltering) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mosque,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = masjid.nama,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(13.dp))
                            Text(
                                text = masjid.alamat,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_edit_masjid_${masjid.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_delete_masjid_${masjid.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Alamat Lengkap
            if (masjid.alamatLengkap.isNotEmpty()) {
                Text(
                    text = masjid.alamatLengkap,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 2.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Kontak info
            if (masjid.kontak.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(start = 2.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Telepon", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                    Text(
                        text = masjid.kontak,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom action: filter khutbah for this masjid
            Button(
                onClick = onFilterToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFiltering) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = if (isFiltering) Color.White else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .testTag("btn_filter_masjid_${masjid.id}"),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = if (isFiltering) Icons.Default.FilterAltOff else Icons.Default.FilterAlt,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isFiltering) "Hilangkan Filter Saringan" else "Lihat Semua Khutbah Masjid Ini",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Form Dialogue for Masjid
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasjidFormDialog(
    masjid: Masjid? = null,
    onDismiss: () -> Unit,
    onSave: (nama: String, alamat: String, alamatLengkap: String, kontak: String) -> Unit
) {
    var nama by remember { mutableStateOf(masjid?.nama ?: "") }
    var alamat by remember { mutableStateOf(masjid?.alamat ?: "") }
    var alamatLengkap by remember { mutableStateOf(masjid?.alamatLengkap ?: "") }
    var kontak by remember { mutableStateOf(masjid?.kontak ?: "") }

    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("masjid_dialog_container"),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
            ) {
                Text(
                    text = if (masjid == null) "Daftarkan Masjid Baru" else "Edit Detail Masjid",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nama,
                    onValueChange = {
                        nama = it
                        if (it.isNotEmpty()) isError = false
                    },
                    label = { Text("Nama Masjid *") },
                    placeholder = { Text("Contoh: Masjid Baitul Makmur") },
                    isError = isError,
                    supportingText = {
                        if (isError) Text("Nama masjid wajib diisi")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_masjid_nama"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = alamat,
                    onValueChange = { alamat = it },
                    label = { Text("Kota / Wilayah") },
                    placeholder = { Text("Contoh: Kebayoran Lama, Jakarta") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_masjid_alamat"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = alamatLengkap,
                    onValueChange = { alamatLengkap = it },
                    label = { Text("Alamat Lengkap") },
                    placeholder = { Text("Jl. Teuku Umar No.12, Kebayoran") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_masjid_alamat_lengkap"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = kontak,
                    onValueChange = { kontak = it },
                    label = { Text("Kontak / Humas DKM") },
                    placeholder = { Text("Telepon atau nama penanggungjawab") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_masjid_kontak"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nama.isBlank()) {
                                isError = true
                            } else {
                                onSave(nama, alamat, alamatLengkap, kontak)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_save_masjid")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// Form Dialogue for Khutbah Log
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhutbahFormDialog(
    record: KhutbahWithMasjid? = null,
    masjids: List<Masjid>,
    onDismiss: () -> Unit,
    onSave: (masjidId: Int, tanggal: String, khatib: String, judul: String, summary: String, durasi: Int) -> Unit
) {
    var expandedMasjidDropdown by remember { mutableStateOf(false) }

    // Init state from record if editing
    var selectedMasjid by remember {
        mutableStateOf(
            masjids.find { it.id == record?.masjidId } ?: masjids.first()
        )
    }
    
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val todayStr = sdf.format(Date())
    
    var tanggal by remember { mutableStateOf(record?.tanggal ?: todayStr) }
    var khatib by remember { mutableStateOf(record?.khatib ?: "") }
    var judul by remember { mutableStateOf(record?.judul ?: "") }
    var summary by remember { mutableStateOf(record?.summary ?: "") }
    var durasi by remember { mutableStateOf(record?.durasiMenit?.toFloat() ?: 20f) }

    // Errors
    var khatibError by remember { mutableStateOf(false) }
    var judulError by remember { mutableStateOf(false) }
    var summaryError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("khutbah_dialog_container"),
            shape = RoundedCornerShape(24.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp)
            ) {
                item {
                    Text(
                        text = if (record == null) "Tulis Catatan Khutbah Baru" else "Edit Catatan Khutbah",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mosque selector dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedMasjidDropdown,
                        onExpandedChange = { expandedMasjidDropdown = !expandedMasjidDropdown },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedMasjid.nama,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Masjid Lokasi *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMasjidDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("select_masjid_trigger"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedMasjidDropdown,
                            onDismissRequest = { expandedMasjidDropdown = false },
                            modifier = Modifier.testTag("masjid_dropdown_menu")
                        ) {
                            masjids.forEach { masjid ->
                                DropdownMenuItem(
                                    text = { Text(masjid.nama) },
                                    onClick = {
                                        selectedMasjid = masjid
                                        expandedMasjidDropdown = false
                                    },
                                    modifier = Modifier.testTag("dropdown_item_${masjid.id}")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tanggal,
                        onValueChange = { tanggal = it },
                        label = { Text("Tanggal Khutbah *") },
                        placeholder = { Text("Format: DD-MM-YYYY") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_khutbah_tanggal"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = khatib,
                        onValueChange = {
                            khatib = it
                            if (it.isNotEmpty()) khatibError = false
                        },
                        label = { Text("Nama Khatib *") },
                        placeholder = { Text("Contoh: Ustadz H. Abdul Somad") },
                        isError = khatibError,
                        supportingText = {
                            if (khatibError) Text("Nama khatib wajib diisi")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_khutbah_khatib"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = judul,
                        onValueChange = {
                            judul = it
                            if (it.isNotEmpty()) judulError = false
                        },
                        label = { Text("Judul / Tema Khutbah *") },
                        placeholder = { Text("Contoh: Mensyukuri Nikmat Kemerdekaan") },
                        isError = judulError,
                        supportingText = {
                            if (judulError) Text("Judul khutbah wajib diisi")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_khutbah_judul"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = summary,
                        onValueChange = {
                            summary = it
                            if (it.isNotEmpty()) summaryError = false
                        },
                        label = { Text("Ringkasan Isi Khutbah *") },
                        placeholder = { Text("Tulis sari pati khutbah jumat, poin-poin utama, rujukan dalil, atau nasihat taqwa...") },
                        isError = summaryError,
                        supportingText = {
                            if (summaryError) Text("Ringkasan / catatan wajib diisi")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_khutbah_summary"),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 4,
                        maxLines = 10
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Durasi slider
                    Text(
                        text = "Durasi Khutbah: ${durasi.toInt()} menit",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Slider(
                        value = durasi,
                        onValueChange = { durasi = it },
                        valueRange = 10f..60f,
                        steps = 9 // 5-minute segments basically
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (khatib.isBlank()) khatibError = true
                                if (judul.isBlank()) judulError = true
                                if (summary.isBlank()) summaryError = true

                                if (khatib.isNotBlank() && judul.isNotBlank() && summary.isNotBlank()) {
                                    onSave(selectedMasjid.id, tanggal, khatib, judul, summary, durasi.toInt())
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_save_khutbah")
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
fun JadwalCard(
    jadwal: JadwalWithMasjid,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("jadwal_card_${jadwal.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mosque details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = jadwal.namaMasjid,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_edit_jadwal_${jadwal.id}")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_delete_jadwal_${jadwal.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle address
            Text(
                text = jadwal.alamatMasjid,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 28.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grid or table-like representation of Scheduled officers
            // First: Date
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = jadwal.tanggal,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "Petugas Jumat",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(12.dp))

            // Detailed Officer Rows
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Khatib
                OfficerRow(
                    role = "Khatib",
                    name = jadwal.khatib,
                    icon = Icons.Default.RecordVoiceOver,
                    iconColor = MaterialTheme.colorScheme.primary
                )

                // Imam
                OfficerRow(
                    role = "Imam",
                    name = if (jadwal.imam.isNotBlank()) jadwal.imam else jadwal.khatib,
                    icon = Icons.Default.Person,
                    iconColor = MaterialTheme.colorScheme.secondary
                )

                if (isExpanded || jadwal.muadzin.isNotBlank() || jadwal.bilal.isNotBlank() || jadwal.keterangan.isNotBlank()) {
                    // Muadzin
                    OfficerRow(
                        role = "Muadzin",
                        name = if (jadwal.muadzin.isNotBlank()) jadwal.muadzin else "-",
                        icon = Icons.Default.VolumeUp,
                        iconColor = MaterialTheme.colorScheme.tertiary
                    )

                    // Bilal
                    OfficerRow(
                        role = "Bilal",
                        name = if (jadwal.bilal.isNotBlank()) jadwal.bilal else "-",
                        icon = Icons.Default.Mic,
                        iconColor = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Expand notes if available
            if (jadwal.keterangan.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Keterangan / Info Tambahan:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = jadwal.keterangan,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
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
                    text = if (isExpanded) "Tutup rincian petugas" else "Lihat rincian lengkap (Muadzin & Bilal)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun OfficerRow(
    role: String,
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconColor.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = role,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalFormDialog(
    jadwal: JadwalWithMasjid? = null,
    masjids: List<Masjid>,
    onDismiss: () -> Unit,
    onSave: (masjidId: Int, tanggal: String, khatib: String, imam: String, muadzin: String, bilal: String, keterangan: String) -> Unit
) {
    if (masjids.isEmpty()) return

    val isEdit = jadwal != null
    
    // Form fields
    var selectedMasjid by remember { mutableStateOf(masjids.find { it.id == jadwal?.masjidId } ?: masjids.first()) }
    var isMasjidMenuExpanded by remember { mutableStateOf(false) }

    var tanggal by remember { mutableStateOf(jadwal?.tanggal ?: "19-06-2026") }
    var khatib by remember { mutableStateOf(jadwal?.khatib ?: "") }
    var imam by remember { mutableStateOf(jadwal?.imam ?: "") }
    var muadzin by remember { mutableStateOf(jadwal?.muadzin ?: "") }
    var bilal by remember { mutableStateOf(jadwal?.bilal ?: "") }
    var keterangan by remember { mutableStateOf(jadwal?.keterangan ?: "") }

    // Validation flags
    var khatibError by remember { mutableStateOf(false) }
    var imamError by remember { mutableStateOf(false) }
    var tanggalError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Text(
                    text = if (isEdit) "Ubah Jadwal Petugas" else "Tambah Jadwal Petugas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Jadwalkan khatib, imam, muadzin, dan bilal Jumat",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Form fields inside a scrollable column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Masjid Selector (Dropdown)
                    Text(
                        text = "Masjid Tempat Jadwal *",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { isMasjidMenuExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dropdown_select_masjid_jadwal"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(selectedMasjid.nama, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = isMasjidMenuExpanded,
                            onDismissRequest = { isMasjidMenuExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            masjids.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.nama, fontSize = 14.sp) },
                                    onClick = {
                                        selectedMasjid = m
                                        isMasjidMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Tanggal input
                    OutlinedTextField(
                        value = tanggal,
                        onValueChange = {
                            tanggal = it
                            if (it.isNotEmpty()) tanggalError = false
                        },
                        label = { Text("Tanggal Jumat (dd-mm-yyyy) *") },
                        placeholder = { Text("E.g., 19-06-2026") },
                        isError = tanggalError,
                        supportingText = {
                            if (tanggalError) Text("Tanggal wajib diisi")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_jadwal_tanggal"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Khatib input
                    OutlinedTextField(
                        value = khatib,
                        onValueChange = {
                            khatib = it
                            if (it.isNotEmpty()) khatibError = false
                        },
                        label = { Text("Nama Khatib *") },
                        placeholder = { Text("E.g., Dr. K.H. Ahmad") },
                        isError = khatibError,
                        supportingText = {
                            if (khatibError) Text("Nama khatib wajib diisi")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_jadwal_khatib"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Imam input
                    OutlinedTextField(
                        value = imam,
                        onValueChange = {
                            imam = it
                            if (it.isNotEmpty()) imamError = false
                        },
                        label = { Text("Nama Imam *") },
                        placeholder = { Text("E.g., Drs. H. Muhammad - kosongkan jika sama dgn khatib") },
                        isError = imamError,
                        supportingText = {
                            if (imamError) Text("Nama imam wajib diisi")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_jadwal_imam"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Muadzin input
                        OutlinedTextField(
                            value = muadzin,
                            onValueChange = { muadzin = it },
                            label = { Text("Muadzin") },
                            placeholder = { Text("Ust. Saiful") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_jadwal_muadzin"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Bilal input
                        OutlinedTextField(
                            value = bilal,
                            onValueChange = { bilal = it },
                            label = { Text("Bilal") },
                            placeholder = { Text("Ust. Bilal") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_jadwal_bilal"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    // Keterangan / Tema input
                    OutlinedTextField(
                        value = keterangan,
                        onValueChange = { keterangan = it },
                        label = { Text("Keterangan Tambahan / Tema") },
                        placeholder = { Text("E.g., Mulai jam 11:45 atau tema khutbah...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_jadwal_keterangan"),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (tanggal.isBlank()) tanggalError = true
                            if (khatib.isBlank()) khatibError = true
                            if (imam.isBlank()) imamError = true

                            if (tanggal.isNotBlank() && khatib.isNotBlank() && imam.isNotBlank()) {
                                onSave(selectedMasjid.id, tanggal, khatib, imam, muadzin, bilal, keterangan)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("btn_save_jadwal")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

// Extension helper
fun Modifier.size(size: androidx.compose.ui.unit.Dp) = this.then(Modifier.width(size).height(size))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: KhutbahViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isAdhanEnabled by viewModel.isAdhanEnabled.collectAsStateWithLifecycle()
    val isAlarmFajr by viewModel.isAlarmFajr.collectAsStateWithLifecycle()
    val isAlarmSunrise by viewModel.isAlarmSunrise.collectAsStateWithLifecycle()
    val isAlarmDhuhr by viewModel.isAlarmDhuhr.collectAsStateWithLifecycle()
    val isAlarmAsr by viewModel.isAlarmAsr.collectAsStateWithLifecycle()
    val isAlarmMaghrib by viewModel.isAlarmMaghrib.collectAsStateWithLifecycle()
    val isAlarmIsha by viewModel.isAlarmIsha.collectAsStateWithLifecycle()

    var showPermissionInfo by remember { mutableStateOf(false) }

    // Launcher for Android 13+ post notifications permission
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.widget.Toast.makeText(context, "Izin notifikasi diberikan.", android.widget.Toast.LENGTH_SHORT).show()
        } else {
            android.widget.Toast.makeText(context, "Izin notifikasi ditolak. Alarm Azan mungkin tidak muncul.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Aesthetic Gradient Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Pengaturan Aplikasi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = "Sesuaikan tampilan dan kelola pengingat alarm azan shalat otomatis.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // TAMPILAN CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tampilan & Tema",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setDarkTheme(!isDarkTheme) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Column {
                                Text(
                                    text = "Mode Gelap (Dark Mode)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isDarkTheme) "Tampilan gelap yang nyaman di mata." else "Tampilan terang klasik yang cerah.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.setDarkTheme(it) },
                            modifier = Modifier.testTag("theme_toggle_switch")
                        )
                    }
                }
            }
        }

        // ALARM CARD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alarm & Pengingat Azan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        IconButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                android.widget.Toast.makeText(context, "Izin notifikasi sudah aktif (versi Android lama).", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Izin Notifikasi",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = "Nyalakan suara alarm (Adhan/Alert) saat memasuki jadwal shalat fardhu dan terbit matahari (syuruq).",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Master Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setAdhanEnabled(!isAdhanEnabled) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isAdhanEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                contentDescription = null,
                                tint = if (isAdhanEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Column {
                                Text(
                                    text = "Aktifkan Semua Alarm Azan",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Mengaktifkan/mematikan seluruh alarm pengingat.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Switch(
                            checked = isAdhanEnabled,
                            onCheckedChange = { viewModel.setAdhanEnabled(it) },
                            modifier = Modifier.testTag("master_alarm_switch")
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Individual Alarms (Subuh, Terbit, Dzuhur, Ashar, Maghrib, Isya)
                    val prayersList = listOf(
                        Triple("Subuh (Fajr)", isAlarmFajr, { b: Boolean -> viewModel.setAlarmFajr(b) }),
                        Triple("Terbit (Sunrise/Syuruq)", isAlarmSunrise, { b: Boolean -> viewModel.setAlarmSunrise(b) }),
                        Triple("Dzuhur (Dhuhr)", isAlarmDhuhr, { b: Boolean -> viewModel.setAlarmDhuhr(b) }),
                        Triple("Ashar (Asr)", isAlarmAsr, { b: Boolean -> viewModel.setAlarmAsr(b) }),
                        Triple("Maghrib", isAlarmMaghrib, { b: Boolean -> viewModel.setAlarmMaghrib(b) }),
                        Triple("Isya (Isha)", isAlarmIsha, { b: Boolean -> viewModel.setAlarmIsha(b) })
                    )

                    prayersList.forEach { (name, stateVal, setter) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isAdhanEnabled) { setter(!stateVal) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (stateVal && isAdhanEnabled) Icons.Default.AlarmOn else Icons.Default.AlarmOff,
                                    contentDescription = null,
                                    tint = if (stateVal && isAdhanEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAdhanEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            Switch(
                                checked = stateVal,
                                onCheckedChange = { setter(it) },
                                enabled = isAdhanEnabled,
                                modifier = Modifier.testTag("switch_${name.replace(Regex("[^a-zA-Z0-9]"), "_").lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // Info card about permissions and alarms
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Alarm azan disesuaikan dengan koordinat lokasi Anda saat ini. Pastikan untuk memberikan akses lokasi pada menu 'Shalat' agar waktu shalat presisi.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuranKuSplashScreen(
    onDismiss: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation = true
        kotlinx.coroutines.delay(2000)
        onDismiss()
    }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearOutSlowInEasing
        ),
        label = "splash_fade"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.0f else 0.85f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "splash_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .testTag("splash_screen_root"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .graphicsLayer(
                    alpha = alphaAnim,
                    scaleX = scaleAnim,
                    scaleY = scaleAnim
                )
                .padding(24.dp)
        ) {
            // Elegant background circle decoration with an intricate Islamic mosque/dome vector feel
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Beautiful mosque symbol
                    Icon(
                        imageVector = Icons.Default.Mosque,
                        contentDescription = "Mosque Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title "Quran ku"
            Text(
                text = "Quran ku",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle
            Text(
                text = "Panduan Ibadah & Al-Qur'an Digital",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
        }

        // Footer at the bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .graphicsLayer(alpha = alphaAnim),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tenang • Berkualitas • Spiritual",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
