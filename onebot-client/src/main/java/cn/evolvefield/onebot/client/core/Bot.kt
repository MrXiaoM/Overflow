package cn.evolvefield.onebot.client.core

import cn.evole.onebot.sdk.action.ActionData
import cn.evole.onebot.sdk.action.ActionList
import cn.evole.onebot.sdk.action.ActionPath
import cn.evole.onebot.sdk.action.ActionRaw
import cn.evole.onebot.sdk.entity.Anonymous
import cn.evole.onebot.sdk.entity.GuildMsgId
import cn.evole.onebot.sdk.entity.MsgId
import cn.evole.onebot.sdk.enums.ActionPathEnum
import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import cn.evole.onebot.sdk.response.contact.FriendInfoResp
import cn.evole.onebot.sdk.response.contact.LoginInfoResp
import cn.evole.onebot.sdk.response.contact.StrangerInfoResp
import cn.evole.onebot.sdk.response.contact.UnidirectionalFriendListResp
import cn.evole.onebot.sdk.response.group.*
import cn.evole.onebot.sdk.response.guild.*
import cn.evole.onebot.sdk.response.misc.*
import cn.evole.onebot.sdk.util.json.GsonUtil
import cn.evole.onebot.sdk.util.json.JsonsObject
import cn.evolvefield.onebot.client.handler.ActionHandler
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import org.java_websocket.WebSocket

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 15:19
 * Version: 1.0
 */
/**
 * @param channel                    [WebSocket]
 * @param actionHandler              [ActionHandler]
 */
