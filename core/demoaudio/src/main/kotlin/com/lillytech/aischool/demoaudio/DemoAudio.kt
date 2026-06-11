package com.lillytech.aischool.demoaudio

import android.content.Context
import android.net.Uri

/**
 * Bundled lesson narrations (res/raw) used as the offline fallback when the
 * production audio stream is unreachable, so playback keeps working in airplane
 * mode, tunnels, parking garages, and other low-connectivity areas.
 *
 * Files are named after the lesson id with hyphens mapped to underscores
 * (`genai-101` becomes `raw/genai_101.m4a`).
 */
object DemoAudio {

    /**
     * Returns an `android.resource://` URI for the bundled narration of
     * [lessonId], or `null` when no local narration is packaged.
     */
    fun narrationUri(context: Context, lessonId: String): Uri? =
        resourceUri(context, lessonId.replace('-', '_'), "raw")

    /**
     * Returns the branded artwork URI for a syllabus [category] (falls back to
     * the app mark for any unrecognized pillar). Each pillar gets its own
     * gradient so the IVI browse grid and Now Playing screen read as a
     * designed product, not stock music tiles.
     */
    fun categoryArtUri(context: Context, category: String): Uri =
        resourceUri(context, artNameFor(category), "drawable")
            ?: resourceUri(context, "art_app", "drawable")!!

    /** Branded app artwork (navy→teal with the AI School knowledge-graph mark). */
    fun appArtUri(context: Context): Uri = resourceUri(context, "art_app", "drawable")!!

    /** Drawable resource name of the branded artwork for a syllabus [category]. */
    fun artNameFor(category: String): String = when {
        category.contains("Infrastructure", ignoreCase = true) -> "art_infra"
        category.contains("Hardware", ignoreCase = true) -> "art_infra"
        category.contains("Tuning", ignoreCase = true) -> "art_tuning"
        category.contains("Generative", ignoreCase = true) -> "art_genai"
        else -> "art_app"
    }

    private fun resourceUri(context: Context, name: String, type: String): Uri? {
        @Suppress("DiscouragedApi") // names are data-driven by design
        val resId = context.resources.getIdentifier(name, type, context.packageName)
        return if (resId != 0) {
            Uri.parse("android.resource://${context.packageName}/$resId")
        } else {
            null
        }
    }
}
