package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class QuranResponse<T>(
    @Json(name = "code") val code: Int,
    @Json(name = "message") val message: String,
    @Json(name = "data") val data: T? = null
)

@JsonClass(generateAdapter = true)
data class SurahModel(
    @Json(name = "nomor") val nomor: Int,
    @Json(name = "nama") val nama: String,
    @Json(name = "namaLatin") val namaLatin: String,
    @Json(name = "jumlahAyat") val jumlahAyat: Int,
    @Json(name = "tempatTurun") val tempatTurun: String = "",
    @Json(name = "arti") val arti: String = "",
    @Json(name = "deskripsi") val deskripsi: String = "",
    @Json(name = "audioFull") val audioFull: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class VerseModel(
    @Json(name = "nomorAyat") val nomorAyat: Int,
    @Json(name = "teksArab") val teksArab: String,
    @Json(name = "teksLatin") val teksLatin: String = "",
    @Json(name = "teksIndonesia") val teksIndonesia: String = "",
    @Json(name = "audio") val audio: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SurahDetailModel(
    @Json(name = "nomor") val nomor: Int,
    @Json(name = "nama") val nama: String,
    @Json(name = "namaLatin") val namaLatin: String,
    @Json(name = "jumlahAyat") val jumlahAyat: Int,
    @Json(name = "tempatTurun") val tempatTurun: String = "",
    @Json(name = "arti") val arti: String = "",
    @Json(name = "deskripsi") val deskripsi: String = "",
    @Json(name = "audioFull") val audioFull: Map<String, String>? = null,
    @Json(name = "ayat") val ayat: List<VerseModel> = emptyList()
)
