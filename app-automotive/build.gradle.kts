plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.lillytech.aischool.automotive"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lillytech.aischool.automotive"
        minSdk = 29 // Android Automotive OS baseline
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        // PUBLIC AOSP platform *test* key — matches the test-keys emulator
        // images, so signature-level car permissions (CONTROL_CAR_WINDOWS)
        // are granted and the VHAL window-pause demo runs on the emulator.
        // Never use for production; OEM builds use the OEM platform key.
        create("aospPlatform") {
            storeFile = file("keystore/aosp_platform.p12")
            storePassword = "android"
            keyAlias = "platform"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("aospPlatform")
        }
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Kotlin 2.2 compilerOptions DSL (kotlinOptions is deprecated)
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

android {

    // Compile against the android.car stubs that ship with the platform SDK
    // (platforms/android-36/optional/android.car.jar). At runtime the library
    // is provided by the AAOS framework — see <uses-library> in the manifest.
    useLibrary("android.car")

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:demoaudio"))

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // MediaBrowserServiceCompat / MediaSessionCompat
    implementation(libs.androidx.media)

    // Compose — VW-style catalog "design preview" screen
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
