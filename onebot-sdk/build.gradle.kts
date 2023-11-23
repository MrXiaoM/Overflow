plugins {
    java
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.24")
    compileOnly("com.google.code.gson:gson:2.8.9")
    compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("org.slf4j:slf4j-api:2.0.1")

    annotationProcessor("org.projectlombok:lombok:1.18.24")
    annotationProcessor("com.google.code.gson:gson:2.8.9")
}
