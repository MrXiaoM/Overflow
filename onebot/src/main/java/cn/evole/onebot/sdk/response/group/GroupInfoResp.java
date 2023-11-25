package cn.evole.onebot.sdk.response.group;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created on 2022/7/8.
 *
 * @author cnlimiter
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GroupInfoResp extends GroupDataResp{

    @SerializedName("group_memo")
    public String groupMemo;

    @SerializedName("group_create_time")
    public int groupCreateTime;

    @SerializedName("group_level")
    public int groupLevel;

    @SerializedName("member_count")
    public Integer memberCount;

    @SerializedName("max_member_count")
    public Integer maxMemberCount;

}
