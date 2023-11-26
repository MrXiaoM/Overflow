plugins {
    kotlin("jvm")
    id("me.him188.kotlin-jvm-blocking-bridge")
}

setupMavenCentralPublication {
    artifact(tasks.jar)
    artifact(tasks.kotlinSourcesJar)
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.26")
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("org.slf4j:slf4j-api:2.0.5")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    implementation("me.him188:kotlin-jvm-blocking-bridge-runtime:3.0.0-180.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.4")

    annotationProcessor("org.java-websocket:Java-WebSocket:1.5.4")
    annotationProcessor("org.projectlombok:lombok:1.18.26")

    testCompileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
}
