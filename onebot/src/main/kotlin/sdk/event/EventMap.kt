package cn.evolvefield.onebot.sdk.event

import cn.evolvefield.onebot.sdk.event.message.GroupMessageEvent
import cn.evolvefield.onebot.sdk.event.message.GuildMessageEvent
import cn.evolvefield.onebot.sdk.event.message.PrivateMessageEvent
import cn.evolvefield.onebot.sdk.event.meta.HeartbeatMetaEvent
import cn.evolvefield.onebot.sdk.event.meta.LifecycleMetaEvent
import cn.evolvefield.onebot.sdk.event.notice.friend.FriendAddNoticeEvent
import cn.evolvefield.onebot.sdk.event.notice.friend.PrivateMsgDeleteNoticeEvent
import cn.evolvefield.onebot.sdk.event.notice.group.*
import cn.evolvefield.onebot.sdk.event.notice.guild.ChannelCreatedNoticeEvent
import cn.evolvefield.onebot.sdk.event.notice.guild.ChannelDestroyedNoticeEvent
import cn.evolvefield.onebot.sdk.event.notice.guild.ChannelUpdateNoticeEvent
import cn.evolvefield.onebot.sdk.event.notice.guild.MessageReactionsUpdatedNoticeEvent
import cn.evolvefield.onebot.sdk.event.notice.misc.ReceiveOfflineFilesNoticeEvent
import cn.evolvefield.onebot.sdk.event.request.FriendAddRequestEvent
import cn.evolvefield.onebot.sdk.event.request.GroupAddRequestEvent

object EventMap {
    private val map = mapOf(
        "groupMessage" to GroupMessageEvent::class,
        "privateMessage" to PrivateMessageEvent::class,
        "wholeMessage" to GroupMessageEvent::class,
        "guildMessage" to GuildMessageEvent::class,
    
        "friend" to FriendAddRequestEvent::class,
        "group" to GroupAddRequestEvent::class,
    
        "group_upload" to GroupUploadNoticeEvent::class,
        "group_admin" to GroupAdminNoticeEvent::class,
        "group_decrease" to GroupDecreaseNoticeEvent::class,
        "group_increase" to GroupIncreaseNoticeEvent::class,
        "group_ban" to GroupBanNoticeEvent::class,
        "group_recall" to GroupMsgDeleteNoticeEvent::class,
        "notify" to GroupNotifyNoticeEvent::class,
        "poke" to GroupPokeNoticeEvent::class,
        "lucky_king" to GroupLuckyKingNoticeEvent::class,
        "honor" to GroupHonorChangeNoticeEvent::class,
        "group_card" to GroupCardChangeNoticeEvent::class,
    
        "friend_add" to FriendAddNoticeEvent::class,
        "friend_recall" to PrivateMsgDeleteNoticeEvent::class,
        "essence" to GroupEssenceNoticeEvent::class,
        "offline_file" to ReceiveOfflineFilesNoticeEvent::class,

        "channel_created" to ChannelCreatedNoticeEvent::class,
        "channel_destroyed" to ChannelDestroyedNoticeEvent::class,
        "channel_updated" to ChannelUpdateNoticeEvent::class,
        "message_reactions_updated" to MessageReactionsUpdatedNoticeEvent::class,
    
        "lifecycle" to LifecycleMetaEvent::class,
        "heartbeat" to HeartbeatMetaEvent::class,
    )
    operator fun get(type: String?): Class<out Event>? {
        return map[type ?: return null]?.run { java }
    }
}
