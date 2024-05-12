package cn.evolvefield.onebot.sdk.response.group

import com.google.gson.annotations.SerializedName
import lombok.Data

@Data
class GroupFileUrlResp {
    @SerializedName("url")
    var url = ""
}
