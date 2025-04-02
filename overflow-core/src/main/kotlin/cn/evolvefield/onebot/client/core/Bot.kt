package cn.evolvefield.onebot.client.core

import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.handler.ActionHandler
import cn.evolvefield.onebot.sdk.action.ActionData
import cn.evolvefield.onebot.sdk.action.ActionList
import cn.evolvefield.onebot.sdk.action.ActionPath
import cn.evolvefield.onebot.sdk.action.ActionRaw
import cn.evolvefield.onebot.sdk.entity.Anonymous
import cn.evolvefield.onebot.sdk.entity.MsgId
import cn.evolvefield.onebot.sdk.enums.ActionPathEnum
import cn.evolvefield.onebot.sdk.response.contact.FriendInfoResp
import cn.evolvefield.onebot.sdk.response.contact.LoginInfoResp
import cn.evolvefield.onebot.sdk.response.contact.StrangerInfoResp
import cn.evolvefield.onebot.sdk.response.ext.CreateGroupFileFolderResp
import cn.evolvefield.onebot.sdk.response.ext.GetFileResp
import cn.evolvefield.onebot.sdk.response.ext.SetGroupReactionResp
import cn.evolvefield.onebot.sdk.response.ext.UploadGroupFileResp
import cn.evolvefield.onebot.sdk.response.group.*
import cn.evolvefield.onebot.sdk.response.misc.*
import cn.evolvefield.onebot.sdk.util.*
import com.google.gson.*
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import org.java_websocket.WebSocket
import top.mrxiaom.overflow.action.ActionContext

/**
 * Onebot 主动操作上下文构建器
 *
 * 有以下方式使用默认上下文
 * - Kotlin 可使用 `{}`
 * - Java 可使用 [Bot.getDefaultContext]
 */
typealias Context = ActionContext.Builder.() -> Unit

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 15:19
 * Version: 1.0
 */
/**
 * @param conn                       [WebSocket]
 * @param config                     [BotConfig]
 * @param actionHandler              [ActionHandler]
 */
