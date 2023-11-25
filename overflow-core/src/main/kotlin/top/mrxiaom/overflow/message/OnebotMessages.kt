package top.mrxiaom.overflow.message

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.Overflow
import top.mrxiaom.overflow.message.data.WrappedAudio
import top.mrxiaom.overflow.message.data.WrappedFileMessage
import top.mrxiaom.overflow.message.data.WrappedVideo

/**
 * Json 数组消息 (Onebot) 与 [MessageChain] (mirai) 的序列化与反序列化
 *
 * ## 反序列化未完成项
 * - 音乐分享 (music/[MusicShare])
 */
object OnebotMessages {
    @OptIn(MiraiExperimentalApi::class)
    internal fun registerSerializers() {
        MessageSerializers.registerSerializer(WrappedAudio::class, WrappedAudio.serializer())
        MessageSerializers.registerSerializer(WrappedVideo::class, WrappedVideo.serializer())
        MessageSerializers.registerSerializer(WrappedFileMessage::class, WrappedFileMessage.serializer())
    }

    fun serializeToOneBotJson(message: Message): String {
        return Json.encodeToString(serializeToOneBotJsonArray(message))
    }
    @OptIn(MiraiExperimentalApi::class)
    fun serializeToOneBotJsonArray(message: Message): JsonArray {
        val messageChain = (message as? MessageChain) ?: listOf(message)
        return buildJsonArray {
            for (single in messageChain) {
                addJsonObject {
                    put("type", single.messageType)
                    putJsonObject("data") {
                        when (single) {
                            is PlainText -> put("text", single.content)
                            is Face -> put("id", single.id)
                            is Image -> put("file", single.onebotFile)
                            is FlashImage -> {
                                put("file", single.onebotFile)
                                put("type", "flash")
                            }
                            is Audio -> put("file", single.onebotFile)
                            is ShortVideo -> put("file", single.onebotFile)
                            is At -> put("qq", single.target)
                            is RockPaperScissors -> put("id", single.id) // Onebot 11 不支持自定义石头剪刀布
                            is Dice -> put("id", single.value) // Onebot 11 不支持自定义骰子
                            is PokeMessage -> {
                                put("type", single.pokeType)
                                put("id", single.id)
                            }
                            is MusicShare -> {
                                // 获取不到音乐 id，故统一 custom
                                put("type", "custom")
                                put("url", single.jumpUrl)
                                put("audio", single.musicUrl)
                                put("title", single.title)
                                put("content", single.summary)
                                put("image", single.pictureUrl)
                            }
                            is QuoteReply -> put("id", single.source.ids[0]) // 忽略分片消息情况
                            // is ForwardMessage -> put("id", single.id) // 转发消息有单独的发送方法
                            is LightApp -> put("data", single.content)
                            is ServiceMessage -> put("data", single.content)
                        }
                    }
                }
            }
        }
    }
    fun Message.findForwardMessage(): ForwardMessage? {
        return when(this){
            is ForwardMessage -> this
            is MessageChain -> firstIsInstanceOrNull()
            else -> null
        }
    }
    fun serializeForwardNodes(nodeList: List<ForwardMessage.Node>): List<Map<String, Any>> {
        val nodes = mutableListOf<Map<String, Any>>()
        for (node in nodeList) {
            val map = mutableMapOf<String, Any>("type" to "node")
            val data = mutableMapOf<String, Any>("name" to node.senderName)
            data["content"] = serializeToOneBotJsonArray(node.messageChain)
            map["data"] = data
        }
        return nodes
    }
    suspend fun deserializeFromOneBotJson(bot: Bot, json: com.google.gson.JsonArray, source: MessageSource? = null): MessageChain {
        return deserializeFromOneBotJson(bot, Json.parseToJsonElement(json.toString()).jsonArray, source)
    }
    suspend fun deserializeFromOneBotJson(bot: Bot, jsonString: String, source: MessageSource? = null): MessageChain {
        return deserializeFromOneBotJson(bot, Json.parseToJsonElement(jsonString).jsonArray, source)
    }
    @OptIn(MiraiInternalApi::class, MiraiExperimentalApi::class)
    suspend fun deserializeFromOneBotJson(bot: Bot, json: JsonArray, source: MessageSource? = null): MessageChain {
        return buildMessageChain {
            if (source != null) add(source)

            for (o in json) {
                val obj = o.jsonObject
                val data = obj["data"]?.jsonObject ?: buildJsonObject {  }
                when (obj["type"].string) {
                    "text" -> add(data["text"].string)
                    "face" -> add(Face(data["id"]!!.jsonPrimitive.int))
                    "image" -> {
                        val image = imageFromFile((data["url"] ?: data["file"]).string)
                        if (data["type"].string == "flash") {
                            add(image.flash())
                        } else {
                            add(image)
                        }
                    }
                    "record" -> add(audioFromFile(data["file"].string))
                    "video" -> add(videoFromFile(data["file"].string))
                    "at" -> add(At(data["qq"]!!.jsonPrimitive.long))
                    // TODO "rps" "dice" 无法通过 OneBot 获取其具体值，先搁置
                    "poke" -> add(PokeMessage(
                        data["name"].string,
                        data["type"]!!.jsonPrimitive.int,
                        data["id"]!!.jsonPrimitive.int
                    ))
                    //"music" -> add(MusicShare())
                    "forward" -> {
                        val id = data["id"].string
                        if (id.isNotEmpty()) {
                            val nodes = Mirai.downloadForwardMessage(bot, id)
                            val raw = RawForwardMessage(nodes)
                            add(raw.render(ForwardMessage.DisplayStrategy))
                        }
                    }
                    "xml" -> add(SimpleServiceMessage(60, data["data"].string))
                    "json" -> add(LightApp(data["data"].string))
                }
            }
        }
    }

    private val Message.messageType: String
        get() = when(this) {
            is PlainText -> "text"
            is Face -> "face"
            is Image -> "image"
            is FlashImage -> "image"
            is Audio -> "record"
            is ShortVideo -> "video"
            is At -> "at"
            is RockPaperScissors -> "rps"
            is Dice -> "dice"
            is PokeMessage -> "poke"
            //is ServiceMessage -> "share"
            // 推荐好友/推荐群 contact
            // 位置 location
            is MusicShare -> "music"
            is QuoteReply -> "reply"
            is ForwardMessage -> "forward"
            is LightApp -> "json"
            is ServiceMessage -> "xml"
            else -> "text"
        }
    private fun imageFromFile(file: String): Image = Image.fromId(file)
    private fun audioFromFile(file: String): Audio = WrappedAudio(file)
    private fun videoFromFile(file: String): ShortVideo = WrappedVideo(file)

    private val Image.onebotFile: String
        get() = imageId
    private val FlashImage.onebotFile: String
        get() = image.onebotFile
    private val Audio.onebotFile: String
        get() = (this as? WrappedAudio)?.file ?: ""
    private val ShortVideo.onebotFile: String
        get() = (this as? WrappedVideo)?.file ?: ""
    private val JsonElement?.string
        get() = this?.jsonPrimitive?.content ?: ""
}