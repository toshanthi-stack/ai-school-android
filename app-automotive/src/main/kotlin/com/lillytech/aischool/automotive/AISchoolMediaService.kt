package com.lillytech.aischool.automotive

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.content.getSystemService
import androidx.media.MediaBrowserServiceCompat
import com.lillytech.aischool.core.model.AudioMetadata
import com.lillytech.aischool.core.model.Course
import com.lillytech.aischool.core.model.Lesson
import com.lillytech.aischool.core.model.MediaIds
import com.lillytech.aischool.core.network.AISchoolApiClient
import com.lillytech.aischool.demoaudio.DemoAudio
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * AI School media browser service for Android Automotive OS.
 *
 * The vehicle's native IVI media template is the only UI: this APK declares
 * no activities and exposes no text-selection, keyboard-input, clipboard, or
 * copy-paste surface of any kind. Content reaching this service has already
 * been sanitized by [AISchoolApiClient.fetchAutomotiveSafeSyllabus]: audio
 * streams plus short semantic summaries only.
 *
 * Cabin-window handling is delegated to [CabinWindowMonitor]: whenever any
 * window moves away from fully-closed, the active lesson is systemically paused
 * via the session's transport controls, and resumes when all windows close.
 */
class AISchoolMediaService : MediaBrowserServiceCompat() {

    private lateinit var session: MediaSessionCompat
    private lateinit var packageValidator: PackageValidator
    private lateinit var cabinWindowMonitor: CabinWindowMonitor

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private val apiClient = AISchoolApiClient()
    private val browseTreeReady = CompletableDeferred<BrowseTree>()
    private var browseTree: BrowseTree? = null

    // ── Playback engine state ───────────────────────────────────────────────
    private var mediaPlayer: MediaPlayer? = null
    private var currentCourse: Course? = null
    private var currentLesson: Lesson? = null
    private var playerPrepared = false

    /**
     * True when playback was paused by a cabin window opening (not by the
     * driver). Gates auto-resume so closing all windows only resumes a lesson
     * the window paused, never one the driver paused by hand. All access is on
     * the main thread (the VHAL callbacks marshal onto [serviceScope]).
     */
    private var pausedByCabinWindow = false

    /**
     * Set just before the window-initiated pause so [onPause] can tell a
     * window pause from a driver pause: a driver pause clears
     * [pausedByCabinWindow] and cancels auto-resume, a window pause does not.
     */
    private var windowInitiatedPause = false

    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { change ->
        when (change) {
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
            -> session.controller.transportControls.pause()
        }
    }

    // ── Lifecycle ───────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()

        audioManager = checkNotNull(getSystemService<AudioManager>())
        packageValidator = PackageValidator(this)

        // Media session + playback state (steering-wheel controls, hardware
        // media keys, and native IVI display taps all route through here).
        session = MediaSessionCompat(this, SESSION_TAG).apply {
            @Suppress("DEPRECATION") // Flags are implicit on modern Android but
            setFlags(                // explicit here per AAOS integration spec.
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setCallback(sessionCallback)
        }
        sessionToken = session.sessionToken
        setPlaybackState(PlaybackStateCompat.STATE_NONE)

        // Real-time VHAL cabin tracking. A window leaving fully-closed triggers
        // a systemic pause, and once every window is closed again the
        // window-paused lesson resumes (a manual pause is never overridden).
        // The VHAL callbacks may arrive off the main thread; marshal onto
        // serviceScope (Main) so media-session and playback state are only ever
        // touched from one thread.
        cabinWindowMonitor = CabinWindowMonitor(
            context = this,
            onCabinWindowOpened = { _, _ ->
                serviceScope.launch {
                    if (mediaPlayer?.isPlaying == true) {
                        pausedByCabinWindow = true
                        windowInitiatedPause = true
                        session.controller.transportControls.pause()
                    }
                }
            },
            onAllWindowsClosed = {
                serviceScope.launch {
                    if (pausedByCabinWindow) {
                        pausedByCabinWindow = false
                        session.controller.transportControls.play()
                    }
                }
            },
        )
        cabinWindowMonitor.start()

        loadSyllabus()
    }

