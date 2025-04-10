plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    id("me.him188.kotlin-jvm-blocking-bridge")
    id("org.ajoberstar.grgit")
}

kotlin {
    optInForAllSourceSets("net.mamoe.mirai.utils.MiraiExperimentalApi")
    optInForAllSourceSets("net.mamoe.mirai.utils.MiraiInternalApi")
    optInForAllSourceSets("net.mamoe.mirai.LowLevelApi")
    optInForAllSourceSets("net.mamoe.mirai.console.ConsoleFrontEndImplementation")
    optInForAllSourceSets("net.mamoe.mirai.console.util.ConsoleExperimentalApi")
}

tasks.register<Jar>("dokkaJavadocJar") {
    group = "documentation"
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

setupMavenCentralPublication {
    artifact(tasks.kotlinSourcesJar)
    artifact(tasks.getByName("dokkaJavadocJar"))
}

val miraiVersion = extra("miraiVersion") ?: "2.16.0"

dependencies {
    compileOnly("net.mamoe:mirai-console:$miraiVersion")
    api("net.mamoe:mirai-core-api:$miraiVersion")
    api("net.mamoe:mirai-core-utils:$miraiVersion")
    api("com.google.code.gson:gson:2.10.1")
    api("org.slf4j:slf4j-api:2.0.5")
    api("org.java-websocket:Java-WebSocket:1.5.7")
    api("com.google.code.gson:gson:2.10.1")
    api("me.him188:kotlin-jvm-blocking-bridge-runtime:3.0.0-180.1")

    fun netty(s: String): Dependency? = api("io.netty:netty-$s:4.1.90.Final")
    netty("codec-http")
    netty("codec-socks")
    netty("transport")

    api(project(":overflow-core-api"))

    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.java-websocket:Java-WebSocket:1.5.7")
    testImplementation("net.mamoe:mirai-console:$miraiVersion")
    testImplementation("net.mamoe:mirai-console-terminal:$miraiVersion")
    testImplementation("ch.qos.logback:logback-classic:1.4.14")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    register<JavaExec>("runConsole") {
        mainClass.set("RunConsoleKt")
        workingDir = File(project.projectDir, "run")
        classpath = sourceSets.test.get().runtimeClasspath.filter {
            val path = it.absolutePath.replace("\\", "/")
            !path.contains("ch.qos.logback/logback-")
        }
        standardInput = System.`in`
    }
}
