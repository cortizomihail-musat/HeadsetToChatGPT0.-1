plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ro.cortizo.headsettochatgpt"
    compileSdk = 35

    defaultConfig {
        applicationId = "ro.cortizo.headsettochatgpt"
        minSdk = 29
        targetSdk = 35
        versionCode = 5
        versionName = "0.5"
    }
}

kotlin {
    jvmToolchain(17)
}
