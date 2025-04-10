package top.mrxiaom.overflow.internal.contact.data

import net.mamoe.mirai.contact.active.MemberActive
import net.mamoe.mirai.contact.active.MemberMedalInfo
import net.mamoe.mirai.contact.active.MemberMedalType
import net.mamoe.mirai.data.GroupHonorType
import top.mrxiaom.overflow.internal.contact.MemberWrapper

internal class MemberActiveWrapper(
    val member: MemberWrapper
) : MemberActive {
    internal var pointInternal = 0
    internal var rankInternal = 1
    internal val honorsInternal: HashSet<GroupHonorType> = hashSetOf()
    override val honors: Set<GroupHonorType>
        get() {
            member.group.active
            return honorsInternal
        }
    override val point: Int
        get() {
            member.group.active
            return pointInternal
        }
    override val rank: Int
        get() {
            member.group.active
            return rankInternal
        }
    override val temperature: Int
        get() = member.impl.levelInt
    override suspend fun queryMedal(): MemberMedalInfo {
        return member.group.active.queryMemberMedal(uid = member.id)
    }
}
internal object EmptyMemberActive : MemberActive {
    override val honors: Set<GroupHonorType> = setOf()
    override val point: Int = 0
    override val rank: Int = 1
    override val temperature: Int = 0
    override suspend fun queryMedal(): MemberMedalInfo = MemberMedalInfo("", "", MemberMedalType.ACTIVE, setOf())
}