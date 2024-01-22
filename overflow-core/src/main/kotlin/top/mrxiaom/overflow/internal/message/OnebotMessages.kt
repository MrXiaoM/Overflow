package top.mrxiaom.overflow.internal.message

import com.google.gson.JsonParser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.internal.asOnebot
import top.mrxiaom.overflow.internal.message.data.*
import top.mrxiaom.overflow.message.data.ContactRecommend
import top.mrxiaom.overflow.message.data.Location
import top.mrxiaom.overflow.message.data.Markdown

/**
 * Json 数组消息 (Onebot) 与 [MessageChain] (mirai) 的序列化与反序列化
 *
 * ## 反序列化未完成项
 * - 音乐分享 (music/[MusicShare])
 */
internal object OnebotMessages {
    internal var appName = "onebot"
    internal var appVersion = "Unknown"
    @OptIn(MiraiExperimentalApi::class)
    internal fun registerSerializers() = MessageSerializers.apply {
        registerSerializer(WrappedAudio::class, WrappedAudio.serializer())
        registerSerializer(WrappedVideo::class, WrappedVideo.serializer())
        registerSerializer(WrappedFileMessage::class, WrappedFileMessage.serializer())
        registerSerializer(UnknownMessage::class, UnknownMessage.serializer())
        registerSerializer(WrappedFileMessage::class, WrappedFileMessage.serializer())
        registerSerializer(WrappedMarketFace::class, WrappedMarketFace.serializer())
        registerSerializer(ContactRecommend::class, ContactRecommend.serializer())
        registerSerializer(Location::class, Location.serializer())
    }

    /**
     * @see serializeToOneBotJsonArray
     */
    internal fun serializeToOneBotJson(message: Message): String {
        return Json.encodeToString(serializeToOneBotJsonArray(message))
    }

