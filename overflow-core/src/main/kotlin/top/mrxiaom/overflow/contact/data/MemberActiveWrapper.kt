package top.mrxiaom.overflow.contact.data

import net.mamoe.mirai.contact.active.MemberActive
import net.mamoe.mirai.contact.active.MemberMedalInfo
import net.mamoe.mirai.data.GroupHonorType

class MemberActiveWrapper : MemberActive {
    override val honors: Set<GroupHonorType>
        get() = TODO("Not yet implemented")
    override val point: Int
        get() = TODO("Not yet implemented")
    override val rank: Int
        get() = TODO("Not yet implemented")
    override val temperature: Int
        get() = TODO("Not yet implemented")

    override suspend fun queryMedal(): MemberMedalInfo {
        TODO("Not yet implemented")
    }
}