package com.example.carebridge.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carebridge.ui.theme.CormorantGaramondFamily
import com.example.carebridge.ui.theme.InterFamily

/**
 * BioMineralSlider: A modular component for the Digital Apothecary system.
 * Animates background color based on biological state (Acidic -> Neutral -> Alkaline).
 */
@Composable
fun BioMineralSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // System Color Palette
    val acidicCoral = Color(0xFFFFB4AB)
    val neutralWhite = Color(0xFFF7F9FB)
    val alkalineSage = Color(0xFFC3ECD7)
    val primaryGreen = Color(0xFF006C49)

    // Calculate target color based on range for smooth interpolation
    val targetColor = when {
        value <= 0.5f -> {
            val fraction = value / 0.5f
            lerp(acidicCoral, neutralWhite, fraction)
        }
        else -> {
            val fraction = (value - 0.5f) / 0.5f
            lerp(neutralWhite, alkalineSage, fraction)
        }
    }

    // Animate color transition for professional feel
    val backgroundColor by animateColorAsState(
        targetValue = targetColor,
        label = "BiologicalStateColor"
    )

    // State-based content logic
    val stateHeading: String
    val stateDescription: String
    when {
        value <= 0.3f -> {
            stateHeading = "ACIDIC STATE"
            stateDescription = "Cellular fatigue and mucus accumulation."
        }
        value <= 0.6f -> {
            stateHeading = "NEUTRAL STATE"
            stateDescription = "Balancing your inner terrain."
        }
        else -> {
            stateHeading = "ALKALINE STATE"
            stateDescription = "Optimal oxygenation and homeostasis."
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // State Display Text
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontFamily = CormorantGaramondFamily,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    ) {
                        append(stateHeading)
                    }
                    append(": ")
                    withStyle(
                        style = SpanStyle(
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    ) {
                        append(stateDescription)
                    }
                },
                color = Color(0xFF191C1E)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Customized Slider
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = primaryGreen,
                    activeTrackColor = primaryGreen,
                    inactiveTrackColor = primaryGreen.copy(alpha = 0.2f)
                )
            )
        }
    }
}
