package top.mrxiaom.overflow.internal.contact.data

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.GroupSettings
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.utils.MiraiExperimentalApi
import top.mrxiaom.overflow.internal.contact.GroupWrapper

class GroupSettingsWrapper(
    val group: GroupWrapper
) : GroupSettings {
    @Deprecated(
        "group.announcements.asFlow().filter { it.parameters.sendToNewMember }.firstOrNull()",
        level = DeprecationLevel.HIDDEN
    )
    override var entranceAnnouncement: String
        get() = group.impl.groupMemo
        set(_) {}

    override var isAllowMemberInvite: Boolean
        get() = false // TODO: Not yet implemented
        set(_) {}

    override var isAnonymousChatEnabled: Boolean
        get() = false // TODO: Not yet implemented
        set(value) = runBlocking {
            group.botWrapper.impl.setGroupAnonymous(group.id, value)
        }

    @MiraiExperimentalApi
    override val isAutoApproveEnabled: Boolean
        get() = false // TODO: Not yet implemented

    internal var muteAll: Boolean = false
    override var isMuteAll: Boolean
        get() = muteAll
        set(value) = runBlocking {
            group.checkBotPermission(MemberPermission.ADMINISTRATOR)
            muteAll = value
            group.botWrapper.impl.setGroupWholeBan(group.id, value)
        }
}
