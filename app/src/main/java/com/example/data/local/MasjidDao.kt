package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Masjid
import kotlinx.coroutines.flow.Flow

@Dao
interface MasjidDao {
    @Query("SELECT * FROM masjids ORDER BY nama ASC")
    fun getAllMasjids(): Flow<List<Masjid>>

    @Query("SELECT * FROM masjids WHERE id = :id")
    suspend fun getMasjidById(id: Int): Masjid?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMasjid(masjid: Masjid): Long

    @Update
    suspend fun updateMasjid(masjid: Masjid)

    @Delete
    suspend fun deleteMasjid(masjid: Masjid)
}
