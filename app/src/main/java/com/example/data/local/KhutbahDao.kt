package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.KhutbahRecord
import com.example.data.model.KhutbahWithMasjid
import kotlinx.coroutines.flow.Flow

@Dao
interface KhutbahDao {
    @Query("""
        SELECT k.id, k.masjidId, m.nama AS namaMasjid, m.alamat AS alamatMasjid,
               k.tanggal, k.khatib, k.judul, k.summary, k.durasiMenit, k.createdAt
        FROM khutbah_records k
        INNER JOIN masjids m ON k.masjidId = m.id
        ORDER BY k.createdAt DESC
    """)
    fun getAllKhutbahWithMasjid(): Flow<List<KhutbahWithMasjid>>

    @Query("""
        SELECT k.id, k.masjidId, m.nama AS namaMasjid, m.alamat AS alamatMasjid,
               k.tanggal, k.khatib, k.judul, k.summary, k.durasiMenit, k.createdAt
        FROM khutbah_records k
        INNER JOIN masjids m ON k.masjidId = m.id
        WHERE k.id = :id
    """)
    suspend fun getKhutbahWithMasjidById(id: Int): KhutbahWithMasjid?

    @Query("""
        SELECT k.id, k.masjidId, m.nama AS namaMasjid, m.alamat AS alamatMasjid,
               k.tanggal, k.khatib, k.judul, k.summary, k.durasiMenit, k.createdAt
        FROM khutbah_records k
        INNER JOIN masjids m ON k.masjidId = m.id
        WHERE k.masjidId = :masjidId
        ORDER BY k.createdAt DESC
    """)
    fun getKhutbahForMasjid(masjidId: Int): Flow<List<KhutbahWithMasjid>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKhutbah(khutbah: KhutbahRecord): Long

    @Update
    suspend fun updateKhutbah(khutbah: KhutbahRecord)

    @Delete
    suspend fun deleteKhutbah(khutbah: KhutbahRecord)

    @Query("""
        SELECT k.id, k.masjidId, m.nama AS namaMasjid, m.alamat AS alamatMasjid,
               k.tanggal, k.khatib, k.judul, k.summary, k.durasiMenit, k.createdAt
        FROM khutbah_records k
        INNER JOIN masjids m ON k.masjidId = m.id
        WHERE k.judul LIKE '%' || :query || '%' 
           OR k.khatib LIKE '%' || :query || '%' 
           OR k.summary LIKE '%' || :query || '%'
           OR m.nama LIKE '%' || :query || '%'
        ORDER BY k.createdAt DESC
    """)
    fun searchKhutbah(query: String): Flow<List<KhutbahWithMasjid>>
}
