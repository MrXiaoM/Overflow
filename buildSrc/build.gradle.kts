import java.util.*

plugins {
    `kotlin-dsl`
}

repositories {
    if (Locale.getDefault().country == "CN") {
        Properties().apply {
            rootDir.parentFile
                .resolve("gradle.properties")
                .reader().use(::load)
        }["mirror.repo"]?.also(::maven)
    }
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
    api("com.google.code.gson:gson:2.10.1")
}
