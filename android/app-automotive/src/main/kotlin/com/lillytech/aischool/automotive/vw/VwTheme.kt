package com.lillytech.aischool.automotive.vw

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.lillytech.aischool.automotive.R

/**
 * Design tokens for the VW-style catalog preview. The palette echoes VW MIB4
 * (near-black canvas, translucent tiles, an amber climate strip) while the
 * accents stay on the AI School brand. The typeface is Nunito, a rounded
 * humanist sans, the open-source stand-in for VW's friendly rounded look
 * (SF Rounded, which can't be redistributed in an app).
 */
object Vw {
    val Bg = Color(0xFF0B0B10)
    val BgTop = Color(0xFF14141C)
    val Tile = Color(0x14FFFFFF)        // ~8% white, translucent tile
    val TileBorder = Color(0x26FFFFFF)  // ~15% white hairline
    val Text = Color(0xFFEDEEF2)
    val TextDim = Color(0xFF9CA0AC)
    val Amber = Color(0xFFFFA64D)       // climate-strip accent
    val Green = Color(0xFF6CDC6C)

    // AI School brand accents (per pillar)
    val Indigo = Color(0xFF6C63FF)
    val Coral = Color(0xFFFF6584)
    val Cyan = Color(0xFF35D6D6)

    val Nunito = FontFamily(
        Font(R.font.nunito_regular, FontWeight.Normal),
        Font(R.font.nunito_medium, FontWeight.Medium),
        Font(R.font.nunito_semibold, FontWeight.SemiBold),
        Font(R.font.nunito_bold, FontWeight.Bold),
        Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
    )

    /** Brand accent for a syllabus pillar/category. */
    fun accentFor(category: String): Color = when {
        category.contains("Infrastructure", true) || category.contains("Hardware", true) -> Cyan
        category.contains("Tuning", true) -> Coral
        else -> Indigo
    }
}
