plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

dependencies {
    api(project(":onebot"))
    api(project(":overflow-core"))
}