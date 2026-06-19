package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_surahs")
data class OfflineSurahEntity(
    @PrimaryKey val nomor: Int,
    val nama: String,
    val namaLatin: String,
    val jumlahAyat: Int,
    val tempatTurun: String,
    val arti: String,
    val deskripsi: String,
    val ayatJson: String,
    val isDownloaded: Boolean = false
)
