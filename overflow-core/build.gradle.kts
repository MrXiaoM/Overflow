plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("me.him188.kotlin-jvm-blocking-bridge")
    id("org.ajoberstar.grgit")
}

setupMavenCentralPublication {
    artifact(tasks.kotlinSourcesJar)
}

val miraiVersion = extra("miraiVersion") ?: "2.16.0"

dependencies {
    implementation(platform("net.mamoe:mirai-bom:$miraiVersion"))

    compileOnly("net.mamoe:mirai-console")
    implementation("net.mamoe:mirai-core-api")
    implementation("net.mamoe:mirai-core-utils")
    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("me.him188:kotlin-jvm-blocking-bridge-runtime:3.0.0-180.1")

    fun netty(s: String): Dependency? = implementation("io.netty:netty-$s:4.1.90.Final")
    netty("codec-http")
    netty("codec-socks")
    netty("transport")

    api(project(":onebot"))
    api(project(":overflow-core-api"))

    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.java-websocket:Java-WebSocket:1.5.7")
    testImplementation("net.mamoe:mirai-console")
    testImplementation("net.mamoe:mirai-console-terminal")
}

tasks {
    register<JavaExec>("runConsole") {
        mainClass.set("RunConsoleKt")
        workingDir = File(project.projectDir, "run")
        classpath = sourceSets.test.get().runtimeClasspath
        standardInput = System.`in`
    }
}