@Suppress("unused")
class Bot(
    val channel: WebSocket,
    val actionHandler: ActionHandler
) {
    private var idInternal: Long = 0
    val id: Long
        get() = idInternal
    /**
     * 发送消息
     *
     * @param event      [GroupMessageEvent]
     * @param msg        要发送的内容
     * @param autoEscape 消息内容是否作为纯文本发送 ( 即不解析 CQ 码 ) , 只在 message 字段是字符串时有效
     * @return [ActionData] of [MsgId]
     */
    @JvmBlockingBridge
    suspend fun sendMsg(event: GroupMessageEvent, msg: String, autoEscape: Boolean): ActionData<MsgId> {
        when (event.messageType) {
            "private" -> return sendPrivateMsg(event.userId, msg, autoEscape)
            "group" -> return sendGroupMsg(event.groupId, msg, autoEscape)
        }
        throw IllegalArgumentException("无效的 messageType=${event.messageType}")
    }

    /**
     * 发送私聊消息
     *
     * @param userId     对方 QQ 号
     * @param msg        要发送的内容
     * @param autoEscape 消息内容是否作为纯文本发送 ( 即不解析 CQ 码 ) , 只在 message 字段是字符串时有效
     * @return [ActionData] of [MsgId]
     */
    @JvmBlockingBridge
    suspend fun sendPrivateMsg(userId: Long, msg: String, autoEscape: Boolean): ActionData<MsgId> {
        val action = ActionPathEnum.SEND_PRIVATE_MSG
        val params = JsonObject()
        params.addProperty("user_id", userId)
        params.addProperty("message", msg)
        params.addProperty("auto_escape", autoEscape)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<MsgId>>() {}.type
        )
    }

    /**
     * 发送群消息
     *
     * @param groupId    群号
     * @param msg        要发送的内容
     * @param autoEscape 消息内容是否作为纯文本发送 ( 即不解析 CQ 码 ) , 只在 message 字段是字符串时有效
     * @return [ActionData] of [MsgId]
     */
    @JvmBlockingBridge
    suspend fun sendGroupMsg(groupId: Long, msg: String, autoEscape: Boolean): ActionData<MsgId> {
        val action = ActionPathEnum.SEND_GROUP_MSG
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("message", msg)
        params.addProperty("auto_escape", autoEscape)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<MsgId>>() {}.type
        )
    }

    /**
     * 获取频道成员列表
     * 由于频道人数较多(数万), 请尽量不要全量拉取成员列表, 这将会导致严重的性能问题
     * 尽量使用 getGuildMemberProfile 接口代替全量拉取
     * nextToken 为空的情况下, 将返回第一页的数据, 并在返回值附带下一页的 token
     *
     * @param guildId   频道ID
     * @param nextToken 翻页Token
     * @return [ActionData] of [GuildMemberListResp]
     */
    @JvmBlockingBridge
    suspend fun getGuildMemberList(guildId: String, nextToken: String): ActionData<GuildMemberListResp> {
        val action = ActionPathEnum.GET_GUILD_LIST
        val params = JsonObject()
        params.addProperty("guild_id", guildId)
        params.addProperty("next_token", nextToken)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GuildMemberListResp>>() {}.type
        )
    }

    /**
     * 发送信息到子频道
     *
     * @param guildId   频道 ID
     * @param channelId 子频道 ID
     * @param msg       要发送的内容
     * @return [ActionData] of [GuildMsgId]
     */
    @JvmBlockingBridge
    suspend fun sendGuildMsg(guildId: String, channelId: String, msg: String): ActionData<GuildMsgId> {
        val action = ActionPathEnum.SEND_GUILD_CHANNEL_MSG
        val params = JsonObject()
        params.addProperty("guild_id", guildId)
        params.addProperty("channel_id", channelId)
        params.addProperty("message", msg)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GuildMsgId>>() {}.type
        )
    }

    /**
     * 获取频道消息
     *
     * @param guildMsgId 频道 ID
     * @param noCache    是否使用缓存
     * @return [ActionData] of [GetGuildMsgResp]
     */
    @JvmBlockingBridge
    suspend fun getGuildMsg(guildMsgId: String, noCache: Boolean): ActionData<GetGuildMsgResp> {
        val action = ActionPathEnum.GET_GUILD_MSG
        val params = JsonObject()
        params.addProperty("message_id", guildMsgId)
        params.addProperty("no_cache", noCache)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GetGuildMsgResp>>() {}.type
        )
    }
    /**
     * 获取频道系统内 BOT 的资料
     *
     * @return [ActionData] of [GuildServiceProfileResp]
     */
    @JvmBlockingBridge
    suspend fun getGuildServiceProfile(): ActionData<GuildServiceProfileResp> {
        val action = ActionPathEnum.GET_GUILD_SERVICE_PROFILE
        val result = actionHandler.action(channel, action, null)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GuildServiceProfileResp>>() {}.type
        )
    }
    /**
     * 获取频道列表
     *
     * @return [ActionList] of [GuildListResp]
     */
    suspend fun getGuildList(): ActionList<GuildListResp> {
        val action = ActionPathEnum.GET_GUILD_LIST
        val result = actionHandler.action(channel, action, null)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionList<GuildListResp>>() {}.type
        )
    }

    /**
     * 通过访客获取频道元数据
     *
     * @param guildId 频道 ID
     * @return [ActionData] of [GuildMetaByGuestResp]
     */
    @JvmBlockingBridge
    suspend fun getGuildMetaByGuest(guildId: String): ActionData<GuildMetaByGuestResp> {
        val action = ActionPathEnum.GET_GUILD_META_BY_GUEST
        val params = JsonObject()
        params.addProperty("guild_id", guildId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GuildMetaByGuestResp>>() {}.type
        )
    }

    /**
     * 获取子频道列表
     *
     * @param guildId 频道 ID
     * @param noCache 是否无视缓存
     * @return [ActionList] of [ChannelInfoResp]
     */
    @JvmBlockingBridge
    suspend fun getGuildChannelList(guildId: String, noCache: Boolean): ActionList<ChannelInfoResp> {
        val action = ActionPathEnum.GET_GUILD_CHANNEL_LIST
        val params = JsonObject()
        params.addProperty("guild_id", guildId)
        params.addProperty("no_cache", noCache)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionList<ChannelInfoResp>>() {}.type
        )
    }

    /**
     * 单独获取频道成员信息
     *
     * @param guildId 频道ID
     * @param userId  用户ID
     * @return [ActionData] of [GuildMemberProfileResp]
     */
    @JvmBlockingBridge
    suspend fun getGuildMemberProfile(guildId: String, userId: String): ActionData<GuildMemberProfileResp> {
        val action = ActionPathEnum.GET_GUILD_MEMBER_PROFILE
        val params = JsonObject()
        params.addProperty("guild_id", guildId)
        params.addProperty("user_id", userId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GuildMemberProfileResp>>() {}.type
        )
    }

    /**
     * 获取消息
     *
     * @param msgId 消息 ID
     * @return [ActionData] of [GetMsgResp]
     */
    @JvmBlockingBridge
    suspend fun getMsg(msgId: Int): ActionData<GetMsgResp> {
        val action = ActionPathEnum.GET_MSG
        val params = JsonObject()
        params.addProperty("message_id", msgId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GetMsgResp>>() {}.type
        )
    }

    /**
     * 获取消息
     *
     * @param messageType 消息类型
     * @param userId 私聊QQ
     * @param groupId 群号
     * @param count 获取的消息数量（默认为20）
     * @param messageSeq 起始消息的message_id（默认为0，表示从最后一条发言往前）
     * @return [ActionData] of [GetHistoryMsgResp]
     */
    @JvmBlockingBridge
    suspend fun getHistoryMsg(
        messageType: String,
        userId: Long? = null,
        groupId: Long? = null,
        count: Int = 20,
        messageSeq: Int = 0
    ): ActionData<GetHistoryMsgResp> {
        val action = ActionPathEnum.GET_HISTORY_MSG
        val params = JsonObject()
        params.addProperty("message_type", messageType)
        if (userId != null) params.addProperty("user_id", userId)
        if (groupId != null) params.addProperty("group_id", groupId)
        params.addProperty("count", count)
        params.addProperty("message_seq", messageSeq)

        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GetHistoryMsgResp>>() {}.type
        )
    }

    /**
     * 获取消息
     *
     * @param groupId 群号
     * @param count 获取的消息数量（默认为20）
     * @param messageSeq 起始消息的message_id（默认为0，表示从最后一条发言往前）
     * @return [ActionData] of [GetHistoryMsgResp]
     */
    @JvmBlockingBridge
    suspend fun getGroupMsgHistory(
        groupId: Long,
        count: Int = 20,
        messageSeq: Int = 0
    ): ActionData<GetHistoryMsgResp> {
        val action = ActionPathEnum.GET_GROUP_MSG_HISTORY
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("count", count)
        params.addProperty("message_seq", messageSeq)

        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GetHistoryMsgResp>>() {}.type
        )
    }

    /**
     * 撤回消息
     *
     * @param msgId 消息 ID
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun deleteMsg(msgId: Int): ActionRaw {
        val action = ActionPathEnum.DELETE_MSG
        val params = JsonObject()
        params.addProperty("message_id", msgId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 群组踢人
     *
     * @param groupId          群号
     * @param userId           要踢的 QQ 号
     * @param rejectAddRequest 拒绝此人的加群请求 (默认false)
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupKick(groupId: Long, userId: Long, rejectAddRequest: Boolean): ActionRaw {
        val action = ActionPathEnum.SET_GROUP_KICK
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("user_id", userId)
        params.addProperty("reject_add_request", rejectAddRequest)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 群组单人禁言
     *
     * @param groupId  群号
     * @param userId   要禁言的 QQ 号
     * @param duration 禁言时长, 单位秒, 0 表示取消禁言 (默认30 * 60)
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupBan(groupId: Long, userId: Long, duration: Int): ActionRaw {
        val action = ActionPathEnum.SET_GROUP_BAN
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("user_id", userId)
        params.addProperty("duration", duration)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 全体禁言
     *
     * @param groupId 群号
     * @param enable  是否禁言（默认True,False为取消禁言）
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupWholeBan(groupId: Long, enable: Boolean): ActionRaw {
        val action = ActionPathEnum.SET_GROUP_WHOLE_BAN
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("enable", enable)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 群组设置管理员
     *
     * @param groupId 群号
     * @param userId  要设置管理员的 QQ 号
     * @param enable  true 为设置，false 为取消
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupAdmin(groupId: Long, userId: Long, enable: Boolean): ActionRaw {
        val action = ActionPathEnum.SET_GROUP_ADMIN
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("user_id", userId)
        params.addProperty("enable", enable)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 群组匿名
     *
     * @param groupId 群号
     * @param enable  是否允许匿名聊天
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupAnonymous(groupId: Long, enable: Boolean): ActionRaw {
        val action = ActionPathEnum.SET_GROUP_ANONYMOUS
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("enable", enable)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 设置群名片（群备注）
     *
     * @param groupId 群号
     * @param userId  要设置的 QQ 号
     * @param card    群名片内容，不填或空字符串表示删除群名片
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupCard(groupId: Long, userId: Long, card: String?): ActionRaw {
        val action = ActionPathEnum.SET_GROUP_CARD
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("user_id", userId)
        params.addProperty("card", card)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 设置群名
     *
     * @param groupId   群号
     * @param groupName 新群名
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupName(groupId: Long, groupName: String?): ActionRaw {
        val action = ActionPathEnum.SET_GROUP_NAME
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("group_name", groupName)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 退出群组
     *
     * @param groupId   群号
     * @param isDismiss 是否解散, 如果登录号是群主, 则仅在此项为 true 时能够解散
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupLeave(groupId: Long, isDismiss: Boolean): ActionRaw {
        val action = ActionPathEnum.SET_GROUP_LEAVE
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("is_dismiss", isDismiss)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 设置群组专属头衔
     *
     * @param groupId      群号
     * @param userId       要设置的 QQ 号
     * @param specialTitle 专属头衔，不填或空字符串表示删除专属头衔
     * @param duration     专属头衔有效期，单位秒，-1 表示永久，不过此项似乎没有效果，可能是只有某些特殊的时间长度有效，有待测试
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupSpecialTitle(groupId: Long, userId: Long, specialTitle: String?, duration: Int): ActionRaw {
        val action = ActionPathEnum.SET_GROUP_SPECIAL_TITLE
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("user_id", userId)
        params.addProperty("special_title", specialTitle)
        params.addProperty("duration", duration)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 处理加好友请求
     *
     * @param flag    加好友请求的 flag（需从上报的数据中获得）
     * @param approve 是否同意请求(默认为true)
     * @param remark  添加后的好友备注（仅在同意时有效）
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setFriendAddRequest(flag: String?, approve: Boolean, remark: String?): ActionRaw {
        val action = ActionPathEnum.SET_FRIEND_ADD_REQUEST
        val params = JsonObject()
        params.addProperty("flag", flag)
        params.addProperty("approve", approve)
        params.addProperty("remark", remark)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 处理加群请求／邀请
     *
     * @param flag    加群请求的 flag（需从上报的数据中获得）
     * @param subType add 或 invite，请求类型（需要和上报消息中的 sub_type 字段相符）
     * @param approve 是否同意请求／邀请
     * @param reason  拒绝理由（仅在拒绝时有效）
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupAddRequest(flag: String?, subType: String?, approve: Boolean, reason: String?): ActionRaw {
        val action = ActionPathEnum.SET_GROUP_ADD_REQUEST
        val params = JsonObject()
        params.addProperty("flag", flag)
        params.addProperty("sub_type", subType)
        params.addProperty("approve", approve)
        params.addProperty("reason", reason)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 获取登录号信息
     *
     * @return [ActionData] of @[LoginInfoResp]
     */
    @JvmBlockingBridge
    suspend fun getLoginInfo(): ActionData<LoginInfoResp> {
        val action = ActionPathEnum.GET_LOGIN_INFO
        val result = actionHandler.action(channel, action, null)
        return GsonUtil.fromJson<ActionData<LoginInfoResp>>(
            result.toString(),
            object : TypeToken<ActionData<LoginInfoResp>>() {}.type
        ).also { idInternal = it.data.userId }
    }

    /**
     * 获取陌生人信息
     *
     * @param userId  QQ 号
     * @param noCache 是否不使用缓存（使用缓存可能更新不及时，但响应更快）
     * @return [ActionData] of [StrangerInfoResp]
     */
    @JvmBlockingBridge
    suspend fun getStrangerInfo(userId: Long, noCache: Boolean): ActionData<StrangerInfoResp> {
        val action = ActionPathEnum.GET_STRANGER_INFO
        val params = JsonObject()
        params.addProperty("user_id", userId)
        params.addProperty("no_cache", noCache)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<StrangerInfoResp>>() {}.type
        )
    }

    /**
     * 获取好友列表
     *
     * @return [ActionList] of [FriendInfoResp]
     */
    @JvmBlockingBridge
    suspend fun getFriendList(): ActionList<FriendInfoResp> {
        val action = ActionPathEnum.GET_FRIEND_LIST
        val result = actionHandler.action(channel, action, null)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionList<FriendInfoResp>>() {}.type
        )
    }

    /**
     * 删除好友
     *
     * @param friendId 好友 QQ 号
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun deleteFriend(friendId: Long): ActionRaw {
        val action = ActionPathEnum.DELETE_FRIEND
        val params = JsonObject()
        params.addProperty("friend_id", friendId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 获取群信息
     *
     * @param groupId 群号
     * @param noCache 是否不使用缓存（使用缓存可能更新不及时，但响应更快）
     * @return [ActionData] of [GroupInfoResp]
     */
    @JvmBlockingBridge
    suspend fun getGroupInfo(groupId: Long, noCache: Boolean): ActionData<GroupInfoResp> {
        val action = ActionPathEnum.GET_GROUP_INFO
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("no_cache", noCache)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GroupInfoResp>>() {}.type
        )
    }

    /**
     * 获取群列表
     *
     * @return [ActionList] of [GroupInfoResp]
     */
    @JvmBlockingBridge
    suspend fun getGroupList(): ActionList<GroupInfoResp> {
        val action = ActionPathEnum.GET_GROUP_LIST
        val result = actionHandler.action(channel, action, null)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionList<GroupInfoResp>>() {}.type
        )
    }

    /**
     * 获取群成员信息
     *
     * @param groupId 群号
     * @param userId  QQ 号
     * @param noCache 是否不使用缓存（使用缓存可能更新不及时，但响应更快）
     * @return [ActionData] of [GroupMemberInfoResp]
     */
    @JvmBlockingBridge
    suspend fun getGroupMemberInfo(groupId: Long, userId: Long, noCache: Boolean): ActionData<GroupMemberInfoResp> {
        val action = ActionPathEnum.GET_GROUP_MEMBER_INFO
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("user_id", userId)
        params.addProperty("no_cache", noCache)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GroupMemberInfoResp>>() {}.type
        )
    }

    /**
     * 获取群成员列表
     *
     * @param groupId 群号
     * @return [ActionList] of [GroupMemberInfoResp]
     */
    @JvmBlockingBridge
    suspend fun getGroupMemberList(groupId: Long): ActionList<GroupMemberInfoResp> {
        val action = ActionPathEnum.GET_GROUP_MEMBER_LIST
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionList<GroupMemberInfoResp>>() {}.type
        )
    }

    /**
     * 获取群荣誉信息
     *
     * @param groupId 群号
     * @param type    要获取的群荣誉类型, 可传入 talkative performer legend strong_newbie emotion 以分别获取单个类型的群荣誉数据, 或传入 all 获取所有数据
     * @return [ActionData] of [GroupHonorInfoResp]
     */
    @JvmBlockingBridge
    suspend fun getGroupHonorInfo(groupId: Long, type: String?): ActionData<GroupHonorInfoResp> {
        val action = ActionPathEnum.GET_GROUP_HONOR_INFO
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("type", type)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GroupHonorInfoResp>>() {}.type
        )
    }

    /**
     * 检查是否可以发送图片
     *
     * @return [ActionData] of [BooleanResp]
     */
    @JvmBlockingBridge
    suspend fun canSendImage(): ActionData<BooleanResp> {
        val action = ActionPathEnum.CAN_SEND_IMAGE
        val result = actionHandler.action(channel, action, null)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<BooleanResp>>() {}.type
        )
    }

    /**
     * 检查是否可以发送语音
     *
     * @return [ActionData] of [BooleanResp]
     */
    @JvmBlockingBridge
    suspend fun canSendRecord(): ActionData<BooleanResp> {
        val action = ActionPathEnum.CAN_SEND_RECORD
        val result = actionHandler.action(channel, action, null)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<BooleanResp>>() {}.type
        )
    }

    /**
     * 设置群头像
     * 目前这个API在登录一段时间后因cookie失效而失效, 请考虑后使用
     *
     * @param groupId 群号
     * @param file    图片文件名（支持绝对路径，网络URL，Base64编码）
     * @param cache   表示是否使用已缓存的文件 （通过网络URL发送时有效, 1表示使用缓存, 0关闭关闭缓存, 默认为1）
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupPortrait(groupId: Long, file: String?, cache: Int): ActionRaw {
        val action = ActionPathEnum.SET_GROUP_PORTRAIT
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("file", file)
        params.addProperty("cache", cache)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 检查链接安全性
     * 安全等级, 1: 安全 2: 未知 3: 危险
     *
     * @param url 需要检查的链接
     * @return [ActionData] of [CheckUrlSafelyResp]
     */
    @JvmBlockingBridge
    suspend fun checkUrlSafely(url: String?): ActionData<CheckUrlSafelyResp> {
        val action = ActionPathEnum.CHECK_URL_SAFELY
        val params = JsonObject()
        params.addProperty("url", url)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<CheckUrlSafelyResp>>() {}.type
        )
    }

    /**
     * 发送群公告
     *
     * @param groupId 群号
     * @param content 公告内容
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun sendGroupNotice(groupId: Long, content: String?): ActionRaw {
        val action = ActionPathEnum.SEN_GROUP_NOTICE
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("content", content)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 获取群 @全体成员 剩余次数
     *
     * @param groupId 群号
     * @return [ActionData] of [GroupAtAllRemainResp]
     */
    @JvmBlockingBridge
    suspend fun getGroupAtAllRemain(groupId: Long): ActionData<GroupAtAllRemainResp> {
        val action = ActionPathEnum.GET_GROUP_AT_ALL_REMAIN
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GroupAtAllRemainResp>>() {}.type
        )
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
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun uploadGroupFile(groupId: Long, file: String?, name: String?, folder: String?): ActionRaw {
        val action = ActionPathEnum.UPLOAD_GROUP_FILE
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("file", file)
        params.addProperty("name", name)
        params.addProperty("folder", folder)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 上传群文件
     * 在不提供 folder 参数的情况下默认上传到根目录
     * 只能上传本地文件, 需要上传 http 文件的话请先下载到本地
     *
     * @param groupId 群号
     * @param file    本地文件路径
     * @param name    储存名称
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun uploadGroupFile(groupId: Long, file: String?, name: String?): ActionRaw {
        val action = ActionPathEnum.UPLOAD_GROUP_FILE
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("file", file)
        params.addProperty("name", name)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 群组匿名用户禁言
     *
     * @param groupId   群号
     * @param anonymous 要禁言的匿名用户对象（群消息上报的 anonymous 字段）
     * @param duration  禁言时长，单位秒，无法取消匿名用户禁言
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupAnonymousBan(groupId: Long, anonymous: Anonymous?, duration: Boolean): ActionRaw {
        val gson = GsonBuilder().create()
        val action = ActionPathEnum.SET_GROUP_ANONYMOUS_BAN
        val an = gson.toJson(anonymous, Anonymous::class.java)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.add("anonymous", JsonsObject(an).get())
        params.addProperty("duration", duration)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 群组匿名用户禁言
     *
     * @param groupId  群号
     * @param flag     要禁言的匿名用户的 flag（需从群消息上报的数据中获得）
     * @param duration 禁言时长，单位秒，无法取消匿名用户禁言
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setGroupAnonymousBan(groupId: Long, flag: String?, duration: Boolean): ActionRaw {
        val action = ActionPathEnum.SET_GROUP_ANONYMOUS_BAN
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("flag", flag)
        params.addProperty("duration", duration)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 调用 go cq http 下载文件
     *
     * @param url         链接地址
     * @param threadCount 下载线程数
     * @param headers     自定义请求头
     * @return [ActionData] of [DownloadFileResp]
     */
    @JvmBlockingBridge
    suspend fun downloadFile(url: String?, threadCount: Int, headers: String?): ActionData<DownloadFileResp> {
        val action = ActionPathEnum.DOWNLOAD_FILE
        val params = JsonObject()
        params.addProperty("url", url)
        params.addProperty("thread_count", threadCount)
        params.addProperty("headers", headers)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<DownloadFileResp>>() {}.type
        )
    }

    /**
     * 调用 go cq http 下载文件
     *
     * @param url 链接地址
     * @return [ActionData] of [DownloadFileResp]
     */
    @JvmBlockingBridge
    suspend fun downloadFile(url: String?): ActionData<DownloadFileResp> {
        val action = ActionPathEnum.DOWNLOAD_FILE
        val params = JsonObject()
        params.addProperty("url", url)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<DownloadFileResp>>() {}.type
        )
    }
    /**
     * 发送合并转发 (群)
     *
     * @param groupId 群号
     * @param msg     自定义转发消息 (可使用 ShiroUtils.generateForwardMsg() 方法创建)
     * [参考文档](https://docs.go-cqhttp.org/cqcode/#%E5%90%88%E5%B9%B6%E8%BD%AC%E5%8F%91)
     * @return [ActionRaw]
     */
    suspend fun sendGroupForwardMsg(groupId: Long, msg: List<Map<String, Any>>): ActionData<MsgId> {
        val action = ActionPathEnum.SEND_GROUP_FORWARD_MSG
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.add("messages", msg.toJsonArray())

        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(result.toString(), object : TypeToken<ActionData<MsgId>>() {}.type)
    }
    /**
     * 获取群根目录文件列表
     *
     * @param groupId 群号
     * @return [ActionData] of [GroupFilesResp]
     */
    @JvmBlockingBridge
    suspend fun getGroupRootFiles(groupId: Long): ActionData<GroupFilesResp> {
        val action = ActionPathEnum.GET_GROUP_ROOT_FILES
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GroupFilesResp>>() {}.type
        )
    }

    /**
     * 获取群子目录文件列表
     *
     * @param groupId  群号
     * @param folderId 文件夹ID 参考 Folder 对象
     * @return [ActionData] of [GroupFilesResp]
     */
    @JvmBlockingBridge
    suspend fun getGroupFilesByFolder(groupId: Long, folderId: String?): ActionData<GroupFilesResp> {
        val action = ActionPathEnum.GET_GROUP_FILES_BY_FOLDER
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("folder_id", folderId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<GroupFilesResp>>() {}.type
        )
    }

    /**
     * 自定义请求 ActionData
     *
     * @param action 请求路径
     * @param params 请求参数
     * @return [ActionData]
     */
    @JvmBlockingBridge
    suspend inline fun <reified T : Any> customRequestData(action: ActionPath, params: JsonObject?): ActionData<T> {
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<T>>() {}.type
        )
    }
    /**
     * 自定义请求 ActionList
     *
     * @param action 请求路径
     * @param params 请求参数
     * @return [ActionList]
     */
    @JvmBlockingBridge
    suspend inline fun <reified T : Any> customRequestList(action: ActionPath, params: JsonObject?): ActionList<T> {
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionList<T>>() {}.type
        )
    }
    /**
     * 自定义请求 ActionRaw
     *
     * @param action 请求路径
     * @param params 请求参数
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun customRequestRaw(action: ActionPath, params: JsonObject?): ActionRaw {
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 获取精华消息列表
     *
     * @param resourceId 转发消息的ID
     * @return [ActionList] of [ForwardMsgResp]
     */
    @JvmBlockingBridge
    suspend fun getForwardMsg(resourceId: String): ActionData<ForwardMsgResp> {
        val action = ActionPathEnum.GET_FORWARD_MSG
        val params = JsonObject()
        params.addProperty("id", resourceId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<ForwardMsgResp>>() {}.type
        )
    }

    /**
     * 获取精华消息列表
     *
     * @param groupId 群号
     * @return [ActionList] of [EssenceMsgResp]
     */
    @JvmBlockingBridge
    suspend fun getEssenceMsgList(groupId: Long): ActionList<EssenceMsgResp> {
        val action = ActionPathEnum.GET_ESSENCE_MSG_LIST
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionList<EssenceMsgResp>>() {}.type
        )
    }

    /**
     * 设置精华消息
     *
     * @param msgId 消息 ID
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setEssenceMsg(msgId: Int): ActionRaw {
        val action = ActionPathEnum.SET_ESSENCE_MSG
        val params = JsonObject()
        params.addProperty("message_id", msgId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 移出精华消息
     *
     * @param msgId 消息 ID
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun deleteEssenceMsg(msgId: Int): ActionRaw {
        val action = ActionPathEnum.DELETE_ESSENCE_MSG
        val params = JsonObject()
        params.addProperty("message_id", msgId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 设置机器人账号资料
     *
     * @param nickname     昵称
     * @param company      公司
     * @param email        邮箱
     * @param college      学校
     * @param personalNote 个性签名
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun setBotProfile(
        nickname: String?,
        company: String?,
        email: String?,
        college: String?,
        personalNote: String?
    ): ActionRaw {
        val action = ActionPathEnum.SET_QQ_PROFILE
        val params = JsonObject()
        params.addProperty("nickname", nickname)
        params.addProperty("company", company)
        params.addProperty("email", email)
        params.addProperty("college", college)
        params.addProperty("personalNote", personalNote)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }
    /**
     * 发送合并转发 (私聊)
     *
     * @param userId 目标用户
     * @param msg    自定义转发消息 (可使用 ShiroUtils.generateForwardMsg() 方法创建)
     * [参考文档](https://docs.go-cqhttp.org/cqcode/#%E5%90%88%E5%B9%B6%E8%BD%AC%E5%8F%91)
     * @return [ActionRaw]
     */
    suspend fun sendPrivateForwardMsg(userId: Long, msg: List<Map<String, Any>>): ActionData<MsgId> {
        val action = ActionPathEnum.SEND_PRIVATE_FORWARD_MSG
        val params = JsonObject()
        params.addProperty("user_id", userId)
        params.add("messages", msg.toJsonArray())

        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(result.toString(), object : TypeToken<ActionData<MsgId>>() {}.type)
    }
    /**
     * 发送合并转发
     *
     * @param event 事件
     * @param msg   自定义转发消息 (可使用 ShiroUtils.generateForwardMsg() 方法创建)
     * [参考文档](https://docs.go-cqhttp.org/cqcode/#%E5%90%88%E5%B9%B6%E8%BD%AC%E5%8F%91)
     * @return [ActionRaw]
     */
    suspend fun sendForwardMsg(event: GroupMessageEvent, msg: List<Map<String, Any>>): ActionData<MsgId> {
        val action = ActionPathEnum.SEND_FORWARD_MSG
        val params = JsonObject()
        when (event.messageType) {
            "private" -> params.addProperty("user_id", event.userId)
            "group" -> params.addProperty("group_id", event.groupId)
        }
        params.add("messages", msg.toJsonArray())

        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(result.toString(), object : TypeToken<ActionData<MsgId>>() {}.type)
    }
    /**
     * 获取中文分词
     *
     * @param content 内容
     * @return [ActionData] of [WordSlicesResp]
     */
    @JvmBlockingBridge
    suspend fun getWordSlices(content: String?): ActionData<WordSlicesResp> {
        val action = ActionPathEnum.GET_WORD_SLICES
        val params = JsonObject()
        params.addProperty("content", content)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<WordSlicesResp>>() {}.type
        )
    }

    /**
     * 获取当前账号在线客户端列表
     *
     * @param noCache 是否无视缓存
     * @return [ActionData] of [ClientsResp]
     */
    @JvmBlockingBridge
    suspend fun getOnlineClients(noCache: Boolean): ActionData<ClientsResp> {
        val action = ActionPathEnum.GET_ONLINE_CLIENTS
        val params = JsonObject()
        params.addProperty("no_cache", noCache)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<ClientsResp>>() {}.type
        )
    }

    /**
     * 图片 OCR
     *
     * @param image 图片ID
     * @return [ActionData] of [OcrResp]
     */
    @JvmBlockingBridge
    suspend fun ocrImage(image: String?): ActionData<OcrResp> {
        val action = ActionPathEnum.OCR_IMAGE
        val params = JsonObject()
        params.addProperty("image", image)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionData<OcrResp>>() {}.type
        )
    }

    /**
     * 私聊发送文件
     *
     * @param userId 目标用户
     * @param file   本地文件路径
     * @param name   文件名
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun uploadPrivateFile(userId: Long, file: String?, name: String?): ActionRaw {
        val action = ActionPathEnum.UPLOAD_PRIVATE_FILE
        val params = JsonObject()
        params.addProperty("user_id", userId)
        params.addProperty("file", file)
        params.addProperty("name", name)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 群打卡
     *
     * @param groupId 群号
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun sendGroupSign(groupId: Long): ActionRaw {
        val action = ActionPathEnum.SEND_GROUP_SIGN
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 删除单向好友
     *
     * @param userId QQ号
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    suspend fun deleteUnidirectionalFriend(userId: Long): ActionRaw {
        val action = ActionPathEnum.DELETE_UNIDIRECTIONAL_FRIEND
        val params = JsonObject()
        params.addProperty("user_id", userId)
        val result = actionHandler.action(channel, action, params)
        return GsonUtil.strToJavaBean(result.toString(), ActionRaw::class.java)
    }

    /**
     * 获取单向好友列表
     *
     * @return [ActionList] of [UnidirectionalFriendListResp]
     */
    suspend fun getUnidirectionalFriendList(): ActionList<UnidirectionalFriendListResp> {
        val action = ActionPathEnum.GET_UNIDIRECTIONAL_FRIEND_LIST
        val result = actionHandler.action(channel, action, null)
        return GsonUtil.fromJson(
            result.toString(),
            object : TypeToken<ActionList<UnidirectionalFriendListResp>>() {}.type
        )
    }
}

fun <T> List<T>.toJsonArray(): JsonArray {
    val array = JsonArray()
    for (any in this) {
        when (any) {
            is JsonElement -> array.add(any)
            is Map<*, *> -> array.add(any.toJsonObject())
            is List<*> -> array.add(any.toJsonArray())
            is Boolean -> array.add(any)
            is Number -> array.add(any)
            is Char -> array.add(any)
            else -> array.add(any.toString())
        }
    }
    return array
}
fun <K,V> Map<K, V>.toJsonObject(): JsonObject {
    val obj = JsonObject()
    for (entry in entries) {
        val key = entry.key.toString()
        when (val value = entry.value) {
            is JsonElement -> obj.add(key, value)
            is Map<*, *> -> obj.add(key, value.toJsonObject())
            is List<*> -> obj.add(key, value.toJsonArray())
            is Boolean -> obj.addProperty(key, value)
            is Number -> obj.addProperty(key, value)
            is Char -> obj.addProperty(key, value)
            else -> obj.addProperty(key, value.toString())
        }
    }
    return obj
}