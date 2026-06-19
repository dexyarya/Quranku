package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "masjids")
data class Masjid(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nama: String,
    val alamat: String,
    val alamatLengkap: String,
    val kontak: String,
    val createdAt: Long = System.currentTimeMillis()
)
