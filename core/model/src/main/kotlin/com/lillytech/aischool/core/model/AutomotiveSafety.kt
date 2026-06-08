package com.lillytech.aischool.core.model

/**
 * Automotive filtering rule (driver-distraction compliance):
 *
 * A lesson reaching the vehicle must carry a streamable audio track and zero
 * visual payload. Lessons flagged `isAutomotiveSafe = false` (heavy Python
 * scripts, raw JSON, architecture diagrams) are NOT dropped — they are
 * sanitized: the visual payload is stripped and the short [Lesson.audioSummary]
 * becomes the only text the IVI is allowed to render.
 */

/**
 * Returns a distraction-free projection of this lesson, or `null` when the
 * lesson has no audio track and therefore nothing to offer in the cabin.
 */
fun Lesson.toAutomotiveSafe(): Lesson? {
    if (audioUrl.isBlank()) return null
    // Strip every visual payload; for unsafe lessons the audioSummary is the
    // sole permitted text surface in the vehicle.
    return copy(visualContentUrl = null)
}

/**
 * Returns a course whose lesson list has been sanitized for the vehicle, or
 * `null` when nothing in the course is playable in the cabin.
 */
fun Course.toAutomotiveSafe(): Course? {
    val safeLessons = lessons.mapNotNull { it.toAutomotiveSafe() }
    return if (safeLessons.isEmpty()) null else copy(lessons = safeLessons)
}

/** Sanitizes an entire syllabus tree for in-vehicle consumption. */
fun List<Course>.toAutomotiveSafeSyllabus(): List<Course> = mapNotNull { it.toAutomotiveSafe() }
