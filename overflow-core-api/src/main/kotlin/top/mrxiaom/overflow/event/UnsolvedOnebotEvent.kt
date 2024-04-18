package top.mrxiaom.overflow.event

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.CancellableEvent

public class UnsolvedOnebotEvent(
    /**
     * 机器人QQ号
     */
    public val botId: Long,
    /**
     * 收到的原事件内容
     */
    public val messageRaw: String,
    /**
     * 收到事件的时间
     */
    public val time: Long,
): AbstractEvent(), CancellableEvent {
    /**
     * 获取机器人实例，可能会失败
     */
    public val bot: Bot
        get() = Bot.getInstance(botId)

    /**
     * 获取机器人实例，可能会失败
     */
    public val botOrNull: Bot?
        get() = Bot.getInstanceOrNull(botId)

    /**
     * 读取事件内容为 json
     */
    public val json: JsonObject
        get() = Json.parseToJsonElement(messageRaw).jsonObject

    public override fun toString(): String = "UnsolvedOnebotEvent(bot=$botId, time=$time, message=$messageRaw)"
}
