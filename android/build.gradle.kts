// Root build file - plugin versions are resolved from gradle/libs.versions.toml.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
}

// Android Studio (the AGP 9 generation) requests
// :<module>:prepareKotlinBuildScriptModel on every subproject during sync, but
// Gradle registers that Kotlin-DSL build-script-model task only on the root
// project. Register a harmless no-op on each subproject so the IDE's task
// lookup succeeds and Gradle sync completes. No effect on the command-line build.
subprojects {
    if (tasks.findByName("prepareKotlinBuildScriptModel") == null) {
        tasks.register("prepareKotlinBuildScriptModel")
    }
}
