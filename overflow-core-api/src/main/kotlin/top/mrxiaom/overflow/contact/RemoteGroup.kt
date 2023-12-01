package top.mrxiaom.overflow.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.NormalMember

interface RemoteGroup {
    @JvmBlockingBridge
    suspend fun updateGroupMemberList(): ContactList<NormalMember>
}
