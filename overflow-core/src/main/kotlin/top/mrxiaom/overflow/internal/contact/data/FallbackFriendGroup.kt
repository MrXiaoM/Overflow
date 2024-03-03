package top.mrxiaom.overflow.internal.contact.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.friendgroup.FriendGroup

class FallbackFriendGroup(
    val bot: Bot
): FriendGroup {
    override val count: Int
        get() = friends.size
    override val friends: Collection<Friend>
        get() = bot.friends
    override val id: Int = 0
    override val name: String = "我的好友"

    override suspend fun delete(): Boolean = throw IllegalStateException("Onebot 未提供好友分组接口")
    override suspend fun moveIn(friend: Friend): Boolean = throw IllegalStateException("Onebot 未提供好友分组接口")
    override suspend fun renameTo(newName: String): Boolean = throw IllegalStateException("Onebot 未提供好友分组接口")
}
