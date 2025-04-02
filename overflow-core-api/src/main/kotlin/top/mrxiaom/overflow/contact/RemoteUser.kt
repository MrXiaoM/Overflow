package top.mrxiaom.overflow.contact

import net.mamoe.mirai.contact.User

interface RemoteUser {
    val onebotData: String

    companion object {
        /**
         * 尝试将 mirai User 转换为 Overflow RemoteUser
         *
         * 当类型不正确时，或没有 Overflow 实现时，将会抛出异常
         */
        @JvmStatic
        @get:Throws(ClassNotFoundException::class, ClassCastException::class)
        val User.asRemoteUser
            get() = this as RemoteUser
    }
}