    /**
     * 将 mirai 消息序列化为 json 数组消息
     *
     * @param message mirai 消息，不支持转发消息
     */
    @OptIn(MiraiExperimentalApi::class)
    internal fun serializeToOneBotJsonArray(message: Message): JsonArray {
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
                            is AtAll -> put("qq", "all")
                            is RockPaperScissors -> put("id", single.id) // Onebot 11 不支持自定义石头剪刀布
                            is Dice -> put("id", single.value) // Onebot 11 不支持自定义骰子
                            is PokeMessage -> {
                                put("type", single.pokeType)
                                put("id", single.id)
                            }
                            is MusicShare -> {
                                when (single.kind) {
                                    MusicKind.NeteaseCloudMusic -> {
                                        put("type", "163")
                                        put("id", single.jumpUrl.substringAfterLast("id="))
                                    }
                                    // MusicKind.QQMusic -> {}
                                    else -> {
                                        put("type", "custom")
                                        put("url", single.jumpUrl)
                                        put("audio", single.musicUrl)
                                        put("title", single.title)
                                        put("content", single.summary)
                                        put("image", single.pictureUrl)
                                    }
                                }
                            }
                            is QuoteReply -> put("id", single.source.ids[0]) // 忽略分片消息情况
                            // is ForwardMessage -> put("id", single.id) // 转发消息有单独的发送方法
                            is LightApp -> put("data", single.content)
                            is ServiceMessage -> put("data", single.content)
                            is Markdown -> when (appName.lowercase()) { // 其它实现可能有其它格式，预留判断
                                "shamrock" -> put("content", single.content)
                                else -> put("content", single.content)
                            }
                            is ContactRecommend -> {
                                put("type", single.type.name.lowercase())
                                put("id", single.id)
                            }
                            is Location -> {
                                put("lat", single.lat)
                                put("lon", single.lon)
                                if (single.title.isNotEmpty()) put("title", single.title)
                                if (single.content.isNotEmpty()) put("content", single.content)
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * 将转发消息节点转换为可供 Onebot 发送的列表
     */
    internal fun serializeForwardNodes(nodeList: List<ForwardMessage.Node>): List<Map<String, Any>> {
        return nodeList.map {
            mutableMapOf(
                "type" to "node",
                "data" to mutableMapOf(
                    "name" to it.senderName,
                    "content" to JsonParser.parseString(serializeToOneBotJson(it.messageChain))
                )
            )
        }
    }

    /**
     * 反序列化消息
     *
     * @param bot 机器人实例
     * @param message 消息内容，应为 json 数组消息，若 json 反序列化失败，将会返回为纯文本消息作为 fallback
     * @param source 消息源
     */
    internal suspend fun deserializeFromOneBot(bot: Bot, message: String, source: MessageSource? = null): MessageChain {
        return kotlin.runCatching { Json.parseToJsonElement(message).jsonArray }
            .map { deserializeFromOneBotJson(bot, it, source) }.getOrNull() ?: kotlin.run {
                source?.plus(message) ?: PlainText(message).toMessageChain()
        }
    }

    /**
     * 反序列化消息
     *
     * @see deserializeFromOneBot
     */
    @OptIn(MiraiInternalApi::class, MiraiExperimentalApi::class)
    internal suspend fun deserializeFromOneBotJson(bot: Bot, json: JsonArray, source: MessageSource? = null): MessageChain {
        return buildMessageChain {
            if (source != null) add(source)

            for (o in json) {
                val obj = o.jsonObject
                val type = obj["type"].string
                val data = obj["data"]?.jsonObject ?: buildJsonObject {  }
                when (type) {
                    "text" -> add(data["text"].string)
                    "face" -> add(Face(data["id"].int))
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
                    "at" -> {
                        if (data["qq"].string.lowercase() == "all")
                            add(AtAll)
                        else
                            add(At(data["qq"]!!.jsonPrimitive.long))
                    }
                    
                    // TODO "rps" "dice" 无法通过 OneBot 获取其具体值，先搁置
                    "rps" -> add(RockPaperScissors.random())
                    "dice" -> add(Dice.random())

                    "new_dice" -> add(Dice(data["id"].int))
                    "poke" -> add(PokeMessage(
                        data["name"].string,
                        data["type"].int,
                        data["id"].int
                    ))
                    "music" -> {
                        val id = data["id"].string
                        when(data["type"].string) {
                            "163" -> add(deserializeNeteaseMusic(id))
                            "qq" -> add(deserializeQQMusic(id))
                            "custom" -> {
                                add(MusicShare(MusicKind.QQMusic,
                                    data["title"].string, data["content"].string,
                                    data["url"].string, data["image"].string,
                                    data["audio"].string
                                ))
                            }
                        }
                    }
                    "forward" -> {
                        val id = data["id"].string
                        if (id.isNotEmpty()) {
                            val nodes = Mirai.downloadForwardMessage(bot, id)
                            val raw = RawForwardMessage(nodes)
                            add(raw.render(ForwardMessage.DisplayStrategy))
                        }
                    }
                    "mface" -> add(WrappedMarketFace(data["id"].string, "[商城表情]")) // TODO 根据 emojiId 获取 name
                    "xml" -> add(SimpleServiceMessage(60, data["data"].string))
                    "json" -> add(LightApp(data["data"].string))

                    "reply" -> {
                        val id = data["id"].int
                        val msgData = bot.asOnebot.impl.getMsg(id).data
                        val msgSource = MessageSourceBuilder()
                            .id(id)
                            .internalId(id)
                        if (msgData != null) msgSource
                            .sender(msgData.sender.userId.toLong())
                            .target(msgData.targetId)
                            .messages { deserializeFromOneBot(bot, msgData.message) }
                            .time(msgData.time)
                        val kind = if (msgData?.groupId == 0L) MessageSourceKind.FRIEND else MessageSourceKind.GROUP
                        
                        add(QuoteReply(msgSource.build(bot.id, kind)))
                    }

                    "file" -> { // OpenShamrock
                        //val sub = data["sub"].string
                        //val biz = data["biz"].int
                        val size = data["size"].long
                        //val expire = data["expire"].int
                        val name = data["name"].string
                        val id = data["id"].string
                        val url = data["url"].string

                        add(WrappedFileMessage(id, 0, name, size, url))
                    }

                    "markdown" -> when (appName.lowercase()) { // 其它实现可能有其它格式，预留判断
                        "shamrock" -> add(Markdown(data["content"].string))
                        else -> add(Markdown(data["content"].string))
                    }

                    "contact" -> {
                        val contactType = when (val typeStr = data["type"].string.lowercase()) {
                            "group" -> ContactRecommend.ContactType.Group
                            "private", "qq" -> ContactRecommend.ContactType.Private
                            else -> throw IllegalArgumentException("未知联系人类型 $typeStr")
                        }
                        val id = data["id"].string
                            .substringBefore("&") // OpenShamrock bug
                            .toLong()
                        add(ContactRecommend(contactType, id))
                    }

                    "location" -> {
                        val lat = data["lat"].string.toFloat()
                        val lon = data["lon"].string.toFloat()
                        val title = data["title"].string
                        val content = data["content"].string
                        add(Location(lat, lon, title, content))
                    }

                    else -> add(UnknownMessage(type, Json.encodeToString(data)))
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
            is AtAll -> "at"
            is RockPaperScissors -> "rps"
            is Dice -> {
                when (appName.lowercase()) {
                    "shamrock" -> "new_dice"
                    else -> "dice"
                }
            }
            is PokeMessage -> "poke"
            //is ServiceMessage -> "share"
            // 推荐好友/推荐群 contact
            // 位置 location
            is MusicShare -> "music"
            is QuoteReply -> "reply"
            is ForwardMessage -> "forward"
            is LightApp -> "json"
            is ServiceMessage -> "xml"
            is FileMessage -> "file"
            is Markdown -> "markdown"
            is ContactRecommend -> "contact"
            is Location -> "location"
            else -> "text"
        }
    internal fun imageFromFile(file: String): Image = Image.fromId(file)
    internal fun audioFromFile(file: String): Audio = WrappedAudio(file, 0)
    internal fun videoFromFile(file: String): ShortVideo = WrappedVideo(file)

    private val Image.onebotFile: String
        get() = imageId
    private val FlashImage.onebotFile: String
        get() = image.onebotFile
    private val Audio.onebotFile: String
        get() = (this as? WrappedAudio)?.file ?: ""
    private val ShortVideo.onebotFile: String
        get() = (this as? WrappedVideo)?.file ?: ""
    internal val JsonElement?.string
        get() = this?.jsonPrimitive?.content ?: ""
    internal val JsonElement?.int
        get() = this?.jsonPrimitive?.intOrNull ?: throw IllegalStateException()
    internal val JsonElement?.long
        get() = this?.jsonPrimitive?.longOrNull ?: throw IllegalStateException()

    internal fun Message.findForwardMessage(): ForwardMessage? {
        return when(this){
            is ForwardMessage -> this
            is MessageChain -> firstIsInstanceOrNull()
            else -> null
        }
    }
}