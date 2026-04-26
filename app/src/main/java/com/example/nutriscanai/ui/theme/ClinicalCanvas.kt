package com.example.nutriscanai.ui.theme

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A custom modifier to implement the "Clinical Canvas" design system.
 * It applies the Surface background and a subtle grid pattern
 * to give a tactile, paper-like "Digital Apothecary" feel.
 */
fun Modifier.clinicalBackground(): Modifier = this
    .background(Surface) // Uses the Surface color from Color.kt (#f7f9fb)
    .drawBehind {
        val gridSize = 24.dp.toPx()
        val gridColor = OutlineVariant.copy(alpha = 0.05f)

        // Draw vertical lines
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += gridSize
        }

        // Draw horizontal lines
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += gridSize
        }
    }
