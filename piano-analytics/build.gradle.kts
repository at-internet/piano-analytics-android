plugins {
    signing
    id("com.android.library")
    id("com.vanniktech.maven.publish")
}

android {
    compileSdk = 32

    defaultConfig {

        minSdk = 16
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFile("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    compileOnly("com.google.android.gms:play-services-ads:21.0.0")
    compileOnly("com.huawei.hms:hms-ads-identifier:3.4.26.303")

    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.test:core:1.4.0")
    testImplementation("org.robolectric:robolectric:4.5.1")
}
