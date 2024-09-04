pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    
}
rootProject.name = "Overflow"

if (System.getProperty("justTasks") == null) {
    include(":onebot")
    include(":overflow-core-api")
    include(":overflow-core")
    include(":overflow-core-all")
}
