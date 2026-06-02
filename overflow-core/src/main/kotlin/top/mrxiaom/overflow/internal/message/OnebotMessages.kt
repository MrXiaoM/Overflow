@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
package top.mrxiaom.overflow.internal.message

import cn.evolvefield.onebot.sdk.entity.MsgId
import cn.evolvefield.onebot.sdk.event.message.MessageEvent
import cn.evolvefield.onebot.sdk.util.CQCode
import com.google.gson.JsonParser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.internal.message.data.MarketFaceImpl
import net.mamoe.mirai.message.MessageSerializers
import net.mamoe.mirai.message.data.*
import top.mrxiaom.overflow.contact.RemoteBot
import top.mrxiaom.overflow.internal.asOnebot
import top.mrxiaom.overflow.internal.message.OnebotMessages.deserializeFromOneBot
import top.mrxiaom.overflow.internal.message.OnebotMessages.serializeToOneBotJsonArray
import top.mrxiaom.overflow.internal.message.data.*
import top.mrxiaom.overflow.message.data.ContactRecommend
import top.mrxiaom.overflow.message.data.InlineKeyboard
import top.mrxiaom.overflow.message.data.Location
import top.mrxiaom.overflow.message.data.Markdown

/**
 * Json 数组消息 (Onebot) 与 [MessageChain] (mirai) 的序列化与反序列化
 */
internal object OnebotMessages {
    internal fun registerSerializers() = MessageSerializers.apply {
        registerSerializer(At::class, At.serializer())
        registerSerializer(Dice::class, Dice.serializer())
        registerSerializer(Face::class, Face.serializer())
        registerSerializer(FlashImage::class, FlashImage.serializer())
        registerSerializer(ForwardMessage::class, ForwardMessage.serializer())
        registerSerializer(LightApp::class, LightApp.serializer())
        registerMessageSourceSerializers()
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
    internal fun serializeToOneBotJsonArray(bot: RemoteBot?, message: Message): JsonArray {
        return BuildMessageContextImpl.toJson(bot, message)
    }

    internal suspend fun sendForwardMessage(contact: Contact, forward: ForwardMessage): MsgId? {
        val bot = contact.bot.asOnebot
        val nodes = serializeForwardNodes(bot, forward.nodeList)
        return when (contact) {
            is Group -> bot.impl.sendGroupForwardMsg(contact.id, nodes, forward.title, forward.summary, forward.preview, forward.brief).data
            else -> bot.impl.sendPrivateForwardMsg(contact.id, nodes, forward.title, forward.summary, forward.preview, forward.brief).data
        }
    }

    internal fun serializeForwardNodeMessage(bot: RemoteBot, appName: String, node: ForwardMessage.Node): Any {
        if (appName.contains("napcat")) {
            val forward = node.messageChain.findForwardMessage()
            if (forward != null) {
                return serializeForwardNodes(bot, forward.nodeList)
            }
        }
        return JsonParser.parseString(serializeToOneBotJson(bot, node.messageChain))
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
            val message = serializeForwardNodeMessage(bot, appName, it)
            mutableMapOf(
                "type" to "node",
                "data" to mutableMapOf(
                    "uin" to when(appName) {
                        "lagrange.onebot" -> it.senderId.toString()
                        else -> it.senderId
                    },
                    "user_id" to it.senderId.toString(),
                    "name" to it.senderName,
                    "nickname" to it.senderName,
                    "content" to message,
                ),
                "time" to it.time
            )
        }
    }

    internal suspend fun MessageEvent.toMiraiMessage(bot: RemoteBot, source: MessageSource? = null): MessageChain {
        return toMiraiMessage(isJsonMessage, message, bot, source)
    }
    internal suspend fun toMiraiMessage(isJsonMessage: Boolean, message: String, bot: RemoteBot, source: MessageSource? = null): MessageChain {
        return run {
            if (isJsonMessage) deserializeMessageFromJson(bot, message, source)
            else deserializeMessageFromCQCode(bot, message, source)
        }
            ?: source?.plus(message)
            ?: PlainText(message).toMessageChain()
    }

    /**
     * 反序列化消息
     *
     * @param bot 机器人实例
     * @param message 消息内容，应为 json 数组消息，若 json 反序列化失败，将会尝试通过 CQ 码反序列化，若依然失败，返回为纯文本消息作为 fallback
     * @param source 消息源
     */
    internal suspend fun deserializeFromOneBot(bot: RemoteBot, message: String, source: MessageSource? = null, resolveReplyMessage: Boolean = true): MessageChain {
        return deserializeMessageFromJson(bot, message, source, resolveReplyMessage)
            ?: deserializeMessageFromCQCode(bot, message, source, resolveReplyMessage)
            ?: source?.plus(message)
            ?: PlainText(message).toMessageChain()
    }

    /**
     * 通过Json消息段反序列化消息
     *
     * @param bot 机器人实例
     * @param message 消息内容，应为 json 数组消息，若 json 反序列化失败，将会返回 null
     * @param source 消息源
     */
    internal suspend fun deserializeMessageFromJson(bot: RemoteBot, message: String, source: MessageSource? = null, resolveReplyMessage: Boolean = true): MessageChain? {
        return runCatching {
            Json.parseToJsonElement(message).jsonArray
        }.map {
            deserializeFromOneBotJson(bot, it, source, resolveReplyMessage)
        }.getOrNull()
    }
    /**
     * 根据CQ码反序列化消息
     *
     * @param bot 机器人实例
     * @param message 消息内容，CQ 码，CQ 码反序列化失败 (通常不会失败)，将会返回 null
     * @param source 消息源
     */
    internal suspend fun deserializeMessageFromCQCode(bot: RemoteBot, message: String, source: MessageSource? = null, resolveReplyMessage: Boolean = true): MessageChain? {
        return runCatching {
            val cqCodeToJson = CQCode.toJson(message).toString()
            Json.parseToJsonElement(cqCodeToJson).jsonArray
        }.map {
            deserializeFromOneBotJson(bot, it, source, resolveReplyMessage)
        }.getOrNull()
    }

    /**
     * 反序列化消息
     *
     * @see deserializeFromOneBot
     */
    internal suspend fun deserializeFromOneBotJson(bot: RemoteBot, json: JsonArray, source: MessageSource? = null, resolveReplyMessage: Boolean = true): MessageChain {
        return BuildMessageContextImpl.toMirai(bot, source, json, resolveReplyMessage)
    }

    internal fun imageFromFile(file: String): Image = Image.fromId(file)
    internal fun audioFromFile(file: String): Audio = WrappedAudio(file, 0)
    internal fun videoFromFile(file: String): ShortVideo = WrappedVideo(file)

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
