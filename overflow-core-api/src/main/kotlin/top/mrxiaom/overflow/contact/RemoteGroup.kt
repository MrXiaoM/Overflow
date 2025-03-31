package top.mrxiaom.overflow.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.announcement.Announcements

interface RemoteGroup {
    @JvmBlockingBridge
    suspend fun updateGroupMemberList(): ContactList<NormalMember>
    @JvmBlockingBridge
    suspend fun updateAnnouncements(): Announcements

    companion object {
        /**
         * 尝试将 mirai Group 转换为 Overflow RemoteGroup
         *
         * 当类型不正确时，或没有 Overflow 实现时，将会抛出异常
         */
        @JvmStatic
        @get:Throws(ClassNotFoundException::class, ClassCastException::class)
        val Group.asRemoteGroup
            get() = this as RemoteGroup
    }
}
