package com.cotemig.receitas.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitInstance {

    private const val URL_BASE = "https://www.themealdb.com/api/json/v1/1/"

    private val json = Json { ignoreUnknownKeys = true }

    val api: ReceitasApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ReceitasApi::class.java)
    }
}
