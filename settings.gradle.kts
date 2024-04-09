pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://developer.huawei.com/repo/")
    }
}

plugins {
    id("com.gradle.develocity") version "3.17"
}
include(
    ":app",
    ":piano-analytics"
)
rootProject.name = "piano-analytics"
