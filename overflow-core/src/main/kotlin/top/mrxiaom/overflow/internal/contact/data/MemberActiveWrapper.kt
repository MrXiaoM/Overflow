package top.mrxiaom.overflow.internal.contact.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import net.mamoe.mirai.contact.active.MemberActive
import net.mamoe.mirai.contact.active.MemberMedalInfo
import net.mamoe.mirai.contact.active.MemberMedalType
import net.mamoe.mirai.data.GroupHonorType
import net.mamoe.mirai.utils.CheckableResponseA
import net.mamoe.mirai.utils.JsonStruct
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.loadAs
import top.mrxiaom.overflow.internal.contact.MemberWrapper
import top.mrxiaom.overflow.internal.utils.httpGet

@OptIn(MiraiInternalApi::class)
internal class MemberActiveWrapper(
    val member: MemberWrapper
) : MemberActive {
    val honorsInternal: HashSet<GroupHonorType> = hashSetOf()
    override val honors: Set<GroupHonorType>
        get() = honorsInternal.apply {
            clear()
            member.groupWrapper.honors.run {
                if (talkativeList.any { it.userId == member.id }) {
                    add(GroupHonorType.TALKATIVE)
                }
                if (performerList.any { it.userId == member.id }) {
                    add(GroupHonorType.PERFORMER)
                }
                if (legendList.any { it.userId == member.id }) {
                    add(GroupHonorType.LEGEND)
                }
                if (strongNewbieList.any { it.userId == member.id }) {
                    add(GroupHonorType.STRONG_NEWBIE)
                }
                if (emotionList.any { it.userId == member.id }) {
                    add(GroupHonorType.EMOTION)
                }
            }
        }
    override val point: Int
        get() = 0 // TODO: Not yet implemented
    override val rank: Int
        get() = 1 // TODO: Not yet implemented
    override val temperature: Int
        get() = member.impl.level

    /**
     * https://github.com/mamoe/mirai/blob/dev/mirai-core/src/commonMain/kotlin/contact/active/GroupActiveImpl.kt#L218-L243
     */
    override suspend fun queryMedal(): MemberMedalInfo {
        val info = member.botWrapper.httpGet(
            url = "https://qun.qq.com/cgi-bin/qunwelcome/medal2/list",
            cookieDomain = "qun.qq.com",
            params = mapOf(
                "gc" to member.group.id,
                "uin" to member.id
            )
        ).loadAs(CgiData.serializer()).loadData(MemberMedalData.serializer())
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

@Serializable
internal data class CgiData(
    @SerialName("cgicode") val cgicode: Int,
    @SerialName("data") val `data`: JsonElement,
    @SerialName("msg") override val errorMessage: String,
    @SerialName("retcode") override val errorCode: Int
) : CheckableResponseA(), JsonStruct
@PublishedApi
internal val defaultJson: Json = Json {
    isLenient = true
    ignoreUnknownKeys = true
}
private fun <T> CgiData.loadData(serializer: KSerializer<T>): T =
    defaultJson.decodeFromJsonElement(serializer, this.data)

@Serializable
internal data class MemberMedalData(
    @SerialName("avatar") val avatar: String,
    @SerialName("face_flag") val faceFlag: Int,
    @SerialName("last_view_ts") val lastViewTs: Int,
    @SerialName("list") val list: List<MemberMedalItem>, // 头衔详情
    @SerialName("nick") val nick: String,
    @SerialName("role") val role: Int, // 身份/权限
    @SerialName("weared") val weared: String, // 目前显示头衔
    @SerialName("weared_color") val wearedColor: String // 头衔颜色
)

@Serializable
internal data class MemberMedalItem(
    @SerialName("achieve_ts") val achieveTs: Int, // 是否拥有
    @SerialName("category_id") val categoryId: Int,
    @SerialName("color") val color: String,
    @SerialName("is_mystery") val isMystery: Int,
    @SerialName("mask") val mask: Int, //  群主 300 管理员 301 特殊 302  活跃 315
    @SerialName("medal_desc") val medalDesc: String,
    @SerialName("name") val name: String,
    @SerialName("order") val order: Int,
    @SerialName("pic") val pic: String,
    @SerialName("rule") val rule: Int,
    @SerialName("rule_desc") val ruleDesc: String, // 来源
    @SerialName("wear_ts") val wearTs: Int // 是否佩戴
)
