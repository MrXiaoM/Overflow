package top.mrxiaom.overflow.internal.contact.data

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.GroupSettings
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.checkBotPermission
import top.mrxiaom.overflow.internal.contact.GroupWrapper

internal class GroupSettingsWrapper(
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
            group.bot.impl.setGroupAnonymous(group.id, value)
        }

    override val isAutoApproveEnabled: Boolean
        get() = false // TODO: Not yet implemented

    internal var muteAll: Boolean
        get() = group.impl.groupAllShut == -1
        set(value) {
            group.impl.groupAllShut = if (value) -1 else 0
        }
    override var isMuteAll: Boolean
        get() = muteAll
        set(value) = runBlocking {
            group.checkBotPermission(MemberPermission.ADMINISTRATOR)
            muteAll = value
            group.bot.impl.setGroupWholeBan(group.id, value)
        }
}
