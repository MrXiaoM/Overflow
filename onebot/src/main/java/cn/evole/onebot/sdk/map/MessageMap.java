package cn.evole.onebot.sdk.map;

import cn.evole.onebot.sdk.event.Event;
import cn.evole.onebot.sdk.event.message.GroupMessageEvent;
import cn.evole.onebot.sdk.event.message.GuildMessageEvent;
import cn.evole.onebot.sdk.event.message.PrivateMessageEvent;
import cn.evole.onebot.sdk.event.meta.HeartbeatMetaEvent;
import cn.evole.onebot.sdk.event.meta.LifecycleMetaEvent;
import cn.evole.onebot.sdk.event.notice.friend.FriendAddNoticeEvent;
import cn.evole.onebot.sdk.event.notice.friend.PrivateMsgDeleteNoticeEvent;
import cn.evole.onebot.sdk.event.notice.group.*;
import cn.evole.onebot.sdk.event.notice.misc.ReceiveOfflineFilesNoticeEvent;
import cn.evole.onebot.sdk.event.request.FriendAddRequestEvent;
import cn.evole.onebot.sdk.event.request.GroupAddRequestEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 17:04
 * Version: 1.0
 */
public class MessageMap {
    public static Map<String, Class<? extends Event>> messageMap = new HashMap<String, Class<? extends Event>>() {{
        put("groupMessage", GroupMessageEvent.class);
        put("privateMessage", PrivateMessageEvent.class);
        put("wholeMessage", GroupMessageEvent.class);
        put("guildMessage", GuildMessageEvent.class);

        put("friend", FriendAddRequestEvent.class);
        put("group", GroupAddRequestEvent.class);

        put("group_upload", GroupUploadNoticeEvent.class);
        put("group_admin", GroupAdminNoticeEvent.class);
        put("group_decrease", GroupDecreaseNoticeEvent.class);
        put("group_increase", GroupIncreaseNoticeEvent.class);
        put("group_ban", GroupBanNoticeEvent.class);
        put("group_recall", GroupMsgDeleteNoticeEvent.class);
        put("notify", GroupNotifyNoticeEvent.class);
        put("poke", GroupPokeNoticeEvent.class);
        put("lucky_king", GroupLuckyKingNoticeEvent.class);
        put("honor", GroupHonorChangeNoticeEvent.class);
        put("group_card", GroupCardChangeNoticeEvent.class);

        put("friend_add", FriendAddNoticeEvent.class);
        put("friend_recall", PrivateMsgDeleteNoticeEvent.class);
        put("essence", GroupEssenceNoticeEvent.class);
        put("offline_file", ReceiveOfflineFilesNoticeEvent.class);

        put("lifecycle", LifecycleMetaEvent.class);
        put("heartbeat", HeartbeatMetaEvent.class);
    }};
}
