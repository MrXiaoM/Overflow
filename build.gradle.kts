@file:Suppress("INVISIBLE_MEMBER")
import org.ajoberstar.grgit.Grgit
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0" apply false
    kotlin("plugin.serialization") version "1.8.0" apply false
    id("org.jetbrains.dokka") version "1.8.10" apply false
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("com.github.gmazzo.buildconfig") version "3.1.0" apply false
    id("me.him188.kotlin-jvm-blocking-bridge") version "3.0.0-180.1" apply false
    id("org.ajoberstar.grgit") version "5.2.2" apply false

    signing
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}

Helper.rootProj = rootProject
group = "top.mrxiaom.mirai"

val overflowVersion = "1.0.2".ext("overflowVersion")
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

println("Mirai version: $miraiVersion")
println("Overflow version: $overflowVersion")
println("Commit: $commit")
println("Version: $version")

allprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }

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
tasks.register("deleteOutdatedArtifacts") {
    group = "publishing"
    val auth = findProperty("MAVEN_AUTHORIZATION")?.toString()
    if (auth == null) {
        println("OSS authorization not found, skipping delete outdated artifacts")
    } else {
        deleteOutdatedArtifacts(rootProject.projectDir, auth)
    }
}
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(findProperty("MAVEN_USERNAME")?.toString())
            password.set(findProperty("MAVEN_PASSWORD")?.toString())
        }
    }
}
