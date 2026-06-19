package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OfflineSurahDao {
    @Query("SELECT * FROM offline_surahs ORDER BY nomor ASC")
    suspend fun getAllOfflineSurahs(): List<OfflineSurahEntity>

    @Query("SELECT * FROM offline_surahs WHERE nomor = :nomor LIMIT 1")
    suspend fun getOfflineSurah(nomor: Int): OfflineSurahEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineSurah(surah: OfflineSurahEntity)

    @Query("DELETE FROM offline_surahs WHERE nomor = :nomor")
    suspend fun deleteOfflineSurah(nomor: Int)
}
