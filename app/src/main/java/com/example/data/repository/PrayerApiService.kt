package com.example.data.repository

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class AladhanResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: AladhanData? = null
)

@JsonClass(generateAdapter = true)
data class AladhanData(
    @Json(name = "timings") val timings: Map<String, String>,
    @Json(name = "date") val date: AladhanDate
)

@JsonClass(generateAdapter = true)
data class AladhanDate(
    @Json(name = "readable") val readable: String,
    @Json(name = "hijri") val hijri: AladhanHijriDate? = null
)

@JsonClass(generateAdapter = true)
data class AladhanHijriDate(
    @Json(name = "date") val date: String,
    @Json(name = "day") val day: String,
    @Json(name = "year") val year: String,
    @Json(name = "month") val month: AladhanHijriMonth? = null
)

@JsonClass(generateAdapter = true)
data class AladhanHijriMonth(
    @Json(name = "number") val number: Int,
    @Json(name = "en") val en: String,
    @Json(name = "ar") val ar: String? = null
)

interface PrayerApiService {
    @GET("v1/timings/{timestamp}")
    suspend fun getTimings(
        @Path("timestamp") timestamp: Long,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 11
    ): AladhanResponse
}

object PrayerApiClient {
    private const val BASE_URL = "https://api.aladhan.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: PrayerApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PrayerApiService::class.java)
    }
}
