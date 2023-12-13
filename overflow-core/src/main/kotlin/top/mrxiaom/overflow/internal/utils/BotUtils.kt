package top.mrxiaom.overflow.internal.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import net.mamoe.mirai.data.UserProfile
import net.mamoe.mirai.utils.CheckableResponseA
import net.mamoe.mirai.utils.JsonStruct
import net.mamoe.mirai.utils.loadAs
import top.mrxiaom.overflow.internal.Overflow
import top.mrxiaom.overflow.internal.contact.BotWrapper
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


internal val defaultJson: Json = Json {
    isLenient = true
    ignoreUnknownKeys = true
}
@Serializable
data class DigestShare(
    @SerialName("share_key")
    val shareKey: String = ""
)

@Serializable
internal data class DigestData(
    @SerialName("data") val `data`: JsonElement = JsonNull,
    @SerialName("wording") val reason: String = "",
    @SerialName("retmsg") override val errorMessage: String,
    @SerialName("retcode") override val errorCode: Int
) : CheckableResponseA(), JsonStruct

private fun <T> DigestData.loadData(serializer: KSerializer<T>): T {
    return try {
        defaultJson.decodeFromJsonElement(serializer, this.data)
    } catch (cause: Exception) {
        throw IllegalStateException("parse digest data error, status: $errorCode - $errorMessage", cause)
    }
}
internal suspend fun BotWrapper.shareDigest(
    groupCode: Long, msgSeq: Long, msgRandom: Long, targetGroupCode: Long
): DigestShare {
    return httpGet(
        url = "https://qun.qq.com/cgi-bin/group_digest/share_digest",
        cookieDomain = "qun.qq.com",
        params = mapOf(
            "group_code" to groupCode,
            "msg_seq" to msgSeq,
            "msg_random" to msgRandom,
            "target_group_code" to targetGroupCode
        )
    ).loadAs(DigestData.serializer()).loadData(DigestShare.serializer())
}
internal suspend fun BotWrapper.httpGet(
    url: String, cookieDomain: String,
    header: Map<String, Any> = mapOf(),
    params: Map<String, Any> = mapOf()
): String {
    val credentials = impl.getCredentials(cookieDomain).data ?: throw IllegalStateException("credentials is empty")
    val cookie = credentials.cookies
    val bkn = credentials.token
    return withContext(Dispatchers.IO) {
        val paramString = params.map { "${it.key}=${URLEncoder.encode(it.value.toString(), "UTF-8")}" }.joinToString("&")
        val conn = URL("$url?$paramString&bkn=$bkn").openConnection() as HttpURLConnection

        conn.requestMethod = "get"
        conn.addRequestProperty("cookie", cookie)
        for ((key, value) in header) {
            conn.addRequestProperty(key, value.toString())
        }
        conn.connect()
        conn.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
    }
}

internal suspend inline fun <reified T : Any> BotWrapper.queryProfile(
    targetId: Long,
    block: UserProfile.() -> T
): T? {
    return runCatching {
        Overflow.instance.queryProfile(this, targetId).block()
    }.onFailure {
        Overflow.logger.warning("获取用户 $targetId 的资料卡时出现一个异常", it)
    }.getOrNull()
}
