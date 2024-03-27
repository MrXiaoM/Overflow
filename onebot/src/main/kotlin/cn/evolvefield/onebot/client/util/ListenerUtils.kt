package cn.evolvefield.onebot.client.util

import cn.evole.onebot.sdk.event.Event
import cn.evole.onebot.sdk.map.MessageMap
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 17:03
 * Version: 1.0
 */
object ListenerUtils {
    fun getMessageType(message: String?): Class<out Event> {
        return getMessageType(JsonParser.parseString(message).asJsonObject)
    }

    /**
     * 获取消息对应的实体类型
     *
     * @param obj json
     * @return
     */
    fun getMessageType(obj: JsonObject): Class<out Event> {
        var type: String? = null
        val postType = obj.get("post_type").asString
        if ("message" == postType) {
            type = "wholeMessage"
            //消息类型
            val messageType = obj.get("message_type").asString
            if ("group" == messageType) {
                //群聊消息类型
                type = "groupMessage"
            } else if ("private" == messageType) {
                //私聊消息类型
                type = "privateMessage"
            } else if ("guild" == messageType) {
                //频道消息，暂不支持私信
                type = "guildMessage"
            }
        } else if ("request" == postType) {
            //请求类型
            type = obj.get("request_type").asString
        } else if ("notice" == postType) {
            //通知类型
            type = obj.get("notice_type").asString
        } else if ("meta_event" == postType) {
            //周期类型
            type = obj.get("meta_event_type").asString
        }
        return MessageMap.messageMap[type] ?: throw OnebotException("无法解析类型为 $type 的事件")
    }
}
