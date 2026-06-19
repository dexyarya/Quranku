package com.example.data.model

data class KhutbahWithMasjid(
    val id: Int,
    val masjidId: Int,
    val namaMasjid: String,
    val alamatMasjid: String,
    val tanggal: String,
    val khatib: String,
    val judul: String,
    val summary: String,
    val durasiMenit: Int,
    val createdAt: Long
)
