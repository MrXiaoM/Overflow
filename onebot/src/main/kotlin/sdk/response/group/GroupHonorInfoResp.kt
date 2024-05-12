package cn.evolvefield.onebot.sdk.response.group

import com.google.gson.annotations.SerializedName
import lombok.Data
import lombok.NoArgsConstructor

@Data
class GroupHonorInfoResp {
    @SerializedName("group_id")
    var groupId = 0L
    @SerializedName("current_talkative")
    private val currentTalkative: CurrentTalkative? = null
    @SerializedName("talkative_list")
    var talkativeList: List<OtherHonor> = mutableListOf()
    @SerializedName("performer_list")
    var performerList: List<OtherHonor> = mutableListOf()
    @SerializedName("legend_list")
    var legendList: List<OtherHonor> = mutableListOf()
    @SerializedName("strong_newbie_list")
    var strongNewbieList: List<OtherHonor> = mutableListOf()
    @SerializedName("emotion_list")
    var emotionList: List<OtherHonor> = mutableListOf()

    /**
     * 活跃天数
     */
    @Data
    @NoArgsConstructor
    class CurrentTalkative {
        @SerializedName("user_id")
        var userId = 0L

        @SerializedName("nickname")
        var nickname = ""

        @SerializedName("avatar")
        var avatar = ""

        @SerializedName("day_count")
        var dayCount = 0
    }

    /**
     * 其它荣耀
     */
    @Data
    @NoArgsConstructor
    class OtherHonor {
        @SerializedName("user_id")
        var userId = 0L

        @SerializedName("nickname")
        var nickname = ""

        @SerializedName("avatar")
        var avatar = ""

        @SerializedName("description")
        var description = ""
    }
}
