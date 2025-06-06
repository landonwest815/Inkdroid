import org.gradle.kotlin.dsl.android
import org.gradle.kotlin.dsl.dependencies

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.drawingappall"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.drawingappall"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
                abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    // Jetpack Compose Core
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.runtime)

    // Jetpack Compose Material
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material)

    // Jetpack Compose Navigation
    implementation(libs.androidx.compose.navigation)

    // Jetpack Compose Lifecycle
    implementation(libs.androidx.compose.lifecycle.viewmodel)
    implementation(libs.androidx.compose.lifecycle.livedata)

    // Activity Compose Support
    implementation(libs.androidx.compose.activity)

    implementation("androidx.compose.ui:ui:1.5.0")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.runtime.testing)
    implementation(libs.androidx.ui.android)
    implementation(libs.androidx.ui.test.junit4.android)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //to get livedata + viewmodel stuff
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    //Fragment stuff
    implementation(libs.androidx.fragment.ktx)
    debugImplementation(libs.androidx.ui.tooling)
    //testImplementation(libs.mockito.kotlin)

    //ROOM STUFF
    implementation(libs.androidx.room.common)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    //Image Preview
    implementation("io.coil-kt:coil-compose:2.1.0")
    testImplementation(kotlin("test"))

    //KTOR dependencies
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.android)
    implementation(libs.kotlinx.serialization.json)
    implementation("io.ktor:ktor-client-core:2.3.3")
    implementation("io.ktor:ktor-client-okhttp:2.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("io.ktor:ktor-client-logging:2.3.3")
}