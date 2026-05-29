plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {

    // АВТО-СОЗДАНИЕ debug.keystore
    val debugKeystore = rootProject.file("debug.keystore")

    if (!debugKeystore.exists()) {
        exec {
            commandLine(
                "keytool",
                "-genkey",
                "-v",
                "-keystore", debugKeystore.absolutePath,
                "-storepass", "android",
                "-alias", "androiddebugkey",
                "-keypass", "android",
                "-keyalg", "RSA",
                "-keysize", "2048",
                "-validity", "10000",
                "-dname", "CN=Android Debug,O=Android,C=US"
            )
        }
    }

    namespace = "com.securevault"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.securevault"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += listOf(
                "armeabi-v7a",
                "arm64-v8a",
                "x86",
                "x86_64"
            )
        }
    }

    // ПОДПИСЬ APK
    signingConfigs {
        create("debug") {
            storeFile = file("../debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {

        debug {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
        }

        release {
            signingConfig = signingConfigs.getByName("debug")
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
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.core.ktx)

    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.activity.compose)

    implementation(platform(libs.compose.bom))

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)

    implementation(libs.material3)
    implementation(libs.material.icons)

    implementation(libs.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.biometric)

    implementation(libs.security.crypto)

    implementation(libs.coroutines.android)

    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")

    debugImplementation(libs.compose.ui.tooling)
}
