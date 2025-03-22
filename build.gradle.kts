// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.8.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    // Optional: Add any other plugin definitions here
    //KSP is used by room
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler) apply false
}
