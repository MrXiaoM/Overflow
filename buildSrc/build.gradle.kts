import java.util.*

plugins {
    `kotlin-dsl`
    id("com.github.gmazzo.buildconfig") version "3.1.0"
}
val gradleProperties = rootDir.parentFile
        .resolve("gradle.properties")
val prop = Properties().apply {
    gradleProperties.reader().use(::load)
}
buildConfig {
    className("BuildConstants")
    useKotlinOutput()
    buildConfigField("String", "PROPERTIES_PATH", "\"" + gradleProperties.absolutePath.replace('\\', '/') + "\"")
    buildConfigField("java.io.File", "PROPERTIES_FILE", "java.io.File(PROPERTIES_PATH)")
}
repositories {
    if (Locale.getDefault().country == "CN") {
        prop["mirror.repo"]?.also(::maven)
    }
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api(gradleApi())
    api(gradleKotlinDsl())
    api("com.google.code.gson:gson:2.10.1")

    api("org.jetbrains.kotlin", "kotlin-gradle-plugin", prop["kotlin.version"].toString()) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }
}
