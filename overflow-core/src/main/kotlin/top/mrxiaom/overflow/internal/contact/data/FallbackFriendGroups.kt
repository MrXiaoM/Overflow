package top.mrxiaom.overflow.internal.contact.data

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.friendgroup.FriendGroup
import net.mamoe.mirai.contact.friendgroup.FriendGroups

class FallbackFriendGroups(
    val bot: Bot
) : FriendGroups {

    internal val fallbackFriendGroup = FallbackFriendGroup(bot)

    override fun asCollection(): Collection<FriendGroup> = listOf(fallbackFriendGroup)
    override suspend fun create(name: String): FriendGroup = fallbackFriendGroup
    override fun get(id: Int): FriendGroup? = fallbackFriendGroup
}