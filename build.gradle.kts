plugins {
    kotlin("jvm") version "1.8.0" apply false
    kotlin("plugin.serialization") version "1.8.0" apply false
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("com.github.gmazzo.buildconfig") version "3.1.0" apply false
    id("me.him188.kotlin-jvm-blocking-bridge") version "3.0.0-180.1" apply false
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
}
