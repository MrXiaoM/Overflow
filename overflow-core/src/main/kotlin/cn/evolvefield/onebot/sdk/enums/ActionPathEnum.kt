package cn.evolvefield.onebot.sdk.enums

import cn.evolvefield.onebot.sdk.action.ActionPath

enum class ActionPathEnum(
    /**
     * 请求路径
     */
    override val path: String
) : ActionPath {
    /**
     * 发送私聊消息
     */
    SEND_PRIVATE_MSG("send_private_msg"),
    /**
     * 发送群消息
     */
    SEND_GROUP_MSG("send_group_msg"),
    /**
     * 撤回消息
     */
    DELETE_MSG("delete_msg"),
    /**
     * 获取消息
     */
    GET_MSG("get_msg"),
    /**
     * 获取历史消息
     */
    GET_HISTORY_MSG("get_history_msg"),
    /**
     * 获取群聊历史消息
     */
    GET_GROUP_MSG_HISTORY("get_group_msg_history"),
    /**
     * 获取转发消息内容
     */
    GET_FORWARD_MSG("get_forward_msg"),
    /**
     * 群组踢人
     */
    SET_GROUP_KICK("set_group_kick"),
    /**
     * 群组单人禁言
     */
    SET_GROUP_BAN("set_group_ban"),
    /**
     * 群组全体禁言
     */
    SET_GROUP_WHOLE_BAN("set_group_whole_ban"),
    /**
     * 群组设置管理员
     */
    SET_GROUP_ADMIN("set_group_admin"),
    /**
     * 群组匿名
     */
    SET_GROUP_ANONYMOUS("set_group_anonymous"),
    /**
     * 设置群名片（群备注）
     */
    SET_GROUP_CARD("set_group_card"),
    /**
     * 设置群名
     */
    SET_GROUP_NAME("set_group_name"),
    /**
     * 退出群组
     */
    SET_GROUP_LEAVE("set_group_leave"),
    /**
     * 设置群组专属头衔
     */
    SET_GROUP_SPECIAL_TITLE("set_group_special_title"),
    /**
     * 处理加好友请求
     */
    SET_FRIEND_ADD_REQUEST("set_friend_add_request"),
    /**
     * 《处理加群请求／邀请
     */
    SET_GROUP_ADD_REQUEST("set_group_add_request"),
    /**
     * 《获取登录号信息
     */
    GET_LOGIN_INFO("get_login_info"),
    /**
     * 获取陌生人信息
     */
    GET_STRANGER_INFO("get_stranger_info"),
    /**
     * 获取好友列表
     */
    GET_FRIEND_LIST("get_friend_list"),
    /**
     * 删除好友
     */
    DELETE_FRIEND("delete_friend"),
    /**
     * 获取群信息
     */
    GET_GROUP_INFO("get_group_info"),
    /**
     * 获取群列表
     */
    GET_GROUP_LIST("get_group_list"),
    /**
     * 获取群成员信息
     */
    GET_GROUP_MEMBER_INFO("get_group_member_info"),
    /**
     * 获取群成员列表
     */
    GET_GROUP_MEMBER_LIST("get_group_member_list"),
    /**
     * 获取群荣誉信息
     */
    GET_GROUP_HONOR_INFO("get_group_honor_info"),
    /**
     * 设置群头像
     */
    SET_GROUP_PORTRAIT("set_group_portrait"),
    /**
     * 发送群公告
     */
    SEND_GROUP_NOTICE("_send_group_notice"),
    /**
     * 获取群公告
     */
    GET_GROUP_NOTICE("_get_group_notice"),
    /**
     * 获取群 @全体成员 剩余次数
     */
    GET_GROUP_AT_ALL_REMAIN("get_group_at_all_remain"),
    /**
     * 上传群文件
     */
    UPLOAD_GROUP_FILE("upload_group_file"),
    /**
     * 群组匿名用户禁言
     */
    SET_GROUP_ANONYMOUS_BAN("set_group_anonymous_ban"),
    /**
     * 发送合并转发 (群)
     */
    SEND_GROUP_FORWARD_MSG("send_group_forward_msg"),
    /**
     * 获取群根目录文件列表
     */
    GET_GROUP_ROOT_FILES("get_group_root_files"),
    /**
     * 获取群子目录文件列表
     */
    GET_GROUP_FILES_BY_FOLDER("get_group_files_by_folder"),
    /**
     * 设置精华消息
     */
    SET_ESSENCE_MSG("set_essence_msg"),
    /**
     * 移出精华消息
     */
    DELETE_ESSENCE_MSG("delete_essence_msg"),
    /**
     * 获取精华消息列表
     */
    GET_ESSENCE_MSG_LIST("get_essence_msg_list"),
    /**
     * 发送合并转发 (私聊)
     */
    SEND_PRIVATE_FORWARD_MSG("send_private_forward_msg"),
    /**
     * 设置QQ资料
     */
    SET_QQ_PROFILE("set_qq_profile"),
    /**
     * 获取当前账号在线客户端列表
     */
    GET_ONLINE_CLIENTS("_get_online_clients"),
    /**
     * 合并转发
     */
    SEND_FORWARD_MSG("send_forward_msg"),
    /**
     * 群打卡
     */
    SEND_GROUP_SIGN("send_group_sign"),
    /**
     * 获取版本信息
     */
    GET_VERSION_INFO("get_version_info"),
    /**
     * 获取版本信息
     */
    GET_VERSION("get_version"),
    /**
     * 获取 Cookie
     */
    GET_COOKIES("get_cookies"),
    /**
     * 获取 CSRF Token
     */
    GET_CSRF_TOKEN("get_csrf_token"),
    /**
     * 获取 Cookie 和 CSRF Token
     */
    GET_CREDENTIALS("get_credentials"),
    /**
     * 删除群文件
     */
    DELETE_GROUP_FILE("delete_group_file"),
    /**
     * 删除群文件夹
     */
    DELETE_GROUP_FOLDER("delete_group_folder"),
    /**
     * 获取群文件下载链接
     */
    GET_GROUP_FILE_URL("get_group_file_url"),

    /**
     * 获取文件下载链接
     *
     * 属于 LLOnebot, NapCat 扩展 API
     */
    EXT_GET_FILE("get_file"),

    /**
     * 获取用户头像
     *
     * 属于 Gensokyo 扩展 API
     */
    EXT_GET_AVATAR("get_avatar"),

    /**
     * 好友戳一戳
     *
     * 属于 LLOnebot, NapCat 扩展 API
     */
    EXT_FRIEND_POKE("friend_poke"),

    /**
     * 群聊戳一戳
     *
     * 属于 LLOnebot, NapCat 扩展 API
     */
    EXT_GROUP_POKE("group_poke"),

    /**
     * 群聊消息小表情反应设置
     *
     * 属于 AstralGocq, Lagrange 扩展 API
     */
    EXT_SET_GROUP_REACTION("set_group_reaction"),

    /**
     * 设置表情回复
     *
     * 属于 LLOnebot, NapCat 扩展 API
     */
    EXT_SET_MSG_EMOJI_LIKE("set_msg_emoji_like"),

    /**
     * 创建群文件夹
     */
    CREATE_GROUP_FILE_FOLDER("create_group_file_folder"),
}
