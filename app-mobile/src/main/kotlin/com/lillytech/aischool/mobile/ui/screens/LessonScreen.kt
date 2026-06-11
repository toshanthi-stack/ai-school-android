package com.lillytech.aischool.mobile.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.lillytech.aischool.core.model.Course
import com.lillytech.aischool.core.model.Lesson
import com.lillytech.aischool.mobile.R
import com.lillytech.aischool.mobile.ui.player.rememberLessonAudioController
import com.lillytech.aischool.mobile.ui.theme.BrandPrimary
import com.lillytech.aischool.mobile.ui.theme.BrandSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    course: Course,
    lesson: Lesson,
    onBack: () -> Unit,
) {
    val audio = rememberLessonAudioController(lesson)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(lesson.title, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        bottomBar = {
            PlayerBar(course = course, lesson = lesson, audio = audio)
        },
    ) { padding ->
        if (lesson.hasVisualPayload) {
            // Interactive sandbox from the production site, with the site's own
            // global chrome (nav, sidebar, cookie banner) stripped so only the
            // lesson content shows.
            LessonWebView(
                url = checkNotNull(lesson.visualContentUrl),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            AudioLessonBody(
                course = course,
                lesson = lesson,
                isPlaying = audio.isPlaying,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

/** Centered, branded layout for audio-only lessons, no empty dead space. */
@Composable
private fun AudioLessonBody(
    course: Course,
    lesson: Lesson,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        // Brand-gradient artwork tile with the audio glyph.
        Box(
            modifier = Modifier
                .fillMaxWidth(0.66f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.linearGradient(listOf(BrandPrimary, BrandSecondary))),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.GraphicEq,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(72.dp),
            )
        }
        Spacer(Modifier.height(28.dp))
        Text(
            course.category.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            lesson.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "${course.title} · ${lesson.durationSeconds / 60} min",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            lesson.audioSummary,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
    }
}

/** Bottom transport bar, insets for the system navigation bar. */
@Composable
private fun PlayerBar(
    course: Course,
    lesson: Lesson,
    audio: com.lillytech.aischool.mobile.ui.player.LessonAudioController,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // keep controls above the system nav bar
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (audio.isBuffering) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else {
                FilledIconButton(
                    onClick = audio::toggle,
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(
                        imageVector = if (audio.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (audio.isPlaying) {
                            stringResource(R.string.pause_lesson)
                        } else {
                            stringResource(R.string.play_lesson)
                        },
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    lesson.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    "${course.title} · ${lesson.durationSeconds / 60} min",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LessonWebView(url: String, modifier: Modifier = Modifier) {
    // CSS injected once the page loads: hide the site's global chrome and any
    // cookie/consent banner so only the lesson content is presented in-app.
    val cleanupCss = """
        (function() {
          var css = `
            header, nav, .navbar, .sidebar, aside, footer,
            [class*="cookie"], [id*="cookie"], [class*="consent"], [id*="consent"],
            [class*="banner"], [class*="newsletter"], .ad, [class*="ads"] { display:none !important; }
            html, body { background:#13131A !important; color:#E4E4E7 !important; margin:0 !important; }
            main, .content, .main, .container, article {
              margin:0 !important; padding:16px !important; width:100% !important; max-width:100% !important; }
          `;
          var s = document.createElement('style'); s.innerHTML = css; document.head.appendChild(s);
        })();
    """.trimIndent()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = true
                setBackgroundColor(0xFF13131A.toInt())
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, finishedUrl: String) {
                        view.evaluateJavascript(cleanupCss, null)
                    }
                }
                loadUrl(url)
            }
        },
        update = { webView ->
            if (webView.url != url) webView.loadUrl(url)
        },
        onRelease = { webView ->
            // Tear down the WebView so its JS context is not leaked when the
            // lesson is left.
            webView.stopLoading()
            webView.destroy()
        },
    )
}
