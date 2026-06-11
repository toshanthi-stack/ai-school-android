package com.lillytech.aischool.automotive

import android.content.Context
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.lillytech.aischool.core.model.AiSchoolPillars
import com.lillytech.aischool.core.model.AudioMetadata
import com.lillytech.aischool.core.model.Course
import com.lillytech.aischool.core.model.Lesson
import com.lillytech.aischool.core.model.MediaIds

/**
 * Immutable browse tree over an automotive-sanitized syllabus.
 *
 * Hierarchy (mapped onto the vehicle's native IVI media templates):
 *
 *   root ─ pillar category (browsable, grid)
 *            └─ course (browsable, list)
 *                  └─ lesson (playable; subtitle/description carry only the
 *                             short semantic audioSummary, never raw text)
 */
class BrowseTree(
    private val context: Context,
    private val courses: List<Course>,
) {

    private val coursesByCategory: Map<String, List<Course>> = courses.groupBy { it.category }

    /** Root categories in canonical pillar order, plus any server-added extras. */
    private val orderedCategories: List<String> =
        AiSchoolPillars.ALL.filter { it in coursesByCategory } +
            (coursesByCategory.keys - AiSchoolPillars.ALL.toSet())

    fun childrenOf(parentId: String): MutableList<MediaBrowserCompat.MediaItem> = when {
        parentId == MediaIds.ROOT -> categoryItems()
        parentId == MediaIds.EMPTY_ROOT -> mutableListOf()
        parentId.startsWith(MediaIds.CATEGORY_PREFIX) ->
            courseItems(parentId.removePrefix(MediaIds.CATEGORY_PREFIX))
        parentId.startsWith(MediaIds.COURSE_PREFIX) ->
            lessonItems(parentId.removePrefix(MediaIds.COURSE_PREFIX))
        else -> mutableListOf()
    }

    fun findLesson(mediaId: String): Pair<Course, Lesson>? {
        val lessonId = MediaIds.lessonIdOf(mediaId) ?: return null
        for (course in courses) {
            course.lessons.firstOrNull { it.id == lessonId }?.let { return course to it }
        }
        return null
    }

    /** Lesson [offset] positions away within the same course, or `null`. */
    fun siblingLesson(mediaId: String, offset: Int): Pair<Course, Lesson>? {
        val (course, lesson) = findLesson(mediaId) ?: return null
        val index = course.lessons.indexOfFirst { it.id == lesson.id }
        val sibling = course.lessons.getOrNull(index + offset) ?: return null
        return course to sibling
    }

    private fun categoryItems(): MutableList<MediaBrowserCompat.MediaItem> =
        orderedCategories
            .map { category ->
                val meta = AudioMetadata.forCategory(
                    category = category,
                    courseCount = coursesByCategory.getValue(category).size,
                )
                browsableItem(meta, browseHint = CONTENT_STYLE_GRID)
            }
            .toMutableList()

    private fun courseItems(category: String): MutableList<MediaBrowserCompat.MediaItem> =
        coursesByCategory[category]
            .orEmpty()
            .map { course -> browsableItem(AudioMetadata.forCourse(course)) }
            .toMutableList()

    private fun lessonItems(courseId: String): MutableList<MediaBrowserCompat.MediaItem> {
        val course = courses.firstOrNull { it.id == courseId } ?: return mutableListOf()
        return course.lessons
            .map { lesson -> playableItem(AudioMetadata.forLesson(course, lesson)) }
            .toMutableList()
    }

    private fun browsableItem(
        meta: AudioMetadata,
        browseHint: Int = CONTENT_STYLE_LIST,
    ): MediaBrowserCompat.MediaItem {
        val extras = Bundle().apply {
            putInt(EXTRA_CONTENT_STYLE_BROWSABLE, browseHint)
            putInt(EXTRA_CONTENT_STYLE_PLAYABLE, CONTENT_STYLE_LIST)
        }
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(meta.mediaId)
            .setTitle(meta.title)
            .setSubtitle(meta.subtitle)
            .setDescription(meta.description)
            .setIconUri(ArtworkProvider.forCategory(context, meta.category))
            .setExtras(extras)
            .build()
        return MediaBrowserCompat.MediaItem(
            description,
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE,
        )
    }

    private fun playableItem(meta: AudioMetadata): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setMediaId(meta.mediaId)
            .setTitle(meta.title)
            .setSubtitle(meta.subtitle)
            // Distraction rule: the only long-form text the IVI ever receives
            // is the short semantic audioSummary carried in `description`.
            .setDescription(meta.description)
            .setIconUri(ArtworkProvider.forCategory(context, meta.category))
            .build()
        return MediaBrowserCompat.MediaItem(
            description,
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE,
        )
    }

    companion object {
        // Content-style hints understood by AAOS / Android Auto media templates.
        const val EXTRA_CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
        const val EXTRA_CONTENT_STYLE_BROWSABLE = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
        const val EXTRA_CONTENT_STYLE_PLAYABLE = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
        const val CONTENT_STYLE_LIST = 1
        const val CONTENT_STYLE_GRID = 2
    }
}
