package com.example.nutriscanai.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    // Switched to 127.0.0.1 because we are using 'adb reverse'
    // This maps the emulator's local port 8000 directly to your Mac's port 8000
    private const val BASE_URL = "http://127.0.0.1:8000" 

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(5, TimeUnit.SECONDS) // Short timeout so you don't wait 15s for failures
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    val api: FoodApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL + "/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FoodApiService::class.java)
    }
}
