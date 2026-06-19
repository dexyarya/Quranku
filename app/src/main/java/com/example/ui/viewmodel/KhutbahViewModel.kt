package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.KhutbahRecord
import com.example.data.model.KhutbahWithMasjid
import com.example.data.model.Masjid
import com.example.data.model.JadwalPetugas
import com.example.data.model.JadwalWithMasjid
import com.example.data.model.SurahModel
import com.example.data.model.VerseModel
import com.example.data.model.SurahDetailModel
import com.example.data.model.QuranStaticData
import com.example.data.model.DoaModel
import com.example.data.model.DoaStaticData
import com.example.data.repository.KhutbahRepository
import com.example.data.repository.QuranApiClient
import com.example.data.local.OfflineSurahEntity
import com.example.data.model.QuranJsonParser
import com.example.data.repository.PrayerApiClient
import com.example.data.repository.AladhanResponse
import com.example.data.repository.AladhanData
import com.example.service.AdhanAlarmScheduler
import com.example.ui.ScaffoldMessengerState
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.HttpURLConnection
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Qari Model Representation
data class Qari(val id: String, val name: String, val folder: String)

@OptIn(ExperimentalCoroutinesApi::class)
class KhutbahViewModel(application: Application) : AndroidViewModel(application) {

    // Predefined famous Qari List
    val qariList = listOf(
        Qari("05", "Syaikh Misyari Rasyid Al-Afasi", "Misyari-Rasyid-Al-Afasi"),
        Qari("03", "Syaikh Abdurrahman As-Sudais", "Abdurrahman-As-Sudais"),
        Qari("01", "Syaikh Abdullah Al-Juhany", "Abdullah-Al-Juhany"),
        Qari("02", "Syaikh Abdul Muhsin Al-Qasim", "Abdul-Muhsin-Al-Qasim"),
        Qari("04", "Syaikh Ibrahim Al-Dossari", "Ibrahim-Al-Dossari")
    )

    private val repository: KhutbahRepository
    
    val allMasjids: StateFlow<List<Masjid>>
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val khutbahListState: StateFlow<List<KhutbahWithMasjid>>
    val jadwalListState: StateFlow<List<JadwalWithMasjid>>

    // Quran State Flow
    private val _quranSearchQuery = MutableStateFlow("")
    val quranSearchQuery = _quranSearchQuery.asStateFlow()
    val filteredSurahs: StateFlow<List<SurahModel>>

    private val _selectedSurahId = MutableStateFlow<Int?>(null)
    val selectedSurahId = _selectedSurahId.asStateFlow()

    private val _surahDetailState = MutableStateFlow<QuranUiState>(QuranUiState.Idle)
    val surahDetailState = _surahDetailState.asStateFlow()

    // Dedicated state for Yasin recitation
    private val _yasinDetailState = MutableStateFlow<QuranUiState>(QuranUiState.Idle)
    val yasinDetailState = _yasinDetailState.asStateFlow()

    // Doa State Flow
    private val _doaSearchQuery = MutableStateFlow("")
    val doaSearchQuery = _doaSearchQuery.asStateFlow()

    private val _selectedDoaCategory = MutableStateFlow("Semua")
    val selectedDoaCategory = _selectedDoaCategory.asStateFlow()

    private val _favoriteDoaIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteDoaIds = _favoriteDoaIds.asStateFlow()

    val filteredDoas: StateFlow<List<DoaModel>>

    // Media Player State Flow
    private var mediaPlayer: MediaPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPlayingSurah = MutableStateFlow<SurahModel?>(null)
    val currentPlayingSurah = _currentPlayingSurah.asStateFlow()

    private val _currentPlayingVerse = MutableStateFlow<Int?>(null) // null if full surah or nothing
    val currentPlayingVerse = _currentPlayingVerse.asStateFlow()

    private val _audioLoading = MutableStateFlow(false)
    val audioLoading = _audioLoading.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress = _playbackProgress.asStateFlow()

    private val _playbackPositionText = MutableStateFlow("00:00 / 00:00")
    val playbackPositionText = _playbackPositionText.asStateFlow()

    private var progressJob: Job? = null
    private val sharedPrefs = application.getSharedPreferences("quran_doa_prefs", Context.MODE_PRIVATE)

    // Qari / Reciter Active Selection
    private val _selectedReciterKey = MutableStateFlow(sharedPrefs.getString("selected_reciter", "05") ?: "05")
    val selectedReciterKey = _selectedReciterKey.asStateFlow()

