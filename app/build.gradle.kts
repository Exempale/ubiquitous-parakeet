plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    dependencies {
        implementation("dev.rikka.shizuku:api:13.1.5")
        implementation("dev.rikka.shizuku:provider:13.1.5")
        coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    }
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

    compileOptions.isCoreLibraryDesugaringEnabled = true

    kotlinOptions {
        jvmTarget = "17"
    }
}
