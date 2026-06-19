package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.JadwalPetugas
import com.example.data.model.JadwalWithMasjid
import kotlinx.coroutines.flow.Flow

@Dao
interface JadwalDao {
    @Query("""
        SELECT j.id, j.masjidId, m.nama AS namaMasjid, m.alamat AS alamatMasjid,
               j.tanggal, j.khatib, j.imam, j.muadzin, j.bilal, j.keterangan, j.createdAt
        FROM jadwal_petugas j
        INNER JOIN masjids m ON j.masjidId = m.id
        ORDER BY j.tanggal DESC, j.createdAt DESC
    """)
    fun getAllJadwalWithMasjid(): Flow<List<JadwalWithMasjid>>

    @Query("""
        SELECT j.id, j.masjidId, m.nama AS namaMasjid, m.alamat AS alamatMasjid,
               j.tanggal, j.khatib, j.imam, j.muadzin, j.bilal, j.keterangan, j.createdAt
        FROM jadwal_petugas j
        INNER JOIN masjids m ON j.masjidId = m.id
        WHERE j.masjidId = :masjidId
        ORDER BY j.tanggal DESC
    """)
    fun getJadwalForMasjid(masjidId: Int): Flow<List<JadwalWithMasjid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJadwal(jadwal: JadwalPetugas): Long

    @Update
    suspend fun updateJadwal(jadwal: JadwalPetugas)

    @Delete
    suspend fun deleteJadwal(jadwal: JadwalPetugas)

    @Query("""
        SELECT j.id, j.masjidId, m.nama AS namaMasjid, m.alamat AS alamatMasjid,
               j.tanggal, j.khatib, j.imam, j.muadzin, j.bilal, j.keterangan, j.createdAt
        FROM jadwal_petugas j
        INNER JOIN masjids m ON j.masjidId = m.id
        WHERE j.khatib LIKE '%' || :query || '%' 
           OR j.imam LIKE '%' || :query || '%' 
           OR j.muadzin LIKE '%' || :query || '%'
           OR j.bilal LIKE '%' || :query || '%'
           OR m.nama LIKE '%' || :query || '%'
        ORDER BY j.tanggal DESC
    """)
    fun searchJadwal(query: String): Flow<List<JadwalWithMasjid>>
}
