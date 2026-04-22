package com.example.carebridge.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.carebridge.R

val CormorantGaramondFamily = FontFamily(
    Font(R.font.cormorantgaramond_italic_variablefont_wght, FontWeight.Normal),
    Font(R.font.cormorantgaramond_italic_variablefont_wght, FontWeight.Bold)
)

val InterFamily = FontFamily(
    Font(R.font.inter_18pt_italic, FontWeight.Normal),
    Font(R.font.inter_18pt_italic, FontWeight.Medium),
    Font(R.font.inter_18pt_italic, FontWeight.Bold)
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = CormorantGaramondFamily,
        fontSize = 32.sp,
        fontStyle = FontStyle.Italic,
        color = Color(0xFF191C1E)
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontSize = 14.sp,
        color = Secondary
    ),
    displayLarge = TextStyle(
        fontFamily = CormorantGaramondFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(
        fontFamily = CormorantGaramondFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
