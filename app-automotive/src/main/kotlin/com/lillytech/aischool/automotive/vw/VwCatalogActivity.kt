package com.lillytech.aischool.automotive.vw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import com.lillytech.aischool.core.model.SeedSyllabus
import com.lillytech.aischool.core.model.toAutomotiveSafeSyllabus

/**
 * Standalone VW-style catalog preview. Not part of the driver-facing media
 * flow (which is rendered by the OEM Media Center from [com.lillytech.aischool
 * .automotive.AISchoolMediaService]); this Activity exists to demonstrate the
 * VW-styled design direction on a real screen. Launch it directly, e.g.:
 *
 *   adb shell am start -n com.lillytech.aischool.automotive/.vw.VwCatalogActivity
 */
class VwCatalogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Audio-safe catalog (same sanitization the vehicle receives).
        val courses = SeedSyllabus.courses.toAutomotiveSafeSyllabus()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                VwApp(courses = courses)
            }
        }
    }
}
