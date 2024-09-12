import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.LocalDate

fun Project.setupMavenCentralPublication(artifactsBlock: MavenPublication.() -> Unit) {
    apply(plugin = "signing")
    apply(plugin = "maven-publish")

    extensions.configure(PublishingExtension::class) {
        publications {
            create<MavenPublication>("maven") {
                from(components.getByName("kotlin"))
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
            sign(extensions.getByType(PublishingExtension::class).publications.getByName("maven"))
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

private const val repo = "https://s01.oss.sonatype.org"
private const val limitDays: Long = 30

private fun HttpRequest.Builder.applyHeaders(vararg pairs: Pair<String, String>) {
    pairs.toList().plus(listOf(
        "Accept" to "application/json,application/vnd.siesta-error-v1+json,application/vnd.siesta-validation-errors-v1+json",
        "Referer" to "$repo/",
        "UserAgent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0"
    )).toMap().forEach(this::setHeader)
}

private data class Version(
    val text: String,
    val commit: Int,
)
private val versionRegex = Regex("\\.([0-9]+)-")
private fun HttpClient.fetchVersionList(): List<Version> {
    val builder = HttpRequest.newBuilder(URI.create(
        "$repo/service/local/repositories/snapshots/content/top/mrxiaom/mirai/overflow-core/?isLocal&_dc=${System.currentTimeMillis()}"
    ))
    builder.applyHeaders()
    val request = builder.GET().build()
    val response = send(request, BodyHandlers.ofString(Charsets.UTF_8))
    val data = JsonParser.parseString(response.body()).asJsonObject["data"].asJsonArray
    return data.mapNotNull { json ->
        val obj = json as? JsonObject ?: return@mapNotNull null
        val text = obj["text"]?.asString?.takeIf { it.endsWith("-SNAPSHOT") } ?: return@mapNotNull null
        val commit = versionRegex.find(text)?.groupValues?.get(1)?.toIntOrNull() ?: return@mapNotNull null
        return@mapNotNull Version(text, commit)
    }.sortedBy { it.text }
}

fun deleteOutdatedArtifacts(projectDir: File, auth: String) {
    val deadDate = LocalDate.now().minusDays(limitDays)
    val process = ProcessBuilder("git rev-list --count HEAD --until=$deadDate".split(' '))
        .directory(projectDir)
        .redirectErrorStream(true)
        .start()
    val deadTime = process.inputStream.bufferedReader().use { it.readText().trim().toInt() }
    process.waitFor()
    println()
    println("已过时版本号 <= $deadTime")
    val client = HttpClient.newHttpClient()
    println("正在扫描版本列表")
    val versionList = client.fetchVersionList()
    println("当前快照仓库有 ${versionList.size} 个版本")
    val toRemove = versionList.filter { it.commit <= deadTime }
    if (toRemove.isEmpty()) {
        println("没有版本需要清理")
        return
    }
    println("共发现 ${toRemove.size} 个在 $deadDate 前发布的旧版本…")
    runDelete(toRemove)
}

private fun runDelete(toRemove: List<Version>) {
    // TODO: 发送 DELETE 请求返回 401，不了解 OSS 怎么登录，以后再想办法解决
    throw IllegalStateException("以下版本需要删除\n${toRemove.joinToString("\n") { it.text }}")
}
