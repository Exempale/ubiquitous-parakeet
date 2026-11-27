plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "dev.limbus.fpsunlocker"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.limbus.fpsunlocker"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    // Keep Java and Kotlin bytecode targets aligned with the JDK 17 build runner.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
