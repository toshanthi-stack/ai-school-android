package com.lillytech.aischool.mobile.ui.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.lillytech.aischool.core.model.Lesson
import com.lillytech.aischool.demoaudio.DemoAudio

/**
 * Streaming-audio controller for the mobile lesson screen, backed by the
 * platform [MediaPlayer]. Stream-first, local-fallback: if the production
 * stream fails, the bundled narration from `:core:demoaudio` plays instead.
 * Lifecycle is owned by the composition via [rememberLessonAudioController].
 */
@Stable
class LessonAudioController(
    private val context: Context,
    private val audioUrl: String,
    private val lessonId: String,
) {

    var isPlaying by mutableStateOf(false)
        private set
    var isBuffering by mutableStateOf(false)
        private set

    private var player: MediaPlayer? = null
    private var prepared = false

    fun toggle() {
        when {
            isBuffering -> Unit // ignore taps while preparing
            player == null -> startNew()
            isPlaying -> pause()
            prepared -> resume()
            else -> startNew()
        }
    }

    private fun startNew(preferLocal: Boolean = false) {
        release()
        isBuffering = true
        val localNarration = DemoAudio.narrationUri(context, lessonId)
        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            setOnPreparedListener { mp ->
                this@LessonAudioController.prepared = true
                this@LessonAudioController.isBuffering = false
                mp.start()
                this@LessonAudioController.isPlaying = true
            }
            setOnCompletionListener {
                this@LessonAudioController.isPlaying = false
            }
            setOnErrorListener { _, _, _ ->
                if (!preferLocal && localNarration != null) {
                    startNew(preferLocal = true)
                } else {
                    this@LessonAudioController.isBuffering = false
                    this@LessonAudioController.isPlaying = false
                    this@LessonAudioController.prepared = false
                }
                true
            }
            try {
                if (preferLocal && localNarration != null) {
                    setDataSource(context, localNarration)
                } else {
                    setDataSource(audioUrl)
                }
                prepareAsync()
            } catch (_: Exception) {
                if (!preferLocal && localNarration != null) {
                    startNew(preferLocal = true)
                } else {
                    this@LessonAudioController.isBuffering = false
                }
            }
        }
    }

    private fun pause() {
        runCatching { player?.pause() }
        isPlaying = false
    }

    private fun resume() {
        runCatching { player?.start() }
        isPlaying = true
    }

    fun release() {
        runCatching { player?.release() }
        player = null
        prepared = false
        isPlaying = false
        isBuffering = false
    }
}

@Composable
fun rememberLessonAudioController(lesson: Lesson): LessonAudioController {
    val context = LocalContext.current.applicationContext
    val controller = remember(lesson.id) {
        LessonAudioController(context, lesson.audioUrl, lesson.id)
    }
    DisposableEffect(controller) {
        onDispose { controller.release() }
    }
    return controller
}
