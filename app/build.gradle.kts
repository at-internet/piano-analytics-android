plugins {
    alias(libs.plugins.android.app)
    alias(libs.plugins.kotlin.android)
}

android {
    defaultConfig {
        minSdk = 21
        compileSdk = 33
        targetSdk = 33
        applicationId = "com.example.piano_analytics_android"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

dependencies {
    implementation(project(":piano-analytics"))
    implementation(libs.appcompat)
    implementation(libs.googleAdsId)
    implementation(libs.material)
    implementation(libs.timber)
    implementation(libs.viewBindingProperty)
}
