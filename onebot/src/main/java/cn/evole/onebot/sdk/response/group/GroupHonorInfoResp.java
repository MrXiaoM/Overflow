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
    public long groupId;

    @SerializedName("current_talkative")
    private CurrentTalkative currentTalkative;

    @SerializedName("talkative_list")
    public List<OtherHonor> talkativeList;

    @SerializedName("performer_list")
    public List<OtherHonor> performerList;

    @SerializedName("legend_list")
    public List<OtherHonor> legendList;

    @SerializedName("strong_newbie_list")
    public List<OtherHonor> strongNewbieList;

    @SerializedName("emotion_list")
    public List<OtherHonor> emotionList;

    /**
     * 活跃天数
     */
    @Data
    @NoArgsConstructor
    public static class CurrentTalkative {

        @SerializedName("user_id")
        public long userId;

        @SerializedName("nickname")
        public String nickname;

        @SerializedName("avatar")
        public String avatar;

        @SerializedName("day_count")
        public int dayCount;


    }

    /**
     * 其它荣耀
     */
    @Data
    @NoArgsConstructor
    public static class OtherHonor {

        @SerializedName("user_id")
        public long userId;

        @SerializedName("nickname")
        public String nickname;

        @SerializedName("avatar")
        public String avatar;

        @SerializedName("description")
        public String description;


    }


}
