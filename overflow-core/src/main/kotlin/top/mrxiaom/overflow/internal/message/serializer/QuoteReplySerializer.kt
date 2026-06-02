package top.mrxiaom.overflow.internal.message.serializer

import cn.evolvefield.onebot.sdk.response.group.GetMsgResp
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.put
import net.mamoe.mirai.Bot
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageSourceBuilder
import net.mamoe.mirai.message.data.MessageSourceKind
import net.mamoe.mirai.message.data.QuoteReply
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.internal.message.OnebotMessages.toMiraiMessage
import top.mrxiaom.overflow.message.BuildMessageContext

class QuoteReplySerializer : AbstractMessageSerializer() {
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "reply"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is QuoteReply
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        val bot = context.bot
        val id = when {
            else -> data["id"].string.toInt()
        }
        val msgData: GetMsgResp? =
            if (context.extra.containsKey("resolveReplyMessage")) {
                (bot as BotWrapper).impl.getMsg(id) { throwExceptions(null) }.data
            } else {
                null
            }
        val msgSource = MessageSourceBuilder()
            .id(id)
            .internalId(id)
        if (msgData != null) msgSource
            .sender(msgData.sender.userId.toLong())
            .target(msgData.groupId.takeIf { it != 0L }?:msgData.targetId)  // LLOneBot 没有提供 targetId
            .messages(toMiraiMessage(msgData.isJsonMessage, msgData.message, bot) as Iterable<Message>)
            .time(msgData.time)
        val kind = if (msgData?.groupId == 0L) MessageSourceKind.FRIEND else MessageSourceKind.GROUP

        // lagrange
        if (context.currentIndex == 0 && context.appName.lowercase() == "lagrange.onebot") {
            context.extra["hasQuote"] = true
        }

        context.add(QuoteReply(msgSource.build((bot as Bot).id, kind)))
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        val element = message as? QuoteReply ?: return false
        context.add("reply") {
            // 忽略分片消息情况
            put("id", element.source.ids[0].toString())
        }
        return true
    }
}