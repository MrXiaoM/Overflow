plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

setupMavenCentralPublication {
    artifact(tasks.shadowJar)
    artifact(tasks.kotlinSourcesJar)
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    api(project(":overflow-core-api"))
    api(project(":overflow-core"))
    testCompileOnly("net.mamoe:mirai-core-api:2.16.0")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

tasks {
    shadowJar {
        mapOf(
            "com.google.gson" to "gson",
            "org.java_websocket" to "websocket",
            "io.netty" to "netty"
        ).forEach { (original, target) ->
            relocate(original, "top.mrxiaom.overflow.internal.deps.$target")
        }
    }
}
