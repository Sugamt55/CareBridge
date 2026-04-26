package com.example.nutriscanai.data.model

import com.google.gson.annotations.SerializedName

data class FoodDatabaseItem(
    @SerializedName("food_name")
    val foodName: String,
    @SerializedName("serving_size")
    val servingSize: String,
    val calories: Int,
    val macronutrients: Macronutrients,
    val micronutrients: Micronutrients,
    @SerializedName("ph_classification")
    val phClassification: String,
    @SerializedName("ph_reason")
    val phReason: String
)

data class Macronutrients(
    @SerializedName("protein_g")
    val proteinG: Double,
    @SerializedName("carbs_g")
    val carbsG: Double,
    @SerializedName("fat_g")
    val fatG: Double,
    @SerializedName("fiber_g")
    val fiberG: Double,
    @SerializedName("sugar_g")
    val sugarG: Double
)

data class Micronutrients(
    @SerializedName("vitamin_a_iu")
    val vitaminAIu: Double,
    @SerializedName("vitamin_c_mg")
    val vitaminCMg: Double,
    @SerializedName("calcium_mg")
    val calciumMg: Double,
    @SerializedName("iron_mg")
    val ironMg: Double,
    @SerializedName("potassium_mg")
    val potassiumMg: Double
)
