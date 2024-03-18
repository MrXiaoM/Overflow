package top.mrxiaom.overflow.internal.message

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.message.OnebotMessages.boolean
import top.mrxiaom.overflow.internal.message.OnebotMessages.int
import top.mrxiaom.overflow.internal.message.OnebotMessages.long
import top.mrxiaom.overflow.internal.message.OnebotMessages.onebotFile
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.internal.message.ProcessorReply.miraiToOnebotReply
import top.mrxiaom.overflow.internal.message.data.UnknownMessage
import top.mrxiaom.overflow.internal.message.data.deserializeNeteaseMusic
import top.mrxiaom.overflow.internal.message.data.deserializeQQMusic
import top.mrxiaom.overflow.message.ForOnebot
import top.mrxiaom.overflow.message.MessageProcessor
import top.mrxiaom.overflow.message.MiraiTypes
import top.mrxiaom.overflow.message.data.*

@MiraiTypes(PlainText::class)
internal object ProcessorText: MessageProcessor<PlainText> {
    override val type: String = "text"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): PlainText {
        return PlainText(data["text"].string)
    }

    override fun miraiToOnebot(message: PlainText, block: JsonObjectBuilder) {
        block.apply {
            put("text", message.content)
        }
    }
}
@MiraiTypes(Face::class)
internal object ProcessorFace: MessageProcessor<Face> {
    override val type: String = "face"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): Face {
        return Face(data["id"].string.toInt())
    }

    override fun miraiToOnebot(message: Face, block: JsonObjectBuilder) {
        block.apply {
            put("id", message.id.toString())
        }
    }
}
@MiraiTypes(Image::class, FlashImage::class)
internal object ProcessorImage: MessageProcessor<SingleMessage> {
    override val type: String = "face"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): SingleMessage {
        val image = OnebotMessages.imageFromFile((data["url"] ?: data["file"]).string)
        return if (data["type"].string == "flash") {
            image.flash()
        } else {
            image
        }
    }

    override fun miraiToOnebot(message: SingleMessage, block: JsonObjectBuilder) {
        block.apply {
            if (message is Image) {
                put("file", message.onebotFile)
            }
            if (message is FlashImage) {
                put("file", message.onebotFile)
                put("type", "flash")
            }
        }
    }
}
@MiraiTypes(Audio::class)
internal object ProcessorRecord: MessageProcessor<Audio> {
    override val type: String = "record"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): Audio {
        return OnebotMessages.audioFromFile(data["file"].string)
    }

    override fun miraiToOnebot(message: Audio, block: JsonObjectBuilder) {
        block.apply {
            put("file", message.onebotFile)
        }
    }
}
@MiraiTypes(ShortVideo::class)
internal object ProcessorVideo: MessageProcessor<ShortVideo> {
    override val type: String = "video"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): ShortVideo {
        return OnebotMessages.videoFromFile(data["file"].string)
    }

    override fun miraiToOnebot(message: ShortVideo, block: JsonObjectBuilder) {
        block.apply {
            put("file", message.onebotFile)
        }
    }
}
@MiraiTypes(At::class, AtAll::class)
internal object ProcessorAt: MessageProcessor<SingleMessage> {
    override val type: String = "at"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): SingleMessage {
        return if (data["qq"].string.lowercase() == "all") {
            AtAll
        } else {
            At(data["qq"].string.toLong())
        }
    }

    override fun miraiToOnebot(message: SingleMessage, block: JsonObjectBuilder) {
        block.apply {
            if (message is At) {
                put("qq", message.target.toString())
            }
            if (message is AtAll) {
                put("qq", "all")
            }
        }
    }
}
@ForOnebot("go-cqhttp", "shamrock")
@MiraiTypes(RockPaperScissors::class)
internal object ProcessorRSP: MessageProcessor<RockPaperScissors> {
    override val type: String = "rsp"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): RockPaperScissors {
        // TODO: 无法通过 OneBot 获取其具体值，先搁置
        return RockPaperScissors.random()
    }

    @OptIn(MiraiExperimentalApi::class)
    override fun miraiToOnebot(message: RockPaperScissors, block: JsonObjectBuilder) {
        block.apply {
            put("id", message.id) // Onebot 11 不支持自定义石头剪刀布
        }
    }
}
@MiraiTypes(Dice::class)
internal object ProcessorDice: MessageProcessor<Dice> {
    override val type: String = "dice"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): Dice {
        // TODO: 无法通过 OneBot 获取其具体值，先搁置
        return Dice.random()
    }

    @OptIn(MiraiExperimentalApi::class)
    override fun miraiToOnebot(message: Dice, block: JsonObjectBuilder) {
        block.apply {
            put("id", message.id) // Onebot 11 不支持自定义骰子
        }
    }
}
@ForOnebot("shamrock")
@MiraiTypes(Dice::class)
internal object ProcessorNewDice: MessageProcessor<Dice> {
    override val type: String = "new_dice"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): Dice {
        return Dice(data["id"].int)
    }

    @OptIn(MiraiExperimentalApi::class)
    override fun miraiToOnebot(message: Dice, block: JsonObjectBuilder) {
        block.apply {
            put("id", message.id)
        }
    }
}
@MiraiTypes(PokeMessage::class)
internal object ProcessorPoke: MessageProcessor<PokeMessage> {
    override val type: String = "poke"

