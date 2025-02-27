package cn.evolvefield.onebot.client.util

import cn.evolvefield.onebot.sdk.event.Event
import cn.evolvefield.onebot.sdk.event.EventMap
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 17:03
 * Version: 1.0
 */
object ListenerUtils {
    operator fun get(message: String): Class<out Event>? {
        return this[JsonParser.parseString(message).asJsonObject]
    }

    /**
     * 获取消息对应的实体类型
     *
     * @param obj json
     * @return 事件对应的类
     */
    operator fun get(obj: JsonObject): Class<out Event>? {
        val type = when (obj.get("post_type").asString) {
            // 消息类型
            "message_sent",
            "message" -> when (obj.get("message_type").asString) {
                    "group" -> "groupMessage" // 群聊消息类型
                    "private" -> "privateMessage" // 私聊消息类型
                    "guild" -> "guildMessage" // 频道消息，暂不支持私信
                    else -> "wholeMessage"
                }

            // 请求类型
            "request" -> obj.get("request_type").asString

            // 通知类型
            "notice" -> obj.get("notice_type").asString

            // 周期类型
            "meta_event" -> obj.get("meta_event_type").asString

            else -> return null
        }
        return EventMap[type]
    }
}
