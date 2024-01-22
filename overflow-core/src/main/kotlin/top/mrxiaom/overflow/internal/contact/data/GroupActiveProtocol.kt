/**
 * https://github.com/mamoe/mirai/blob/dev/mirai-core/src/commonMain/kotlin/contact/active/GroupActiveProtocol.kt
 */
package top.mrxiaom.overflow.internal.contact.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.active.ActiveChart
import net.mamoe.mirai.contact.active.ActiveHonorInfo
import net.mamoe.mirai.contact.active.ActiveRecord
import net.mamoe.mirai.utils.CheckableResponseA
import net.mamoe.mirai.utils.JsonStruct
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.loadAs
import top.mrxiaom.overflow.internal.contact.BotWrapper
import top.mrxiaom.overflow.internal.utils.httpGet

@Serializable
internal data class SetResult(
    @SerialName("ec") override val errorCode: Int = 0,
    @SerialName("em") override val errorMessage: String? = null,
    @SerialName("errcode") val errCode: Int?
) : CheckableResponseA(), JsonStruct

/**
 * 群等级信息
 */
@Serializable
internal data class GroupLevelInfo(
    @SerialName("ec") override val errorCode: Int = 0,
    @SerialName("em") override val errorMessage: String? = null,
    @SerialName("errcode") val errCode: Int?,
    @SerialName("levelflag") val levelFlag: Int = 0,
    @SerialName("levelname") val levelName: Map<String, String> = emptyMap(),
    @SerialName("levelnewflag") val levelNewFlag: Int = 0,
    @SerialName("levelnewname") val levelNewName: Map<String, String> = emptyMap()
) : CheckableResponseA(), JsonStruct

@Serializable
internal data class MemberLevelInfo(
    @SerialName("ec") override val errorCode: Int = 0,
    @SerialName("em") override val errorMessage: String? = null,
    @SerialName("errcode") val errCode: Int?,
    @SerialName("role") val role: Int = 0,
    @SerialName("mems") val mems: Map<Long, MemberInfo> = emptyMap(),
    @SerialName("lv") val lv: Map<Long, LevelInfo> = emptyMap(),
    @SerialName("levelflag") val levelFlag: Int = 0,
    @SerialName("levelname") val levelName: Map<String, String> = emptyMap(),
    @SerialName("honourflag") val honourFlag: Int = 0
) : CheckableResponseA(), JsonStruct {

    @Serializable
    data class MemberInfo(
        @SerialName("u") val u: Long = 0, @SerialName("g") val g: Int = 0, @SerialName("n") val n: String = ""
    )

    @Serializable
    data class LevelInfo(
        @SerialName("u") val u: Long = 0,
        @SerialName("d") val d: Int = 0,
        @SerialName("p") val p: Int = 0,
        @SerialName("l") val l: Int = 1
    )
}

/**
 * 群统计信息
 */
@Serializable
internal data class GroupActiveData(
    @SerialName("ec") override val errorCode: Int = 0,
    @SerialName("em") override val errorMessage: String? = null,
    @SerialName("errcode") val errCode: Int?,
    @SerialName("ginfo") val info: ActiveInfo,
    @SerialName("query") val query: Int? = 0,
    @SerialName("role") val role: Int? = 0
) : CheckableResponseA(), JsonStruct {

    @Serializable
    data class Situation(
        @SerialName("date") val date: String, @SerialName("num") val num: Int
    )

    @Serializable
    data class MostActive(
        @SerialName("name") val name: String,  // 名称 不完整
        @SerialName("sentences_num") val sentencesNum: Int,   // 发言数
        @SerialName("sta") val sta: Int = 0, @SerialName("uin") val uin: Long = 0
    )

    @Serializable
    data class ActiveInfo(
        @SerialName("g_act_num") val actNum: List<Situation>? = null,    //发言人数列表
        @SerialName("g_createtime") val createTime: Int? = 0,
        @SerialName("g_exit_num") val exitNum: List<Situation>? = null,  //退群人数列表
        @SerialName("g_join_num") val joinNum: List<Situation>? = null,
        @SerialName("g_mem_num") val memNum: List<Situation>? = null,   //人数变化
        @SerialName("g_most_act") val mostAct: List<MostActive>? = null,  //发言排行
        @SerialName("g_sentences") val sentences: List<Situation>? = null,
        @SerialName("gc") val gc: Int? = null,
        @SerialName("gn") val gn: String? = null,
        @SerialName("gowner") val owner: String? = null,
        @SerialName("isEnd") val isEnd: Int
    )
}

@Serializable
internal data class CgiData(
    @SerialName("cgicode") val cgicode: Int,
    @SerialName("data") val `data`: JsonElement,
    @SerialName("msg") override val errorMessage: String,
    @SerialName("retcode") override val errorCode: Int
) : CheckableResponseA(), JsonStruct

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

