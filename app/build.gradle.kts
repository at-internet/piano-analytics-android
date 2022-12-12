plugins {
    alias(libs.plugins.android.app)
    alias(libs.plugins.kotlin.android)
}

android {
    defaultConfig {
        minSdk = 19
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
    namespace = "com.example.piano_analytics_android"
}

dependencies {
    implementation(project(":piano-analytics"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.1")
    implementation(libs.appcompat)
    implementation(libs.googleAdsId)
    implementation(libs.material)
//    implementation("org.reflections:reflections:0.10.2")
//    implementation("org.slf4j:slf4j-api:1.7.35")
//    implementation("org.slf4j:slf4j-simple:1.7.35")
}
