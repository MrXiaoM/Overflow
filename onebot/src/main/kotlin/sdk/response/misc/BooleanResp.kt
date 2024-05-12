package cn.evolvefield.onebot.sdk.response.misc

import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data

@Data
@AllArgsConstructor
class BooleanResp {
    @SerializedName("yes")
    var yes = false
}