@Serializable
internal data class MemberHonorInfo(
    @SerialName("add_friend") val addFriend: Int = 0,
    @SerialName("avatar") val avatar: String,
    @SerialName("avatar_size") val avatarSize: Int,
    @SerialName("day_count") val dayCount: Int,
    @SerialName("day_count_history") val dayCountHistory: Int = 1,
    @SerialName("day_count_max") val dayCountMax: Int = 1,
    @SerialName("honor_ids") val honorIds: List<Int> = emptyList(),
    @SerialName("nick") val nick: String,
    @SerialName("uin") val uin: Long,
    @SerialName("update_ymd") val updated: Long = 0, // 格式为 yyyyMMdd 的 数字，表示最后更新时间
)

internal interface MemberHonorList : JsonStruct {
    val current: MemberHonorInfo? get() = null
    val total: Int
    val list: List<MemberHonorInfo>
}

@Serializable
internal data class MemberTalkativeInfo(
    @SerialName("current_talkative") val currentTalkative: MemberHonorInfo? = null,
    @SerialName("talkative_amount") val talkativeAmount: Int,
    @SerialName("talkative_list") val talkativeList: List<MemberHonorInfo>
) : MemberHonorList {
    override val current: MemberHonorInfo? get() = currentTalkative
    override val total: Int get() = talkativeAmount
    override val list: List<MemberHonorInfo> get() = talkativeList
}

@Serializable
internal data class MemberEmotionInfo(
    @SerialName("emotion_list") val emotionList: List<MemberHonorInfo>, @SerialName("total") override val total: Int
) : MemberHonorList {
    override val list: List<MemberHonorInfo> get() = emotionList
}

@Serializable
internal data class MemberHomeworkExcellentInfo(
    @SerialName("hwexcellent_list") val excellentList: List<MemberHonorInfo>,
    @SerialName("total") override val total: Int
) : MemberHonorList {
    override val list: List<MemberHonorInfo> get() = excellentList
}

@Serializable
internal data class MemberHomeworkActiveInfo(
    @SerialName("hwactive_list") val activeList: List<MemberHonorInfo>, @SerialName("total") override val total: Int
) : MemberHonorList {
    override val list: List<MemberHonorInfo> get() = activeList
}

@Serializable
internal data class MemberContinuousInfo(
    @SerialName("continuous_list") val continuousList: List<MemberHonorInfo>,
    @SerialName("total") override val total: Int
) : MemberHonorList {
    override val list: List<MemberHonorInfo> get() = continuousList
}

@Serializable
internal data class MemberRicherHonorInfo(
    @SerialName("current_richer_honor") val currentRicherHonor: MemberHonorInfo? = null,
    @SerialName("richer_amount") val richerAmount: Int,
    @SerialName("richer_honor_list") val richerHonorList: List<MemberHonorInfo>
) : MemberHonorList {
    override val current: MemberHonorInfo? get() = currentRicherHonor
    override val total: Int get() = richerAmount
    override val list: List<MemberHonorInfo> get() = richerHonorList
}

@Serializable
internal data class MemberRedPacketInfo(
    @SerialName("current_redpacket_honor") val currentRedPacketHonor: MemberHonorInfo? = null,
    @SerialName("redpacket_amount") val redPacketAmount: Int,
    @SerialName("redpacket_honor_list") val redPacketHonorList: List<MemberHonorInfo>
) : MemberHonorList {
    override val current: MemberHonorInfo? get() = currentRedPacketHonor
    override val total: Int get() = redPacketAmount
    override val list: List<MemberHonorInfo> get() = redPacketHonorList
}

@Serializable
internal data class MemberScoreData(
    @SerialName("level_list") val levels: List<Level>,
    @SerialName("member_level_list") val mapping: List<MemberLevel>,
    @SerialName("member_title_info") val self: MemberScoreInfo,
    @SerialName("members_list") val members: List<MemberScoreInfo>,
    @SerialName("msg") override val errorMessage: String,
    @SerialName("retcode") override val errorCode: Int
) : CheckableResponseA(), JsonStruct {
    @Serializable
    data class Level(
        @SerialName("level") val level: String, @SerialName("name") val name: String
    )

    @Serializable
    data class MemberLevel(
        @SerialName("level") val level: Int,
        @SerialName("lower_limit") val lowerLimit: Int,
        @SerialName("mapping_level") val mappingLevel: Int,
        @SerialName("name") val name: String
    )

    @Serializable
    data class MemberScoreInfo(
        @SerialName("level_id") val levelId: Int,
        @SerialName("nf") val nf: Int = 0,
        @SerialName("nick_name") val nickName: String,
        @SerialName("role") val role: Int,
        @SerialName("score") val score: Int,
        @SerialName("uin") val uin: Long
    )
}


