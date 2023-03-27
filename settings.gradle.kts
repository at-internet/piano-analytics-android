pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://developer.huawei.com/repo/")
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

include(":app", ":piano-analytics")
rootProject.name = "piano-analytics"