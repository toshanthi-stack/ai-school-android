package com.lillytech.aischool.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import com.lillytech.aischool.mobile.R

/** Inter — the AI School brand typeface (from lillytechsystems.com). */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extrabold, FontWeight.ExtraBold),
)

/** Material 3 type scale, restyled onto Inter. */
val AiSchoolTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = Inter),
        displayMedium = displayMedium.copy(fontFamily = Inter),
        displaySmall = displaySmall.copy(fontFamily = Inter),
        headlineLarge = headlineLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Bold),
        headlineMedium = headlineMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontFamily = Inter, fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = Inter, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontFamily = Inter),
        bodyMedium = bodyMedium.copy(fontFamily = Inter),
        bodySmall = bodySmall.copy(fontFamily = Inter),
        labelLarge = labelLarge.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        labelMedium = labelMedium.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
        labelSmall = labelSmall.copy(fontFamily = Inter, fontWeight = FontWeight.Medium),
    )
}

@Suppress("unused")
private val unusedTextStyle = TextStyle()
