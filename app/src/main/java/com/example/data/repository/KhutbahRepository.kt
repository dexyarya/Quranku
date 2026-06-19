package com.example.data.repository

import com.example.data.local.KhutbahDao
import com.example.data.local.MasjidDao
import com.example.data.local.JadwalDao
import com.example.data.local.OfflineSurahDao
import com.example.data.local.OfflineSurahEntity
import com.example.data.model.KhutbahRecord
import com.example.data.model.KhutbahWithMasjid
import com.example.data.model.Masjid
import com.example.data.model.JadwalPetugas
import com.example.data.model.JadwalWithMasjid
import kotlinx.coroutines.flow.Flow

class KhutbahRepository(
    private val masjidDao: MasjidDao,
    private val khutbahDao: KhutbahDao,
    private val jadwalDao: JadwalDao,
    private val offlineSurahDao: OfflineSurahDao
) {
    // Offline Quran Support
    suspend fun getAllOfflineSurahs(): List<OfflineSurahEntity> = offlineSurahDao.getAllOfflineSurahs()
    suspend fun getOfflineSurah(nomor: Int): OfflineSurahEntity? = offlineSurahDao.getOfflineSurah(nomor)
    suspend fun insertOfflineSurah(surah: OfflineSurahEntity) = offlineSurahDao.insertOfflineSurah(surah)
    suspend fun deleteOfflineSurah(nomor: Int) = offlineSurahDao.deleteOfflineSurah(nomor)
    // Flows
    val allMasjids: Flow<List<Masjid>> = masjidDao.getAllMasjids()
    val allKhutbahs: Flow<List<KhutbahWithMasjid>> = khutbahDao.getAllKhutbahWithMasjid()
    val allJadwals: Flow<List<JadwalWithMasjid>> = jadwalDao.getAllJadwalWithMasjid()

    // Masjid CRUD
    suspend fun getMasjidById(id: Int): Masjid? = masjidDao.getMasjidById(id)
    suspend fun insertMasjid(masjid: Masjid): Long = masjidDao.insertMasjid(masjid)
    suspend fun updateMasjid(masjid: Masjid) = masjidDao.updateMasjid(masjid)
    suspend fun deleteMasjid(masjid: Masjid) = masjidDao.deleteMasjid(masjid)

    // Khutbah CRUD
    suspend fun getKhutbahById(id: Int): KhutbahWithMasjid? = khutbahDao.getKhutbahWithMasjidById(id)
    fun getKhutbahForMasjid(masjidId: Int): Flow<List<KhutbahWithMasjid>> = khutbahDao.getKhutbahForMasjid(masjidId)
    suspend fun insertKhutbah(khutbah: KhutbahRecord): Long = khutbahDao.insertKhutbah(khutbah)
    suspend fun updateKhutbah(khutbah: KhutbahRecord) = khutbahDao.updateKhutbah(khutbah)
    suspend fun deleteKhutbah(khutbah: KhutbahRecord) = khutbahDao.deleteKhutbah(khutbah)

    // Search
    fun searchKhutbah(query: String): Flow<List<KhutbahWithMasjid>> = khutbahDao.searchKhutbah(query)

    // Jadwal CRUD
    fun getJadwalForMasjid(masjidId: Int): Flow<List<JadwalWithMasjid>> = jadwalDao.getJadwalForMasjid(masjidId)
    suspend fun insertJadwal(jadwal: JadwalPetugas): Long = jadwalDao.insertJadwal(jadwal)
    suspend fun updateJadwal(jadwal: JadwalPetugas) = jadwalDao.updateJadwal(jadwal)
    suspend fun deleteJadwal(jadwal: JadwalPetugas) = jadwalDao.deleteJadwal(jadwal)
    fun searchJadwal(query: String): Flow<List<JadwalWithMasjid>> = jadwalDao.searchJadwal(query)
}