internal suspend fun BotWrapper.getRawGroupLevelInfo(
    groupCode: Long
): GroupLevelInfo {
    return httpGet(
        url = "https://qinfo.clt.qq.com/cgi-bin/qun_info/get_group_level_new_info",
        cookieDomain = "qinfo.clt.qq.com",
        params = mapOf(
            "gc" to groupCode,
            "bkn" to "",
            "src" to "qinfo_v3"
        )
    ).loadAs(GroupLevelInfo.serializer())
}

internal suspend fun BotWrapper.getRawMemberLevelInfo(
    groupCode: Long
): MemberLevelInfo {
    return httpGet(
        url = "https://qinfo.clt.qq.com/cgi-bin/qun_info/get_group_members_lite",
        cookieDomain = "qinfo.clt.qq.com",
        params = mapOf(
            "gc" to groupCode,
            "bkn" to "",
            "src" to "qinfo_v3"
        )
    ).loadAs(MemberLevelInfo.serializer())
}

internal suspend fun BotWrapper.getRawMemberMedalInfo(
    groupCode: Long, uid: Long
): MemberMedalData {
    return httpGet(
        url = "https://qun.qq.com/cgi-bin/qunwelcome/medal2/list",
        cookieDomain = "qun.qq.com",
        params = mapOf(
            "gc" to groupCode,
            "uin" to uid
        )
    ).loadAs(CgiData.serializer())
        .loadData(MemberMedalData.serializer())
}

internal suspend fun BotWrapper.getRawTalkativeInfo(
    groupCode: Long
): MemberTalkativeInfo {
    return httpGet(
        url = "https://qun.qq.com/cgi-bin/qunapp/honor_talkative",
        cookieDomain = "qun.qq.com",
        params = mapOf(
            "gc" to groupCode,
            "num" to 3000
        )
    ).loadAs(CgiData.serializer())
        .loadData(MemberTalkativeInfo.serializer())
}

internal suspend fun BotWrapper.getRawEmotionInfo(
    groupCode: Long
): MemberEmotionInfo {
    return httpGet(
        url = "https://qun.qq.com/cgi-bin/qunapp/honor_emotion",
        cookieDomain = "qun.qq.com",
        params = mapOf(
            "gc" to groupCode,
            "num" to 3000
        )
    ).loadAs(CgiData.serializer())
        .loadData(MemberEmotionInfo.serializer())
}

@PublishedApi
internal val defaultJson: Json = Json {
    isLenient = true
    ignoreUnknownKeys = true
}

internal fun <T> CgiData.loadData(serializer: KSerializer<T>): T =
    defaultJson.decodeFromJsonElement(serializer, this.data)

/**
 * @param type 取值 1 2 3 分别对应 学术新星 顶尖学霸 至尊学神
 */
internal suspend fun BotWrapper.getRawHomeworkExcellentInfo(
    groupCode: Long, type: Int
): MemberHomeworkExcellentInfo {
    return httpGet(
        url = "https://qun.qq.com/cgi-bin/qunapp/honor_hwexcellent",
        cookieDomain = "qun.qq.com",
        params = mapOf(
            "gc" to groupCode,
            "req_type" to type,
            "num" to 3000
        )
    ).loadAs(CgiData.serializer())
        .loadData(MemberHomeworkExcellentInfo.serializer())
}

internal suspend fun BotWrapper.getRawHomeworkActiveInfo(
    groupCode: Long
): MemberHomeworkActiveInfo {
    return httpGet(
        url = "https://qun.qq.com/cgi-bin/qunapp/honor_hwactive",
        cookieDomain = "qun.qq.com",
        params = mapOf(
            "gc" to groupCode,
            "num" to 3000
        )
    ).loadAs(CgiData.serializer())
        .loadData(MemberHomeworkActiveInfo.serializer())
}

/**
 * @param type 取值 2 3 5 分别对应 群聊之火 群聊炽焰 冒尖小春笋
 */
internal suspend fun BotWrapper.getRawContinuousInfo(
    groupCode: Long, type: Int
): MemberContinuousInfo {
    return httpGet(
        url = "https://qun.qq.com/cgi-bin/qunapp/honor_continuous",
        cookieDomain = "qun.qq.com",
        params = mapOf(
            "gc" to groupCode,
            "num" to 3000,
            "continuous_type" to type
        )
    ).loadAs(CgiData.serializer())
        .loadData(MemberContinuousInfo.serializer())
}

