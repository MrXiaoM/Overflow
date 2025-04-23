import java.util.*

plugins {
    java
    `groovy-gradle-plugin`
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

    compileOnly("org.apache.httpcomponents:httpclient:4.5.13")
    compileOnly("org.apache.httpcomponents:httpmime:4.5.13")

    api("org.jetbrains.kotlin", "kotlin-gradle-plugin", prop["kotlin.version"].toString()) {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib")
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
    }
}

gradlePlugin {
    website.set("https://github.com/Karlatemp/maven-central-publish")
    vcsUrl.set("https://github.com/Karlatemp/maven-central-publish")

    testSourceSets(sourceSets.test.get())

    plugins {
        register("maven-publishing") {
            id = "moe.karla.maven-publishing"
            implementationClass = "moe.karla.maven.publishing.MavenPublishingPlugin"

            displayName = "Maven Central Publishing"
            description = "Publishing your software to Maven Central"
            tags.set(listOf("signing", "publishing"))
        }
    }
}
