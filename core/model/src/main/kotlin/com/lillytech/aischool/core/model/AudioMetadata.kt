package com.lillytech.aischool.core.model

import kotlinx.serialization.Serializable

/**
 * Flavor-agnostic encapsulation of the metadata flags required to populate an
 * Android Media Session (`MediaMetadataCompat`) and a media browse item.
 *
 * The automotive service translates this into `MediaMetadataCompat` /
 * `MediaDescriptionCompat`; keeping the projection here keeps the service
 * free of mapping policy and trivially testable on the JVM.
 */
@Serializable
data class AudioMetadata(
    val mediaId: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val category: String,
    val durationMillis: Long,
    val isPlayable: Boolean,
    val isBrowsable: Boolean,
) {
    companion object {
        /** Metadata projection for a playable lesson inside [course]. */
        fun forLesson(course: Course, lesson: Lesson): AudioMetadata = AudioMetadata(
            mediaId = MediaIds.forLesson(lesson.id),
            title = lesson.title,
            subtitle = course.title,
            description = lesson.audioSummary,
            category = course.category,
            durationMillis = lesson.durationMillis,
            isPlayable = true,
            isBrowsable = false,
        )

        /** Metadata projection for a browsable course node. */
        fun forCourse(course: Course): AudioMetadata = AudioMetadata(
            mediaId = MediaIds.forCourse(course.id),
            title = course.title,
            subtitle = course.category,
            description = course.description,
            category = course.category,
            durationMillis = course.totalDurationSeconds * 1000L,
            isPlayable = false,
            isBrowsable = true,
        )

        /** Metadata projection for a top-level pillar category node. */
        fun forCategory(category: String, courseCount: Int): AudioMetadata = AudioMetadata(
            mediaId = MediaIds.forCategory(category),
            title = category,
            subtitle = "$courseCount courses",
            description = "AI School pillar: $category",
            category = category,
            durationMillis = 0L,
            isPlayable = false,
            isBrowsable = true,
        )
    }
}
