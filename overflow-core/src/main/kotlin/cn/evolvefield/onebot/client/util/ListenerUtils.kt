package cn.evolvefield.onebot.client.util

import cn.evolvefield.onebot.sdk.event.Event
import cn.evolvefield.onebot.sdk.event.message.GroupMessageEvent
import cn.evolvefield.onebot.sdk.event.message.PrivateMessageEvent
import cn.evolvefield.onebot.sdk.event.meta.HeartbeatMetaEvent
import cn.evolvefield.onebot.sdk.event.meta.LifecycleMetaEvent
import cn.evolvefield.onebot.sdk.event.notice.NotifyNoticeEvent
import cn.evolvefield.onebot.sdk.event.notice.friend.FriendAddNoticeEvent
import cn.evolvefield.onebot.sdk.event.notice.friend.PrivateMsgDeleteNoticeEvent
import cn.evolvefield.onebot.sdk.event.notice.group.*
import cn.evolvefield.onebot.sdk.event.notice.misc.GroupMsgEmojiLikeNotice
import cn.evolvefield.onebot.sdk.event.notice.misc.GroupReactionNotice
import cn.evolvefield.onebot.sdk.event.notice.misc.ReceiveOfflineFilesNoticeEvent
import cn.evolvefield.onebot.sdk.event.request.FriendAddRequestEvent
import cn.evolvefield.onebot.sdk.event.request.GroupAddRequestEvent
import cn.evolvefield.onebot.sdk.util.ignorable
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlin.reflect.KClass

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 17:03
 * Version: 1.0
 */
object ListenerUtils {
    private val map = mapOf(
        "groupMessage" to GroupMessageEvent::class,
        "privateMessage" to PrivateMessageEvent::class,
        "wholeMessage" to GroupMessageEvent::class,

        "friend" to FriendAddRequestEvent::class,
        "group" to GroupAddRequestEvent::class,

        "group_upload" to GroupUploadNoticeEvent::class,
        "group_admin" to GroupAdminNoticeEvent::class,
        "group_decrease" to GroupDecreaseNoticeEvent::class,
        "group_increase" to GroupIncreaseNoticeEvent::class,
        "group_ban" to GroupBanNoticeEvent::class,
        "group_recall" to GroupMsgDeleteNoticeEvent::class,
        "group_name_change" to GroupNameChangeNoticeEvent::class,
        "notify" to NotifyNoticeEvent::class,
        "lucky_king" to GroupLuckyKingNoticeEvent::class,
        "honor" to GroupHonorChangeNoticeEvent::class,
        "group_card" to GroupCardChangeNoticeEvent::class,

        "friend_add" to FriendAddNoticeEvent::class,
        "friend_recall" to PrivateMsgDeleteNoticeEvent::class,
        "essence" to GroupEssenceNoticeEvent::class,
        "offline_file" to ReceiveOfflineFilesNoticeEvent::class,

        // LLOnebot, NapCat
        "group_msg_emoji_like" to GroupMsgEmojiLikeNotice::class,
        // Lagrange
        "reaction" to GroupReactionNotice::class,

        "lifecycle" to LifecycleMetaEvent::class,
        "heartbeat" to HeartbeatMetaEvent::class,
    )
    operator fun get(message: String): KClass<out Event>? {
        return this[JsonParser.parseString(message).asJsonObject]
    }

    /**
     * 获取消息对应的实体类型
     *
     * @param obj json
     * @return 事件对应的类
     */
    operator fun get(obj: JsonObject): KClass<out Event>? {
        val type = when (obj.ignorable("post_type", "")) {
            // 消息类型
            "message_sent",
            "message" -> when (obj.ignorable("message_type", "")) {
                    "group" -> "groupMessage" // 群聊消息类型
                    "private" -> "privateMessage" // 私聊消息类型
                    "guild" -> "guildMessage" // 频道消息，暂不支持私信
                    else -> "wholeMessage"
                }

            // 请求类型
            "request" -> obj.ignorable("request_type", "")

            // 通知类型
            "notice" -> obj.ignorable("notice_type", "")

            // 周期类型
            "meta_event" -> obj.ignorable("meta_event_type", "")

            else -> return null
        }
        return map[type]
    }
}
