package com.lillytech.aischool.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// AI School brand palette (from lillytechsystems.com/ai-school)
val BrandPrimary = Color(0xFF6C63FF)   // indigo/violet
val BrandPrimaryDark = Color(0xFF5A52D5)
val BrandPrimaryLight = Color(0xFF8B85FF)
val BrandSecondary = Color(0xFFFF6584) // coral/pink
val BrandAccent = Color(0xFF43E97B)    // green
val BrandBg = Color(0xFF13131A)        // near-black
val BrandCard = Color(0xFF1E1E2F)
val BrandBorder = Color(0xFF2A2A3C)
val BrandText = Color(0xFFE4E4E7)
val BrandTextDim = Color(0xFF9CA3AF)

// AI School is a dark-first brand; the app uses a single dark scheme.
private val AiSchoolColors = darkColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = BrandSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A2230),
    onSecondaryContainer = Color(0xFFFFD9E0),
    tertiary = BrandAccent,
    onTertiary = Color(0xFF003918),
    background = BrandBg,
    onBackground = BrandText,
    surface = BrandBg,
    onSurface = BrandText,
    surfaceVariant = BrandCard,
    onSurfaceVariant = BrandTextDim,
    outline = BrandBorder,
    outlineVariant = BrandBorder,
)

@Composable
fun AiSchoolTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AiSchoolColors,
        typography = AiSchoolTypography,
        content = content,
    )
}
