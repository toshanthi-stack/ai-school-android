package com.lillytech.aischool.core.model

import kotlinx.serialization.Serializable

/**
 * A course in the AI School syllabus: an ordered set of [Lesson]s grouped
 * under one of the [AiSchoolPillars] categories.
 */
@Serializable
data class Course(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val lessons: List<Lesson>,
) {
    val totalDurationSeconds: Int get() = lessons.sumOf { it.durationSeconds }
}
