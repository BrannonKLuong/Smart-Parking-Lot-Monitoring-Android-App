plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    // Apply Kotlin Android plugin with version
    id("org.jetbrains.kotlin.android") version "1.8.21"
}

android {
    namespace      = "com.example.parkingandroid"
    compileSdk     = 35

    defaultConfig {
        applicationId        = "com.example.parkingandroid"
        minSdk               = 24
        targetSdk            = 35
        versionCode          = 1
        versionName          = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Core & AndroidX
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // Use the latest version
    // Compose UI
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.ui:ui:1.5.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.1")
    implementation("androidx.compose.material3:material3:1.2.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.5.1")

    // Networking (Retrofit + Moshi)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    // Firebase Messaging
    implementation("com.google.firebase:firebase-messaging:23.1.0")

    // Background work
    implementation("androidx.work:work-runtime:2.7.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
