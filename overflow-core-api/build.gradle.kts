plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("me.him188.kotlin-jvm-blocking-bridge")
}

setupMavenCentralPublication {
    artifact(tasks.kotlinSourcesJar)
}

val miraiVersion = rootProject.ext["miraiVersion"].toString()

dependencies {
    implementation(platform("net.mamoe:mirai-bom:$miraiVersion"))

    implementation("net.mamoe:mirai-core-api")
    implementation("net.mamoe:mirai-core-utils")

    implementation("me.him188:kotlin-jvm-blocking-bridge-runtime:3.0.0-180.1")
}
