package com.lillytech.aischool.automotive.vw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lillytech.aischool.core.model.Course
import com.lillytech.aischool.core.model.SeedSyllabus
import com.lillytech.aischool.core.model.toAutomotiveSafeSyllabus
import com.lillytech.aischool.core.network.AISchoolApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Standalone VW-style catalog preview. Not part of the driver-facing media
 * flow (which is rendered by the OEM Media Center from [com.lillytech.aischool
 * .automotive.AISchoolMediaService]); this Activity demonstrates the VW-styled
 * design direction on a real screen, backed by the same live syllabus feed the
 * Media Center streams (audio-safe, falling back to the bundled seed offline).
 *
 *   adb shell am start -n com.lillytech.aischool.automotive/.vw.VwCatalogActivity
 */
class VwCatalogActivity : ComponentActivity() {
    private val apiClient = AISchoolApiClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
                LaunchedEffect(Unit) {
                    courses = withContext(Dispatchers.IO) {
                        runCatching { apiClient.fetchAutomotiveSafeSyllabus() }
                            .getOrElse { SeedSyllabus.courses.toAutomotiveSafeSyllabus() }
                    }
                }
                VwApp(courses = courses)
            }
        }
    }

    override fun onDestroy() {
        apiClient.close()
        super.onDestroy()
    }
}
