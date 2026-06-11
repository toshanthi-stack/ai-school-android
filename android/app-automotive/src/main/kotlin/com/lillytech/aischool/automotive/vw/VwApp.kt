package com.lillytech.aischool.automotive.vw

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lillytech.aischool.core.model.AiSchoolPillars
import com.lillytech.aischool.core.model.Course
import com.lillytech.aischool.core.model.Lesson

/**
 * VW-styled preview flow for the AI School catalog. Large, glanceable
 * typography (Nunito), generous touch targets, dark canvas with translucent
 * tiles and pill tabs, matching the VW MIB design language. No app-drawn
 * status or climate bars: the real AAOS system chrome frames the content.
 */
@Composable
fun VwApp(courses: List<Course>) {
    // Lightweight state nav: home <-> now-playing.
    var playing by remember { mutableStateOf<Pair<Course, Lesson>?>(null) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Vw.BgTop, Vw.Bg))),
    ) {
        val now = playing
        if (now == null) {
            VwHome(courses) { course ->
                course.lessons.firstOrNull()?.let { playing = course to it }
            }
        } else {
            VwNowPlaying(now.first, now.second, onBack = { playing = null })
        }
    }
}

private data class Pillar(val tab: String, val full: String, val icon: ImageVector)

private val PILLARS = listOf(
    Pillar("Generative AI", AiSchoolPillars.GENERATIVE_AI, Icons.Filled.AutoAwesome),
    Pillar("Infrastructure", AiSchoolPillars.INFRASTRUCTURE, Icons.Filled.Memory),
    Pillar("Advanced Tuning", AiSchoolPillars.ADVANCED_TUNING, Icons.Filled.Tune),
)

@Composable
private fun VwHome(courses: List<Course>, onCourse: (Course) -> Unit) {
    var selected by remember { mutableStateOf(0) }
    val pillar = PILLARS[selected]
    val shown = courses.filter { it.category == pillar.full }

    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp)) {
        Spacer(Modifier.height(18.dp))
        // Compact brand header (no clock, AAOS shows the time)
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandGlyph(40.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                "AI School",
                color = Vw.Text,
                fontFamily = Vw.Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 30.sp,
            )
        }
        Spacer(Modifier.height(18.dp))
        // Pill tabs, short labels, large, never truncate
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            PILLARS.forEachIndexed { i, p ->
                val active = i == selected
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .background(if (active) Vw.accentFor(p.full).copy(alpha = 0.20f) else Vw.Tile)
                        .border(
                            1.dp,
                            if (active) Vw.accentFor(p.full) else Vw.TileBorder,
                            RoundedCornerShape(26.dp),
                        )
                        .clickable { selected = i }
                        .padding(horizontal = 22.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        p.icon,
                        contentDescription = null,
                        tint = if (active) Vw.accentFor(p.full) else Vw.TextDim,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        p.tab,
                        color = if (active) Vw.Text else Vw.TextDim,
                        fontFamily = Vw.Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        maxLines = 1,
                    )
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
        ) {
            items(shown, key = { it.id }) { c -> CourseTile(c) { onCourse(c) } }
        }
    }
}

@Composable
private fun CourseTile(course: Course, onClick: () -> Unit) {
    val accent = Vw.accentFor(course.category)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Vw.Tile)
            .border(1.dp, Vw.TileBorder, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(iconFor(course.category), null, tint = accent, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                course.title,
                color = Vw.Text,
                fontFamily = Vw.Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${course.lessons.size} lessons · ${course.totalDurationSeconds / 60} min",
                color = Vw.TextDim,
                fontFamily = Vw.Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun VwNowPlaying(course: Course, lesson: Lesson, onBack: () -> Unit) {
    val accent = Vw.accentFor(course.category)
    val total = lesson.durationSeconds.coerceAtLeast(1)
    // Live playback position. Keyed on the lesson id so it resets to 0 when the
    // lesson changes, auto-plays on entry.
    var positionSec by remember(lesson.id) { mutableIntStateOf(0) }
    var playing by remember(lesson.id) { mutableStateOf(true) }
    LaunchedEffect(lesson.id, playing) {
        while (playing && positionSec < total) {
            delay(1000L)
            positionSec += 1
            if (positionSec >= total) playing = false
        }
    }
    val fraction = (positionSec.toFloat() / total).coerceIn(0f, 1f)
    Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onBack)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Vw.Text, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(12.dp))
            Text("Now Playing", color = Vw.Text, fontFamily = Vw.Nunito, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(Vw.Indigo, accent))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.GraphicEq, null, tint = Color.White, modifier = Modifier.size(84.dp))
            }
            Spacer(Modifier.width(28.dp))
            Column(Modifier.weight(1f)) {
                Text(course.category.uppercase(), color = accent, fontFamily = Vw.Nunito, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    lesson.title,
                    color = Vw.Text, fontFamily = Vw.Nunito, fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp, lineHeight = 38.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${course.title} · ${lesson.durationSeconds / 60} min",
                    color = Vw.TextDim, fontFamily = Vw.Nunito, fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
                )
            }
        }
        Spacer(Modifier.height(28.dp))
        // progress bar (live playback position)
        Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Vw.Tile)) {
            if (fraction > 0f) {
                Box(Modifier.fillMaxWidth(fraction).height(6.dp).clip(CircleShape).background(accent))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTime(positionSec), color = Vw.TextDim, fontFamily = Vw.Nunito, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(formatTime(total), color = Vw.TextDim, fontFamily = Vw.Nunito, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.SkipPrevious, "Previous", tint = Vw.Text, modifier = Modifier.size(48.dp))
            Spacer(Modifier.width(40.dp))
            Box(
                modifier = Modifier.size(84.dp).clip(CircleShape).background(accent).clickable {
                    if (!playing && positionSec >= total) positionSec = 0 // replay from start
                    playing = !playing
                },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    if (playing) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp),
                )
            }
            Spacer(Modifier.width(40.dp))
            Icon(Icons.Filled.SkipNext, "Next", tint = Vw.Text, modifier = Modifier.size(48.dp))
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%d:%02d".format(m, s)
}

@Composable
private fun BrandGlyph(sz: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(sz)
            .clip(RoundedCornerShape(11.dp))
            .background(Brush.linearGradient(listOf(Vw.Indigo, Vw.Coral))),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.GraphicEq, null, tint = Color.White, modifier = Modifier.size(sz * 0.55f))
    }
}

private fun iconFor(category: String): ImageVector = when (category) {
    AiSchoolPillars.INFRASTRUCTURE -> Icons.Filled.Memory
    AiSchoolPillars.ADVANCED_TUNING -> Icons.Filled.Tune
    else -> Icons.Filled.AutoAwesome
}
