import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.collections.*

plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.github.gmazzo.buildconfig") version "3.1.0"
}

group = "top.mrxiaom"
val miraiVersion = "2.16.0-RC"
val overflowVersion = "0001"
version = "$miraiVersion.$overflowVersion"

buildConfig {
    className("BuildConstants")
    packageName("${project.group}.${rootProject.name}")
    useKotlinOutput()

    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("String", "MIRAI_VERSION", "\"$miraiVersion\"")
}
allprojects {
    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenCentral()
    }
}

dependencies {
    implementation("net.mamoe:mirai-core-api:$miraiVersion")
    implementation("net.mamoe:mirai-core-utils:$miraiVersion")

    fun netty(s: String): Dependency? = implementation("io.netty:netty-$s:4.1.90.Final")
    netty("codec-http")
    netty("codec-socks")
    netty("transport")

    implementation(project(":onebot-client"))

    testImplementation("net.mamoe:mirai-console:$miraiVersion")
    testImplementation("net.mamoe:mirai-console-terminal:$miraiVersion")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mapOf(
            "io.netty" to "netty",
            "org.intellij.lang.annotations" to "annotations.intellij",
            "org.jetbrains.annotations" to "annotations.jetbrains"
        ).forEach {
            relocate(it.key, "${project.group}.${rootProject.name}.libs.${it.value}")
        }
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}
