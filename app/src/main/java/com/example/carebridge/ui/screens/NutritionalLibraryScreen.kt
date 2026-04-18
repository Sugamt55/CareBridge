package com.example.carebridge.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carebridge.data.model.FoodItem

@Composable
fun NutritionalLibraryScreen(modifier: Modifier = Modifier) {
    val foodList = getNutritionalData()
    val orangeColor = Color(0xFFFF6B00)
    val whiteColor = Color(0xFFFFFFFF)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Nutritional Encyclopedia",
                color = whiteColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 38.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Detailed insights into common foods and their metabolic impact.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(foodList) { food ->
            FoodCard(food = food, orangeColor = orangeColor)
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FoodCard(food: FoodItem, orangeColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Surface(
                    color = if (food.alkalineOrAcidic == "Alkaline") Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = food.alkalineOrAcidic.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = if (food.alkalineOrAcidic == "Alkaline") Color(0xFF065F46) else Color(0xFF991B1B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            
            Text(
                text = "pH Level: ${food.phLevel}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (food.alkalineOrAcidic == "Alkaline") Color(0xFF065F46) else Color(0xFF991B1B)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            NutrientSection(title = "Key Nutrients", items = food.nutrients, accentColor = orangeColor)
            NutrientSection(title = "Vitamins", items = food.vitamins, accentColor = orangeColor)
            NutrientSection(title = "Minerals", items = food.minerals, accentColor = orangeColor)
        }
    }
}

@Composable
fun NutrientSection(title: String, items: List<String>, accentColor: Color) {
    if (items.isNotEmpty()) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = items.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun getNutritionalData(): List<FoodItem> {
    return listOf(
        FoodItem("Apple", 3.6, "Alkaline", listOf("Fiber", "Sugar"), listOf("Vitamin C", "Vitamin K"), listOf("Potassium")),
        FoodItem("Banana", 4.8, "Alkaline", listOf("Fiber", "Potassium"), listOf("Vitamin C", "Vitamin B6"), listOf("Magnesium")),
        FoodItem("Cabbage", 5.5, "Alkaline", listOf("Fiber", "Protein"), listOf("Vitamin K", "Vitamin C"), listOf("Folate")),
        FoodItem("Carrot", 6.0, "Alkaline", listOf("Fiber", "Beta-carotene"), listOf("Vitamin A", "Vitamin K1"), listOf("Potassium")),
        FoodItem("Chicken", 5.8, "Acidic", listOf("Protein", "Niacin"), listOf("Vitamin B6", "Vitamin B12"), listOf("Phosphorus", "Selenium")),
        FoodItem("Corn", 6.5, "Acidic", listOf("Carbohydrates", "Fiber"), listOf("Vitamin B1", "Vitamin B9"), listOf("Magnesium", "Phosphorus")),
        FoodItem("Cucumber", 5.4, "Alkaline", listOf("Water", "Fiber"), listOf("Vitamin K", "Vitamin C"), listOf("Potassium")),
        FoodItem("Ginger", 5.6, "Alkaline", listOf("Gingerol"), listOf("Vitamin B3", "Vitamin B6"), listOf("Iron", "Potassium")),
        FoodItem("Goat", 6.2, "Acidic", listOf("Protein", "Iron"), listOf("Vitamin B12", "Riboflavin"), listOf("Zinc", "Potassium")),
        FoodItem("Grapes", 3.8, "Alkaline", listOf("Sugar", "Antioxidants"), listOf("Vitamin C", "Vitamin K"), listOf("Potassium")),
        FoodItem("Mango", 4.2, "Alkaline", listOf("Fiber", "Sugar"), listOf("Vitamin A", "Vitamin C"), listOf("Folate")),
        FoodItem("Onion", 5.5, "Alkaline", listOf("Fiber", "Quercetin"), listOf("Vitamin C", "Vitamin B6"), listOf("Folate")),
        FoodItem("Orange", 3.5, "Alkaline", listOf("Sugar", "Fiber"), listOf("Vitamin C", "Folate"), listOf("Potassium")),
        FoodItem("Potato", 5.7, "Alkaline", listOf("Carbohydrates", "Fiber"), listOf("Vitamin C", "Vitamin B6"), listOf("Potassium", "Manganese")),
        FoodItem("Tomato", 4.5, "Alkaline", listOf("Lycopene", "Water"), listOf("Vitamin C", "Vitamin K"), listOf("Potassium", "Folate")),
        FoodItem("Watermelon", 5.4, "Alkaline", listOf("Water", "Lycopene"), listOf("Vitamin C", "Vitamin A"), listOf("Potassium"))
    )
}
