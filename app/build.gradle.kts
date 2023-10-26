plugins {
    alias(libs.plugins.android.app)
    alias(libs.plugins.kotlin.android)
}

android {
    defaultConfig {
        minSdk = 21
        compileSdk = 34
        targetSdk = 34
        applicationId = "com.example.piano_analytics_android"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        viewBinding = true
    }
    namespace = "com.example.pianoanalytics"
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    implementation(project(":piano-analytics"))
    implementation(libs.appcompat)
    implementation(libs.googleAdsId)
    implementation(libs.material)
    implementation(libs.timber)
    implementation(libs.viewBindingProperty)
}
