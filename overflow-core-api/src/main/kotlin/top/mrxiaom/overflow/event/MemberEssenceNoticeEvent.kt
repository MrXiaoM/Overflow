package top.mrxiaom.overflow.event

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.message.data.MessageChain
import top.mrxiaom.overflow.contact.RemoteBot.Companion.asRemoteBot

public abstract class MemberEssenceNoticeEvent private constructor(
    /**
     * 机器人
     */
    public val bot: Bot,
    /**
     * 群聊
     */
    public val group: Group,
    /**
     * 消息发送者
     */
    public val member: NormalMember,
    /**
     * 精华消息添加者
     */
    public val operator: NormalMember,
    /**
     * 精华消息ID
     */
    public val messageId: Int
): AbstractEvent() {

    /**
     * 获取精华消息内容
     */
    @JvmBlockingBridge
    suspend fun queryMessage(): MessageChain? {
        return bot.asRemoteBot.getMsg(messageId)
    }

    protected open val type: String = ""

    public class Add(
        bot: Bot,
        group: Group,
        member: NormalMember,
        operator: NormalMember,
        messageId: Int
    ) : MemberEssenceNoticeEvent(bot, group, member, operator, messageId) {
        override val type: String = ".Add"
    }

    public class Delete(
        bot: Bot,
        group: Group,
        member: NormalMember,
        operator: NormalMember,
        messageId: Int
    ) : MemberEssenceNoticeEvent(bot, group, member, operator, messageId) {
        override val type: String = ".Delete"
    }

    public override fun toString(): String = "MemberEssenceNoticeEvent$type(bot=${bot.id}, group=${group.id}, operator=${operator.id}, member=${member.id}, messageId=$messageId)"
}
