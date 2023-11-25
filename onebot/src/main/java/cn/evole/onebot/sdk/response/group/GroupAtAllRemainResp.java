package cn.evole.onebot.sdk.response.group;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Created on 2022/7/8.
 *
 * @author cnlimiter
 */
@Data
public class GroupAtAllRemainResp {

    /**
     * 是否可以 @全体成员
     */
    @SerializedName("can_at_all")
    public boolean canAtAll;

    /**
     * 群内所有管理当天剩余 @全体成员 次数
     */
    @SerializedName("remain_at_all_count_for_group")
    public int remainAtAllCountForGroup;

    /**
     * Bot 当天剩余 @全体成员 次数
     */
    @SerializedName("remain_at_all_count_for_uin")
    public int remainAtAllCountForUin;

}
