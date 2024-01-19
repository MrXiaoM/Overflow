package top.mrxiaom.overflow.internal.data

import net.mamoe.mirai.data.UserProfile

internal class UserProfileImpl(
    override val age: Int,
    override val email: String,
    override val friendGroupId: Int,
    override val nickname: String,
    override val qLevel: Int,
    override val sex: UserProfile.Sex,
    override val sign: String
) : UserProfile
