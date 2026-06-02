package top.mrxiaom.overflow.internal.message.serializer

import cn.evolvefield.onebot.sdk.response.group.ForwardMsgResp
import cn.evolvefield.onebot.sdk.util.JsonHelper.gson
import kotlinx.serialization.json.JsonObject
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.RawForwardMessage
import top.mrxiaom.overflow.internal.message.OnebotMessages.deserializeFromOneBot
import top.mrxiaom.overflow.internal.message.OnebotMessages.string
import top.mrxiaom.overflow.message.BuildMessageContext

class ForwardMessageSerializer : AbstractMessageSerializer(){
    override fun isMatchJson(element: JsonObject): Boolean {
        return element["type"].string == "forward"
    }

    override fun isMatchMirai(message: Message): Boolean {
        return message is ForwardMessage
    }

    override suspend fun toMirai(
        context: BuildMessageContext.ToMirai,
        element: JsonObject
    ): Boolean {
        val data = element.data() ?: return false
        val bot = context.bot
        val id = data["id"].string

        val nodes = run {
            val tryContent = kotlin.runCatching {
                gson.fromJson(data.toString(), ForwardMsgResp::class.java)
            }.getOrNull()

            //尝试将合并转发消息块作为完整的含数据消息进行解析，
            //若报错则主动获取合并转发消息，否则将其中的数据构造为合并转发消息
            if (tryContent != null) {
                // TODO: 使用新的方法解析 JSON 消息
                return@run tryContent.message.map {
                    val msg = deserializeFromOneBot(bot, it.message, resolveReplyMessage=false)
                    ForwardMessage.Node(
                        it.sender!!.userId,
                        it.time,
                        it.sender!!.nickname.takeIf(String::isNotEmpty) ?: "QQ用户",
                        msg
                    )
                }
            }

            if (id.isNotEmpty()) {
                return@run Mirai.downloadForwardMessage(bot as Bot, id)
            }
            throw IllegalStateException("合并转发消息没有ID")
        }

        val raw = RawForwardMessage(nodes)
        context.add(raw.render(ForwardMessage.DisplayStrategy))
        return true
    }

    override fun toJson(
        context: BuildMessageContext.ToJson,
        message: Message
    ): Boolean {
        // TODO: 需要独立的接口来处理转发消息节点的序列化
        return false
    }
}
