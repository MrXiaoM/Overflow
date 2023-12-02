plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

setupMavenCentralPublication {
    artifact(tasks.shadowJar)
    artifact(tasks.kotlinSourcesJar)
}

dependencies {
    api(project(":onebot"))
    api(project(":overflow-core-api"))
    api(project(":overflow-core"))
}
