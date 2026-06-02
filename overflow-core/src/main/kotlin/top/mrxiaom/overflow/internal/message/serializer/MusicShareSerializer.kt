package top.mrxiaom.overflow.internal.message.serializer

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.internal.message.data.deserializeNeteaseMusic
import top.mrxiaom.overflow.internal.message.data.deserializeQQMusic
import top.mrxiaom.overflow.message.BuildMessageContext

class MusicShareSerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "music"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is MusicShare
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        val id = data["id"].string
        when (data["type"].string) {
            "163" -> context.add(deserializeNeteaseMusic(id))
            "qq" -> context.add(deserializeQQMusic(id))
            "custom" -> {
                context.add(
                    MusicShare(
                        MusicKind.QQMusic,
                        data["title"].string, data["content"].string,
                        data["url"].string, data["image"].string,
                        data["audio"].string
                    )
                )
            }
            else -> return false
        }
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? MusicShare ?: return false
        context.add("music") {
            when (element.kind) {
                MusicKind.NeteaseCloudMusic -> {
                    put("type", "163")
                    put("id", element.jumpUrl.substringAfterLast("id="))
                }
                // MusicKind.QQMusic -> {}
                else -> {
                    put("type", "custom")
                    put("url", element.jumpUrl)
                    put("audio", element.musicUrl)
                    put("title", element.title)
                    put("content", element.summary)
                    put("image", element.pictureUrl)
                }
            }
        }
        return true
    }
}