package top.mrxiaom.overflow.internal.message

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.toUHexString
import top.mrxiaom.overflow.internal.asOnebot
import top.mrxiaom.overflow.internal.message.data.*
import java.net.URL

/**
 * Json 数组消息 (Onebot) 与 [MessageChain] (mirai) 的序列化与反序列化
 *
 * ## 反序列化未完成项
 * - 音乐分享 (music/[MusicShare])
 */
object OnebotMessages {
    internal var appName = "onebot"
    internal var appVersion = "Unknown"
    @OptIn(MiraiExperimentalApi::class)
    internal fun registerSerializers() = MessageSerializers.apply {
        registerSerializer(WrappedAudio::class, WrappedAudio.serializer())
        registerSerializer(WrappedVideo::class, WrappedVideo.serializer())
        registerSerializer(WrappedFileMessage::class, WrappedFileMessage.serializer())
        registerSerializer(UnknownMessage::class, UnknownMessage.serializer())
    }

    /**
     * @see serializeToOneBotJsonArray
     */
    fun serializeToOneBotJson(message: Message): String {
        return Json.encodeToString(serializeToOneBotJsonArray(message))
    }

    /**
     * 将 mirai 消息序列化为 json 数组消息
     *
     * @param message mirai 消息，不支持转发消息
     */
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
                        }
                    }
                }
            }
        }
    }
    /**
     * 将转发消息节点转换为可供 Onebot 发送的列表
     */
    fun serializeForwardNodes(nodeList: List<ForwardMessage.Node>): List<Map<String, Any>> {
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
    suspend fun deserializeFromOneBot(bot: Bot, message: String, source: MessageSource? = null): MessageChain {
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
    suspend fun deserializeFromOneBotJson(bot: Bot, json: JsonArray, source: MessageSource? = null): MessageChain {
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
                        val sub = data["sub"].string
                        val biz = data["biz"].int
                        val size = data["size"].long
                        val expire = data["expire"].int
                        val name = data["name"].string
                        val id = data["id"].string
                        val url = data["url"].string

                        add(WrappedFileMessage(id, 0, name, size, url))
                    }

                    else -> add(UnknownMessage(type, data))
                }
            }
        }
    }

    suspend fun deserializeNeteaseMusic(id: String): MusicShare {
        return withContext(Dispatchers.IO) {
            val conn = URL("https://music.163.com/api/song/detail/?id=$id&ids=[$id]")
                .openConnection().also { it.connect() }
            val result = conn.inputStream.use {
                it.readBytes().toString(Charsets.UTF_8)
            }
            val songInfo = Json.parseToJsonElement(result).jsonObject["songs"]!!.jsonArray.first().jsonObject
            val title = songInfo["name"].string
            val singerName = songInfo["artists"]!!.jsonArray.first().jsonObject["name"].string
            val previewUrl = songInfo["album"]!!.jsonObject["picUrl"].string
            val playUrl = "https://music.163.com/song/media/outer/url?id=$id.mp3"
            val jumpUrl = "https://music.163.com/#/song?id=$id"
            MusicShare(MusicKind.NeteaseCloudMusic, title, singerName, jumpUrl, previewUrl, playUrl)
        }
    }

    suspend fun deserializeQQMusic(id: String): MusicShare {
        return withContext(Dispatchers.IO) {
            val conn = URL("https://u.y.qq.com/cgi-bin/musicu.fcg?format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq.json&needNewCode=0&data={%22comm%22:{%22ct%22:24,%22cv%22:0},%22songinfo%22:{%22method%22:%22get_song_detail_yqq%22,%22param%22:{%22song_type%22:0,%22song_mid%22:%22%22,%22song_id%22:$id},%22module%22:%22music.pf_song_detail_svr%22}}")
                .openConnection().also { it.connect() }
            val result = conn.inputStream.use {
                it.readBytes().toString(Charsets.UTF_8)
            }
            val songInfo = Json.parseToJsonElement(result).jsonObject["songinfo"]!!.jsonObject.takeIf { it["code"].int != 0 } ?: throw IllegalStateException("QQMusic code = 0")
            val data = songInfo["data"]!!.jsonObject
            val trackInfo = data["track_info"]!!.jsonObject
            val mid = trackInfo["mid"].string
            val previewMid = trackInfo["album"]!!.jsonObject["mid"].string
            val singerMid = (trackInfo["singer"] as? JsonArray)?.let {
                it[0].jsonObject["mid"]?.jsonPrimitive?.contentOrNull
            } ?: ""
            val title = trackInfo["title"].string
            val singerName = trackInfo["singer"]!!.jsonArray.first().jsonObject["name"].string
            val vs = (trackInfo["vs"] as? JsonArray)?.let {
                it[0].jsonPrimitive.contentOrNull
            } ?: ""
            val code = "${mid}q;z(&l~sdf2!nK".toByteArray().toUHexString("").substring(0 .. 4).uppercase()
            val playUrl = "http://c6.y.qq.com/rsc/fcgi-bin/fcg_pyq_play.fcg?songid=&songmid=$mid&songtype=1&fromtag=50&uin=&code=$code"
            val previewUrl = if (vs.isNotEmpty()) {
                "http://y.gtimg.cn/music/photo_new/T062R150x150M000$vs}.jpg"
            } else if (previewMid.isNotEmpty()) {
                "http://y.gtimg.cn/music/photo_new/T002R150x150M000$previewMid.jpg"
            } else if (singerMid.isNotEmpty()){
                "http://y.gtimg.cn/music/photo_new/T001R150x150M000$singerMid.jpg"
            } else {
                ""
            }
            val jumpUrl = "https://i.y.qq.com/v8/playsong.html?platform=11&appshare=android_qq&appversion=10030010&hosteuin=oKnlNenz7i-s7c**&songmid=${mid}&type=0&appsongtype=1&_wv=1&source=qq&ADTAG=qfshare"

            MusicShare(MusicKind.QQMusic, title, singerName, jumpUrl, previewUrl, playUrl)
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
    private val JsonElement?.string
        get() = this?.jsonPrimitive?.content ?: ""
    private val JsonElement?.int
        get() = this?.jsonPrimitive?.intOrNull ?: throw IllegalStateException()
    private val JsonElement?.long
        get() = this?.jsonPrimitive?.longOrNull ?: throw IllegalStateException()

    fun Message.findForwardMessage(): ForwardMessage? {
        return when(this){
            is ForwardMessage -> this
            is MessageChain -> firstIsInstanceOrNull()
            else -> null
        }
    }
}