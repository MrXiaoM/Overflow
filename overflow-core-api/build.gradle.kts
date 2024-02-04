plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    id("me.him188.kotlin-jvm-blocking-bridge")
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

val miraiVersion = rootProject.ext["miraiVersion"].toString()

dependencies {
    implementation(platform("net.mamoe:mirai-bom:$miraiVersion"))

    implementation("net.mamoe:mirai-core-api")
    implementation("net.mamoe:mirai-core-utils")

    implementation("me.him188:kotlin-jvm-blocking-bridge-runtime:3.0.0-180.1")

    implementation("org.slf4j:slf4j-api:2.0.5")
}
