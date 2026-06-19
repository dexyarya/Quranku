package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "jadwal_petugas",
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
data class JadwalPetugas(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val masjidId: Int,
    val tanggal: String, // Format: dd-MM-yyyy
    val khatib: String,
    val imam: String,
    val muadzin: String,
    val bilal: String,
    val keterangan: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
