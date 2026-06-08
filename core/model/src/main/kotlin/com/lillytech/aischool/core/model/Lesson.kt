package com.lillytech.aischool.core.model

import kotlinx.serialization.Serializable

/**
 * A single unit of learning content.
 *
 * @property audioUrl Streamable narration track for the lesson. Always present
 *   for lessons published to the automotive surface.
 * @property visualContentUrl Rich visual payload (video player, code editor,
 *   interactive sandbox) rendered by the mobile flavor only. `null` for
 *   audio-only lectures — and always stripped before reaching the vehicle.
 * @property isAutomotiveSafe `false` for lessons whose primary content is
 *   visually dense (raw Python scripts, JSON payloads, architecture diagrams).
 *   The automotive layer must never surface their raw text; it exposes
 *   [audioSummary] instead.
 * @property audioSummary Short semantic summary suitable for glanceable,
 *   distraction-free display on the IVI and for TTS-style narration context.
 */
@Serializable
data class Lesson(
    val id: String,
    val title: String,
    val durationSeconds: Int,
    val audioUrl: String,
    val visualContentUrl: String? = null,
    val isAutomotiveSafe: Boolean,
    val audioSummary: String,
) {
    val durationMillis: Long get() = durationSeconds * 1000L

    val hasVisualPayload: Boolean get() = !visualContentUrl.isNullOrBlank()
}
