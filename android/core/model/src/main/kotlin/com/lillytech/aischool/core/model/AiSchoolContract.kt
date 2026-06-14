package com.lillytech.aischool.core.model

/**
 * Endpoints of the live AI School production ecosystem.
 *
 * The published catalog page is [INDEX_PAGE]; structured syllabus data is
 * served from [SYLLABUS_JSON] alongside it. Per-lesson media follows the
 * [AUDIO_BASE]/[LESSON_BASE] conventions.
 */
object AiSchoolEndpoints {
    const val BASE_URL = "https://www.lillytechsystems.com/ai-school/"
    const val INDEX_PAGE = BASE_URL + "index.html"

    // Content feed (syllabus + audio) hosted on GitHub Pages. Lessons carry
    // absolute audio URLs into this host; the website links stay on BASE_URL.
    const val FEED_BASE = "https://toshanthi-stack.github.io/ai-school-feed/"
    const val SYLLABUS_JSON = FEED_BASE + "syllabus.json"
    const val AUDIO_BASE = FEED_BASE + "audio/"
    const val LESSON_BASE = BASE_URL + "lessons/"
}

/**
 * The top-level pillars of the AI School syllabus. These map 1:1 to the
 * root-level browse categories shown on the vehicle's native IVI template.
 */
object AiSchoolPillars {
    const val GENERATIVE_AI = "Generative AI Skills"
    const val INFRASTRUCTURE = "AI Infrastructure & Hardware"
    const val ADVANCED_TUNING = "Advanced LLM Tuning"

    val ALL: List<String> = listOf(GENERATIVE_AI, INFRASTRUCTURE, ADVANCED_TUNING)
}

/**
 * Stable media-id scheme shared between the browse tree, the media session
 * metadata, and `onPlayFromMediaId` dispatch. Keeping it in `:core:model`
 * guarantees both app flavors and the service agree on the wire format.
 */
object MediaIds {
    const val ROOT = "aischool_root"
    const val EMPTY_ROOT = "aischool_empty_root"

    const val CATEGORY_PREFIX = "category/"
    const val COURSE_PREFIX = "course/"
    const val LESSON_PREFIX = "lesson/"

    fun forCategory(category: String): String = CATEGORY_PREFIX + category
    fun forCourse(courseId: String): String = COURSE_PREFIX + courseId
    fun forLesson(lessonId: String): String = LESSON_PREFIX + lessonId

    fun categoryOf(mediaId: String): String? =
        mediaId.takeIf { it.startsWith(CATEGORY_PREFIX) }?.removePrefix(CATEGORY_PREFIX)

    fun courseIdOf(mediaId: String): String? =
        mediaId.takeIf { it.startsWith(COURSE_PREFIX) }?.removePrefix(COURSE_PREFIX)

    fun lessonIdOf(mediaId: String): String? =
        mediaId.takeIf { it.startsWith(LESSON_PREFIX) }?.removePrefix(LESSON_PREFIX)
}
