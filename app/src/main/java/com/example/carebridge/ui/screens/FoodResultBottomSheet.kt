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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodResultBottomSheet(
    foodData: FoodPredictionResponse,
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
                Text(
                    text = foodData.foodName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
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
            PhScaleBar(phLevel = foodData.phLevel, isAlkaline = foodData.isAlkaline)

            Spacer(modifier = Modifier.height(32.dp))

            // Nutrition Grid
            Text(
                "Nutritional Information",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            val nutritionItems = listOf(
                "Calories" to "${foodData.calories} kcal",
                "Protein" to foodData.protein,
                "Carbs" to foodData.carbs,
                "Fats" to foodData.fat
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(140.dp) // Fixed height for grid
            ) {
                items(nutritionItems.size) { index ->
                    val item = nutritionItems[index]
                    NutritionCard(label = item.first, value = item.second)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            Button(
                onClick = { /* TODO: Save logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0056B3)
                )
            ) {
                Text("Save to Daily Log", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun PhScaleBar(phLevel: Double, isAlkaline: Boolean) {
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
        
        Surface(
            color = if (isAlkaline) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = if (isAlkaline) "Alkaline Choice" else "Acidic Choice",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = if (isAlkaline) Color(0xFF065F46) else Color(0xFF991B1B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
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
