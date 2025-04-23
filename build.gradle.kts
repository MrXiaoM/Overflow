@file:Suppress("INVISIBLE_MEMBER")
import org.ajoberstar.grgit.Grgit
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import moe.karla.maven.publishing.MavenPublishingExtension.PublishingType

plugins {
    // kotlin("jvm") moved to `buildSrc/build.gradle.kts`
    kotlin("plugin.serialization") version prop("kotlin.version") apply false
    id("org.jetbrains.dokka") version "1.8.10" apply false
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("com.github.gmazzo.buildconfig") version "3.1.0" apply false
    id("me.him188.kotlin-jvm-blocking-bridge") version "3.0.0-180.1" apply false
    id("org.ajoberstar.grgit") version "5.2.2" apply false

    id("moe.karla.maven-publishing")
}

Helper.proj = rootProject
group = "top.mrxiaom.mirai"

val overflowVersion = "1.0.4".ext("overflowVersion")
val miraiVersion = "2.16.0".ext("miraiVersion")

var commitHash = "local"
var commitCount = 0
if (File(rootProject.projectDir, ".git").exists()) {
    Grgit.open(mapOf("currentDir" to rootProject.projectDir)).use { repo ->
        commitHash = repo.head().abbreviatedId
        val log = repo.log()
        commitCount = log.size
    }
}
commitHash.ext("commitHash")
commitCount.ext("commitCount")
val commit =
    if (commitHash == "local") "9999-local"
    else "$commitCount-${commitHash.substring(0, 7)}"

version = overflowVersion

if (findProperty("IS_SNAPSHOT") == "true") {
    version = "$version.$commit-SNAPSHOT"
}
findProperty("OVERRIDE_VERSION")?.also { version = it }

println("Mirai version: $miraiVersion")
println("Overflow version: $overflowVersion")
println("Commit: $commit")
println("Version: $version")

mavenPublishing {
    publishingType = PublishingType.AUTOMATIC
    url = "https://github.com/MrXiaoM/Overflow"
}

allprojects {
    group = rootProject.group
    version = rootProject.version

    val javaVersion = "1.8"
    tasks {
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = javaVersion
        }
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
    }
}