    @OptIn(MiraiInternalApi::class)
    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): PokeMessage {
        return PokeMessage(
            data["name"].string,
            data["type"].string.toInt(),
            data["id"].string.toInt()
        )
    }

    override fun miraiToOnebot(message: PokeMessage, block: JsonObjectBuilder) {
        block.apply {
            put("type", message.pokeType.toString())
            put("id", message.id.toString())
        }
    }
}
@MiraiTypes(MusicShare::class)
internal object ProcessorMusic: MessageProcessor<SingleMessage> {
    override val type: String = "music"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): SingleMessage {
        val id = data["id"].string
        return when(data["type"].string) {
            "163" -> deserializeNeteaseMusic(id)
            "qq" -> deserializeQQMusic(id)
            "custom" -> {
                MusicShare(MusicKind.QQMusic,
                    data["title"].string, data["content"].string,
                    data["url"].string, data["image"].string,
                    data["audio"].string
                )
            }
            else -> UnknownMessage(type, Json.encodeToString(data)).printLog()
        }
    }

    override fun miraiToOnebot(message: SingleMessage, block: JsonObjectBuilder) {
        if (message is MusicShare) block.apply {
            when (message.kind) {
                MusicKind.NeteaseCloudMusic -> {
                    put("type", "163")
                    put("id", message.jumpUrl.substringAfterLast("id="))
                }
                // MusicKind.QQMusic -> {}
                else -> {
                    put("type", "custom")
                    put("url", message.jumpUrl)
                    put("audio", message.musicUrl)
                    put("title", message.title)
                    put("content", message.summary)
                    put("image", message.pictureUrl)
                }
            }
        }
    }
}
@MiraiTypes(QuoteReply::class)
internal object ProcessorReply: MessageProcessor<QuoteReply> {
    override val type: String = "reply"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): QuoteReply {
        val id = when {
            else -> data["id"].string.toInt()
        }
        return onebotToMirai(bot, id)
    }
    
    suspend fun onebotToMirai(bot: Bot, id: Int): QuoteReply {
        val msgData = (bot as BotWrapper).impl.getMsg(id).data
        val msgSource = MessageSourceBuilder()
            .id(id)
            .internalId(id)
        if (msgData != null) msgSource
            .sender(msgData.sender.userId.toLong())
            .target(msgData.targetId)
            .messages { OnebotMessages.deserializeFromOneBot(bot, msgData.message) }
            .time(msgData.time)
        val kind = if (msgData?.groupId == 0L) MessageSourceKind.FRIEND else MessageSourceKind.GROUP

        return QuoteReply(msgSource.build(bot.id, kind))
    }

    override fun miraiToOnebot(message: QuoteReply, block: JsonObjectBuilder) {
        block.miraiToOnebotReply(message.source.ids[0].toString())
    }

    fun JsonObjectBuilder.miraiToOnebotReply(id: String) {
        put("id", id)
    }
}
@ForOnebot("lagrange.onebot")
@MiraiTypes(QuoteReply::class)
internal object ProcessorReplyLagrange: MessageProcessor<QuoteReply> {
    // TODO: 待定，Lagrange.Core 某个版本的 reply id 类型较混乱，写入的时候 uint.ToString()，读取的时候 (uint)int.Parse()
    override val type: String = "reply"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): QuoteReply {
        val id = data["id"].string.toUInt().toInt()
        return ProcessorReply.onebotToMirai(bot, id)
    }

    override fun miraiToOnebot(message: QuoteReply, block: JsonObjectBuilder) {
        block.miraiToOnebotReply(message.source.ids[0].toUInt().toString())
    }
}
@MiraiTypes(LightApp::class)
internal object ProcessorJson: MessageProcessor<LightApp> {
    override val type: String = "json"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): LightApp {
        return LightApp(data["data"].string)
    }

    override fun miraiToOnebot(message: LightApp, block: JsonObjectBuilder) {
        block.apply {
            put("data", message.content)
        }
    }
}
@MiraiTypes(ServiceMessage::class)
internal object ProcessorXML: MessageProcessor<ServiceMessage> {
    override val type: String = "xml"

