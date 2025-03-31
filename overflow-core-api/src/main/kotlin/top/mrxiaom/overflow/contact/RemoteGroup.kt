package top.mrxiaom.overflow.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.NormalMember
import net.mamoe.mirai.contact.announcement.Announcements
import kotlin.jvm.Throws

interface RemoteGroup {
    @JvmBlockingBridge
    suspend fun updateGroupMemberList(): ContactList<NormalMember>
    @JvmBlockingBridge
    suspend fun updateAnnouncements(): Announcements

    /**
     * 设置群消息反应表情
     *
     * @param messageId 消息ID，可通过 `MessageSource.ids[0]` 取得
     * @param icon 图标ID，详见 Overflow 开发文档
     * @param enable 添加或取消反应表情
     */
    @JvmBlockingBridge
    suspend fun setMsgReaction(messageId: Int, icon: String, enable: Boolean)

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
