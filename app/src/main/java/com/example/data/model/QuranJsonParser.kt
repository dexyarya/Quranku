package com.example.data.model

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object QuranJsonParser {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, VerseModel::class.java)
    private val adapter = moshi.adapter<List<VerseModel>>(listType)

    fun toJson(list: List<VerseModel>): String {
        return adapter.toJson(list)
    }

    fun fromJson(json: String): List<VerseModel>? {
        return try {
            adapter.fromJson(json)
        } catch (e: Exception) {
            null
        }
    }
}