    private fun loadSyllabus() {
        serviceScope.launch {
            val courses = withContext(Dispatchers.IO) {
                apiClient.fetchAutomotiveSafeSyllabus()
            }
            val tree = BrowseTree(this@AISchoolMediaService, courses)
            browseTree = tree
            if (!browseTreeReady.isCompleted) browseTreeReady.complete(tree)
            notifyChildrenChanged(MediaIds.ROOT)
            Log.i(TAG, "Automotive-safe syllabus loaded: ${courses.size} courses.")
        }
    }

    override fun onDestroy() {
        // Lifecycle hook: unregister the VHAL callback and disconnect
        // from the car service, no leaked callbacks, no idle IVI compute.
        cabinWindowMonitor.stop()

        releasePlayer()
        abandonAudioFocus()

        session.isActive = false
        session.release()

        serviceJob.cancel()
        apiClient.close()
        super.onDestroy()
    }

    // ── Browse tree (MediaBrowserServiceCompat) ─────────────────────────────

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot? {
        // Package validation: only trusted, distraction-optimized system
        // surfaces may browse. Unknown callers are refused outright.
        if (!packageValidator.isKnownCaller(clientPackageName, clientUid)) {
            return null
        }
        val extras = Bundle().apply {
            putBoolean(BrowseTree.EXTRA_CONTENT_STYLE_SUPPORTED, true)
            putInt(BrowseTree.EXTRA_CONTENT_STYLE_BROWSABLE, BrowseTree.CONTENT_STYLE_GRID)
            putInt(BrowseTree.EXTRA_CONTENT_STYLE_PLAYABLE, BrowseTree.CONTENT_STYLE_LIST)
        }
        return BrowserRoot(MediaIds.ROOT, extras)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        // The syllabus may still be loading on a cold start; answer async.
        result.detach()
        serviceScope.launch {
            val tree = browseTreeReady.await()
            result.sendResult(tree.childrenOf(parentId))
        }
    }

    // ── Media session callback ──────────────────────────────────────────────

