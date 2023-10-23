plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.moshiIR)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.mavenRelease)
}

@Suppress("PropertyName")
val GROUP: String by project

@Suppress("PropertyName")
val VERSION_NAME: String by project

group = GROUP
version = VERSION_NAME

android {
    defaultConfig {
        minSdk = 21
        compileSdk = 33
        buildConfigField("String", "SDK_VERSION", """"${project.version}"""")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    namespace = "io.piano.android.analytics"
}

kotlin {
    explicitApi()
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

ktlint {
    android = true
    version = "1.0.1"
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
    testImplementation(libs.okhttpMockServer)
}
