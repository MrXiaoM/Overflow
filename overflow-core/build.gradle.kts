import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    id("com.github.gmazzo.buildconfig")
}

val miraiVersion = rootProject.ext["miraiVersion"].toString()

buildConfig {
    className("BuildConstants")
    packageName("${project.group}.${rootProject.name.toLowerCase()}")
    useKotlinOutput()

    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("String", "MIRAI_VERSION", "\"$miraiVersion\"")
}

dependencies {
    implementation("net.mamoe:mirai-core-api:$miraiVersion")
    implementation("net.mamoe:mirai-core-utils:$miraiVersion")

    fun netty(s: String): Dependency? = implementation("io.netty:netty-$s:4.1.90.Final")
    netty("codec-http")
    netty("codec-socks")
    netty("transport")

    implementation(project(":onebot-sdk"))
    implementation(project(":onebot-client"))
    implementation("org.java-websocket:Java-WebSocket:1.5.4")

    testImplementation("net.mamoe:mirai-console:$miraiVersion")
    testImplementation("net.mamoe:mirai-console-terminal:$miraiVersion")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "8"
    }
}
