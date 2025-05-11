pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        // Android Gradle Plugin
        id("com.android.application") version "8.1.0"
        // Kotlin Android plugin
        id("org.jetbrains.kotlin.android") version "1.8.21"
        // Firebase services Gradle plugin
        id("com.google.gms.google-services") version "4.4.0"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ParkingAndroid"
include(":app")
