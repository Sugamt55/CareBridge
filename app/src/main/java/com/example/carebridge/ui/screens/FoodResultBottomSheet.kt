package com.example.carebridge.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carebridge.data.FoodPredictionResponse
import com.example.carebridge.data.model.FoodDatabaseItem
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodResultBottomSheet(
    foodData: FoodPredictionResponse,
    detailedData: FoodDatabaseItem? = null,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = detailedData?.foodName ?: foodData.foodName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = detailedData?.servingSize ?: foodData.servingSize,
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // pH Scale Indicator
            Text(
                "Alkalinity (pH Scale)",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            PhScaleBar(
                phLevel = foodData.phLevel, 
                isAlkaline = foodData.isAlkaline,
                classification = detailedData?.phClassification,
                reason = detailedData?.phReason
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Nutrition Grid
            Text(
                "Nutritional Information (per ${detailedData?.servingSize ?: foodData.servingSize})",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val nutritionItems = if (detailedData != null) {
                listOf(
                    "Calories" to "${detailedData.calories} kcal",
                    "Protein" to "${detailedData.macronutrients.proteinG}g",
                    "Carbs" to "${detailedData.macronutrients.carbsG}g",
                    "Fats" to "${detailedData.macronutrients.fatG}g",
                    "Fiber" to "${detailedData.macronutrients.fiberG}g",
                    "Sugar" to "${detailedData.macronutrients.sugarG}g"
                )
            } else {
                listOf(
                    "Calories" to "${foodData.calories} kcal",
                    "Protein" to foodData.protein,
                    "Carbs" to foodData.carbs,
                    "Fats" to foodData.fat
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(if (detailedData != null) 210.dp else 140.dp)
            ) {
                items(nutritionItems.size) { index ->
                    val item = nutritionItems[index]
                    NutritionCard(label = item.first, value = item.second)
                }
            }

            if (detailedData != null) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Micronutrients",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResultSuggestionChip(label = "Iron: ${detailedData.micronutrients.ironMg}mg")
                    ResultSuggestionChip(label = "Potassium: ${detailedData.micronutrients.potassiumMg}mg")
                    ResultSuggestionChip(label = "Vit C: ${detailedData.micronutrients.vitaminCMg}mg")
                }
            }
        }
    }
}

@Composable
fun PhScaleBar(
    phLevel: Double, 
    isAlkaline: Boolean, 
    classification: String? = null,
    reason: String? = null
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(Color(0xFFEF4444), Color(0xFFFACC15), Color(0xFF22C55E))
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
        ) {
            // Marker
            val bias = ((phLevel.coerceIn(0.0, 14.0) / 14.0) * 2 - 1).toFloat()
            Box(
                modifier = Modifier
                    .align(BiasAlignment(bias, 0f))
                    .size(16.dp)
                    .background(Color.White, CircleShape)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Acidic (0)", fontSize = 10.sp, color = Color.Gray)
            Text("Neutral (7)", fontSize = 10.sp, color = Color.Gray)
            Text("Alkaline (14)", fontSize = 10.sp, color = Color.Gray)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Added the pH level text here
        Text(
            text = "pH Level: %.1f".format(phLevel),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAlkaline) Color(0xFF065F46) else Color(0xFF991B1B)
        )

        Spacer(modifier = Modifier.height(4.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = if (isAlkaline) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                shape = RoundedCornerShape(8.dp)
            ) {
                val displayText = classification?.replace("-", " ")?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } ?: if (isAlkaline) "Alkaline Choice" else "Acidic Choice"
                Text(
                    text = displayText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (isAlkaline) Color(0xFF065F46) else Color(0xFF991B1B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        reason?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun NutritionCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ResultSuggestionChip(label: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        color = Color.White
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 12.sp,
            color = Color(0xFF1B4985),
            fontWeight = FontWeight.Medium
        )
    }
}
