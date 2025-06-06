plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

java.withJavadocJar() // Dummy javadoc
setupMavenCentralPublication {
    artifact(tasks.shadowJar)
    artifact(tasks.kotlinSourcesJar)
    artifact(tasks.getByName("javadocJar"))
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    api(project(":overflow-core-api"))
    api(project(":overflow-core"))
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
