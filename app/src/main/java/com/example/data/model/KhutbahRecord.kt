package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "khutbah_records",
    foreignKeys = [
        ForeignKey(
            entity = Masjid::class,
            parentColumns = ["id"],
            childColumns = ["masjidId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["masjidId"])]
)
data class KhutbahRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val masjidId: Int,
    val tanggal: String, // Format: dd-MM-yyyy or similar readable date
    val khatib: String,
    val judul: String,
    val summary: String,
    val durasiMenit: Int = 20,
    val createdAt: Long = System.currentTimeMillis()
)
