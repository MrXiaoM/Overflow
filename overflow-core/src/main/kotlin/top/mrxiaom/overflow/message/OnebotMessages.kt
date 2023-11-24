package top.mrxiaom.overflow.message

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.message.data.WrappedAudio
import top.mrxiaom.overflow.message.data.WrappedVideo

/**
 * Json 数组消息 (Onebot) 与 [MessageChain] (mirai) 的序列化与反序列化
 *
 * ## 反序列化未完成项
 * - 音乐分享 (music/[MusicShare])
 * - 转发消息 (forward/[ForwardMessage])
 */
object OnebotMessages {
    @OptIn(MiraiExperimentalApi::class)
    internal fun registerSerializers() {
        MessageSerializers.registerSerializer(WrappedAudio::class, WrappedAudio.serializer())
        MessageSerializers.registerSerializer(WrappedVideo::class, WrappedVideo.serializer())
    }

    @OptIn(MiraiExperimentalApi::class)
    fun serializeToOneBotJson(message: Message): String {
        val messageChain = (message as? MessageChain) ?: listOf(message)
        return Json.encodeToString(buildJsonArray {
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
                            // is ForwardMessage -> put("id", single.id) // TODO: 支持转发消息
                            is LightApp -> put("data", single.content)
                            is ServiceMessage -> put("data", single.content)
                        }
                    }
                }
            }
        })
    }
    @OptIn(MiraiInternalApi::class, MiraiExperimentalApi::class)
    suspend fun deserializeFromOneBotJson(bot: Bot, jsonString: String, source: MessageSource?): MessageChain {
        val json = Json.parseToJsonElement(jsonString).jsonArray
        return buildMessageChain {
            if (source != null) add(source)

            for (o in json) {
                val obj = o.jsonObject
                val data = obj["data"]?.jsonObject ?: buildJsonObject {  }
                when (obj["type"].toString()) {
                    "text" -> add(data["text"].toString())
                    "face" -> add(Face(data["id"]!!.jsonPrimitive.int))
                    "image" -> {
                        val image = imageFromFile(data["file"].toString())
                        if (data["type"].toString() == "flash") {
                            add(image.flash())
                        } else {
                            add(image)
                        }
                    }
                    "record" -> add(audioFromFile(data["file"].toString()))
                    "video" -> add(videoFromFile(data["file"].toString()))
                    "at" -> add(At(data["qq"]!!.jsonPrimitive.long))
                    // TODO "rps" "dice" 无法通过 OneBot 获取其具体值，先搁置
                    "poke" -> add(PokeMessage(
                        data["name"].toString(),
                        data["type"]!!.jsonPrimitive.int,
                        data["id"]!!.jsonPrimitive.int
                    ))
                    //"music" -> add(MusicShare())
                    "forward" -> {
                        val id = data["id"]!!.toString()
                        val nodes = Mirai.downloadForwardMessage(bot, id)

                        TODO("解析转发消息")
                    }
                    "xml" -> add(SimpleServiceMessage(60, data["data"].toString()))
                    "json" -> add(LightApp(data["data"].toString()))
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
    private val ForwardMessage.id: Long
        get() {
            TODO("获取转发消息ID")
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

}