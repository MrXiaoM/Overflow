@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        if (java.util.Locale.getDefault().country == "CN") {
            extra["mirror.repo"]?.also(::maven)
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        if (java.util.Locale.getDefault().country == "CN") {
            extra["mirror.repo"]?.also(::maven)
        }
        mavenCentral()
    }
}
rootProject.name = "Overflow"

if (System.getProperty("justTasks") == null) {
    include(":overflow-core-api")
    include(":overflow-core")
    include(":overflow-core-all")
}