@Suppress("unused")
class Bot(
    internal var conn: WebSocket,
    val config: BotConfig,
    val actionHandler: ActionHandler
) {
    private var name: String = "Onebot"
    private var version: String = "Unknown"
    private var onebot: Int = 11
    val appName: String
        get() = name
    val appVersion: String
        get() = version
    val onebotVersion: Int
        get() = onebot
    val channel: WebSocket
        get() = conn
    private var idInternal: Long = 0
    val id: Long
        get() = idInternal

    private fun JsonObject.addMessage(key: String, msg: String) {
        runCatching {
            JsonParser.parseString(msg).asJsonArray
        }.onSuccess {
            if (config.useCQCode) {
                addProperty(key, CQCode.fromJson(it))
            } else {
                add(key, it)
            }
        }.onFailure {
            addProperty(key, msg)
        }
    }

    companion object {
        fun Context.build(action: ActionPath): ActionContext {
            return ActionContext.builder(action.path).also(::invoke).build()
        }

        @JvmStatic
        fun getDefaultContext(): Context = {}
    }

    /**
     * 发送私聊消息
     *
     * @param userId     对方 QQ 号
     * @param groupId    主动发起临时会话时的来源群号 (可选，机器人本身必须是管理员/群主)
     * @param msg        要发送的内容，请使用CQ码或json数组消息
     * @param autoEscape 消息内容是否作为纯文本发送 ( 即不解析 CQ 码 ) , 只在 message 字段是字符串时有效
     * @param context    Onebot 主动操作的上下文
     * @return [ActionData] of [MsgId]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun sendPrivateMsg(
        userId: Long,
        groupId: Long?,
        msg: String,
        autoEscape: Boolean,
        context: Context = {},
    ): ActionData<MsgId> {
        val action = context.build(ActionPathEnum.SEND_PRIVATE_MSG)
        val params = JsonObject()
        params.addProperty("user_id", userId)
        if (groupId != null) params.addProperty("group_id", groupId)
        params.addMessage("message", msg)
        params.addProperty("auto_escape", autoEscape)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 发送群消息
     *
     * @param groupId    群号
     * @param msg        要发送的内容，请使用CQ码或json数组消息
     * @param autoEscape 消息内容是否作为纯文本发送 ( 即不解析 CQ 码 ) , 只在 message 字段是字符串时有效
     * @param context    Onebot 主动操作的上下文
     * @return [ActionData] of [MsgId]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun sendGroupMsg(
        groupId: Long,
        msg: String,
        autoEscape: Boolean,
        context: Context = {},
    ): ActionData<MsgId> {
        val action = context.build(ActionPathEnum.SEND_GROUP_MSG)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addMessage("message", msg)
        params.addProperty("auto_escape", autoEscape)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取消息
     *
     * @param msgId      消息 ID
     * @param context    Onebot 主动操作的上下文
     * @return [ActionData] of [GetMsgResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getMsg(
        msgId: Int,
        context: Context = {},
    ): ActionData<GetMsgResp> {
        val action = context.build(ActionPathEnum.GET_MSG)
        val params = JsonObject()
        params.addProperty("message_id", msgId)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取历史消息
     *
     * @param messageType 消息类型
     * @param userId      私聊QQ
     * @param groupId     群号
     * @param count       获取的消息数量（默认为20）
     * @param messageSeq  起始消息的message_id（默认为0，表示从最后一条发言往前）
     * @param context     Onebot 主动操作的上下文
     * @return [ActionData] of [GetHistoryMsgResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getHistoryMsg(
        messageType: String,
        userId: Long? = null,
        groupId: Long? = null,
        count: Int = 20,
        messageSeq: Int = 0,
        context: Context = {}
    ): ActionData<GetHistoryMsgResp> {
        val action = context.build(ActionPathEnum.GET_HISTORY_MSG)
        val params = JsonObject()
        params.addProperty("message_type", messageType)
        if (userId != null) params.addProperty("user_id", userId)
        if (groupId != null) params.addProperty("group_id", groupId)
        params.addProperty("count", count)
        params.addProperty("message_seq", messageSeq)

        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取群聊历史消息
     *
     * @param groupId    群号
     * @param count      获取的消息数量（默认为20）
     * @param messageSeq 起始消息的message_id（默认为0，表示从最后一条发言往前）
     * @param context    Onebot 主动操作的上下文
     * @return [ActionData] of [GetHistoryMsgResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getGroupMsgHistory(
        groupId: Long,
        count: Int = 20,
        messageSeq: Int = 0,
        context: Context = {}
    ): ActionData<GetHistoryMsgResp> {
        val action = context.build(ActionPathEnum.GET_GROUP_MSG_HISTORY)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("count", count)
        params.addProperty("message_seq", messageSeq)

        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 撤回消息
     *
     * @param msgId      消息 ID
     * @param context    Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun deleteMsg(
        msgId: Int,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.DELETE_MSG)
        val params = JsonObject()
        params.addProperty("message_id", msgId)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 群组踢人
     *
     * @param groupId          群号
     * @param userId           要踢的 QQ 号
     * @param rejectAddRequest 拒绝此人的加群请求 (默认false)
     * @param context          Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupKick(
        groupId: Long,
        userId: Long,
        rejectAddRequest: Boolean,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_GROUP_KICK)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("user_id", userId)
        params.addProperty("reject_add_request", rejectAddRequest)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 群组单人禁言
     *
     * @param groupId  群号
     * @param userId   要禁言的 QQ 号
     * @param duration 禁言时长, 单位秒, 0 表示取消禁言 (默认30 * 60)
     * @param context  Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupBan(
        groupId: Long,
        userId: Long,
        duration: Int,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_GROUP_BAN)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("user_id", userId)
        params.addProperty("duration", duration)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 全体禁言
     *
     * @param groupId 群号
     * @param enable  是否禁言（默认True,False为取消禁言）
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupWholeBan(
        groupId: Long,
        enable: Boolean,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_GROUP_WHOLE_BAN)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("enable", enable)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 群组设置管理员
     *
     * @param groupId 群号
     * @param userId  要设置管理员的 QQ 号
     * @param enable  true 为设置，false 为取消
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupAdmin(
        groupId: Long,
        userId: Long,
        enable: Boolean,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_GROUP_ADMIN)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("user_id", userId)
        params.addProperty("enable", enable)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 群组匿名
     *
     * @param groupId 群号
     * @param enable  是否允许匿名聊天
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupAnonymous(
        groupId: Long,
        enable: Boolean,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_GROUP_ANONYMOUS)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("enable", enable)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 设置群名片（群备注）
     *
     * @param groupId 群号
     * @param userId  要设置的 QQ 号
     * @param card    群名片内容，不填或空字符串表示删除群名片
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupCard(
        groupId: Long,
        userId: Long,
        card: String,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_GROUP_CARD)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("user_id", userId)
        params.addProperty("card", card)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 设置群名
     *
     * @param groupId   群号
     * @param groupName 新群名
     * @param context   Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupName(
        groupId: Long,
        groupName: String,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_GROUP_NAME)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("group_name", groupName)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 退出群组
     *
     * @param groupId   群号
     * @param isDismiss 是否解散, 如果登录号是群主, 则仅在此项为 true 时能够解散
     * @param context   Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupLeave(
        groupId: Long,
        isDismiss: Boolean,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_GROUP_LEAVE)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("is_dismiss", isDismiss)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 设置群组专属头衔
     *
     * @param groupId      群号
     * @param userId       要设置的 QQ 号
     * @param specialTitle 专属头衔，不填或空字符串表示删除专属头衔
     * @param duration     专属头衔有效期，单位秒，-1 表示永久，不过此项似乎没有效果，可能是只有某些特殊的时间长度有效，有待测试
     * @param context      Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupSpecialTitle(
        groupId: Long,
        userId: Long,
        specialTitle: String,
        duration: Int,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_GROUP_SPECIAL_TITLE)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("user_id", userId)
        params.addProperty("special_title", specialTitle)
        params.addProperty("duration", duration)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 处理加好友请求
     *
     * @param flag    加好友请求的 flag（需从上报的数据中获得）
     * @param approve 是否同意请求(默认为true)
     * @param remark  添加后的好友备注（仅在同意时有效）
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setFriendAddRequest(
        flag: String,
        approve: Boolean,
        remark: String,
        context: Context = {}
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_FRIEND_ADD_REQUEST)
        val params = JsonObject()
        params.addProperty("flag", flag)
        params.addProperty("approve", approve)
        params.addProperty("remark", remark)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 处理加群请求／邀请
     *
     * @param flag    加群请求的 flag（需从上报的数据中获得）
     * @param subType add 或 invite，请求类型（需要和上报消息中的 sub_type 字段相符）
     * @param approve 是否同意请求／邀请
     * @param reason  拒绝理由（仅在拒绝时有效）
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupAddRequest(
        flag: String,
        subType: String,
        approve: Boolean,
        reason: String,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_GROUP_ADD_REQUEST)
        val params = JsonObject()
        params.addProperty("flag", flag)
        params.addProperty("sub_type", subType)
        params.addProperty("approve", approve)
        params.addProperty("reason", reason)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 获取登录号信息
     *
     * @param context    Onebot 主动操作的上下文
     * @return [ActionData] of @[LoginInfoResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getLoginInfo(
        context: Context = {},
    ): ActionData<LoginInfoResp> {
        val action = context.build(ActionPathEnum.GET_LOGIN_INFO)
        val result = actionHandler.action(this, action)
        return result.withToken<ActionData<LoginInfoResp>>().also {
            it.data?.userId?.also { id -> idInternal = id }
        }
    }

    /**
     * 获取陌生人信息
     *
     * @param userId  QQ 号
     * @param noCache 是否不使用缓存（使用缓存可能更新不及时，但响应更快）
     * @param context Onebot 主动操作的上下文
     * @return [ActionData] of [StrangerInfoResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getStrangerInfo(
        userId: Long,
        noCache: Boolean,
        context: Context = {},
    ): ActionData<StrangerInfoResp> {
        val action = context.build(ActionPathEnum.GET_STRANGER_INFO)
        val params = JsonObject()
        params.addProperty("user_id", userId)
        params.addProperty("no_cache", noCache)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取好友列表
     *
     * @param context    Onebot 主动操作的上下文
     * @return [ActionList] of [FriendInfoResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getFriendList(
        context: Context = {},
    ): ActionList<FriendInfoResp> {
        val action = context.build(ActionPathEnum.GET_FRIEND_LIST)
        val result = actionHandler.action(this, action)
        return result.withToken()
    }

    /**
     * 删除好友
     *
     * @param friendId 好友 QQ 号
     * @param context  Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun deleteFriend(
        friendId: Long,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.DELETE_FRIEND)
        val params = JsonObject()
        params.addProperty("friend_id", friendId)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 获取群信息
     *
     * @param groupId 群号
     * @param noCache 是否不使用缓存（使用缓存可能更新不及时，但响应更快）
     * @param context Onebot 主动操作的上下文
     * @return [ActionData] of [GroupInfoResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getGroupInfo(
        groupId: Long,
        noCache: Boolean,
        context: Context = {},
    ): ActionData<GroupInfoResp> {
        val action = context.build(ActionPathEnum.GET_GROUP_INFO)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("no_cache", noCache)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取群列表
     *
     * @param context    Onebot 主动操作的上下文
     * @return [ActionList] of [GroupInfoResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getGroupList(
        context: Context = {},
    ): ActionList<GroupInfoResp> {
        val action = context.build(ActionPathEnum.GET_GROUP_LIST)
        val result = actionHandler.action(this, action, JsonObject().apply { addProperty("no_cache", false) })
        return result.withToken()
    }

    /**
     * 获取群成员信息
     *
     * @param groupId 群号
     * @param userId  QQ 号
     * @param noCache 是否不使用缓存（使用缓存可能更新不及时，但响应更快）
     * @param context Onebot 主动操作的上下文
     * @return [ActionData] of [GroupMemberInfoResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getGroupMemberInfo(
        groupId: Long,
        userId: Long,
        noCache: Boolean,
        context: Context = {},
    ): ActionData<GroupMemberInfoResp> {
        val action = context.build(ActionPathEnum.GET_GROUP_MEMBER_INFO)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("user_id", userId)
        params.addProperty("no_cache", noCache)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取群成员列表
     *
     * @param groupId 群号
     * @param context Onebot 主动操作的上下文
     * @return [ActionList] of [GroupMemberInfoResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getGroupMemberList(
        groupId: Long,
        context: Context = {},
    ): ActionList<GroupMemberInfoResp> {
        val action = context.build(ActionPathEnum.GET_GROUP_MEMBER_LIST)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("no_cache", false)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取群荣誉信息
     *
     * @param groupId 群号
     * @param type    要获取的群荣誉类型, 可传入 talkative performer legend strong_newbie emotion 以分别获取单个类型的群荣誉数据, 或传入 all 获取所有数据
     * @param context Onebot 主动操作的上下文
     * @return [ActionData] of [GroupHonorInfoResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getGroupHonorInfo(
        groupId: Long,
        type: String,
        context: Context = {},
    ): ActionData<GroupHonorInfoResp> {
        val action = context.build(ActionPathEnum.GET_GROUP_HONOR_INFO)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("type", type)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 设置群头像
     * 目前这个API在登录一段时间后因cookie失效而失效, 请考虑后使用
     *
     * @param groupId 群号
     * @param file    图片文件名（支持绝对路径，网络URL，Base64编码）
     * @param cache   表示是否使用已缓存的文件 （通过网络URL发送时有效, 1表示使用缓存, 0关闭关闭缓存, 默认为1）
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupPortrait(
        groupId: Long,
        file: String,
        cache: Int,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_GROUP_PORTRAIT)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("file", file)
        params.addProperty("cache", cache)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 发送群公告
     *
     * @param groupId 群号
     * @param content 公告内容
     * @param image   公告图片
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun sendGroupNotice(
        groupId: Long,
        content: String,
        image: String?,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SEND_GROUP_NOTICE)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("content", content)
        if (image != null) params.addProperty("image", image)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }
    
    /**
     * 获取群公告
     *
     * @param groupId 群号
     * @param context Onebot 主动操作的上下文
     * @return [ActionList] of [GroupNoticeResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getGroupNotice(
        groupId: Long,
        context: Context = {},
    ): ActionList<GroupNoticeResp> {
        val action = context.build(ActionPathEnum.GET_GROUP_NOTICE)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取群 @全体成员 剩余次数
     *
     * @param groupId 群号
     * @param context Onebot 主动操作的上下文
     * @return [ActionData] of [GroupAtAllRemainResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getGroupAtAllRemain(
        groupId: Long,
        context: Context = {},
    ): ActionData<GroupAtAllRemainResp> {
        val action = context.build(ActionPathEnum.GET_GROUP_AT_ALL_REMAIN)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
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
     * @param context Onebot 主动操作的上下文
     * @return [ActionData] of [UploadGroupFileResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun uploadGroupFile(
        groupId: Long,
        file: String,
        name: String,
        folder: String,
        context: Context = {},
    ): ActionData<UploadGroupFileResp> {
        val action = context.build(ActionPathEnum.UPLOAD_GROUP_FILE)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("file", file)
        params.addProperty("name", name)
        params.addProperty("folder", folder)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 群组匿名用户禁言
     *
     * @param groupId   群号
     * @param anonymous 要禁言的匿名用户对象（群消息上报的 anonymous 字段）
     * @param duration  禁言时长，单位秒，无法取消匿名用户禁言
     * @param context   Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupAnonymousBan(
        groupId: Long,
        anonymous: Anonymous?,
        duration: Int,
        context: Context = {},
    ): ActionRaw {
        val gson = GsonBuilder().create()
        val action = context.build(ActionPathEnum.SET_GROUP_ANONYMOUS_BAN)
        val an = gson.toJsonTree(anonymous, Anonymous::class.java)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.add("anonymous", an)
        params.addProperty("duration", duration)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 群组匿名用户禁言
     *
     * @param groupId  群号
     * @param flag     要禁言的匿名用户的 flag（需从群消息上报的数据中获得）
     * @param duration 禁言时长，单位秒，无法取消匿名用户禁言
     * @param context  Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setGroupAnonymousBan(
        groupId: Long,
        flag: String,
        duration: Int,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_GROUP_ANONYMOUS_BAN)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("flag", flag)
        params.addProperty("duration", duration)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 发送合并转发 (群)
     *
     * @param groupId 群号
     * @param msg     自定义转发消息
     * @param source  转发来源（标题 `XXX的聊天记录`）
     * @param summary 转发总概述（底部的 `查看XX条转发消息`）
     * @param preview 转发预览（中间的 `人名: 消息`）
     * @param prompt  在消息列表看到的纯文字外显（`[转发消息]`）
     * @param context Onebot 主动操作的上下文
     *
     * [参考文档](https://docs.go-cqhttp.org/cqcode/#%E5%90%88%E5%B9%B6%E8%BD%AC%E5%8F%91)
     * @return [ActionData]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun sendGroupForwardMsg(
        groupId: Long,
        msg: List<Map<String, Any>>,
        // NapCat: 转发消息外显
        source: String? = null,
        summary: String? = null,
        preview: List<String>? = null,
        prompt: String? = null,
        context: Context = {},
    ): ActionData<MsgId> {
        val action = context.build(ActionPathEnum.SEND_GROUP_FORWARD_MSG)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.add("messages", msg.toJsonArray())
        // NapCat: 转发消息外显
        if (source != null) params.addProperty("source", source)
        if (summary != null) params.addProperty("summary", summary)
        if (prompt != null) params.addProperty("prompt", prompt)
        if (preview != null) {
            val array = JsonArray()
            for (line in preview) {
                array.add(JsonObject().apply {
                    addProperty("text", line)
                })
            }
            params.add("news", array)
        }

        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }
    /**
     * 发送合并转发 (群)
     *
     * @param groupId 群号
     * @param msg     自定义转发消息
     * @param context Onebot 主动操作的上下文
     * [参考文档](https://lagrangedev.github.io/Lagrange.Doc/Lagrange.OneBot/API/Extend/#%E5%8F%91%E9%80%81%E5%90%88%E5%B9%B6%E8%BD%AC%E5%8F%91-%E7%BE%A4%E8%81%8A)
     * @return [ActionData] resId
     */
    @Deprecated(
        message = "自 Lagrange 0.0.2 (7bcfdbb) 起，发送合并转发的实现与 go-cqhttp 一致，无需额外兼容",
        replaceWith = ReplaceWith("sendGroupForwardMsg"),
        level = DeprecationLevel.ERROR
    )
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun sendGroupForwardMsgLagrange(
        groupId: Long,
        msg: List<Map<String, Any>>,
        context: Context = {}
    ): ActionData<String> {
        val action = context.build(ActionPathEnum.SEND_GROUP_FORWARD_MSG)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.add("messages", msg.toJsonArray())

        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }
    /**
     * 获取群根目录文件列表
     *
     * @param groupId 群号
     * @param context Onebot 主动操作的上下文
     * @return [ActionData] of [GroupFilesResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getGroupRootFiles(
        groupId: Long,
        context: Context = {},
    ): ActionData<GroupFilesResp> {
        val action = context.build(ActionPathEnum.GET_GROUP_ROOT_FILES)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取群子目录文件列表
     *
     * @param groupId  群号
     * @param folderId 文件夹ID 参考 Folder 对象
     * @param context  Onebot 主动操作的上下文
     * @return [ActionData] of [GroupFilesResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getGroupFilesByFolder(
        groupId: Long,
        folderId: String,
        context: Context = {},
    ): ActionData<GroupFilesResp> {
        val action = context.build(ActionPathEnum.GET_GROUP_FILES_BY_FOLDER)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("folder_id", folderId)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 创建群文件夹
     * @param groupId  群号
     * @param name     文件夹名称
     * @param parentId 父文件夹ID 参考 Folder 对象
     * @param context  Onebot 主动操作的上下文
     * @return [ActionData] of [CreateGroupFileFolderResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun createGroupFileFolder(
        groupId: Long,
        name: String,
        parentId: String,
        context: Context = {},
    ): ActionData<CreateGroupFileFolderResp> {
        val action = context.build(ActionPathEnum.CREATE_GROUP_FILE_FOLDER)
        val params = JsonObject().apply {
            addProperty("group_id", groupId)
            addProperty("name", name)
            addProperty("parent_id", parentId)
        }
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取群文件下载链接
     *
     * @param groupId  群号
     * @param fileId   文件ID
     * @param busid    此参数可在群文件相关接口中获取
     * @param context  Onebot 主动操作的上下文
     * @return [ActionData] of [GroupFileUrlResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getGroupFileUrl(
        groupId: Long,
        fileId: String,
        busid: Int?,
        context: Context = {},
    ): ActionData<GroupFileUrlResp> {
        val action = context.build(ActionPathEnum.GET_GROUP_FILE_URL)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("file_id", fileId)
        if (busid != null) params.addProperty("busid", busid)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 自定义请求 ActionData
     *
     * @param action  请求路径
     * @param params  请求参数
     * @param context Onebot 主动操作的上下文
     * @return [ActionData]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend inline fun <reified T : Any> customRequestData(
        action: ActionPath,
        params: JsonObject?,
        noinline context: Context = {
            ignoreStatus(true)
        },
    ): ActionData<T> {
        val result = actionHandler.action(this, context.build(action), params)
        return result.withToken()
    }
    /**
     * 自定义请求 ActionList
     *
     * @param action  请求路径
     * @param params  请求参数
     * @param context Onebot 主动操作的上下文
     * @return [ActionList]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend inline fun <reified T : Any> customRequestList(
        action: ActionPath,
        params: JsonObject?,
        noinline context: Context = {
            ignoreStatus(true)
        },
    ): ActionList<T> {
        val result = actionHandler.action(this, context.build(action), params)
        return result.withToken()
    }
    /**
     * 自定义请求 ActionRaw
     *
     * @param action  请求路径
     * @param params  请求参数
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun customRequestRaw(
        action: ActionPath,
        params: JsonObject?,
        context: Context = {
            ignoreStatus(true)
        },
    ): ActionRaw {
        val result = actionHandler.action(this, context.build(action), params)
        return result.withClass()
    }
    /**
     * 自定义请求
     *
     * @param action  请求路径
     * @param params  请求参数 json
     * @param context Onebot 主动操作的上下文
     * @return [JsonObject]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun customRequest(
        action: String,
        params: String?,
        context: Context = {
            ignoreStatus(true)
        },
    ): JsonObject {
        return customRequest(object: ActionPath {
            override val path: String = action
        }, params, context)
    }

    /**
     * 自定义请求
     *
     * @param action  请求路径
     * @param params  请求参数 json
     * @param context Onebot 主动操作的上下文
     * @return [JsonObject]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun customRequest(
        action: ActionPath,
        params: String?,
        context: Context = {
            ignoreStatus(true)
        },
    ): JsonObject {
        return customRequest(
            params = params,
            context = context.build(action),
        )
    }

    /**
     * 自定义请求
     *
     * @param params  请求参数 json
     * @param context Onebot 主动操作的上下文
     * @return [JsonObject]
     */
    @JvmBlockingBridge
    suspend fun customRequest(
        params: String?,
        context: ActionContext,
    ): JsonObject {
        return actionHandler.action(
            bot = this,
            context = context,
            params = params?.run { JsonParser.parseString(this).asJsonObject },
        )
    }

    /**
     * 获取转发消息
     *
     * @param resourceId 转发消息的ID
     * @param context    Onebot 主动操作的上下文
     * @return [ActionList] of [ForwardMsgResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getForwardMsg(
        resourceId: String,
        context: Context = {},
    ): ActionData<ForwardMsgResp> {
        val action = context.build(ActionPathEnum.GET_FORWARD_MSG)
        val params = JsonObject()
        params.addProperty("id", resourceId)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取精华消息列表
     *
     * @param groupId 群号
     * @param context    Onebot 主动操作的上下文
     * @return [ActionList] of [EssenceMsgResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getEssenceMsgList(
        groupId: Long,
        page: Int = 0,
        pageSize: Int = 20,
        context: Context = {},
    ): ActionList<EssenceMsgResp> {
        val action = context.build(ActionPathEnum.GET_ESSENCE_MSG_LIST)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("page", page)
        params.addProperty("page_size", pageSize)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 设置精华消息
     *
     * @param msgId      消息 ID
     * @param context    Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setEssenceMsg(
        msgId: Int,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_ESSENCE_MSG)
        val params = JsonObject()
        params.addProperty("message_id", msgId)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 移出精华消息
     *
     * @param msgId   消息 ID
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun deleteEssenceMsg(
        msgId: Int,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.DELETE_ESSENCE_MSG)
        val params = JsonObject()
        params.addProperty("message_id", msgId)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 设置机器人账号资料
     *
     * @param nickname     昵称
     * @param company      公司
     * @param email        邮箱
     * @param college      学校
     * @param personalNote 个性签名
     * @param context      Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun setBotProfile(
        nickname: String,
        company: String,
        email: String,
        college: String,
        personalNote: String,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SET_QQ_PROFILE)
        val params = JsonObject()
        params.addProperty("nickname", nickname)
        params.addProperty("company", company)
        params.addProperty("email", email)
        params.addProperty("college", college)
        params.addProperty("personalNote", personalNote)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }
    /**
     * 发送合并转发 (私聊)
     *
     * @param userId 目标用户
     * @param msg    自定义转发消息
     * @param source  转发来源（标题 `XXX的聊天记录`）
     * @param summary 转发总概述（底部的 `查看XX条转发消息`）
     * @param preview 转发预览（中间的 `人名: 消息`）
     * @param prompt  在消息列表看到的纯文字外显（`[转发消息]`）
     * @param context Onebot 主动操作的上下文
     *
     * [参考文档](https://docs.go-cqhttp.org/cqcode/#%E5%90%88%E5%B9%B6%E8%BD%AC%E5%8F%91)
     * @return [ActionData]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun sendPrivateForwardMsg(
        userId: Long,
        msg: List<Map<String, Any>>,
        // NapCat: 转发消息外显
        source: String? = null,
        summary: String? = null,
        preview: List<String>? = null,
        prompt: String? = null,
        context: Context = {},
    ): ActionData<MsgId> {
        val action = context.build(ActionPathEnum.SEND_PRIVATE_FORWARD_MSG)
        val params = JsonObject()
        params.addProperty("user_id", userId)
        params.add("messages", msg.toJsonArray())
        // NapCat: 转发消息外显
        if (source != null) params.addProperty("source", source)
        if (summary != null) params.addProperty("summary", summary)
        if (prompt != null) params.addProperty("prompt", prompt)
        if (preview != null) {
            val array = JsonArray()
            for (line in preview) {
                array.add(JsonObject().apply {
                    addProperty("text", line)
                })
            }
            params.add("news", array)
        }

        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }
    /**
     * 上传合并转发
     *
     * @param msg     自定义转发消息
     * @param context Onebot 主动操作的上下文
     * [参考文档](https://github.com/LagrangeDev/Lagrange.Core/blob/master/Lagrange.OneBot/Core/Operation/Message/SendForwardMessageOperation.cs)
     * @return [ActionData] resId
     */
    @Deprecated(
        message = "自 Lagrange 0.0.2 (7bcfdbb) 起，发送合并转发的实现与 go-cqhttp 一致，无需额外兼容",
        replaceWith = ReplaceWith("sendForwardMsg"),
        level = DeprecationLevel.ERROR
    )
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun sendForwardMsgLagrange(
        msg: List<Map<String, Any>>,
        context: Context = {},
    ): ActionData<String> {
        val action = context.build(ActionPathEnum.SEND_FORWARD_MSG)
        val params = JsonObject()

        params.add("messages", msg.toJsonArray())

        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取当前账号在线客户端列表
     *
     * @param noCache 是否无视缓存
     * @param context Onebot 主动操作的上下文
     * @return [ActionData] of [ClientsResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getOnlineClients(
        noCache: Boolean,
        context: Context = {},
    ): ActionData<ClientsResp> {
        val action = context.build(ActionPathEnum.GET_ONLINE_CLIENTS)
        val params = JsonObject()
        params.addProperty("no_cache", noCache)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 群打卡
     *
     * @param groupId 群号
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun sendGroupSign(
        groupId: Long,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.SEND_GROUP_SIGN)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 获取版本信息
     *
     * @param context    Onebot 主动操作的上下文
     * @return [JsonObject]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getVersionInfo(
        context: Context = {
            showWarning(false)
        },
    ): JsonObject {
        val action11 = context.build(ActionPathEnum.GET_VERSION_INFO)
        return actionHandler.action(this, action11).apply {
            if (ignorable("status", "failed") != "ok") {
                val action12 = context.build(ActionPathEnum.GET_VERSION)
                val result = actionHandler.action(this@Bot, action12)
                val data = result.ignorableObject("data") { JsonObject() }
                val target = data.ignorable("onebot_version", "unknown (12 request)")
                if (target != "12") {
                    throw IllegalStateException("无法获取该 Onebot 实现的版本信息 [$target]，确定你连接的 Onebot 实现为 Onebot 11 或 Onebot 12 吗？")
                }
                name = data.ignorable("impl", "onebot").trim()
                version = data.ignorable("version", "Unknown").trim()
                onebot = 12
            } else {
                val data = ignorableObject("data") { JsonObject() }
                val target = data.ignorable("protocol_version", "unknown (11 request)")
                if (target != "v11") {
                    throw IllegalStateException("无法获取该 Onebot 实现的版本信息 [$target]，确定你连接的 Onebot 实现为 Onebot 11 或 Onebot 12 吗？")
                }
                name = data.ignorable("app_name", "onebot").trim()
                version = data.ignorable("app_version", "Unknown").trim()
                onebot = 11
            }
        }
    }

    /**
     * 获取 Cookie
     *
     * @param domain     需要获取 Cookie 的网站域名
     * @param context    Onebot 主动操作的上下文
     * @return [ActionData] of [CookiesResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getCookies(
        domain: String,
        context: Context = {}
    ): ActionData<CookiesResp> {
        val action = context.build(ActionPathEnum.GET_COOKIES)
        val params = JsonObject()
        params.addProperty("domain", domain)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取 CSRF Token
     *
     * @param context    Onebot 主动操作的上下文
     * @return [ActionData] of [CSRFTokenResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getCSRFToken(
        context: Context = {},
    ): ActionData<CSRFTokenResp> {
        val action = context.build(ActionPathEnum.GET_CSRF_TOKEN)
        val result = actionHandler.action(this, action)
        return result.withToken()
    }

    /**
     * 获取 Cookie 和 CSRF Token
     *
     * @param domain     需要获取 Cookie 的网站域名
     * @param context    Onebot 主动操作的上下文
     * @return [ActionData] of [CredentialsResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun getCredentials(
        domain: String,
        context: Context = {},
    ): ActionData<CredentialsResp> {
        val action = context.build(ActionPathEnum.GET_CREDENTIALS)
        val params = JsonObject()
        params.addProperty("domain", domain)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 删除群文件
     *
     * @param groupId    群号
     * @param fileId     文件ID
     * @param busid      此参数可在群文件相关接口中获取
     * @param context    Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun deleteGroupFile(
        groupId: Long,
        fileId: String,
        busid: Int?,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.DELETE_GROUP_FILE)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("file_id", fileId)
        if (busid != null) params.addProperty("busid", busid)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 删除群文件夹
     *
     * @param groupId  群号
     * @param folderId 文件夹ID
     * @param context  Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun deleteGroupFolder(
        groupId: Long,
        folderId: String,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.DELETE_GROUP_FOLDER)
        val params = JsonObject()
        params.addProperty("group_id", groupId)
        params.addProperty("folder_id", folderId)
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 获取群或好友文件信息
     *
     * LLOnebot, NapCat
     * @param fileId    文件ID
     * @param context   Onebot 主动操作的上下文
     * @return [ActionData] of [GetFileResp]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun extGetFile(
        fileId: String,
        context: Context = {},
    ): ActionData<GetFileResp> {
        val action = context.build(ActionPathEnum.EXT_GET_FILE)
        val params = JsonObject()
        params.addProperty("file_id", fileId)
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    /**
     * 获取用户头像地址
     *
     * Gensokyo
     * @param groupId  群号，获取群成员头像时使用
     * @param userId   QQ号，获取其它用户头像时使用
     * @param context  Onebot 主动操作的上下文
     * @return [ActionData] of [String]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun extGetAvatar(
        groupId: Long?,
        userId: Long,
        context: Context = {
            ignoreStatus(true)
        },
    ): ActionData<String> {
        val action = context.build(ActionPathEnum.EXT_GET_AVATAR)
        val params = JsonObject().apply {
            if (groupId != null) addProperty("group_id", groupId)
            addProperty("user_id", userId)
        }
        val result = actionHandler.action(this, action, params)
        if (!result.has("status")) {
            return ActionData("success", 0, result.nullableString("message"), result.nullableString("echo"))
        }
        return result.withToken()
    }

    /**
     * 好友戳一戳
     *
     * LLOnebot、NapCat
     * @param userId  好友QQ号
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun extFriendPoke(
        userId: Long,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.EXT_FRIEND_POKE)
        val params = JsonObject().apply {
            addProperty("user_id", userId)
        }
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    /**
     * 群聊戳一戳
     *
     * LLOnebot、NapCat
     * @param groupId 群号
     * @param userId  好友QQ号
     * @param context Onebot 主动操作的上下文
     * @return [ActionRaw]
     */
    @JvmBlockingBridge
    @JvmOverloads
    suspend fun extGroupPoke(
        groupId: Long,
        userId: Long,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.EXT_GROUP_POKE)
        val params = JsonObject().apply {
            addProperty("group_id", groupId)
            addProperty("user_id", userId)
        }
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }

    suspend fun extGroupReaction(
        groupId: Long,
        messageId: Int,
        icon: String,
        enable: Boolean,
        context: Context = {},
    ): ActionData<SetGroupReactionResp> {
        val action = context.build(ActionPathEnum.EXT_SET_GROUP_REACTION)
        val params = JsonObject().apply {
            when (appName.lowercase()) {
                "go-cqhttp" -> { // astral
                    addProperty("message_id", messageId)
                    addProperty("icon_id", icon)
                    addProperty("icon_type", if (icon.length > 3) 2 else 1)
                    addProperty("enable", enable)
                }
                "lagrange.onebot" -> { // Lagrange
                    addProperty("group_id", groupId)
                    addProperty("message_id", messageId)
                    addProperty("code", icon)
                    addProperty("is_add", enable)
                }
                else -> throw IllegalStateException("这个接口只能在 AstralGocq 或 Lagrange 执行")
            }
        }
        val result = actionHandler.action(this, action, params)
        return result.withToken()
    }

    suspend fun extSetMsgEmojiLike(
        messageId: Int,
        emojiId: String,
        context: Context = {},
    ): ActionRaw {
        val action = context.build(ActionPathEnum.EXT_SET_MSG_EMOJI_LIKE)
        val params = JsonObject().apply {
            when (appName.lowercase()) {
                "napcat.onebot" -> addProperty("message_id", messageId)
                "llonebot" -> addProperty("message_id", messageId.toString())
                else -> throw IllegalStateException("这个接口只能在 NapCat 或 LLOnebot 执行")
            }
            addProperty("emoji_id", emojiId)
        }
        val result = actionHandler.action(this, action, params)
        return result.withClass()
    }
}
