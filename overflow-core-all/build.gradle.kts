plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    api(project(":onebot-sdk"))
    api(project(":onebot-client"))
    api(project(":overflow-core"))
}