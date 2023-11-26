import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension

fun Project.setupMavenCentralPublication(artifactsBlock: MavenPublication.() -> Unit) {
    apply(plugin = "signing")
    apply(plugin = "maven-publish")

    extensions.configure(PublishingExtension::class) {
        publications {
            create<MavenPublication>("mavenRelease") {
                groupId = rootProject.group.toString()
                artifactId = project.name
                version = rootProject.version.toString()

                artifactsBlock()
                pom(mavenPom(artifactId))
            }
        }
    }
    extensions.configure(SigningExtension::class) {
        val signingKey = findProperty("signingKey")?.toString()
        val signingPassword = findProperty("signingPassword")?.toString()
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(extensions.getByType(PublishingExtension::class).publications.getByName("mavenRelease"))
        } else {
            logger.warn("子模块 ${project.name} 未找到签名配置")
        }
    }
}
fun mavenPom(artifactId: String): Action<MavenPom> = action {
    name.set(artifactId)
    description.set("One of the Overflow project modules")
    url.set("https://github.com/MrXiaoM/Overflow")
    licenses {
        license {
            name.set("AGPL-3.0")
            url.set("https://github.com/MrXiaoM/Overflow/blob/master/LICENSE")
        }
    }
    developers {
        developer {
            name.set("MrXiaoM")
            email.set("mrxiaom@qq.com")
        }
    }
    scm {
        url.set("https://github.com/MrXiaoM/Overflow")
        connection.set("scm:git:https://github.com/MrXiaoM/Overflow.git")
        developerConnection.set("scm:git:https://github.com/MrXiaoM/Overflow.git")
    }
}

inline fun <reified T : Any> action(
    crossinline block: T.() -> Unit
): Action<T> = Action<T> { block() }
