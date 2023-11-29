plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.gmazzo.buildconfig")
    id("me.him188.kotlin-jvm-blocking-bridge")
}

setupMavenCentralPublication {
    artifact(tasks.jar)
    artifact(tasks.kotlinSourcesJar)
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
    implementation(platform("net.mamoe:mirai-bom:$miraiVersion"))

    compileOnly("net.mamoe:mirai-console")

    fun netty(s: String): Dependency? = implementation("io.netty:netty-$s:4.1.90.Final")
    netty("codec-http")
    netty("codec-socks")
    netty("transport")

    api(project(":onebot"))
    api(project(":overflow-core-api"))

    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.java-websocket:Java-WebSocket:1.5.4")
    testImplementation("net.mamoe:mirai-console")
    testImplementation("net.mamoe:mirai-console-terminal")
}
