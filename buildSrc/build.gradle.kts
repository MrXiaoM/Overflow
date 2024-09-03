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
    api(gradleKotlinDsl())
    api("com.google.code.gson:gson:2.10.1")
}
