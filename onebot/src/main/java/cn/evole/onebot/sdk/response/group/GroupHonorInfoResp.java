package cn.evole.onebot.sdk.response.group;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created on 2022/7/8.
 *
 * @author cnlimiter
 */
@Data
public class GroupHonorInfoResp {

    @SerializedName("group_id")
    private long groupId;

    @SerializedName("current_talkative")
    private CurrentTalkative currentTalkative;

    @SerializedName("talkative_list")
    private List<OtherHonor> talkativeList;

    @SerializedName("performer_list")
    private List<OtherHonor> performerList;

    @SerializedName("legend_list")
    private List<OtherHonor> legendList;

    @SerializedName("strong_newbie_list")
    private List<OtherHonor> strongNewbieList;

    @SerializedName("emotion_list")
    private List<OtherHonor> emotionList;

    /**
     * 活跃天数
     */
    @Data
    @NoArgsConstructor
    public static class CurrentTalkative {

        @SerializedName("user_id")
        private long userId;

        @SerializedName("nickname")
        private String nickname;

        @SerializedName("avatar")
        private String avatar;

        @SerializedName("day_count")
        private int dayCount;


    }

    /**
     * 其它荣耀
     */
    @Data
    @NoArgsConstructor
    public static class OtherHonor {

        @SerializedName("user_id")
        private long userId;

        @SerializedName("nickname")
        private String nickname;

        @SerializedName("avatar")
        private String avatar;

        @SerializedName("description")
        private String description;


    }


}