    private val sessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
            serviceScope.launch {
                val tree = browseTreeReady.await()
                val found = tree.findLesson(mediaId)
                if (found == null) {
                    Log.w(TAG, "Unknown mediaId requested: $mediaId")
                    setPlaybackState(PlaybackStateCompat.STATE_ERROR)
                    return@launch
                }
                playLesson(found.first, found.second)
            }
        }

        override fun onPlay() {
            // Once playing, there is nothing pending for the window to resume.
            pausedByCabinWindow = false
            windowInitiatedPause = false
            val player = mediaPlayer
            when {
                player != null && playerPrepared && !player.isPlaying -> {
                    if (requestAudioFocus()) {
                        player.start()
                        session.isActive = true
                        setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                    }
                }
                currentLesson != null && currentCourse != null ->
                    playLesson(currentCourse!!, currentLesson!!)
            }
        }

        override fun onPause() {
            // Distinguish a window-initiated pause (keep auto-resume armed) from
            // a driver pause (cancel auto-resume so closing windows won't resume).
            if (windowInitiatedPause) {
                windowInitiatedPause = false
            } else {
                pausedByCabinWindow = false
            }
            val player = mediaPlayer ?: return
            if (playerPrepared && player.isPlaying) {
                player.pause()
                setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }
        }

        override fun onStop() {
            pausedByCabinWindow = false
            windowInitiatedPause = false
            releasePlayer()
            abandonAudioFocus()
            session.isActive = false
            setPlaybackState(PlaybackStateCompat.STATE_STOPPED)
        }

        override fun onSkipToNext() = skip(offset = 1)

        override fun onSkipToPrevious() = skip(offset = -1)

        private fun skip(offset: Int) {
            val lesson = currentLesson ?: return
            val tree = browseTree ?: return
            val sibling = tree.siblingLesson(MediaIds.forLesson(lesson.id), offset) ?: return
            playLesson(sibling.first, sibling.second)
        }
    }

    // ── Playback engine ─────────────────────────────────────────────────────

    /**
     * Stream-first, local-fallback playback: the production stream is tried
     * first; if it fails (offline cabin, missing asset) the bundled
     * narration from `:core:demoaudio` plays instead, so the experience
     * never dead-ends in the vehicle.
     */
    private fun playLesson(course: Course, lesson: Lesson, preferLocal: Boolean = false) {
        if (!requestAudioFocus()) {
            Log.w(TAG, "Audio focus denied; not starting '${lesson.title}'.")
            return
        }

        releasePlayer()
        currentCourse = course
        currentLesson = lesson
        publishMetadata(course, lesson)
        setPlaybackState(PlaybackStateCompat.STATE_BUFFERING)

        val localNarration = DemoAudio.narrationUri(this, lesson.id)

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            setOnPreparedListener { player ->
                playerPrepared = true
                player.start()
                session.isActive = true
                setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
            }
            setOnCompletionListener {
                // Auto-advance through the course; stop at the end.
                val next = browseTree?.siblingLesson(MediaIds.forLesson(lesson.id), 1)
                if (next != null) {
                    playLesson(next.first, next.second)
                } else {
                    setPlaybackState(PlaybackStateCompat.STATE_STOPPED)
                }
            }
            setOnErrorListener { _, what, extra ->
                Log.w(TAG, "MediaPlayer error what=$what extra=$extra for '${lesson.title}'")
                playerPrepared = false
                if (!preferLocal && localNarration != null) {
                    Log.i(TAG, "Stream failed; falling back to bundled narration.")
                    playLesson(course, lesson, preferLocal = true)
                } else {
                    setPlaybackState(PlaybackStateCompat.STATE_ERROR)
                }
                true
            }
            try {
                if (preferLocal && localNarration != null) {
                    setDataSource(this@AISchoolMediaService, localNarration)
                } else {
                    setDataSource(lesson.audioUrl)
                }
                prepareAsync()
            } catch (e: Exception) {
                Log.w(TAG, "Unable to open audio source for '${lesson.title}'", e)
                if (!preferLocal && localNarration != null) {
                    playLesson(course, lesson, preferLocal = true)
                } else {
                    setPlaybackState(PlaybackStateCompat.STATE_ERROR)
                }
            }
        }
    }

    private fun publishMetadata(course: Course, lesson: Lesson) {
        val meta: AudioMetadata = AudioMetadata.forLesson(course, lesson)
        // Branded artwork as a content:// URI served by ArtworkProvider, the
        // form the AAOS Media Center's image loader can actually resolve.
        val artUri = ArtworkProvider.forCategory(this, course.category)
        grantArtworkReadPermission(artUri)
        val artUriString = artUri.toString()
        session.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, meta.mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, meta.title)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, meta.category)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, meta.subtitle)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, meta.title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, meta.subtitle)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, meta.description)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, artUriString)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, artUriString)
                .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, artUriString)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, meta.durationMillis)
                .build()
        )
    }

    /** Grants the system Media Center read access to a branded artwork URI. */
    private fun grantArtworkReadPermission(artUri: android.net.Uri) {
        for (pkg in ARTWORK_READER_PACKAGES) {
            try {
                grantUriPermission(pkg, artUri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {
                // Package may be absent on a given head unit, best effort.
            }
        }
    }

    private fun setPlaybackState(state: Int) {
        val position = if (playerPrepared) {
            mediaPlayer?.currentPosition?.toLong() ?: PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
        } else {
            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
        }
        session.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(SUPPORTED_ACTIONS)
                .setState(state, position, PLAYBACK_SPEED_NORMAL)
                .build()
        )
    }

    private fun releasePlayer() {
        runCatching { mediaPlayer?.release() }
        mediaPlayer = null
        playerPrepared = false
    }

    // ── Audio focus ─────────────────────────────────────────────────────────

    private fun requestAudioFocus(): Boolean {
        val request = audioFocusRequest ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setOnAudioFocusChangeListener(audioFocusListener)
            .build()
            .also { audioFocusRequest = it }
        return audioManager.requestAudioFocus(request) ==
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun abandonAudioFocus() {
        audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        audioFocusRequest = null
    }

    private companion object {
        const val TAG = "AISchoolMediaService"
        const val SESSION_TAG = "AISchoolMediaSession"
        const val PLAYBACK_SPEED_NORMAL = 1.0f

        // System surfaces that render our browse/Now-Playing artwork.
        val ARTWORK_READER_PACKAGES = listOf(
            "com.android.car.media",
            "com.android.systemui",
            "com.android.car.carlauncher",
        )

        val SUPPORTED_ACTIONS: Long =
            PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
    }
}
