package cn.evole.onebot.sdk.response.group;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/11 18:22
 * Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupDataResp {
    @SerializedName("group_id")
    public long groupId;

    @SerializedName("group_name")
    public String groupName;
}