    @OptIn(MiraiExperimentalApi::class)
    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): ServiceMessage {
        return SimpleServiceMessage(60, data["data"].string)
    }

    override fun miraiToOnebot(message: ServiceMessage, block: JsonObjectBuilder) {
        block.apply {
            put("data", message.content)
        }
    }
}
@ForOnebot("shamrock", "gensokyo")
@MiraiTypes(Markdown::class)
internal object ProcessorMarkdown: MessageProcessor<Markdown> {
    override val type: String = "markdown"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): Markdown {
        return Markdown(data["content"].string)
    }

    override fun miraiToOnebot(message: Markdown, block: JsonObjectBuilder) {
        block.apply {
            put("content", message.content)
        }
    }
}
@ForOnebot("gensokyo") // TODO: Gensokyo 模板型 Markdown 消息
@MiraiTypes()
internal object ProcessorMarkdownGensokyo: MessageProcessor<SingleMessage> {
    override val type: String = "markdown"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): SingleMessage {
        TODO("Not yet implemented")
    }

    override fun miraiToOnebot(message: SingleMessage, block: JsonObjectBuilder) {
        TODO("Not yet implemented")
    }
}
@MiraiTypes(ContactRecommend::class)
internal object ProcessorContact: MessageProcessor<ContactRecommend> {
    override val type: String = "contact"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): ContactRecommend {
        val contactType = when (val typeStr = data["type"].string.lowercase()) {
            "group" -> ContactRecommend.ContactType.Group
            "private", "qq" -> ContactRecommend.ContactType.Private
            else -> throw IllegalArgumentException("未知联系人类型 $typeStr")
        }
        val id = data["id"].string
            .substringBefore("&") // OpenShamrock bug
            .toLong()
        return ContactRecommend(contactType, id)
    }

    override fun miraiToOnebot(message: ContactRecommend, block: JsonObjectBuilder) {
        block.apply {
            put("type", message.contactType.name.lowercase())
            put("id", message.id.toString())
        }
    }
}
@MiraiTypes(Location::class)
internal object ProcessorLocation: MessageProcessor<Location> {
    override val type: String = "location"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): Location {
        val lat = data["lat"].string.toFloat()
        val lon = data["lon"].string.toFloat()
        val title = data["title"].string
        val content = data["content"].string
        return Location(lat, lon, title, content)
    }

    override fun miraiToOnebot(message: Location, block: JsonObjectBuilder) {
        block.apply {
            put("lat", message.lat.toString())
            put("lon", message.lon.toString())
            if (message.title.isNotEmpty()) put("title", message.title)
            if (message.content.isNotEmpty()) put("content", message.content)
        }
    }
}
@ForOnebot("shamrock")
@MiraiTypes(InlineKeyboard::class)
internal object ProcessorInlineKeyboardShamrock: MessageProcessor<InlineKeyboard> {
    override val type: String = "inline_keyboard"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): InlineKeyboard {
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
        return InlineKeyboard(botAppId, rows)
    }

    override fun miraiToOnebot(message: InlineKeyboard, block: JsonObjectBuilder) {
        block.apply {
            put("bot_appid", message.botAppId)
            putJsonArray("rows") {
                message.rows.forEach {  row ->
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
@ForOnebot("gensokyo") // TODO: 实现 Gensokyo 格式内联键盘消息
@MiraiTypes(InlineKeyboard::class)
internal object ProcessorInlineKeyboardGensokyo: MessageProcessor<InlineKeyboard> {
    override val type: String = "inline_keyboard"

    override suspend fun onebotToMirai(bot: Bot, data: JsonObject): InlineKeyboard {
        TODO("Not yet implemented")
    }

    override fun miraiToOnebot(message: InlineKeyboard, block: JsonObjectBuilder) {
        TODO("Not yet implemented")
    }
}
