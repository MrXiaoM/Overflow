pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    
}
rootProject.name = "overflow"

include(":onebot-sdk")
include(":onebot-client")
include(":overflow-core")
include(":overflow-core-all")