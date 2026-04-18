package com.example.carebridge.data.model

data class FoodItem(
    val name: String,
    val phLevel: Double,
    val alkalineOrAcidic: String,
    val nutrients: List<String>,
    val vitamins: List<String>,
    val minerals: List<String>
)
