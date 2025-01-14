plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 33
    buildToolsVersion = "33.0.0"
    namespace = "com.xposed.miuiime"

    defaultConfig {
        applicationId = "com.xposed.miuiime"
        minSdk = 28
        targetSdk = 33
        versionCode = 11
        versionName = "1.13"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
        }
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packagingOptions {
        resources {
            excludes += arrayOf("META-INF/**", "kotlin/**", "google/**", "**.bin")
        }
    }
    applicationVariants.all {
        val outputFileName = "Unlock_MIUI_IME-${versionName}_${buildType.name}.apk"
        outputs.all {
            val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output?.outputFileName = outputFileName
        }
    }
    dependenciesInfo {
        includeInApk = false
    }
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    compileOnly("de.robv.android.xposed:api:82")
    implementation("com.github.kyuubiran:EzXHelper:1.0.3")
}
