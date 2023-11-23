package cn.evolvefield.onebot.client.core;


import cn.evolvefield.onebot.client.handler.ActionHandler;
import cn.evole.onebot.sdk.action.ActionData;
import cn.evole.onebot.sdk.action.ActionList;
import cn.evole.onebot.sdk.action.ActionPath;
import cn.evole.onebot.sdk.action.ActionRaw;
import cn.evole.onebot.sdk.entity.Anonymous;
import cn.evole.onebot.sdk.entity.GuildMsgId;
import cn.evole.onebot.sdk.entity.MsgId;
import cn.evole.onebot.sdk.enums.ActionPathEnum;
import cn.evole.onebot.sdk.event.message.GroupMessageEvent;
import cn.evole.onebot.sdk.response.contact.FriendInfoResp;
import cn.evole.onebot.sdk.response.contact.LoginInfoResp;
import cn.evole.onebot.sdk.response.contact.StrangerInfoResp;
import cn.evole.onebot.sdk.response.contact.UnidirectionalFriendListResp;
import cn.evole.onebot.sdk.response.group.*;
import cn.evole.onebot.sdk.response.guild.*;
import cn.evole.onebot.sdk.response.misc.*;
import cn.evole.onebot.sdk.util.json.GsonUtil;
import cn.evole.onebot.sdk.util.json.JsonsObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.java_websocket.WebSocket;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 15:19
 * Version: 1.0
 */
@SuppressWarnings("unused")
public class Bot {

    private final ActionHandler actionHandler;

    @Getter
    @Setter
    private WebSocket channel;

    /**
     * @param channel                    {@link WebSocket}
     * @param actionHandler              {@link ActionHandler}
     */
    public Bot(WebSocket channel, ActionHandler actionHandler) {
        this.channel = channel;
        this.actionHandler = actionHandler;
    }

    /**
     * 发送消息
     *
     * @param event      {@link GroupMessageEvent}
     * @param msg        要发送的内容
     * @param autoEscape 消息内容是否作为纯文本发送 ( 即不解析 CQ 码 ) , 只在 message 字段是字符串时有效
     * @return {@link ActionData} of {@link MsgId}
     */
    public ActionData<MsgId> sendMsg(GroupMessageEvent event, String msg, boolean autoEscape) {
        switch (event.getMessageType()) {
            case "private":
                return sendPrivateMsg(event.getUserId(), msg, autoEscape);
            case "group" :
                return sendGroupMsg(event.getGroupId(), msg, autoEscape);
        }
        return null;
    }