    val selectedReciter = _selectedReciterKey.map { key ->
        qariList.find { it.id == key } ?: qariList.first()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), qariList.first())

    // Last Read Bookmark State
    private val _lastReadSurahId = MutableStateFlow(sharedPrefs.getInt("last_read_surah_id", 0))
    val lastReadSurahId = _lastReadSurahId.asStateFlow()

    private val _lastReadSurahName = MutableStateFlow(sharedPrefs.getString("last_read_surah_name", "") ?: "")
    val lastReadSurahName = _lastReadSurahName.asStateFlow()

    private val _lastReadVerseNo = MutableStateFlow(sharedPrefs.getInt("last_read_verse_no", 0))
    val lastReadVerseNo = _lastReadVerseNo.asStateFlow()

    // Quran Reminder & Consistency States
    private val _isQuranReminderEnabled = MutableStateFlow(sharedPrefs.getBoolean("quran_reminder_enabled", false))
    val isQuranReminderEnabled = _isQuranReminderEnabled.asStateFlow()

    // Sub-tab tracker for tab 2 (Doa, Panduan, Tasbih, Tahlil & Yasin)
    private val _activeDoaSubTab = MutableStateFlow(0)
    val activeDoaSubTab = _activeDoaSubTab.asStateFlow()

    fun setActiveDoaSubTab(tabIndex: Int) {
        _activeDoaSubTab.value = tabIndex
    }

    private val _quranReminderHour = MutableStateFlow(sharedPrefs.getInt("quran_reminder_hour", 18))
    val quranReminderHour = _quranReminderHour.asStateFlow()

    private val _quranReminderMinute = MutableStateFlow(sharedPrefs.getInt("quran_reminder_minute", 0))
    val quranReminderMinute = _quranReminderMinute.asStateFlow()

    private val _quranReminderTargetDays = MutableStateFlow(sharedPrefs.getInt("quran_reminder_target_days", 7))
    val quranReminderTargetDays = _quranReminderTargetDays.asStateFlow()

    private val _quranReadDates = MutableStateFlow(sharedPrefs.getString("quran_read_dates", "") ?: "")
    val quranReadDates = _quranReadDates.asStateFlow()

    val quranReadStreak = _quranReadDates.map { calculateStreakValue(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isQuranReadToday = _quranReadDates.map { isReadTodayValue(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Download/Offline State
    private val _downloadedSurahIds = MutableStateFlow<Set<Int>>(emptySet())
    val downloadedSurahIds = _downloadedSurahIds.asStateFlow()

    private val _downloadingSurahId = MutableStateFlow<Int?>(null)
    val downloadingSurahId = _downloadingSurahId.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress = _downloadProgress.asStateFlow()

    // Quran Auto-Sync (Gradual offline database caching) States
    private val _autoSyncProgress = MutableStateFlow(0f)
    val autoSyncProgress = _autoSyncProgress.asStateFlow()

    private val _autoSyncStatusText = MutableStateFlow("Menyiapkan sinkronisasi...")
    val autoSyncStatusText = _autoSyncStatusText.asStateFlow()

    private val _isAutoSyncRunning = MutableStateFlow(false)
    val isAutoSyncRunning = _isAutoSyncRunning.asStateFlow()

    private val _autoSyncSuccessCount = MutableStateFlow(0)
    val autoSyncSuccessCount = _autoSyncSuccessCount.asStateFlow()

    // Prayer Times (Jadwal Shalat) States
    private val _userLatitude = MutableStateFlow(-6.2088)
    val userLatitude = _userLatitude.asStateFlow()

    private val _userLongitude = MutableStateFlow(106.8456)
    val userLongitude = _userLongitude.asStateFlow()

    private val _userLocationName = MutableStateFlow("Jakarta")
    val userLocationName = _userLocationName.asStateFlow()

    private val _prayerTimesState = MutableStateFlow<PrayerTimesUiState>(PrayerTimesUiState.Loading)
    val prayerTimesState = _prayerTimesState.asStateFlow()

    // Preferences & Shared Settings (Light/Dark Mode and Adhan Settings)
    private val prefs = application.getSharedPreferences("khutbah_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", true))
    val isDarkTheme = _isDarkTheme.asStateFlow()

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        prefs.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    private val _isAdhanEnabled = MutableStateFlow(prefs.getBoolean("is_adhan_enabled", true))
    val isAdhanEnabled = _isAdhanEnabled.asStateFlow()

    private val _isAlarmFajr = MutableStateFlow(prefs.getBoolean("alarm_fajr", true))
    val isAlarmFajr = _isAlarmFajr.asStateFlow()

    private val _isAlarmSunrise = MutableStateFlow(prefs.getBoolean("alarm_sunrise", true))
    val isAlarmSunrise = _isAlarmSunrise.asStateFlow()

    private val _isAlarmDhuhr = MutableStateFlow(prefs.getBoolean("alarm_dhuhr", true))
    val isAlarmDhuhr = _isAlarmDhuhr.asStateFlow()

    private val _isAlarmAsr = MutableStateFlow(prefs.getBoolean("alarm_asr", true))
    val isAlarmAsr = _isAlarmAsr.asStateFlow()

    private val _isAlarmMaghrib = MutableStateFlow(prefs.getBoolean("alarm_maghrib", true))
    val isAlarmMaghrib = _isAlarmMaghrib.asStateFlow()

    private val _isAlarmIsha = MutableStateFlow(prefs.getBoolean("alarm_isha", true))
    val isAlarmIsha = _isAlarmIsha.asStateFlow()

    fun setAdhanEnabled(enabled: Boolean) {
        _isAdhanEnabled.value = enabled
        prefs.edit().putBoolean("is_adhan_enabled", enabled).apply()
        updateScheduledAlarms()
    }

    fun setAlarmFajr(enabled: Boolean) {
        _isAlarmFajr.value = enabled
        prefs.edit().putBoolean("alarm_fajr", enabled).apply()
        updateScheduledAlarms()
    }

    fun setAlarmSunrise(enabled: Boolean) {
        _isAlarmSunrise.value = enabled
        prefs.edit().putBoolean("alarm_sunrise", enabled).apply()
        updateScheduledAlarms()
    }

    fun setAlarmDhuhr(enabled: Boolean) {
        _isAlarmDhuhr.value = enabled
        prefs.edit().putBoolean("alarm_dhuhr", enabled).apply()
        updateScheduledAlarms()
    }

    fun setAlarmAsr(enabled: Boolean) {
        _isAlarmAsr.value = enabled
        prefs.edit().putBoolean("alarm_asr", enabled).apply()
        updateScheduledAlarms()
    }

    fun setAlarmMaghrib(enabled: Boolean) {
        _isAlarmMaghrib.value = enabled
        prefs.edit().putBoolean("alarm_maghrib", enabled).apply()
        updateScheduledAlarms()
    }

    fun setAlarmIsha(enabled: Boolean) {
        _isAlarmIsha.value = enabled
        prefs.edit().putBoolean("alarm_isha", enabled).apply()
        updateScheduledAlarms()
    }

    fun updateScheduledAlarms() {
        val state = _prayerTimesState.value
        if (state is PrayerTimesUiState.Success) {
            val app = getApplication<Application>()
            if (_isAdhanEnabled.value) {
                AdhanAlarmScheduler.scheduleAlarms(app, state.timings)
            } else {
                AdhanAlarmScheduler.cancelAlarms(app)
            }
        }
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = KhutbahRepository(database.masjidDao(), database.khutbahDao(), database.jadwalDao(), database.offlineSurahDao())
        
        refreshDownloadedSurahs()
        
        // Load last stored location to prevent reset to default
        val savedLat = prefs.getString("saved_lat", "-6.2088")?.toDoubleOrNull() ?: -6.2088
        val savedLng = prefs.getString("saved_lng", "106.8456")?.toDoubleOrNull() ?: 106.8456
        val savedName = prefs.getString("saved_location_name", "Jakarta") ?: "Jakarta"
        
        _userLatitude.value = savedLat
        _userLongitude.value = savedLng
        _userLocationName.value = savedName

        // Load local prayer times first (offline mode)
        val cached = getLocalPrayerTimes()
        if (cached != null) {
            _prayerTimesState.value = cached
            updateScheduledAlarms()
        } else {
            fetchPrayerTimes(savedLat, savedLng)
        }
        
        com.example.service.QuranReminderScheduler.scheduleReminder(application)

        allMasjids = repository.allMasjids.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Reactively search or list all sermons.
        khutbahListState = _searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.allKhutbahs
                } else {
                    repository.searchKhutbah(query)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Reactively search or list all Friday officer schedules.
        jadwalListState = _searchQuery
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.allJadwals
                } else {
                    repository.searchJadwal(query)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        // Setup filtered Quran surahs
        filteredSurahs = combine(_quranSearchQuery, MutableStateFlow(QuranStaticData.surahs)) { query, list ->
            if (query.isBlank()) {
                list
            } else {
                list.filter {
                    it.namaLatin.contains(query, ignoreCase = true) ||
                    it.arti.contains(query, ignoreCase = true) ||
                    it.tempatTurun.contains(query, ignoreCase = true) ||
                    it.nomor.toString() == query.trim()
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QuranStaticData.surahs
        )

        // Setup favorite Doa loading
        val favs = sharedPrefs.getStringSet("fav_doa_ids", emptySet()) ?: emptySet()
        _favoriteDoaIds.value = favs.mapNotNull { it.toIntOrNull() }.toSet()

        // Setup filtered Doas
        filteredDoas = combine(_doaSearchQuery, _selectedDoaCategory, _favoriteDoaIds, MutableStateFlow(DoaStaticData.prayers)) { query, category, favIds, list ->
            val listByCategory = when (category) {
                "Semua" -> list
                "Favorit" -> list.filter { favIds.contains(it.id) }
                else -> list.filter { it.category.equals(category, ignoreCase = true) }
            }

            if (query.isBlank()) {
                listByCategory
            } else {
                listByCategory.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.latin.contains(query, ignoreCase = true) ||
                    it.translation.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DoaStaticData.prayers
        )

        // Prepopulate if empty
        viewModelScope.launch {
            repository.allMasjids.collect { list ->
                if (list.isEmpty()) {
                    prepopulateData()
                }
            }
        }

        startQuranAutoSync()
    }

    private suspend fun prepopulateData() {
        // Prepopulate standard registered Indonesian mosques
        val istiqlalId = repository.insertMasjid(
            Masjid(
                nama = "Masjid Istiqlal",
                alamat = "Sawah Besar, Jakarta Pusat",
                alamatLengkap = "Jl. Taman Wijaya Kusuma, Ps. Baru, Sawah Besar, Jakarta Pusat",
                kontak = "Humas Istiqlal (021-3811708)"
            )
        ).toInt()

        val alAzharId = repository.insertMasjid(
            Masjid(
                nama = "Masjid Agung Al-Azhar",
                alamat = "Kebayoran Baru, Jakarta Selatan",
                alamatLengkap = "Jl. Sisingamangaraja No.1, Selong, Kebayoran Baru, Jakarta Selatan",
                kontak = "Sekretariat Al-Azhar (021-7278368)"
            )
        ).toInt()

        val bandungId = repository.insertMasjid(
            Masjid(
                nama = "Masjid Raya Bandung",
                alamat = "Alun-Alun Bandung",
                alamatLengkap = "Jl. Asia Afrika, Balonggede, Regol, Kota Bandung, Jawa Barat",
                kontak = "DKM Bandung (022-4207080)"
            )
        ).toInt()

        val baiturrahmanId = repository.insertMasjid(
            Masjid(
                nama = "Masjid Raya Baiturrahman",
                alamat = "Baiturrahman, Banda Aceh",
                alamatLengkap = "Jl. Moh. Jam No.1, Kampung Baru, Baiturrahman, Banda Aceh, Aceh",
                kontak = "DKM Baiturrahman"
            )
        ).toInt()

        // Insert initial template sermon records
        repository.insertKhutbah(
            KhutbahRecord(
                masjidId = istiqlalId,
                tanggal = "12-06-2026",
                khatib = "Prof. Dr. KH. Nasaruddin Umar, M.A.",
                judul = "Menjaga Harmoni Kemanusiaan dan Kedamaian Umat",
                summary = "Khutbah menekankan pentingnya persatuan nasional (Ukhuwah Wathaniyah) dan kerukunan beragama di Indonesia. Khatib mengingatkan bahwa perbedaan adalah rahmat, dan masjid harus menjadi pusat kedamaian serta pencerahan spiritual umat.",
                durasiMenit = 25
            )
        )

        repository.insertKhutbah(
            KhutbahRecord(
                masjidId = alAzharId,
                tanggal = "05-06-2026",
                khatib = "Ust. Dr. Adi Hidayat, Lc., M.A.",
                judul = "Adab dalam Berbeda Pendapat Berdasarkan Sunnah",
                summary = "Khutbah menyajikan tinjauan mendalam tentang adab ilmiah (ikhtilaf). Umat Islam diajak untuk tetap santun, membiasakan tabayyun (klarifikasi), menghindari caci maki di media sosial, serta meneladani toleransi para ulama salaf.",
                durasiMenit = 22
            )
        )

        repository.insertKhutbah(
            KhutbahRecord(
                masjidId = bandungId,
                tanggal = "15-05-2026",
                khatib = "KH. M. Cholil Nafis, Ph.D.",
                judul = "Membangun Ketahanan Ekonomi Keluarga Sakinah",
                summary = "Khatib membahas pilar-pilar penting ekonomi syariah skala mikro dalam rumah tangga. Menekankan keutamaan rezeki yang halal, menghindari riba dan sifat boros (tabdzir), serta pentingnya gemar berzakat, infak, dan sedekah.",
                durasiMenit = 18
            )
        )

        // Prepopulate baseline schedules for Friday officers
        repository.insertJadwal(
            JadwalPetugas(
                masjidId = istiqlalId,
                tanggal = "19-06-2026",
                khatib = "Prof. Dr. KH. Nasaruddin Umar, M.A.",
                imam = "Drs. H. Syarifuddin El-Anshary",
                muadzin = "H. Raden Harmoko",
                bilal = "H. Ahmad Syaifullah",
                keterangan = "Tema: Keutamaan Menuntut Ilmu di Era Penyesuaian Global"
            )
        )

        repository.insertJadwal(
            JadwalPetugas(
                masjidId = alAzharId,
                tanggal = "19-06-2026",
                khatib = "Ustadz H. Abdul Somad, Lc., D.E.S.A., Ph.D.",
                imam = "H. Mukhtar Luthfi, M.A.",
                muadzin = "Ustadz Ibrahim",
                bilal = "Ustadz Bilal",
                keterangan = "Tema: Akhlak Mulia Menurut Para Pendahulu Sholeh"
            )
        )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Mosque Actions
    fun addMasjid(nama: String, alamat: String, alamatLengkap: String, kontak: String) {
        viewModelScope.launch {
            repository.insertMasjid(
                Masjid(
                    nama = nama,
                    alamat = alamat,
                    alamatLengkap = alamatLengkap,
                    kontak = kontak
                )
            )
        }
    }

    fun updateMasjid(masjid: Masjid) {
        viewModelScope.launch {
            repository.updateMasjid(masjid)
        }
    }

    fun deleteMasjid(masjid: Masjid) {
        viewModelScope.launch {
            repository.deleteMasjid(masjid)
        }
    }

    // Khutbah Actions
    fun addKhutbah(masjidId: Int, tanggal: String, khatib: String, judul: String, summary: String, durasiMenit: Int) {
        viewModelScope.launch {
            repository.insertKhutbah(
                KhutbahRecord(
                    masjidId = masjidId,
                    tanggal = tanggal,
                    khatib = khatib,
                    judul = judul,
                    summary = summary,
                    durasiMenit = durasiMenit
                )
            )
        }
    }

    fun updateKhutbah(khutbah: KhutbahRecord) {
        viewModelScope.launch {
            repository.updateKhutbah(khutbah)
        }
    }

    fun deleteKhutbah(khutbahRecord: KhutbahWithMasjid) {
        viewModelScope.launch {
            // Find complete entity by ID to delete
            val fullRecord = KhutbahRecord(
                id = khutbahRecord.id,
                masjidId = khutbahRecord.masjidId,
                tanggal = khutbahRecord.tanggal,
                khatib = khutbahRecord.khatib,
                judul = khutbahRecord.judul,
                summary = khutbahRecord.summary,
                durasiMenit = khutbahRecord.durasiMenit,
                createdAt = khutbahRecord.createdAt
            )
            repository.deleteKhutbah(fullRecord)
        }
    }

    // Jadwal Petugas Actions
    fun addJadwal(masjidId: Int, tanggal: String, khatib: String, imam: String, muadzin: String, bilal: String, keterangan: String) {
        viewModelScope.launch {
            repository.insertJadwal(
                JadwalPetugas(
                    masjidId = masjidId,
                    tanggal = tanggal,
                    khatib = khatib,
                    imam = imam,
                    muadzin = muadzin,
                    bilal = bilal,
                    keterangan = keterangan
                )
            )
        }
    }

    fun updateJadwal(jadwal: JadwalPetugas) {
        viewModelScope.launch {
            repository.updateJadwal(jadwal)
        }
    }

    fun deleteJadwal(jadwalWithMasjid: JadwalWithMasjid) {
        viewModelScope.launch {
            val fullRecord = JadwalPetugas(
                id = jadwalWithMasjid.id,
                masjidId = jadwalWithMasjid.masjidId,
                tanggal = jadwalWithMasjid.tanggal,
                khatib = jadwalWithMasjid.khatib,
                imam = jadwalWithMasjid.imam,
                muadzin = jadwalWithMasjid.muadzin,
                bilal = jadwalWithMasjid.bilal,
                keterangan = jadwalWithMasjid.keterangan,
                createdAt = jadwalWithMasjid.createdAt
            )
            repository.deleteJadwal(fullRecord)
        }
    }

    // Al-Qur'an & Doa Actions
    fun updateQuranSearchQuery(query: String) {
        _quranSearchQuery.value = query
    }

    fun loadYasinDetail() {
        viewModelScope.launch {
            _yasinDetailState.value = QuranUiState.Loading
            val cached = repository.getOfflineSurah(36)
            if (cached != null) {
                val parsedAyat = QuranJsonParser.fromJson(cached.ayatJson)
                if (parsedAyat != null) {
                    val detail = SurahDetailModel(
                        nomor = cached.nomor,
                        nama = cached.nama,
                        namaLatin = cached.namaLatin,
                        jumlahAyat = cached.jumlahAyat,
                        tempatTurun = cached.tempatTurun,
                        arti = cached.arti,
                        deskripsi = cached.deskripsi,
                        ayat = parsedAyat
                    )
                    _yasinDetailState.value = QuranUiState.Success(detail)
                    return@launch
                }
            }
            // Try loading from api
            try {
                val response = QuranApiClient.apiService.getSurahDetail(36)
                if (response.code == 200 && response.data != null) {
                    val detail = response.data
                    _yasinDetailState.value = QuranUiState.Success(detail)
                    // Auto Cache
                    launch(Dispatchers.IO) {
                        try {
                            val entity = com.example.data.local.OfflineSurahEntity(
                                nomor = detail.nomor,
                                nama = detail.nama,
                                namaLatin = detail.namaLatin,
                                jumlahAyat = detail.jumlahAyat,
                                tempatTurun = detail.tempatTurun,
                                arti = detail.arti,
                                deskripsi = detail.deskripsi,
                                ayatJson = QuranJsonParser.toJson(detail.ayat),
                                isDownloaded = false
                            )
                            repository.insertOfflineSurah(entity)
                        } catch (e: Exception) {}
                    }
                } else {
                    _yasinDetailState.value = QuranUiState.Error("Gagal memuat Surat Yasin. Harap hubungkan internet.")
                }
            } catch (e: Exception) {
                _yasinDetailState.value = QuranUiState.Error("Koneksi internet diperlukan untuk mengunduh Surat Yasin pertama kali.")
            }
        }
    }

    fun selectSurah(surahId: Int?) {
        _selectedSurahId.value = surahId
        if (surahId == null) {
            _surahDetailState.value = QuranUiState.Idle
        } else {
            fetchSurahDetail(surahId)
        }
    }

    private fun fetchSurahDetail(surahId: Int) {
        viewModelScope.launch {
            _surahDetailState.value = QuranUiState.Loading
            
            // 1. Coba memuat dari Room SQLite Cache lokal terlebih dahulu (Mode Offline)
            val cached = repository.getOfflineSurah(surahId)
            if (cached != null) {
                val parsedAyat = QuranJsonParser.fromJson(cached.ayatJson)
                if (parsedAyat != null) {
                    val detail = SurahDetailModel(
                        nomor = cached.nomor,
                        nama = cached.nama,
                        namaLatin = cached.namaLatin,
                        jumlahAyat = cached.jumlahAyat,
                        tempatTurun = cached.tempatTurun,
                        arti = cached.arti,
                        deskripsi = cached.deskripsi,
                        ayat = parsedAyat
                    )
                    _surahDetailState.value = QuranUiState.Success(detail)
                    return@launch
                }
            }

            // 2. Jika tidak ada cache, ambil dari Server API resmi
            try {
                val response = QuranApiClient.apiService.getSurahDetail(surahId)
                if (response.code == 200 && response.data != null) {
                    val detail = response.data
                    _surahDetailState.value = QuranUiState.Success(detail)

                    // Auto Cache in background SQLite whenever loaded online so it works offline subsequently!
                    launch(Dispatchers.IO) {
                        try {
                            val entity = com.example.data.local.OfflineSurahEntity(
                                nomor = detail.nomor,
                                nama = detail.nama,
                                namaLatin = detail.namaLatin,
                                jumlahAyat = detail.jumlahAyat,
                                tempatTurun = detail.tempatTurun,
                                arti = detail.arti,
                                deskripsi = detail.deskripsi,
                                ayatJson = QuranJsonParser.toJson(detail.ayat),
                                isDownloaded = false // Cached text only
                            )
                            repository.insertOfflineSurah(entity)
                        } catch (e: Exception) {
                            // ignore auto cache error
                        }
                    }
                } else {
                    _surahDetailState.value = QuranUiState.Error("Gagal memuat surat: Server mengembalikan kode ${response.code}")
                }
            } catch (e: Exception) {
                _surahDetailState.value = QuranUiState.Error("Koneksi internet terputus dan Surat ini belum diunduh untuk dapat dibaca secara offline.")
            }
        }
    }

    // Helper folder & pencarian audio offline
    fun getOfflineAudioFile(surahNumber: Int, reciterId: String): File {
        val dir = File(getApplication<Application>().filesDir, "murottal/$reciterId")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, String.format("%03d.mp3", surahNumber))
    }

    fun isAudioDownloaded(surahNumber: Int, reciterId: String): Boolean {
        return getOfflineAudioFile(surahNumber, reciterId).exists()
    }

    fun refreshDownloadedSurahs() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getAllOfflineSurahs()
            _downloadedSurahIds.value = list.filter { it.isDownloaded }.map { it.nomor }.toSet()
        }
    }

    fun selectReciter(key: String) {
        _selectedReciterKey.value = key
        sharedPrefs.edit().putString("selected_reciter", key).apply()
    }

    fun updateLastRead(surahId: Int, surahName: String, verseNo: Int) {
        _lastReadSurahId.value = surahId
        _lastReadSurahName.value = surahName
        _lastReadVerseNo.value = verseNo
        sharedPrefs.edit().apply {
            putInt("last_read_surah_id", surahId)
            putString("last_read_surah_name", surahName)
            putInt("last_read_verse_no", verseNo)
        }.apply()
        // Record read today to maintain streak!
        recordQuranReadToday()
    }

    fun recordQuranReadToday() {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(java.util.Date())
        
        val currentStr = _quranReadDates.value
        val dateSet = currentStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
        if (!dateSet.contains(todayStr)) {
            dateSet.add(todayStr)
            val newStr = dateSet.joinToString(",")
            _quranReadDates.value = newStr
            sharedPrefs.edit().putString("quran_read_dates", newStr).apply()
        }
    }

    fun setQuranReminderEnabled(enabled: Boolean) {
        _isQuranReminderEnabled.value = enabled
        sharedPrefs.edit().putBoolean("quran_reminder_enabled", enabled).apply()
        importComServiceQuranReminder()
    }

    fun setQuranReminderTime(hour: Int, minute: Int) {
        _quranReminderHour.value = hour
        _quranReminderMinute.value = minute
        sharedPrefs.edit().putInt("quran_reminder_hour", hour).putInt("quran_reminder_minute", minute).apply()
        importComServiceQuranReminder()
    }

    fun setQuranReminderTargetDays(days: Int) {
        _quranReminderTargetDays.value = days
        sharedPrefs.edit().putInt("quran_reminder_target_days", days).apply()
    }

    private fun importComServiceQuranReminder() {
        com.example.service.QuranReminderScheduler.scheduleReminder(getApplication())
    }

    private fun calculateStreakValue(rawDates: String): Int {
        if (rawDates.isEmpty()) return 0
        try {
            val dateList = rawDates.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            if (dateList.isEmpty()) return 0

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayStr = sdf.format(java.util.Date())
            
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(cal.time)

            // If neither today nor yesterday is in the set, streak is 0
            if (!dateList.contains(todayStr) && !dateList.contains(yesterdayStr)) {
                return 0
            }

            var streak = 0
            val checkCal = java.util.Calendar.getInstance()
            if (dateList.contains(todayStr)) {
                while (true) {
                    val dateToCheck = sdf.format(checkCal.time)
                    if (dateList.contains(dateToCheck)) {
                        streak++
                        checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                    } else {
                        break
                    }
                }
            } else {
                checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                while (true) {
                    val dateToCheck = sdf.format(checkCal.time)
                    if (dateList.contains(dateToCheck)) {
                        streak++
                        checkCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                    } else {
                        break
                    }
                }
            }
            return streak
        } catch (e: Exception) {
            return 0
        }
    }

    private fun isReadTodayValue(rawDates: String): Boolean {
        if (rawDates.isEmpty()) return false
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(java.util.Date())
        return rawDates.split(",").map { it.trim() }.toSet().contains(todayStr)
    }

    // Aksi Download Murottal dan Text Surah agar bisa diakses Full Offline
    fun downloadSurah(surah: SurahModel) {
        viewModelScope.launch(Dispatchers.IO) {
            _downloadingSurahId.value = surah.nomor
            _downloadProgress.value = 0.05f
            
            val activeQari = selectedReciter.value
            var detail: SurahDetailModel? = null
            
            // Periksa cache teks di DB terlebih dahulu
            val cached = repository.getOfflineSurah(surah.nomor)
            if (cached != null) {
                val parsed = QuranJsonParser.fromJson(cached.ayatJson)
                if (parsed != null) {
                    detail = SurahDetailModel(
                        nomor = cached.nomor,
                        nama = cached.nama,
                        namaLatin = cached.namaLatin,
                        jumlahAyat = cached.jumlahAyat,
                        tempatTurun = cached.tempatTurun,
                        arti = cached.arti,
                        deskripsi = cached.deskripsi,
                        ayat = parsed
                    )
                }
            }
            
            // Ambil dari server jika teks detail belum di DB
            if (detail == null) {
                try {
                    val response = QuranApiClient.apiService.getSurahDetail(surah.nomor)
                    if (response.code == 200 && response.data != null) {
                        detail = response.data
                        val entity = OfflineSurahEntity(
                            nomor = detail.nomor,
                            nama = detail.nama,
                            namaLatin = detail.namaLatin,
                            jumlahAyat = detail.jumlahAyat,
                            tempatTurun = detail.tempatTurun,
                            arti = detail.arti,
                            deskripsi = detail.deskripsi,
                            ayatJson = QuranJsonParser.toJson(detail.ayat),
                            isDownloaded = true
                        )
                        repository.insertOfflineSurah(entity)
                    }
                } catch (e: Exception) {
                    // silent fail
                }
            }
            
            _downloadProgress.value = 0.2f
            
            // Lakukan pengunduhan audio surah (.mp3)
            val audioUrlString = "https://equran.nos.wjv-1.neo.id/audio-full/${activeQari.folder}/${String.format("%03d", surah.nomor)}.mp3"
            val destFile = getOfflineAudioFile(surah.nomor, activeQari.id)
            
            try {
                val url = URL(audioUrlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 12000
                connection.readTimeout = 12000
                connection.connect()
                
                if (connection.responseCode == 200) {
                    val fileLength = connection.contentLength
                    var totalDownloadedBytes: Long = 0
                    
                    val input = connection.inputStream
                    val output = FileOutputStream(destFile)
                    
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        totalDownloadedBytes += bytesRead
                        if (fileLength > 0) {
                            _downloadProgress.value = 0.2f + (totalDownloadedBytes.toFloat() / fileLength.toFloat()) * 0.8f
                        }
                        output.write(buffer, 0, bytesRead)
                    }
                    
                    output.flush()
                    output.close()
                    input.close()
                    
                    // Pastikan Surah tercatat di SQLite meskipun download audio saja
                    if (detail != null) {
                        val entity = OfflineSurahEntity(
                            nomor = detail.nomor,
                            nama = detail.nama,
                            namaLatin = detail.namaLatin,
                            jumlahAyat = detail.jumlahAyat,
                            tempatTurun = detail.tempatTurun,
                            arti = detail.arti,
                            deskripsi = detail.deskripsi,
                            ayatJson = QuranJsonParser.toJson(detail.ayat),
                            isDownloaded = true
                        )
                        repository.insertOfflineSurah(entity)
                    }
                    
                    refreshDownloadedSurahs()
                }
            } catch (e: Exception) {
                if (destFile.exists()) {
                    destFile.delete()
                }
            } finally {
                _downloadingSurahId.value = null
                _downloadProgress.value = 0f
            }
        }
    }

    // Menghapus data unduhan surah
    fun deleteDownloadedSurah(surahId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteOfflineSurah(surahId)
            // Hapus semua file audio terkait surah tersebut
            qariList.forEach { qari ->
                val audioFile = getOfflineAudioFile(surahId, qari.id)
                if (audioFile.exists()) {
                    audioFile.delete()
                }
            }
            refreshDownloadedSurahs()
        }
    }

    // Media Player Actions
    fun playMurottal(surah: SurahModel) {
        viewModelScope.launch {
            val currentlyPlaying = _currentPlayingSurah.value
            if (currentlyPlaying?.nomor == surah.nomor) {
                // Toggle play/pause if it is the exact same surah!
                togglePlayPause()
                return@launch
            }

            val qari = selectedReciter.value
            val localAudio = getOfflineAudioFile(surah.nomor, qari.id)
            if (localAudio.exists()) {
                playAudioFromUrl(localAudio.absolutePath, surah)
            } else {
                val url = "https://equran.nos.wjv-1.neo.id/audio-full/${qari.folder}/${String.format("%03d", surah.nomor)}.mp3"
                playAudioFromUrl(url, surah)
            }
        }
    }

    fun playVerseAudio(surah: SurahModel, verse: VerseModel) {
        val qariId = selectedReciter.value.id
        val targetUrl = verse.audio?.get(qariId) ?: verse.audio?.values?.firstOrNull()
        if (targetUrl != null) {
            playAudioFromUrl(targetUrl, surah, verse.nomorAyat)
        }
    }

    private fun playAudioFromUrl(url: String, surah: SurahModel, verseNum: Int? = null) {
        _audioLoading.value = true
        _currentPlayingSurah.value = surah // Set immediately for instant UI feedback!
        _currentPlayingVerse.value = verseNum
        progressJob?.cancel()
        
        try {
            mediaPlayer?.let {
                try {
                    if (it.isPlaying) {
                        it.stop()
                    }
                } catch (e: Exception) {}
                try {
                    it.release()
                } catch (e: Exception) {}
            }
            
            mediaPlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_MUSIC)
                
                val file = java.io.File(url)
                if (file.exists() && file.isFile) {
                    java.io.FileInputStream(file).use { fis ->
                        setDataSource(fis.fd)
                    }
                } else {
                    setDataSource(url)
                }
                
                setOnPreparedListener { mp ->
                    _audioLoading.value = false
                    _isPlaying.value = true
                    mp.start()
                    startProgressTracker()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPlayingVerse.value = null
                    _currentPlayingSurah.value = null
                    _playbackProgress.value = 0f
                    progressJob?.cancel()
                }
                setOnErrorListener { _, what, extra ->
                    android.util.Log.e("MurottalPlayer", "MediaPlayer Error: what=$what, extra=$extra")
                    _audioLoading.value = false
                    _isPlaying.value = false
                    _currentPlayingVerse.value = null
                    _currentPlayingSurah.value = null
                    progressJob?.cancel()
                    false
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            android.util.Log.e("MurottalPlayer", "MediaPlayer Exception", e)
            _audioLoading.value = false
            _isPlaying.value = false
            _currentPlayingSurah.value = null
            _currentPlayingVerse.value = null
        }
    }
    fun updateDoaSearchQuery(query: String) {
        _doaSearchQuery.value = query
    }

    fun updateDoaCategory(category: String) {
        _selectedDoaCategory.value = category
    }

    fun toggleFavoriteDoa(doaId: Int) {
        val current = _favoriteDoaIds.value.toMutableSet()
        if (current.contains(doaId)) {
            current.remove(doaId)
        } else {
            current.add(doaId)
        }
        _favoriteDoaIds.value = current
        sharedPrefs.edit().putStringSet("fav_doa_ids", current.map { it.toString() }.toSet()).apply()
    }

    fun togglePlayPause() {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.pause()
                    _isPlaying.value = false
                } else {
                    mp.start()
                    _isPlaying.value = true
                    startProgressTracker()
                }
            } catch (e: Exception) {
                // Ignore player errors
            }
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.reset()
                _isPlaying.value = false
                _currentPlayingSurah.value = null
                _currentPlayingVerse.value = null
                _playbackProgress.value = 0f
                _playbackPositionText.value = "00:00 / 00:00"
                progressJob?.cancel()
            }
        } catch (e: Exception) {
            // Ignore player errors
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (_isPlaying.value) {
                mediaPlayer?.let { mp ->
                    try {
                        if (mp.isPlaying) {
                            val duration = mp.duration.toFloat()
                            val position = mp.currentPosition.toFloat()
                            if (duration > 0) {
                                _playbackProgress.value = position / duration
                                
                                val posMinutes = (mp.currentPosition / 1000) / 60
                                val posSeconds = (mp.currentPosition / 1000) % 60
                                val durMinutes = (mp.duration / 1000) / 60
                                val durSeconds = (mp.duration / 1000) % 60
                                
                                _playbackPositionText.value = String.format(
                                    "%02d:%02d / %02d:%02d",
                                    posMinutes, posSeconds, durMinutes, durSeconds
                                )
                            }
                        }
                    } catch (e: Exception) {
                        // ignore if mediaplayer is in invalid state
                    }
                }
                delay(1000)
            }
        }
    }

    fun selectManualCity(name: String, lat: Double, lng: Double) {
        _userLocationName.value = name
        _userLatitude.value = lat
        _userLongitude.value = lng
        
        prefs.edit()
            .putString("saved_lat", lat.toString())
            .putString("saved_lng", lng.toString())
            .putString("saved_location_name", name)
            .apply()
            
        fetchPrayerTimes(lat, lng)
    }

    fun savePrayerTimesToLocal(timings: Map<String, String>, hijriDate: String, readableDate: String) {
        val editor = prefs.edit()
        editor.putString("prayer_hijri_date", hijriDate)
        editor.putString("prayer_readable_date", readableDate)
        // Clear previous keys with prefix
        prefs.all.keys.filter { it.startsWith("prayer_time_") }.forEach { editor.remove(it) }
        // Save new values
        for ((key, value) in timings) {
            editor.putString("prayer_time_$key", value)
        }
        editor.apply()
    }

    fun getLocalPrayerTimes(): PrayerTimesUiState.Success? {
        val hijriDate = prefs.getString("prayer_hijri_date", null)
        val readableDate = prefs.getString("prayer_readable_date", null)
        if (hijriDate == null || readableDate == null) return null
        
        val timings = mutableMapOf<String, String>()
        prefs.all.forEach { (key, value) ->
            if (key.startsWith("prayer_time_") && value is String) {
                val timingName = key.removePrefix("prayer_time_")
                timings[timingName] = value
            }
        }
        
        if (timings.isEmpty()) return null
        return PrayerTimesUiState.Success(timings, hijriDate, readableDate, isOffline = true)
    }

    fun fetchPrayerTimes(lat: Double, lng: Double) {
        _prayerTimesState.value = PrayerTimesUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val epochSeconds = System.currentTimeMillis() / 1000
                val response = PrayerApiClient.apiService.getTimings(
                    timestamp = epochSeconds,
                    latitude = lat,
                    longitude = lng,
                    method = 11 // Ministry of Religious Affairs Indonesia (Kemenag)
                )
                if (response.code == 200 && response.data != null) {
                    val timings = response.data.timings
                    val hijri = response.data.date.hijri
                    val readable = response.data.date.readable
                    
                    val hijriString = if (hijri != null) {
                        "${hijri.day} ${hijri.month?.en ?: ""} ${hijri.year} H"
                    } else {
                        ""
                    }
                    
                    _prayerTimesState.value = PrayerTimesUiState.Success(
                        timings = timings,
                        hijriDate = hijriString,
                        readableDate = readable,
                        isOffline = false
                    )
                    
                    savePrayerTimesToLocal(timings, hijriString, readable)
                    updateScheduledAlarms()
                } else {
                    val cached = getLocalPrayerTimes()
                    if (cached != null) {
                        _prayerTimesState.value = cached
                    } else {
                        _prayerTimesState.value = PrayerTimesUiState.Error("Gagal memuat jadwal shalat: Kode ${response.code}")
                    }
                }
            } catch (e: Exception) {
                val cached = getLocalPrayerTimes()
                if (cached != null) {
                    _prayerTimesState.value = cached
                } else {
                    _prayerTimesState.value = PrayerTimesUiState.Error("Gagal menghubungkan ke server jadwal shalat: ${e.localizedMessage}")
                }
            }
        }
    }

    fun detectLocationAndFetchPrayerTimes(context: Context) {
        _prayerTimesState.value = PrayerTimesUiState.Loading
        viewModelScope.launch {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                val hasCoarse = context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasFine = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                if (hasCoarse || hasFine) {
                    // Try Network Provider first (faster and works better indoors), then GPS
                    var location = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                    if (location == null) {
                        location = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                    }
                        
                    if (location != null) {
                        val currentLat = location.latitude
                        val currentLng = location.longitude
                        
                        // Check if location changed significantly (e.g. > 0.015 degree, ~1.5km)
                        val distLat = Math.abs(currentLat - _userLatitude.value)
                        val distLng = Math.abs(currentLng - _userLongitude.value)
                        
                        var city = "Lokasi Deteksi GPS"
                        try {
                            val geocoder = android.location.Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(currentLat, currentLng, 1)
                            if (!addresses.isNullOrEmpty()) {
                                city = addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea ?: "Lokasi GPS"
                            }
                        } catch (e: Exception) {
                            city = String.format(Locale.getDefault(), "Lat: %.3f, Lng: %.3f", currentLat, currentLng)
                        }
                        
                        // Only fetch if location actually changed significantly or if we lack success state
                        val isLocationChanged = distLat > 0.015 || distLng > 0.015 || _userLocationName.value != city
                        
                        if (isLocationChanged || _prayerTimesState.value !is PrayerTimesUiState.Success) {
                            _userLatitude.value = currentLat
                            _userLongitude.value = currentLng
                            _userLocationName.value = city
                            
                            // Save to prefs
                            prefs.edit()
                                .putString("saved_lat", currentLat.toString())
                                .putString("saved_lng", currentLng.toString())
                                .putString("saved_location_name", city)
                                .apply()
                                
                            fetchPrayerTimes(currentLat, currentLng)
                        } else {
                            // Location is virtually the same, keep cached state
                            val cached = getLocalPrayerTimes()
                            if (cached != null) {
                                _prayerTimesState.value = cached
                            } else {
                                fetchPrayerTimes(currentLat, currentLng)
                            }
                            ScaffoldMessengerState.showToast(context, "Lokasi tidak berubah: $city")
                        }
                    } else {
                        // fallback to cached/default
                        val cached = getLocalPrayerTimes()
                        if (cached != null) {
                            _prayerTimesState.value = cached
                            ScaffoldMessengerState.showToast(context, "Gagal melacak GPS, menggunakan data cache setempat.")
                        } else {
                            _userLocationName.value = "GPS Aktif (Default Jakarta)"
                            fetchPrayerTimes(_userLatitude.value, _userLongitude.value)
                        }
                    }
                } else {
                    val cached = getLocalPrayerTimes()
                    if (cached != null) {
                        _prayerTimesState.value = cached
                        ScaffoldMessengerState.showToast(context, "Izin lokasi ditolak, menggunakan data cache setempat.")
                    } else {
                        _userLocationName.value = "Jakarta (Akses Lokasi Ditolak)"
                        fetchPrayerTimes(_userLatitude.value, _userLongitude.value)
                    }
                }
            } catch (e: Exception) {
                val cached = getLocalPrayerTimes()
                if (cached != null) {
                    _prayerTimesState.value = cached
                } else {
                    _userLocationName.value = "Jakarta"
                    fetchPrayerTimes(_userLatitude.value, _userLongitude.value)
                }
            }
        }
    }

    fun startQuranAutoSync() {
        if (_isAutoSyncRunning.value) return
        
        viewModelScope.launch(Dispatchers.IO) {
            _isAutoSyncRunning.value = true
            _autoSyncStatusText.value = "Memeriksa status Al-Qur'an offline..."
            
            val totalSurahs = 114
            var cachedCount = 0
            
            // Fast scan first to determine current sync progress
            val allCached = repository.getAllOfflineSurahs()
            val cachedMap = allCached.associateBy { it.nomor }
            
            cachedCount = allCached.size
            _autoSyncSuccessCount.value = cachedCount
            _autoSyncProgress.value = cachedCount.toFloat() / totalSurahs.toFloat()
            
            _autoSyncStatusText.value = "Tersambung: $cachedCount / $totalSurahs Surat siap offline"
            
            // Short delay then begin gradual syncing of missing surahs
            delay(1200)

            for (i in 1..totalSurahs) {
                val cached = cachedMap[i]
                if (cached != null) {
                    continue
                }
                
                // Wait for network to be available if offline
                var offlineChecks = 0
                while (!isNetworkAvailable()) {
                    _autoSyncStatusText.value = "Sinkronisasi terjeda: Menunggu internet..."
                    delay(4000)
                    offlineChecks++
                    if (offlineChecks > 30) { // If offline for more than 2 minutes, pause running sync gently
                        _isAutoSyncRunning.value = false
                        _autoSyncStatusText.value = "Sinkronisasi ditunda (Offline)"
                        return@launch
                    }
                }
                
                try {
                    val surahLabel = QuranStaticData.surahs.getOrNull(i - 1)?.namaLatin ?: "Surat $i"
                    _autoSyncStatusText.value = "Mengunduh latar QS. $surahLabel ($i/114)..."
                    
                    val response = QuranApiClient.apiService.getSurahDetail(i)
                    if (response.code == 200 && response.data != null) {
                        val detail = response.data
                        val entity = com.example.data.local.OfflineSurahEntity(
                            nomor = detail.nomor,
                            nama = detail.nama,
                            namaLatin = detail.namaLatin,
                            jumlahAyat = detail.jumlahAyat,
                            tempatTurun = detail.tempatTurun,
                            arti = detail.arti,
                            deskripsi = detail.deskripsi,
                            ayatJson = QuranJsonParser.toJson(detail.ayat),
                            isDownloaded = false // Cached text only
                        )
                        repository.insertOfflineSurah(entity)
                        cachedCount++
                        _autoSyncSuccessCount.value = cachedCount
                        _autoSyncProgress.value = cachedCount.toFloat() / totalSurahs.toFloat()
                        
                        // Automatically update current reading screen if looking at it
                        val currentSelected = _selectedSurahId.value
                        if (currentSelected == i) {
                            _surahDetailState.value = QuranUiState.Success(detail)
                        }
                    }
                    // Gentle background download rate pacing (1200 ms)
                    delay(1200)
                } catch (e: java.lang.Exception) {
                    delay(4000) // retry buffer on failure
                }
            }
            
            _isAutoSyncRunning.value = false
            _autoSyncStatusText.value = "Semua $totalSurahs Surat Al-Qur'an gratis offline siap dibaca!"
            _autoSyncProgress.value = 1f
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            if (connectivityManager != null) {
                val activeNetwork = connectivityManager.activeNetwork ?: return false
                val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
                capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
            } else {
                false
            }
        } catch (e: Exception) {
            true // Default to true if exception occurs
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

sealed class PrayerTimesUiState {
    object Loading : PrayerTimesUiState()
    data class Success(
        val timings: Map<String, String>,
        val hijriDate: String,
        val readableDate: String,
        val isOffline: Boolean = false
    ) : PrayerTimesUiState()
    data class Error(val message: String) : PrayerTimesUiState()
}

sealed class QuranUiState {
    object Idle : QuranUiState()
    object Loading : QuranUiState()
    data class Success(val surahDetail: SurahDetailModel) : QuranUiState()
    data class Error(val message: String) : QuranUiState()
}
