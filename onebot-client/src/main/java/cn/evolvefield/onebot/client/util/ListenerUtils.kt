package cn.evolvefield.onebot.client.util

import cn.evole.onebot.sdk.event.Event
import cn.evole.onebot.sdk.map.MessageMap
import cn.evole.onebot.sdk.util.json.JsonsObject

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 17:03
 * Version: 1.0
 */
object ListenerUtils {
    fun getMessageType(message: String?): Class<out Event> {
        return getMessageType(JsonsObject(message))
    }

    /**
     * 获取消息对应的实体类型
     *
     * @param jsonsObject json
     * @return
     */
    fun getMessageType(jsonsObject: JsonsObject): Class<out Event> {
        var type: String? = null
        val postType = jsonsObject.optString("post_type")
        if ("message" == postType) {
            type = "wholeMessage"
            //消息类型
            val messageType = jsonsObject.optString("message_type")
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
            type = jsonsObject.optString("request_type")
        } else if ("notice" == postType) {
            //通知类型
            type = jsonsObject.optString("notice_type")
        } else if ("meta_event" == postType) {
            //周期类型
            type = jsonsObject.optString("meta_event_type")
        }
        return MessageMap.messageMap[type]!!
    }
}