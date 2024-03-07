package top.mrxiaom.overflow.internal.message

import cn.evole.onebot.sdk.entity.MsgId
import com.google.gson.JsonParser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.message.data.MarketFaceImpl
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.contact.RemoteBot
import top.mrxiaom.overflow.internal.asOnebot
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.message.data.*
import top.mrxiaom.overflow.message.data.*

/**
 * Json 数组消息 (Onebot) 与 [MessageChain] (mirai) 的序列化与反序列化
 */
internal object OnebotMessages {
    @OptIn(MiraiExperimentalApi::class)
    internal fun registerSerializers() = MessageSerializers.apply {
        registerSerializer(At::class, At.serializer())
        registerSerializer(Dice::class, Dice.serializer())
        registerSerializer(Face::class, Face.serializer())
        registerSerializer(FlashImage::class, FlashImage.serializer())
        registerSerializer(ForwardMessage::class, ForwardMessage.serializer())
        registerSerializer(LightApp::class, LightApp.serializer())
        registerSerializer(MessageSource::class, MessageSource.serializer())
        registerSerializer(MusicShare::class, MusicShare.serializer())
        registerSerializer(PlainText::class, PlainText.serializer())
        registerSerializer(PokeMessage::class, PokeMessage.serializer())
        registerSerializer(QuoteReply::class, QuoteReply.serializer())
        registerSerializer(RockPaperScissors::class, RockPaperScissors.serializer())
        registerSerializer(SimpleServiceMessage::class, SimpleServiceMessage.serializer())
        registerSerializer(VipFace::class, VipFace.serializer())

        registerSerializer(WrappedAudio::class, WrappedAudio.serializer())
        registerSerializer(WrappedVideo::class, WrappedVideo.serializer())
        registerSerializer(WrappedImage::class, WrappedImage.serializer())
        registerSerializer(WrappedFileMessage::class, WrappedFileMessage.serializer())
        registerSerializer(UnknownMessage::class, UnknownMessage.serializer())
        registerSerializer(WrappedFileMessage::class, WrappedFileMessage.serializer())
        registerSerializer(MarketFaceImpl::class, MarketFaceImpl.serializer())
        registerSerializer(ContactRecommend::class, ContactRecommend.serializer())
        registerSerializer(Location::class, Location.serializer())
        registerSerializer(Markdown::class, Markdown.serializer())
        registerSerializer(InlineKeyboard::class, InlineKeyboard.serializer())
    }

    /**
     * @see serializeToOneBotJsonArray
     */
    internal fun serializeToOneBotJson(bot: RemoteBot?, message: Message): String {
        return Json.encodeToString(serializeToOneBotJsonArray(bot, message))
    }

