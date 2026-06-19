package com.example.data.model

data class DoaModel(
    val id: Int,
    val title: String,
    val arabic: String,
    val latin: String,
    val translation: String,
    val category: String,
    val fadhilah: String = ""
)
