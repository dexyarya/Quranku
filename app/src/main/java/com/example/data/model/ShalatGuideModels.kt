package com.example.data.model

data class ShalatBacaanModel(
    val id: Int,
    val stepName: String,
    val arabic: String,
    val latin: String,
    val translation: String,
    val instruction: String = ""
)

data class ShalatTataCaraModel(
    val id: Int,
    val stepNumber: Int,
    val title: String,
    val description: String,
    val illustration: String, // Emoji or short visual indicator
    val legalStatus: String = "Rukun" // Rukun / Sunnah
)
