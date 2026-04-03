package com.example.carebridge.data.repository

import android.content.Context
import com.example.carebridge.data.model.FoodDatabaseItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class FoodDatabaseRepository(private val context: Context) {

    private val gson = Gson()
    private var foodMap: Map<String, FoodDatabaseItem> = emptyList<FoodDatabaseItem>().associateBy { it.foodName }

    init {
        loadDatabase()
    }

    private fun loadDatabase() {
        try {
            val inputStream = context.assets.open("food_nutrition_ph_database.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<Map<String, FoodDatabaseItem>>() {}.type
            foodMap = gson.fromJson(reader, type)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFoodBySlug(slug: String): FoodDatabaseItem? {
        return foodMap[slug]
    }

    fun getFoodByName(name: String): FoodDatabaseItem? {
        return foodMap.values.find { it.foodName.equals(name, ignoreCase = true) }
    }

    fun getAllFoods(): List<FoodDatabaseItem> = foodMap.values.toList()
}
