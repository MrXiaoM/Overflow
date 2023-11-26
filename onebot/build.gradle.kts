plugins {
    kotlin("jvm")
    id("me.him188.kotlin-jvm-blocking-bridge")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

setupMavenCentralPublication {
    artifact(tasks.jar)
    artifact(tasks.kotlinSourcesJar)
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.24")
    compileOnly("com.google.code.gson:gson:2.8.9")
    compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("org.slf4j:slf4j-api:2.0.1")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    implementation("me.him188:kotlin-jvm-blocking-bridge-runtime:3.0.0-180.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.4")

    annotationProcessor("org.java-websocket:Java-WebSocket:1.5.4")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    testCompileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
}
