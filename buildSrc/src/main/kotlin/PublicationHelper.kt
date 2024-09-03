import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPom
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

private fun HttpClient.login(auth: String): String? {
    val builder = HttpRequest.newBuilder(URI.create(
        "$repo/service/local/authentication/login?_dc=${System.currentTimeMillis()}"
    ))
    builder.applyHeaders(
        "Authorization" to "Basic $auth"
    )
    val request = builder.GET().build()
    val response = send(request, BodyHandlers.ofString(Charsets.UTF_8))
    if (!response.body().replace(" ", "").contains("\"loggedIn\":true")) return null
    return response.headers().allValues("Set-Cookie").firstOrNull { it.startsWith("NXSESSIONID=") }?.run { substringBefore(';') }
}

private data class Version(
    val text: String,
    val time: LocalDateTime
)

private fun HttpClient.fetchVersionList(sessionId: String): List<Version> {
    val builder = HttpRequest.newBuilder(URI.create(
        "$repo/service/local/repositories/snapshots/content/top/mrxiaom/mirai/overflow-core/?isLocal&_dc=${System.currentTimeMillis()}"
    ))
    builder.applyHeaders(
        "Cookie" to "NXSESSIONID=$sessionId"
    )
    val request = builder.GET().build()
    val response = send(request, BodyHandlers.ofString(Charsets.UTF_8))
    val data = JsonParser.parseString(response.body()).asJsonObject["data"].asJsonArray
    val format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return data.mapNotNull { json ->
        val obj = json as? JsonObject ?: return@mapNotNull null
        val text = obj["text"]?.asString?.takeIf { it.endsWith("-SNAPSHOT") } ?: return@mapNotNull null
        val lastModified = obj["lastModified"]?.asString ?: return@mapNotNull null
        val time = LocalDateTime.parse(lastModified.substring(0, 19), format)
        return@mapNotNull Version(text, time)
    }.sortedBy { it.text }
}

private fun HttpClient.deleteArtifact(
    sessionId: String,
    group: String,
    artifact: String,
    version: String
) {
    runCatching {
        val builder = HttpRequest.newBuilder(URI.create(
            "$repo/service/local/repositories/snapshots/content/${group.replace(".", "/")}/$artifact/$version"
        ))
        builder.applyHeaders(
            "Cookie" to "NXSESSIONID=$sessionId",
            "Origin" to repo
        )
        val request = builder.DELETE().build()
        val response = send(request, BodyHandlers.discarding())
        val status = response.statusCode()
        if (status != 200 && status != 204) {
            throw IllegalStateException("删除 $group:$artifact:$version 时出现错误: HTTP $status")
        }
    }.onFailure {
        it.printStackTrace()
    }
}

fun deleteOutdatedArtifacts(auth: String) {
    val client = HttpClient.newHttpClient()
    val sessionId = client.login(auth)
    if (sessionId == null) {
        println("登录失败")
        return
    }
    val versionList = client.fetchVersionList(sessionId)
    println("当前快照仓库有 ${versionList.size} 个版本")
    val deadDate = LocalDate.now().minusDays(limitDays)
    val deadTime = deadDate.atTime(0, 0, 0)
    val toRemove = versionList.filter { it.time.isBefore(deadTime) }
    if (toRemove.isEmpty()) return
    println("正在清理 ${toRemove.size} 个在 $deadDate 前发布的旧版本…")
    val group = "top.mrxiaom.mirai"
    for (v in toRemove) {
        val version = v.text
        println("正在清理 $version (${v.time})")
        client.deleteArtifact(sessionId, group, "onebot", version)
        client.deleteArtifact(sessionId, group, "overflow-core-api", version)
        client.deleteArtifact(sessionId, group, "overflow-core", version)
        client.deleteArtifact(sessionId, group, "overflow-core-all", version)
    }
    println("清理完成")
}
