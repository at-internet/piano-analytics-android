plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.moshiIR)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.mavenRelease)
}

android {
    defaultConfig {
        minSdk = 21
        compileSdk = 33
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
    namespace = "io.piano.analytics"
}

ktlint {
    version.set("0.45.2")
    android.set(true)
}

dependencies {
    compileOnly(libs.googleAdsId)
    compileOnly(libs.huaweiAdsId)
    implementation(libs.lifecycleProcess)
    implementation(libs.timber)
    implementation(libs.okhttp)
    implementation(libs.okhttpLogging)
    implementation(libs.moshi)

    testImplementation(libs.kotlinJunit)
    testImplementation(libs.mockitoKotlin)
    testImplementation(libs.mockitoCore)
    testImplementation(libs.junit)
    testImplementation(libs.androidxTestCore)
}