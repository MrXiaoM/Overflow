@file:OptIn(MiraiInternalApi::class)
package top.mrxiaom.overflow.internal.contact.data

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.contact.active.*
import net.mamoe.mirai.contact.checkBotPermission
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.check
import top.mrxiaom.overflow.internal.contact.GroupWrapper

/**
 * https://github.com/mamoe/mirai/blob/dev/mirai-core/src/commonMain/kotlin/contact/active/GroupActiveImpl.kt
 */
internal class GroupActiveWrapper(
    private val group: GroupWrapper
) : GroupActive {
    private var _isHonorVisible: Boolean = false
    private var _isTitleVisible: Boolean = false
    private var _isTemperatureVisible: Boolean = false
    private var _isRankVisible: Map<Int, String> = mapOf()
    private var _temperatureTitles: Map<Int, String> = mapOf()

    private suspend fun getGroupLevelInfo(): GroupLevelInfo {
        if (group.bot.noPlatform) return GroupLevelInfo(errCode = null)
        return group.bot.getRawGroupLevelInfo(groupCode = group.id).check()
    }

    private suspend fun refreshRank() {
        val info = getGroupLevelInfo()
        _isTitleVisible = info.levelFlag == 1
        _isTemperatureVisible = info.levelNewFlag == 1
        _isRankVisible = info.levelName.mapKeys { (level, _) -> level.removePrefix("lvln").toInt() }
        _temperatureTitles = info.levelNewName.mapKeys { (level, _) -> level.removePrefix("lvln").toInt() }
    }

    override val isHonorVisible: Boolean get() = _isHonorVisible

    override suspend fun setHonorVisible(newValue: Boolean) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        if (!group.bot.noPlatform) {
            group.bot.setGroupHonourFlag(groupCode = group.id, flag = newValue).check()
            _isHonorVisible = newValue
        }
    }

    override val rankTitles: Map<Int, String> get() = _isRankVisible

    override suspend fun setRankTitles(newValue: Map<Int, String>) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        if (!group.bot.noPlatform) {
            group.bot.setGroupLevelInfo(groupCode = group.id, new = false, titles = newValue).check()
            refreshRank()
        }
    }

    override val isTitleVisible: Boolean get() = _isTitleVisible

    override suspend fun setTitleVisible(newValue: Boolean) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        if (!group.bot.noPlatform) {
            group.bot.setGroupSetting(groupCode = group.id, new = false, show = newValue).check()
            refreshRank()
        }
    }

    override val temperatureTitles: Map<Int, String> get() = _temperatureTitles

    override suspend fun setTemperatureTitles(newValue: Map<Int, String>) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        if (!group.bot.noPlatform) {
            group.bot.setGroupLevelInfo(groupCode = group.id, new = true, titles = newValue).check()
            refreshRank()
        }
    }

    override val isTemperatureVisible: Boolean get() = _isTemperatureVisible

    override suspend fun setTemperatureVisible(newValue: Boolean) {
        group.checkBotPermission(MemberPermission.ADMINISTRATOR)
        if (!group.bot.noPlatform) {
            group.bot.setGroupSetting(groupCode = group.id, new = true, show = newValue).check()
            refreshRank()
        }
    }

    override suspend fun refresh() {
        refreshRank()
        if (!group.bot.noPlatform) return
        val info = group.bot.getRawMemberLevelInfo(groupCode = group.id).check()
        _isHonorVisible = info.honourFlag == 1
        _isTitleVisible = info.levelFlag == 1
        _isRankVisible = info.levelName.mapKeys { (level, _) -> level.removePrefix("lvln").toInt() }

        for (member in group.members) {
            val (_, _, point, rank) = info.lv[member.id] ?: continue

            member.active.pointInternal = point
            member.active.rankInternal = rank
        }
    }

    private suspend fun getGroupActiveData(page: Int?): GroupActiveData {
        if (group.bot.noPlatform) return GroupActiveData(
            errCode = 0,
            info = GroupActiveData.ActiveInfo(isEnd = 1)
        )
        return group.bot.getRawGroupActiveData(group.id, page).check()
    }

    override fun asFlow(): Flow<ActiveRecord> {
        return flow {
            var page = 0
            while (currentCoroutineContext().isActive) {
                val result = getGroupActiveData(page = page)
                val most = result.info.mostAct ?: break

                for (active in most) emit(active.toActiveRecord(group))

                if (result.info.isEnd == 1) break
                page++
            }
        }
    }

    override suspend fun queryChart(): ActiveChart {
        return getGroupActiveData(page = null).info.toActiveChart()
    }

    private suspend fun getHonorInfo(type: GroupHonorType): MemberHonorList {
        if (group.bot.noPlatform) return object : MemberHonorList {
            override val total: Int = 0
            override val list: List<MemberHonorInfo> = listOf()
        }
        return when (type) {
            GroupHonorType.TALKATIVE -> group.bot.getRawTalkativeInfo(group.id)
            GroupHonorType.PERFORMER -> group.bot.getRawContinuousInfo(group.id, type.id)
            GroupHonorType.LEGEND -> group.bot.getRawContinuousInfo(group.id, type.id)
            GroupHonorType.STRONG_NEWBIE -> group.bot.getRawContinuousInfo(group.id, type.id)
            GroupHonorType.EMOTION -> group.bot.getRawEmotionInfo(group.id)
            GroupHonorType.BRONZE -> group.bot.getRawHomeworkExcellentInfo(group.id, 1)
            GroupHonorType.SILVER -> group.bot.getRawHomeworkExcellentInfo(group.id, 2)
            GroupHonorType.GOLDEN -> group.bot.getRawHomeworkExcellentInfo(group.id, 3)
            GroupHonorType.WHIRLWIND -> group.bot.getRawHomeworkActiveInfo(group.id)
            GroupHonorType.RICHER -> group.bot.getRawRicherHonorInfo(group.id)
            GroupHonorType.RED_PACKET -> group.bot.getRawRedPacketInfo(group.id)
            else -> group.bot.getRawContinuousInfo(group.id, type.id)
        }
    }

    override suspend fun queryHonorHistory(type: GroupHonorType): ActiveHonorList {
        val data = getHonorInfo(type)

        when (type) {
            GroupHonorType.TALKATIVE, GroupHonorType.RICHER, GroupHonorType.RED_PACKET -> {
                val current = data.current?.uin
                for (member in group.members) {
                    if (member.id != current) {
                        member.active.honorsInternal += type
                    } else {
                        member.active.honorsInternal -= type
                    }
                }
                data.current?.let { group.members[it.uin] }?.let {
                    it.active.honorsInternal += type
                }
            }

            GroupHonorType.LEGEND -> {
                val current = data.list.mapTo(HashSet()) { it.uin }
                for (member in group.members) {
                    if (member.id in current) {
                        member.active.honorsInternal += GroupHonorType.LEGEND
                        member.active.honorsInternal -= GroupHonorType.PERFORMER
                    } else {
                        member.active.honorsInternal -= GroupHonorType.LEGEND
                    }
                }
            }

            else -> {
                val current = data.list.mapTo(HashSet()) { it.uin }
                for (member in group.members) {
                    if (member.id in current) {
                        member.active.honorsInternal += type
                    } else {
                        member.active.honorsInternal -= type
                    }
                }
            }
        }

        @Suppress("INVISIBLE_MEMBER")
        return ActiveHonorList(
            type = type,
            current = data.current?.toActiveHonorInfo(group),
            records = data.list.map { it.toActiveHonorInfo(group) },
        )
    }

    private suspend fun getMemberScoreData(): MemberScoreData {
        if (group.bot.noPlatform) return MemberScoreData(
            levels = listOf(),
            mapping = listOf(),
            self = MemberScoreData.MemberScoreInfo(
                levelId = 0,
                nickName = group.bot.nick,
                role = 0,
                score = 0,
                uin = group.bot.id
            ),
            members = listOf(),
            errorMessage = "Overflow 开启了 no_platform 模式",
            errorCode = 0
        )
        return group.bot.getRawMemberTitleList(group.id).check()
    }

    override suspend fun queryActiveRank(): List<ActiveRankRecord> {
        val data = getMemberScoreData()

        @Suppress("INVISIBLE_MEMBER")
        return data.members.map {
            ActiveRankRecord(
                memberId = it.uin,
                memberName = it.nickName,
                member = group.get(id = it.uin),
                temperature = it.levelId,
                score = it.score
            )
        }
    }

    private suspend fun getMemberMedalInfo(uid: Long): MemberMedalData {
        if (group.bot.noPlatform) {
            val member = group.queryMember(uid)
            return MemberMedalData(
                avatar = "http://q.qlogo.cn/g?b=qq&nk=${uid}&s=640",
                faceFlag = 0,
                lastViewTs = 0,
                list = listOf(),
                nick = member?.nick ?: uid.toString(),
                role = 0,
                weared = "",
                wearedColor = ""
            )
        }
        return group.bot.getRawMemberMedalInfo(group.id, uid)
    }

    suspend fun queryMemberMedal(uid: Long): MemberMedalInfo {
        val info = getMemberMedalInfo(uid = uid)
        val medals: MutableSet<MemberMedalType> = HashSet()
        var worn: MemberMedalType = MemberMedalType.ACTIVE

        for (item in info.list) {
            if (item.achieveTs == 0) continue
            val type = when (item.mask) {
                MemberMedalType.OWNER.mask -> MemberMedalType.OWNER
                MemberMedalType.ADMIN.mask -> MemberMedalType.ADMIN
                MemberMedalType.SPECIAL.mask -> MemberMedalType.SPECIAL
                MemberMedalType.ACTIVE.mask -> MemberMedalType.ACTIVE
                else -> continue
            }
            medals.add(type)
            if (item.wearTs != 0) worn = type
        }

        @Suppress("INVISIBLE_MEMBER")
        return MemberMedalInfo(
            title = info.weared,
            color = info.wearedColor,
            wearing = worn,
            medals = medals
        )
    }
}