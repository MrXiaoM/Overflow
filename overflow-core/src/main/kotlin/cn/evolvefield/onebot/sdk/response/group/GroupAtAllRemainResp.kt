package cn.evolvefield.onebot.sdk.response.group

import com.google.gson.annotations.SerializedName

class GroupAtAllRemainResp {
    /**
     * 是否可以 @全体成员
     */
    @SerializedName("can_at_all")
    var canAtAll = false
    /**
     * 群内所有管理当天剩余 @全体成员 次数
     */
    @SerializedName("remain_at_all_count_for_group")
    var remainAtAllCountForGroup = 0
    /**
     * Bot 当天剩余 @全体成员 次数
     */
    @SerializedName("remain_at_all_count_for_uin")
    var remainAtAllCountForUin = 0
}
