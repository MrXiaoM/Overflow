plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    id("com.github.gmazzo.buildconfig")
    id("me.him188.kotlin-jvm-blocking-bridge")
}

var commitHash = extra("commitHash") ?: "local"
var commitCount: Int? = extra("commitCount")
val miraiVersion = extra("miraiVersion") ?: "2.16.0"

kotlin {
    optInForAllSourceSets("net.mamoe.mirai.utils.MiraiExperimentalApi")
    optInForAllSourceSets("net.mamoe.mirai.utils.MiraiInternalApi")
}

buildConfig {
    className("BuildConstants")
    packageName("top.mrxiaom.overflow")
    useKotlinOutput()
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("String", "MIRAI_VERSION", "\"$miraiVersion\"")
    buildConfigField("String", "COMMIT_HASH", "\"$commitHash\"")
    buildConfigField("kotlin.Int?", "COMMIT_COUNT", commitCount?.toString() ?: "null")
    buildConfigField("java.time.Instant", "BUILD_TIME", "java.time.Instant.ofEpochSecond(${System.currentTimeMillis() / 1000L}L)")
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

dependencies {
    api("net.mamoe:mirai-core-api:$miraiVersion")
    api("net.mamoe:mirai-core-utils:$miraiVersion")

    api("me.him188:kotlin-jvm-blocking-bridge-runtime:3.0.0-180.1")

    api("org.slf4j:slf4j-api:2.0.5")
}
