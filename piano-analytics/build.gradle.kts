plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.mavenRelease)
}

android {
    defaultConfig {
        minSdk = 19
        compileSdk = 33
        targetSdk = 33
        buildConfigField("String", "SDK_VERSION", """"${project.version}"""")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

ktlint {
    version.set("0.45.2")
    android.set(true)
}

dependencies {
    compileOnly(libs.googleAdsId)
    compileOnly(libs.huaweiAdsId)

    testImplementation(libs.junit)
    testImplementation(libs.androidxTestCore)
    testImplementation(libs.robolectric)
}
