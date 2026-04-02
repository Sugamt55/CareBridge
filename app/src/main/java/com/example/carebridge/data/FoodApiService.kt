package com.example.carebridge.data

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FoodApiService {
    @Multipart
    @POST("analyze")
    suspend fun analyzeFood(
        @Part image: MultipartBody.Part
    ): FoodResponse
}

data class FoodResponse(
    val name: String,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)
