plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // No, do not use libs.plugins.kotlin.compose, that is not a real plugin.
    // Instead use the following:
    id("org.jetbrains.kotlin.plugin.compose")
    // ADD THIS PLUGIN for JSON parsing
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
}

android {
    namespace = "com.example.project_dex"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.project_dex"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Image loading library
    implementation("io.coil-kt:coil-compose:2.7.0")
    // Updated Networking Dependencies
    implementation("com.codepath.libraries:asynchttpclient:2.2.0") // Use the new library
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") // Keep for JSON parsing

    // REMOVED these lines:
    // implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    // END: Updated Networking Dependencies

    // For ViewModel lifecycle in Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