internal suspend fun BotWrapper.getRawRicherHonorInfo(
    groupCode: Long
): MemberRicherHonorInfo {
    return httpGet(
        url = "https://qun.qq.com/cgi-bin/new_honor/list_honor/list_richer_honor",
        cookieDomain = "qun.qq.com",
        params = mapOf(
            "group_code" to groupCode,
            "num" to 3000
        )
    ).loadAs(CgiData.serializer())
        .loadData(MemberRicherHonorInfo.serializer())
}

internal suspend fun BotWrapper.getRawRedPacketInfo(
    groupCode: Long
): MemberRedPacketInfo {
    return httpGet(
        url = "https://qun.qq.com/cgi-bin/new_honor/list_honor/list_redpacket_honor",
        cookieDomain = "qun.qq.com",
        params = mapOf(
            "group_code" to groupCode,
            "num" to 3000
        )
    ).loadAs(CgiData.serializer())
        .loadData(MemberRedPacketInfo.serializer())
}

/**
 * 只有前 50 名的数据
 */
internal suspend fun BotWrapper.getRawMemberTitleList(
    groupCode: Long
): MemberScoreData {
    return httpGet(
        url = "https://qun.qq.com/cgi-bin/honorv2/honor_title_list",
        cookieDomain = "qun.qq.com",
        params = mapOf(
            "group_code" to groupCode,
            "request_type" to 2,
        ),
        bknKey = "g_tk"
    ).loadAs(MemberScoreData.serializer())
}

internal suspend fun BotWrapper.setGroupLevelInfo(
    groupCode: Long, new: Boolean, titles: Map<Int, String>
): SetResult {
    val params: MutableMap<String, Any> = titles.map { (index, name) ->
        "lvln$index" to name
    }.toMap().toMutableMap()
    params.putAll(
        mapOf(
            "new" to if (new) 1 else 0,
            "gc" to groupCode,
            "src" to "qinfo_v3"
        )
    )
    return httpGet(
        url = "https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_level_info",
        cookieDomain = "qinfo.clt.qq.com",
        params = params

    ).loadAs(SetResult.serializer())
}

internal suspend fun BotWrapper.setGroupSetting(
    groupCode: Long, new: Boolean, show: Boolean
): SetResult {
    val flag = if (new) "levelnewflag" else "levelflag"
    return httpGet(
        url = "https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_setting",
        cookieDomain = "qinfo.clt.qq.com",
        params = mapOf(
            flag to if (show) 1 else 0,
            "gc" to groupCode,
            "src" to "qinfo_v3"
        )
    ).loadAs(SetResult.serializer())
}

internal suspend fun BotWrapper.setGroupHonourFlag(
    groupCode: Long, flag: Boolean
): SetResult {
    return httpGet(
        url = "https://qinfo.clt.qq.com/cgi-bin/qun_info/set_honour_flag",
        cookieDomain = "qinfo.clt.qq.com",
        params = mapOf(
            "gc" to groupCode,
            "bkn" to "",
            "src" to "qinfo_v3",
            "flag" to if (flag) 0 else 1
        )
    ).loadAs(SetResult.serializer())
}

internal suspend fun BotWrapper.getRawGroupActiveData(
    groupCode: Long, page: Int? = null
): GroupActiveData {
    return httpGet(
        url = "https://qqweb.qq.com/c/activedata/get_mygroup_data",
        cookieDomain = "qqweb.qq.com",
        params = mapOf(
            "bkn" to "",
            "gc" to groupCode,
            "page" to page
        )
    ).loadAs(GroupActiveData.serializer())
}

@Suppress("INVISIBLE_MEMBER")
internal fun GroupActiveData.MostActive.toActiveRecord(group: Group): ActiveRecord {
    return ActiveRecord(
        memberId = uin, memberName = name, periodDays = sentencesNum, messagesCount = sta, member = group.get(id = uin)
    )
}

@OptIn(MiraiInternalApi::class)
@Suppress("INVISIBLE_MEMBER")
internal fun GroupActiveData.ActiveInfo.toActiveChart(): ActiveChart {
    return ActiveChart(
        actives = actNum?.associate { it.date to it.num }.orEmpty(),
        sentences = sentences?.associate { it.date to it.num }.orEmpty(),
        members = memNum?.associate { it.date to it.num }.orEmpty(),
        join = joinNum?.associate { it.date to it.num }.orEmpty(),
        exit = exitNum?.associate { it.date to it.num }.orEmpty()
    )
}

@Suppress("INVISIBLE_MEMBER")
internal fun MemberHonorInfo.toActiveHonorInfo(group: Group): ActiveHonorInfo {
    return ActiveHonorInfo(
        memberName = nick,
        memberId = uin,
        avatar = avatar + avatarSize,
        member = group.get(id = uin),
        termDays = dayCount,
        historyDays = dayCountHistory,
        maxTermDays = dayCountMax
    )
}