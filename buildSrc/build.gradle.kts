plugins {
    `kotlin-dsl`
}

repositories {
    mavenLocal()
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api(gradleApi())
}
