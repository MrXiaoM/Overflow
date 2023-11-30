package top.mrxiaom.overflow.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*

interface OverflowAPI {
    fun imageFromFile(file: String): Image
    fun audioFromFile(file: String): Audio
    fun videoFromFile(file: String): ShortVideo
    fun serializeMessage(message: Message): String
    @JvmBlockingBridge
    suspend fun deserializeMessage(bot: Bot, message: String): MessageChain
    @JvmBlockingBridge
    suspend fun updateGroupMemberList(group: Group): ContactList<NormalMember>

    companion object {
        @JvmStatic
        fun get(): OverflowAPI = Mirai as OverflowAPI
    }
}