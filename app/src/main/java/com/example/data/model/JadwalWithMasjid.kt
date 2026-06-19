package com.example.data.model

data class JadwalWithMasjid(
    val id: Int,
    val masjidId: Int,
    val namaMasjid: String,
    val alamatMasjid: String,
    val tanggal: String,
    val khatib: String,
    val imam: String,
    val muadzin: String,
    val bilal: String,
    val keterangan: String,
    val createdAt: Long
)
