import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0" apply false
    kotlin("plugin.serialization") version "1.8.0" apply false
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("com.github.gmazzo.buildconfig") version "3.1.0" apply false
    id("me.him188.kotlin-jvm-blocking-bridge") version "3.0.0-180.1" apply false
    id("org.ajoberstar.grgit") version "3.0.0" apply false

    signing
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "top.mrxiaom"

val miraiVersion = "2.16.0"
rootProject.ext["miraiVersion"] = miraiVersion
version = miraiVersion

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
