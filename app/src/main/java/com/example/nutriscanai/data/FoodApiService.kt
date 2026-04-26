package com.example.nutriscanai.data

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FoodApiService {
    @Multipart
    @POST("predict")
    suspend fun predictFood(
        @Part image: MultipartBody.Part
    ): FoodPredictionResponse

    // Added for connection testing
    @GET("/")
    suspend fun healthCheck(): Map<String, Any>
}

data class FoodPredictionResponse(
    @SerializedName("food_name")
    val foodName: String,
    @SerializedName("serving_size")
    val servingSize: String,
    val calories: Int,
    val protein: String,
    val carbs: String,
    val fat: String,
    @SerializedName("ph_level")
    val phLevel: Double,
    @SerializedName("is_alkaline")
    val isAlkaline: Boolean
)