    /**
     * 将 mirai 消息序列化为 json 数组消息
     *
     * @param bot 机器人实例，用于获取当前 Onebot 实现名称以兼容各个实现
     * @param message mirai 消息，不支持转发消息
     */
    @OptIn(MiraiExperimentalApi::class)
    internal fun serializeToOneBotJsonArray(bot: RemoteBot?, message: Message): JsonArray {
        val messageChain = (message as? MessageChain) ?: listOf(message)
        return buildJsonArray {
            val app = (bot?.appName ?: "Onebot").lowercase()

            for (single in messageChain) {
                addJsonObject {
                    put("type", single.messageType(bot))
                    putJsonObject("data") {
                        when (single) {
                            is PlainText -> put("text", single.content)
                            is Face -> put("id", single.id.toString())
                            is Image -> put("file", single.onebotFile)
                            is FlashImage -> {
                                put("file", single.onebotFile)
                                put("type", "flash")
                            }
                            is Audio -> put("file", single.onebotFile)
                            is ShortVideo -> put("file", single.onebotFile)
                            is At -> put("qq", single.target.toString())
                            is AtAll -> put("qq", "all")
                            is RockPaperScissors -> put("id", single.id) // Onebot 11 不支持自定义石头剪刀布
                            is Dice -> put("id", single.value) // Onebot 11 不支持自定义骰子
                            is PokeMessage -> {
                                put("type", single.pokeType.toString())
                                put("id", single.id.toString())
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
                            is QuoteReply -> when { // 忽略分片消息情况
                                // TODO: 待定，Lagrange.Core 中的 reply id 类型较混乱，写入的时候 uint.ToString()，读取的时候 (uint)int.Parse()
                                app.contains("lagrange") -> put("id", single.source.ids[0].toUInt().toString())
                                else -> put("id", single.source.ids[0].toString())
                            }
                            // is ForwardMessage -> put("id", single.id) // 转发消息有单独的发送方法
                            is LightApp -> put("data", single.content)
                            is ServiceMessage -> put("data", single.content)
                            is Markdown -> when (app) { // 其它实现可能有其它格式，预留判断
                                "shamrock" -> put("content", single.content)
                                else -> put("content", single.content)
                            }
                            is ContactRecommend -> {
                                put("type", single.contactType.name.lowercase())
                                put("id", single.id.toString())
                            }
                            is Location -> {
                                put("lat", single.lat.toString())
                                put("lon", single.lon.toString())
                                if (single.title.isNotEmpty()) put("title", single.title)
                                if (single.content.isNotEmpty()) put("content", single.content)
                            }
                            is InlineKeyboard -> { // OpenShamrock
                                put("bot_appid", single.botAppId)
                                putJsonArray("rows") {
                                    single.rows.forEach {  row ->
                                        add(buildJsonObject row@{
                                            putJsonArray("buttons") {
                                                row.buttons.forEach { button ->
                                                    add(buildJsonObject {
                                                        put("id", button.id)
                                                        put("label", button.label)
                                                        put("visited_label", button.visitedLabel)
                                                        put("style", button.style)
                                                        put("type", button.type)
                                                        put("click_limit", button.clickLimit)
                                                        put("unsupport_tips", button.unsupportTips)
                                                        put("data", button.data)
                                                        put("at_bot_show_channel_list", button.atBotShowChannelList)
                                                        put("permission_type", button.permissionType)
                                                        putJsonArray("specify_role_ids") {
                                                            button.specifyRoleIds.forEach { add(it) }
                                                        }
                                                        putJsonArray("specify_tinyids") {
                                                            button.specifyTinyIds.forEach { add(it) }
                                                        }
                                                    })
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    internal suspend fun sendForwardMessage(contact: Contact, forward: ForwardMessage): MsgId? {
        val bot = contact.bot.asOnebot
        val nodes = serializeForwardNodes(bot, forward.nodeList)
        when (bot.appName.lowercase()) {
            "lagrange.onebot" -> {
                val resId = bot.impl.sendForwardMsgLagrange(nodes).data
                if (resId != null) {
                    val forwardMsg = Json.encodeToString(buildJsonArray {
                        buildJsonObject {
                            put("type", "forward")
                            putJsonObject("data") {
                                put("id", resId)
                            }
                        }
                    })

                    return when (contact) {
                        is Group -> bot.impl.sendGroupMsg(contact.id, forwardMsg, false).data
                        else -> bot.impl.sendPrivateMsg(contact.id, forwardMsg, false).data
                    }
                }
                return null
            }
            else -> { // go-cqhttp, shamrock
                return when (contact) {
                    is Group -> bot.impl.sendGroupForwardMsg(contact.id, nodes).data
                    else -> bot.impl.sendPrivateForwardMsg(contact.id, nodes).data
                }
            }
        }
    }

    /**
     * 将转发消息节点转换为可供 Onebot 发送的列表
     *
     * @param bot 机器人实例，用于获取当前 Onebot 实现名称以兼容各个实现
     * @param nodeList mirai 转发消息节点
     */
    internal fun serializeForwardNodes(bot: RemoteBot, nodeList: List<ForwardMessage.Node>): List<Map<String, Any>> {
        val appName = bot.appName.lowercase()
        return nodeList.map {
            val message = JsonParser.parseString(serializeToOneBotJson(bot, it.messageChain))
            when (appName) {
                "shamrock", "go-cqhttp", "lagrange.onebot" -> mutableMapOf(
                    "type" to "node",
                    "data" to mutableMapOf(
                        "name" to it.senderName,
                        "uin" to it.senderId,
                        "content" to message,
                    ),
                    "time" to it.time
                )
                else -> mutableMapOf(
                    "type" to "node",
                    "data" to mutableMapOf(
                        "user_id" to it.senderId,
                        "nickname" to it.senderName,
                        "content" to message
                    )
                )
            }
        }
    }

    /**
     * 反序列化消息
     *
     * @param bot 机器人实例
     * @param message 消息内容，应为 json 数组消息，若 json 反序列化失败，将会返回为纯文本消息作为 fallback
     * @param source 消息源
     */
    internal suspend fun deserializeFromOneBot(bot: RemoteBot, message: String, source: MessageSource? = null): MessageChain {
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
    internal suspend fun deserializeFromOneBotJson(bot: RemoteBot, json: JsonArray, source: MessageSource? = null): MessageChain {
        return buildMessageChain {
            if (source != null) add(source)

            val app = bot.appName.lowercase()
            for (o in json) {
                val obj = o.jsonObject
                val type = obj["type"].string
                val data = obj["data"]?.jsonObject ?: buildJsonObject {  }
                when (type) {
                    "text" -> add(data["text"].string)
                    "face" -> add(Face(data["id"].string.toInt()))
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
                            add(At(data["qq"].string.toLong()))
                    }
                    
                    // TODO "rps" "dice" 无法通过 OneBot 获取其具体值，先搁置
                    "rps" -> add(RockPaperScissors.random())
                    "dice" -> add(Dice.random())

                    "new_dice" -> add(Dice(data["id"].int))
                    "poke" -> add(PokeMessage(
                        data["name"].string,
                        data["type"].string.toInt(),
                        data["id"].string.toInt()
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
                            val nodes = Mirai.downloadForwardMessage(bot as Bot, id)
                            val raw = RawForwardMessage(nodes)
                            add(raw.render(ForwardMessage.DisplayStrategy))
                        }
                    }
                    "mface" -> add(MarketFaceImpl(ImMsgBody.MarketFace( // TODO 根据 emojiId 获取 name
                        faceId = data["id"].string.encodeToByteArray()
                    )))
                    "xml" -> add(SimpleServiceMessage(60, data["data"].string))
                    "json" -> add(LightApp(data["data"].string))

                    "reply" -> {
                        val id = when {
                            app.contains("lagrange") -> data["id"].string.toUInt().toInt()
                            else -> data["id"].string.toInt()
                        }
                        val msgData = (bot as BotWrapper).impl.getMsg(id).data
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

                    "markdown" -> when (app) { // 其它实现可能有其它格式，预留判断
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

                    "inline_keyboard" -> { // OpenShamrock
                        val botAppId = data["bot_appid"].long
                        val rows = data["rows"]!!.jsonArray.map { e1 ->
                            val obj1 = e1.jsonObject
                            InlineKeyboardRow(
                                buttons = obj1["buttons"]!!.jsonArray.map { e2 ->
                                    val button = e2.jsonObject
                                    InlineKeyboardButton(
                                        id = button["id"].string,
                                        label = button["label"].string,
                                        visitedLabel = button["visited_label"].string,
                                        style = button["style"].int,
                                        type = button["type"].int,
                                        clickLimit = button["click_limit"].int,
                                        unsupportTips = button["unsupport_tips"].string,
                                        data = button["data"].string,
                                        atBotShowChannelList = button["at_bot_show_channel_list"].boolean,
                                        permissionType = button["permission_type"].int,
                                        specifyRoleIds = button["specify_role_ids"]!!.jsonArray.map { it.string },
                                        specifyTinyIds = button["specify_tinyids"]!!.jsonArray.map { it.string }
                                    )
                                }
                            )
                        }
                        add(InlineKeyboard(botAppId, rows))
                    }

                    else -> add(UnknownMessage(type, Json.encodeToString(data)).printLog())
                }
            }
        }
    }

    private fun Message.messageType(bot: RemoteBot?): String {
        val appName = (bot?.appName ?: "Onebot").lowercase()
        return when (this) {
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
                when (appName) {
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
            is InlineKeyboard -> "inline_keyboard"
            else -> "text"
        }
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
    internal val JsonElement?.boolean
        get() = this?.jsonPrimitive?.booleanOrNull ?: throw IllegalStateException()

    internal fun Message.findForwardMessage(): ForwardMessage? {
        return when(this){
            is ForwardMessage -> this
            is MessageChain -> firstIsInstanceOrNull()
            else -> null
        }
    }
}