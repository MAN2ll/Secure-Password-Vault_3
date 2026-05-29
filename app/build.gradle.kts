plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.securevault"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.securevault"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        
        // Оставляем фильтры, чтобы APK работал на всех телефонах
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    // Используем стандартную отладочную подпись (она есть на GitHub по умолчанию)
    // Убрали кастомный keystore, чтобы избежать ошибок "файл не найден"
    
    buildTypes {
        debug {
            // Оставляем дефолтную подпись
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false // Для диплома пока не включаем сжатие, чтобы не было ошибок
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
    
    buildFeatures { compose = true }
    
    packaging { 
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } 
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
    debugImplementation(libs.compose.ui.tooling)
    implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")
} 