    /**
     * 发送私聊消息
     *
     * @param userId     对方 QQ 号
     * @param msg        要发送的内容
     * @param autoEscape 消息内容是否作为纯文本发送 ( 即不解析 CQ 码 ) , 只在 message 字段是字符串时有效
     * @return {@link ActionData} of {@link MsgId}
     */
    public ActionData<MsgId> sendPrivateMsg(long userId, String msg, boolean autoEscape) {
        ActionPathEnum action = ActionPathEnum.SEND_PRIVATE_MSG;

        JsonObject params = new JsonObject();

        params.addProperty("user_id", userId);
        params.addProperty("message", msg);
        params.addProperty("auto_escape", autoEscape);
        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<MsgId>>() {}.getType()) : null;
    }

    /**
     * 发送群消息
     *
     * @param groupId    群号
     * @param msg        要发送的内容
     * @param autoEscape 消息内容是否作为纯文本发送 ( 即不解析 CQ 码 ) , 只在 message 字段是字符串时有效
     * @return {@link ActionData} of {@link MsgId}
     */
    public ActionData<MsgId> sendGroupMsg(long groupId, String msg, boolean autoEscape) {
        ActionPathEnum action = ActionPathEnum.SEND_GROUP_MSG;
        JsonObject params = new JsonObject();
        params.addProperty("group_id", groupId);
        params.addProperty("message", msg);
        params.addProperty("auto_escape", autoEscape);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<MsgId>>() {}.getType()) : null;

    }

    /**
     * 获取频道成员列表
     * 由于频道人数较多(数万), 请尽量不要全量拉取成员列表, 这将会导致严重的性能问题
     * 尽量使用 getGuildMemberProfile 接口代替全量拉取
     * nextToken 为空的情况下, 将返回第一页的数据, 并在返回值附带下一页的 token
     *
     * @param guildId   频道ID
     * @param nextToken 翻页Token
     * @return {@link ActionData} of {@link GuildMemberListResp}
     */
    public ActionData<GuildMemberListResp> getGuildMemberList(String guildId, String nextToken) {
        ActionPathEnum action = ActionPathEnum.GET_GUILD_LIST;
        JsonObject params = new JsonObject();
            params.addProperty("guild_id", guildId);
            params.addProperty("next_token", nextToken);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GuildMemberListResp>>() {
        }.getType()) : null;
    }

    /**
     * 发送信息到子频道
     *
     * @param guildId   频道 ID
     * @param channelId 子频道 ID
     * @param msg       要发送的内容
     * @return {@link ActionData} of {@link GuildMsgId}
     */
    public ActionData<GuildMsgId> sendGuildMsg(String guildId, String channelId, String msg) {
        ActionPathEnum action = ActionPathEnum.SEND_GUILD_CHANNEL_MSG;
        JsonObject params = new JsonObject();
            params.addProperty("guild_id", guildId);
            params.addProperty("channel_id", channelId);
            params.addProperty("message", msg);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GuildMsgId>>() {
        }.getType()) : null;
    }

    /**
     * 获取频道消息
     *
     * @param guildMsgId 频道 ID
     * @param noCache    是否使用缓存
     * @return {@link ActionData} of {@link GetGuildMsgResp}
     */
    public ActionData<GetGuildMsgResp> getGuildMsg(String guildMsgId, boolean noCache) {
        ActionPathEnum action = ActionPathEnum.GET_GUILD_MSG;
        JsonObject params = new JsonObject();
            params.addProperty("message_id", guildMsgId);
            params.addProperty("no_cache", noCache);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GetGuildMsgResp>>() {
        }.getType()) : null;
    }

    /**
     * 获取频道系统内 BOT 的资料
     *
     * @return {@link ActionData} of {@link GuildServiceProfileResp}
     */
    public ActionData<GuildServiceProfileResp> getGuildServiceProfile() {
        ActionPathEnum action = ActionPathEnum.GET_GUILD_SERVICE_PROFILE;
        JsonsObject result = actionHandler.action(channel, action, null);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GuildServiceProfileResp>>() {
        }.getType()) : null;
    }

    /**
     * 获取频道列表
     *
     * @return {@link ActionList} of {@link GuildListResp}
     */
    public ActionList<GuildListResp> getGuildList() {
        ActionPathEnum action = ActionPathEnum.GET_GUILD_LIST;
        JsonsObject result = actionHandler.action(channel, action, null);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionList<GuildListResp>>() {
        }.getType()) : null;
    }

    /**
     * 通过访客获取频道元数据
     *
     * @param guildId 频道 ID
     * @return {@link ActionData} of {@link GuildMetaByGuestResp}
     */
    public ActionData<GuildMetaByGuestResp> getGuildMetaByGuest(String guildId) {
        ActionPathEnum action = ActionPathEnum.GET_GUILD_META_BY_GUEST;
        JsonObject params = new JsonObject();
            params.addProperty("guild_id", guildId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GuildMetaByGuestResp>>() {
        }.getType()) : null;
    }

    /**
     * 获取子频道列表
     *
     * @param guildId 频道 ID
     * @param noCache 是否无视缓存
     * @return {@link ActionList} of {@link ChannelInfoResp}
     */
    public ActionList<ChannelInfoResp> getGuildChannelList(String guildId, boolean noCache) {
        ActionPathEnum action = ActionPathEnum.GET_GUILD_CHANNEL_LIST;
        JsonObject params = new JsonObject();
            params.addProperty("guild_id", guildId);
            params.addProperty("no_cache", noCache);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionList<ChannelInfoResp>>() {
        }.getType()) : null;
    }

    /**
     * 单独获取频道成员信息
     *
     * @param guildId 频道ID
     * @param userId  用户ID
     * @return {@link ActionData} of {@link GuildMemberProfileResp}
     */
    public ActionData<GuildMemberProfileResp> getGuildMemberProfile(String guildId, String userId) {
        ActionPathEnum action = ActionPathEnum.GET_GUILD_MEMBER_PROFILE;
        JsonObject params = new JsonObject();
            params.addProperty("guild_id", guildId);
            params.addProperty("user_id", userId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GuildMemberProfileResp>>() {
        }.getType()) : null;
    }

    /**
     * 获取消息
     *
     * @param msgId 消息 ID
     * @return {@link ActionData} of {@link GetMsgResp}
     */
    public ActionData<GetMsgResp> getMsg(int msgId) {
        ActionPathEnum action = ActionPathEnum.GET_MSG;
        JsonObject params = new JsonObject();
            params.addProperty("message_id", msgId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GetMsgResp>>() {
        }.getType()) : null;
    }

    /**
     * 撤回消息
     *
     * @param msgId 消息 ID
     * @return {@link ActionRaw}
     */
    public ActionRaw deleteMsg(int msgId) {
        ActionPathEnum action = ActionPathEnum.DELETE_MSG;
        JsonObject params = new JsonObject();
            params.addProperty("message_id", msgId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(), ActionRaw.class) : null;
    }

    /**
     * 群组踢人
     *
     * @param groupId          群号
     * @param userId           要踢的 QQ 号
     * @param rejectAddRequest 拒绝此人的加群请求 (默认false)
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupKick(long groupId, long userId, boolean rejectAddRequest) {
        ActionPathEnum action = ActionPathEnum.SET_GROUP_KICK;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("user_id", userId);
            params.addProperty("reject_add_request", rejectAddRequest);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 群组单人禁言
     *
     * @param groupId  群号
     * @param userId   要禁言的 QQ 号
     * @param duration 禁言时长, 单位秒, 0 表示取消禁言 (默认30 * 60)
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupBan(long groupId, long userId, int duration) {
        ActionPathEnum action = ActionPathEnum.SET_GROUP_BAN;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("user_id", userId);
            params.addProperty("duration", duration);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 全体禁言
     *
     * @param groupId 群号
     * @param enable  是否禁言（默认True,False为取消禁言）
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupWholeBan(long groupId, boolean enable) {
        ActionPathEnum action = ActionPathEnum.SET_GROUP_WHOLE_BAN;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("enable", enable);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 群组设置管理员
     *
     * @param groupId 群号
     * @param userId  要设置管理员的 QQ 号
     * @param enable  true 为设置，false 为取消
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupAdmin(long groupId, long userId, boolean enable) {
        ActionPathEnum action = ActionPathEnum.SET_GROUP_ADMIN;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("user_id", userId);
            params.addProperty("enable", enable);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 群组匿名
     *
     * @param groupId 群号
     * @param enable  是否允许匿名聊天
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupAnonymous(long groupId, boolean enable) {
        ActionPathEnum action = ActionPathEnum.SET_GROUP_ANONYMOUS;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("enable", enable);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 设置群名片（群备注）
     *
     * @param groupId 群号
     * @param userId  要设置的 QQ 号
     * @param card    群名片内容，不填或空字符串表示删除群名片
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupCard(long groupId, long userId, String card) {
        ActionPathEnum action = ActionPathEnum.SET_GROUP_CARD;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("user_id", userId);
            params.addProperty("card", card);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 设置群名
     *
     * @param groupId   群号
     * @param groupName 新群名
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupName(long groupId, String groupName) {
        ActionPathEnum action = ActionPathEnum.SET_GROUP_NAME;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("group_name", groupName);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 退出群组
     *
     * @param groupId   群号
     * @param isDismiss 是否解散, 如果登录号是群主, 则仅在此项为 true 时能够解散
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupLeave(long groupId, boolean isDismiss) {
        ActionPathEnum action = ActionPathEnum.SET_GROUP_LEAVE;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("is_dismiss", isDismiss);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 设置群组专属头衔
     *
     * @param groupId      群号
     * @param userId       要设置的 QQ 号
     * @param specialTitle 专属头衔，不填或空字符串表示删除专属头衔
     * @param duration     专属头衔有效期，单位秒，-1 表示永久，不过此项似乎没有效果，可能是只有某些特殊的时间长度有效，有待测试
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupSpecialTitle(long groupId, long userId, String specialTitle, int duration) {
        ActionPathEnum action = ActionPathEnum.SET_GROUP_SPECIAL_TITLE;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("user_id", userId);
            params.addProperty("special_title", specialTitle);
            params.addProperty("duration", duration);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 处理加好友请求
     *
     * @param flag    加好友请求的 flag（需从上报的数据中获得）
     * @param approve 是否同意请求(默认为true)
     * @param remark  添加后的好友备注（仅在同意时有效）
     * @return {@link ActionRaw}
     */
    public ActionRaw setFriendAddRequest(String flag, boolean approve, String remark) {
        ActionPathEnum action = ActionPathEnum.SET_FRIEND_ADD_REQUEST;
        JsonObject params = new JsonObject();
            params.addProperty("flag", flag);
            params.addProperty("approve", approve);
            params.addProperty("remark", remark);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 处理加群请求／邀请
     *
     * @param flag    加群请求的 flag（需从上报的数据中获得）
     * @param subType add 或 invite，请求类型（需要和上报消息中的 sub_type 字段相符）
     * @param approve 是否同意请求／邀请
     * @param reason  拒绝理由（仅在拒绝时有效）
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupAddRequest(String flag, String subType, boolean approve, String reason) {
        ActionPathEnum action = ActionPathEnum.SET_GROUP_ADD_REQUEST;
        JsonObject params = new JsonObject();
            params.addProperty("flag", flag);
            params.addProperty("sub_type", subType);
            params.addProperty("approve", approve);
            params.addProperty("reason", reason);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 获取登录号信息
     *
     * @return {@link ActionData} of @{@link LoginInfoResp}
     */
    public ActionData<LoginInfoResp> getLoginInfo() {
        ActionPathEnum action = ActionPathEnum.GET_LOGIN_INFO;
        JsonsObject result = actionHandler.action(channel, action, null);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<LoginInfoResp>>() {
        }.getType()) : null;
    }

    /**
     * 获取陌生人信息
     *
     * @param userId  QQ 号
     * @param noCache 是否不使用缓存（使用缓存可能更新不及时，但响应更快）
     * @return {@link ActionData} of {@link StrangerInfoResp}
     */
    public ActionData<StrangerInfoResp> getStrangerInfo(long userId, boolean noCache) {
        ActionPathEnum action = ActionPathEnum.GET_STRANGER_INFO;
        JsonObject params = new JsonObject();
            params.addProperty("user_id", userId);
            params.addProperty("no_cache", noCache);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<StrangerInfoResp>>() {
        }.getType()) : null;
    }

    /**
     * 获取好友列表
     *
     * @return {@link ActionList} of {@link FriendInfoResp}
     */
    public ActionList<FriendInfoResp> getFriendList() {
        ActionPathEnum action = ActionPathEnum.GET_FRIEND_LIST;
        JsonsObject result = actionHandler.action(channel, action, null);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionList<FriendInfoResp>>() {
        }.getType()) : null;
    }

    /**
     * 删除好友
     *
     * @param friendId 好友 QQ 号
     * @return {@link ActionRaw}
     */
    public ActionRaw deleteFriend(long friendId) {
        ActionPathEnum action = ActionPathEnum.DELETE_FRIEND;
        JsonObject params = new JsonObject();
            params.addProperty("friend_id", friendId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 获取群信息
     *
     * @param groupId 群号
     * @param noCache 是否不使用缓存（使用缓存可能更新不及时，但响应更快）
     * @return {@link ActionData} of {@link GroupInfoResp}
     */
    public ActionData<GroupInfoResp> getGroupInfo(long groupId, boolean noCache) {
        ActionPathEnum action = ActionPathEnum.GET_GROUP_INFO;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("no_cache", noCache);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GroupInfoResp>>() {
        }.getType()) : null;
    }

    /**
     * 获取群列表
     *
     * @return {@link ActionList} of {@link GroupInfoResp}
     */
    public ActionList<GroupInfoResp> getGroupList() {
        ActionPathEnum action = ActionPathEnum.GET_GROUP_LIST;
        JsonsObject result = actionHandler.action(channel, action, null);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionList<GroupInfoResp>>() {
        }.getType()) : null;
    }

    /**
     * 获取群成员信息
     *
     * @param groupId 群号
     * @param userId  QQ 号
     * @param noCache 是否不使用缓存（使用缓存可能更新不及时，但响应更快）
     * @return {@link ActionData} of {@link GroupMemberInfoResp}
     */
    public ActionData<GroupMemberInfoResp> getGroupMemberInfo(long groupId, long userId, boolean noCache) {
        ActionPathEnum action = ActionPathEnum.GET_GROUP_MEMBER_INFO;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("user_id", userId);
            params.addProperty("no_cache", noCache);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GroupMemberInfoResp>>() {
        }.getType()) : null;
    }

    /**
     * 获取群成员列表
     *
     * @param groupId 群号
     * @return {@link ActionList} of {@link GroupMemberInfoResp}
     */
    public ActionList<GroupMemberInfoResp> getGroupMemberList(long groupId) {
        ActionPathEnum action = ActionPathEnum.GET_GROUP_MEMBER_LIST;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionList<GroupMemberInfoResp>>() {
        }.getType()) : null;
    }

    /**
     * 获取群荣誉信息
     *
     * @param groupId 群号
     * @param type    要获取的群荣誉类型, 可传入 talkative performer legend strong_newbie emotion 以分别获取单个类型的群荣誉数据, 或传入 all 获取所有数据
     * @return {@link ActionData} of {@link GroupHonorInfoResp}
     */
    public ActionData<GroupHonorInfoResp> getGroupHonorInfo(long groupId, String type) {
        ActionPathEnum action = ActionPathEnum.GET_GROUP_HONOR_INFO;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("type", type);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GroupHonorInfoResp>>() {
        }.getType()) : null;
    }

    /**
     * 检查是否可以发送图片
     *
     * @return {@link ActionData} of {@link BooleanResp}
     */
    public ActionData<BooleanResp> canSendImage() {
        ActionPathEnum action = ActionPathEnum.CAN_SEND_IMAGE;
        JsonsObject result = actionHandler.action(channel, action, null);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<BooleanResp>>() {
        }.getType()) : null;
    }

    /**
     * 检查是否可以发送语音
     *
     * @return {@link ActionData} of {@link BooleanResp}
     */
    public ActionData<BooleanResp> canSendRecord() {
        ActionPathEnum action = ActionPathEnum.CAN_SEND_RECORD;
        JsonsObject result = actionHandler.action(channel, action, null);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<BooleanResp>>() {
        }.getType()) : null;
    }

    /**
     * 设置群头像
     * 目前这个API在登录一段时间后因cookie失效而失效, 请考虑后使用
     *
     * @param groupId 群号
     * @param file    图片文件名（支持绝对路径，网络URL，Base64编码）
     * @param cache   表示是否使用已缓存的文件 （通过网络URL发送时有效, 1表示使用缓存, 0关闭关闭缓存, 默认为1）
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupPortrait(long groupId, String file, int cache) {
        ActionPathEnum action = ActionPathEnum.SET_GROUP_PORTRAIT;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("file", file);
            params.addProperty("cache", cache);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 检查链接安全性
     * 安全等级, 1: 安全 2: 未知 3: 危险
     *
     * @param url 需要检查的链接
     * @return {@link ActionData} of {@link CheckUrlSafelyResp}
     */
    public ActionData<CheckUrlSafelyResp> checkUrlSafely(String url) {
        ActionPathEnum action = ActionPathEnum.CHECK_URL_SAFELY;
        JsonObject params = new JsonObject();
            params.addProperty("url", url);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<CheckUrlSafelyResp>>() {
        }.getType()) : null;
    }

    /**
     * 发送群公告
     *
     * @param groupId 群号
     * @param content 公告内容
     * @return {@link ActionRaw}
     */
    public ActionRaw sendGroupNotice(long groupId, String content) {
        ActionPathEnum action = ActionPathEnum.SEN_GROUP_NOTICE;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("content", content);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 获取群 @全体成员 剩余次数
     *
     * @param groupId 群号
     * @return {@link ActionData} of {@link GroupAtAllRemainResp}
     */
    public ActionData<GroupAtAllRemainResp> getGroupAtAllRemain(long groupId) {
        ActionPathEnum action = ActionPathEnum.GET_GROUP_AT_ALL_REMAIN;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GroupAtAllRemainResp>>() {
        }.getType()) : null;
    }

    /**
     * 上传群文件
     * 在不提供 folder 参数的情况下默认上传到根目录
     * 只能上传本地文件, 需要上传 http 文件的话请先下载到本地
     *
     * @param groupId 群号
     * @param file    本地文件路径
     * @param name    储存名称
     * @param folder  父目录ID
     * @return {@link ActionRaw}
     */
    public ActionRaw uploadGroupFile(long groupId, String file, String name, String folder) {
        ActionPathEnum action = ActionPathEnum.UPLOAD_GROUP_FILE;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("file", file);
            params.addProperty("name", name);
            params.addProperty("folder", folder);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 上传群文件
     * 在不提供 folder 参数的情况下默认上传到根目录
     * 只能上传本地文件, 需要上传 http 文件的话请先下载到本地
     *
     * @param groupId 群号
     * @param file    本地文件路径
     * @param name    储存名称
     * @return {@link ActionRaw}
     */
    public ActionRaw uploadGroupFile(long groupId, String file, String name) {
        ActionPathEnum action = ActionPathEnum.UPLOAD_GROUP_FILE;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("file", file);
            params.addProperty("name", name);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 群组匿名用户禁言
     *
     * @param groupId   群号
     * @param anonymous 要禁言的匿名用户对象（群消息上报的 anonymous 字段）
     * @param duration  禁言时长，单位秒，无法取消匿名用户禁言
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupAnonymousBan(long groupId, Anonymous anonymous, boolean duration) {
        Gson gson = new GsonBuilder().create();
        ActionPathEnum action = ActionPathEnum.SET_GROUP_ANONYMOUS_BAN;
        String an = gson.toJson(anonymous, Anonymous.class);
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.add("anonymous", new JsonsObject(an).get());
            params.addProperty("duration", duration);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 群组匿名用户禁言
     *
     * @param groupId  群号
     * @param flag     要禁言的匿名用户的 flag（需从群消息上报的数据中获得）
     * @param duration 禁言时长，单位秒，无法取消匿名用户禁言
     * @return {@link ActionRaw}
     */
    public ActionRaw setGroupAnonymousBan(long groupId, String flag, boolean duration) {
        ActionPathEnum action = ActionPathEnum.SET_GROUP_ANONYMOUS_BAN;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("flag", flag);
            params.addProperty("duration", duration);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 调用 go cq http 下载文件
     *
     * @param url         链接地址
     * @param threadCount 下载线程数
     * @param headers     自定义请求头
     * @return {@link ActionData} of {@link DownloadFileResp}
     */
    public ActionData<DownloadFileResp> downloadFile(String url, int threadCount, String headers) {
        ActionPathEnum action = ActionPathEnum.DOWNLOAD_FILE;
        JsonObject params = new JsonObject();
            params.addProperty("url", url);
            params.addProperty("thread_count", threadCount);
            params.addProperty("headers", headers);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<DownloadFileResp>>() {
        }.getType()) : null;
    }

    /**
     * 调用 go cq http 下载文件
     *
     * @param url 链接地址
     * @return {@link ActionData} of {@link DownloadFileResp}
     */
    public ActionData<DownloadFileResp> downloadFile(String url) {
        ActionPathEnum action = ActionPathEnum.DOWNLOAD_FILE;
        JsonObject params = new JsonObject();
            params.addProperty("url", url);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<DownloadFileResp>>() {
        }.getType()) : null;

    }

    /**
     * 发送合并转发 (群)
     *
     * @param groupId 群号
     * @param msg     自定义转发消息 (可使用 ShiroUtils.generateForwardMsg() 方法创建)
     *                <a href="https://docs.go-cqhttp.org/cqcode/#%E5%90%88%E5%B9%B6%E8%BD%AC%E5%8F%91">参考文档</a>
     * @return {@link ActionRaw}
     */
//    public ActionData<MsgId> sendGroupForwardMsg(long groupId, List<Map<String, Object>> msg) {
//        ActionPathEnum action = ActionPathEnum.SEND_GROUP_FORWARD_MSG;
//        JsonObject params = new JsonObject();
//            params.addProperty("group_id", groupId);
//            params.addProperty("messages", msg);
//
//        JsonsObject result = actionHandler.action(channel, action, params);
//        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<MsgId>>() {
//        }.getType()) : null;
//    }

    /**
     * 获取群根目录文件列表
     *
     * @param groupId 群号
     * @return {@link ActionData} of {@link GroupFilesResp}
     */
    public ActionData<GroupFilesResp> getGroupRootFiles(long groupId) {
        ActionPathEnum action = ActionPathEnum.GET_GROUP_ROOT_FILES;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GroupFilesResp>>() {
        }.getType()) : null;
    }

    /**
     * 获取群子目录文件列表
     *
     * @param groupId  群号
     * @param folderId 文件夹ID 参考 Folder 对象
     * @return {@link ActionData} of {@link GroupFilesResp}
     */
    public ActionData<GroupFilesResp> getGroupFilesByFolder(long groupId, String folderId) {
        ActionPathEnum action = ActionPathEnum.GET_GROUP_FILES_BY_FOLDER;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);
            params.addProperty("folder_id", folderId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<GroupFilesResp>>() {
        }.getType()) : null;
    }

    /**
     * 自定义请求
     *
     * @param action 请求路径
     * @param params 请求参数
     * @return {@link ActionData}
     */
    @SuppressWarnings("rawtypes")
    public ActionData customRequest(ActionPath action, JsonObject params) {
        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionData.class) : null;
    }

    /**
     * 获取精华消息列表
     *
     * @param groupId 群号
     * @return {@link ActionList} of {@link EssenceMsgResp}
     */
    public ActionList<EssenceMsgResp> getEssenceMsgList(long groupId) {
        ActionPathEnum action = ActionPathEnum.GET_ESSENCE_MSG_LIST;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionList<EssenceMsgResp>>() {
        }.getType()) : null;
    }

    /**
     * 设置精华消息
     *
     * @param msgId 消息 ID
     * @return {@link ActionRaw}
     */
    public ActionRaw setEssenceMsg(int msgId) {
        ActionPathEnum action = ActionPathEnum.SET_ESSENCE_MSG;
        JsonObject params = new JsonObject();
            params.addProperty("message_id", msgId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 移出精华消息
     *
     * @param msgId 消息 ID
     * @return {@link ActionRaw}
     */
    public ActionRaw deleteEssenceMsg(int msgId) {
        ActionPathEnum action = ActionPathEnum.DELETE_ESSENCE_MSG;
        JsonObject params = new JsonObject();
            params.addProperty("message_id", msgId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 设置机器人账号资料
     *
     * @param nickname     昵称
     * @param company      公司
     * @param email        邮箱
     * @param college      学校
     * @param personalNote 个性签名
     * @return {@link  ActionRaw}
     */
    public ActionRaw setBotProfile(String nickname, String company, String email, String college, String personalNote) {
        ActionPathEnum action = ActionPathEnum.SET_QQ_PROFILE;
        JsonObject params = new JsonObject();
            params.addProperty("nickname", nickname);
            params.addProperty("company", company);
            params.addProperty("email", email);
            params.addProperty("college", college);
            params.addProperty("personalNote", personalNote);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 发送合并转发 (私聊)
     *
     * @param userId 目标用户
     * @param msg    自定义转发消息 (可使用 ShiroUtils.generateForwardMsg() 方法创建)
     *               <a href="https://docs.go-cqhttp.org/cqcode/#%E5%90%88%E5%B9%B6%E8%BD%AC%E5%8F%91">参考文档</a>
     * @return {@link ActionRaw}
     */
//    public ActionData<MsgId> sendPrivateForwardMsg(long userId, List<Map<String, Object>> msg) {
//        ActionPathEnum action = ActionPathEnum.SEND_PRIVATE_FORWARD_MSG;
//        JsonObject params = new JsonObject();
//            params.addProperty("user_id", userId);
//            params.addProperty("messages", msg);
//
//        JsonsObject result = actionHandler.action(channel, action, params);
//        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<MsgId>>() {
//        }.getType()) : null;
//    }

    /**
     * 发送合并转发
     *
     * @param event 事件
     * @param msg   自定义转发消息 (可使用 ShiroUtils.generateForwardMsg() 方法创建)
     *              <a href="https://docs.go-cqhttp.org/cqcode/#%E5%90%88%E5%B9%B6%E8%BD%AC%E5%8F%91">参考文档</a>
     * @return {@link ActionRaw}
     */
//    public ActionData<MsgId> sendForwardMsg(GroupMessageEvent event, List<Map<String, Object>> msg) {
//        ActionPathEnum action = ActionPathEnum.SEND_FORWARD_MSG;
//        var gson = new Gson();
//        JsonObject params = new JsonObject();
//            params.addProperty("messages", msg);
//
//        switch (event.getMessageType()) {
//            case "private": {
//                params.params.addProperty("user_id", event.getUserId());
//                break;
//            }
//            case "group": {
//                params.params.addProperty("group_id", event.getGroupId());
//                break;
//            }
//            default:
//        }
//        JsonsObject result = actionHandler.action(channel, action, params);
//        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<MsgId>>() {
//        }.getType()) : null;
//    }

    /**
     * 获取中文分词
     *
     * @param content 内容
     * @return {@link ActionData} of {@link WordSlicesResp}
     */
    public ActionData<WordSlicesResp> getWordSlices(String content) {
        ActionPathEnum action = ActionPathEnum.GET_WORD_SLICES;
        JsonObject params = new JsonObject();
            params.addProperty("content", content);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<WordSlicesResp>>() {
        }.getType()) : null;
    }

    /**
     * 获取当前账号在线客户端列表
     *
     * @param noCache 是否无视缓存
     * @return {@link ActionData} of {@link ClientsResp}
     */
    public ActionData<ClientsResp> getOnlineClients(boolean noCache) {
        ActionPathEnum action = ActionPathEnum.GET_ONLINE_CLIENTS;
        JsonObject params = new JsonObject();
            params.addProperty("no_cache", noCache);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<ClientsResp>>() {
        }.getType()) : null;
    }

    /**
     * 图片 OCR
     *
     * @param image 图片ID
     * @return {@link ActionData} of {@link OcrResp}
     */
    public ActionData<OcrResp> ocrImage(String image) {
        ActionPathEnum action = ActionPathEnum.OCR_IMAGE;
        JsonObject params = new JsonObject();
            params.addProperty("image", image);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionData<OcrResp>>() {
        }.getType()) : null;
    }

    /**
     * 私聊发送文件
     *
     * @param userId 目标用户
     * @param file   本地文件路径
     * @param name   文件名
     * @return {@link ActionRaw}
     */
    public ActionRaw uploadPrivateFile(long userId, String file, String name) {
        ActionPathEnum action = ActionPathEnum.UPLOAD_PRIVATE_FILE;
        JsonObject params = new JsonObject();
            params.addProperty("user_id", userId);
            params.addProperty("file", file);
            params.addProperty("name", name);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 群打卡
     *
     * @param groupId 群号
     * @return {@link ActionRaw}
     */
    public ActionRaw sendGroupSign(long groupId) {
        ActionPathEnum action = ActionPathEnum.SEND_GROUP_SIGN;
        JsonObject params = new JsonObject();
            params.addProperty("group_id", groupId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 删除单向好友
     *
     * @param userId QQ号
     * @return {@link ActionRaw}
     */
    public ActionRaw deleteUnidirectionalFriend(long userId) {
        ActionPathEnum action = ActionPathEnum.DELETE_UNIDIRECTIONAL_FRIEND;
        JsonObject params = new JsonObject();
            params.addProperty("user_id", userId);

        JsonsObject result = actionHandler.action(channel, action, params);
        return result != null ? GsonUtil.strToJavaBean(result.toString(),ActionRaw.class) : null;
    }

    /**
     * 获取单向好友列表
     *
     * @return {@link ActionList} of {@link  UnidirectionalFriendListResp}
     */
    public ActionList<UnidirectionalFriendListResp> getUnidirectionalFriendList() {
        ActionPathEnum action = ActionPathEnum.GET_UNIDIRECTIONAL_FRIEND_LIST;
        JsonsObject result = actionHandler.action(channel, action, null);
        return result != null ?  GsonUtil.fromJson(result.toString(), new TypeToken<ActionList<UnidirectionalFriendListResp>>() {
        }.getType()) : null;
    }
}
