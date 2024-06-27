package top.mrxiaom.overflow.internal.contact.data

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.GroupSettings
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.event.events.GroupAllowMemberInviteEvent
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import top.mrxiaom.overflow.internal.contact.GroupWrapper

/**
 * GroupSettingsWrapper类用于封装群设置，继承自GroupSettings接口。
 * 该类提供了对群公告、成员邀请、匿名聊天、自动批准和全员禁言等设置的访问和修改。
 *
 * @param group 一个GroupWrapper对象，封装了与群相关的信息和操作。
 */
internal class GroupSettingsWrapper(
    val group: GroupWrapper
) : GroupSettings {
    /**
     * 入群公告的属性，已废弃。使用asFlow().filter { it.parameters.sendToNewMember }.firstOrNull()替代。
     * 获取当前群的入群公告。
     * 设置入群公告的操作已被禁用。
     */
    @Deprecated(
        "group.announcements.asFlow().filter { it.parameters.sendToNewMember }.firstOrNull()",
        level = DeprecationLevel.HIDDEN
    )
    override var entranceAnnouncement: String
        get() = group.impl.groupMemo
        set(newValue) = runBlocking{
            group.bot.impl.setGroupEntranceAnnouncement(group.id, newValue)
        }

    private var isAllowMemberInviteField: Boolean = false


    /**
     * 是否允许群成员邀请其他人加入群聊的属性。
     *
     * 通过此属性可以获取和设置群聊是否允许成员邀请他人加入。
     * 设置新值时，会通过内部API改变群的邀请策略，并可能触发相应的事件。
     *
     * @return 当前群聊的成员邀请策略，true表示允许，false表示不允许。
     */
    @OptIn(MiraiInternalApi::class)
    override var isAllowMemberInvite: Boolean
        get() = isAllowMemberInviteField
        set(value) = runBlocking{
            // 使用内部API设置群的成员邀请权限
            group.bot.impl.setGroupMemberInvite(group.id, value)

            // 根据旧值和新值触发群允许成员邀请事件
            // 是否需要触发此事件？
            GroupAllowMemberInviteEvent(
                    isAllowMemberInviteField,
                    value,
                    group,
                    null)
        }


    private var isAnonymousChatEnabledField: Boolean = false

    /**
     * 是否启用匿名聊天的属性。
     *
     * 此属性用于控制群组中是否允许匿名聊天。通过设置此属性的值，可以动态启用或禁用匿名聊天功能。
     * 注意，修改此属性的值会触发后台异步操作，以更新群组的匿名聊天设置。
     */
    override var isAnonymousChatEnabled: Boolean
        get() = isAnonymousChatEnabledField
        set(value) = runBlocking {
            // group.checkBotPermission(MemberPermission.ADMINISTRATOR)
            group.bot.impl.setGroupAnonymous(group.id, value)
        }


    private var isAutoApproveEnabledField: Boolean = false

    /**
     * 是否启用自动批准功能的属性。
     *
     * 此属性用于指示系统是否应自动批准某些操作而无需进一步的人工干预。
     * 读取此属性始终返回 [isAutoApproveEnabledField] 的值。
     * 尝试设置此属性的值将抛出 [UnsupportedOperationException]，因为此操作不被支持。
     *
     * @note 此属性的修改操作被禁用，尝试修改将抛出异常。
     * @return 当前自动批准功能的启用状态。
     */
    @MiraiExperimentalApi
    override var isAutoApproveEnabled: Boolean
        get() = isAutoApproveEnabledField
        @Suppress("UNUSED_PARAMETER")
        set(value) {
            throw UnsupportedOperationException("Modification of auto approve status is not supported.")
        }



    internal var muteAll: Boolean = false

    /**
     * 是否全局禁言的
     *
     * 该属性用于控制群组中的全局静音状态。当设置为true时，群组内所有成员将被静音；
     * 当设置为false时，取消全局静音，成员可以正常发言。
     *
     * 注意：设置该属性需要具有管理员权限。
     */
    override var isMuteAll: Boolean
        get() = muteAll
        set(value) = runBlocking {
            // 检查机器人是否具有管理员权限，以确保有权执行静音操作。
            group.checkBotPermission(MemberPermission.ADMINISTRATOR)
            // 更新静音状态。
            muteAll = value
            // 通过机器人实现设置群组的全局静音状态。
            group.bot.impl.setGroupWholeBan(group.id, value)
        }

}
