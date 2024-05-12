package cn.evolvefield.onebot.sdk.response.contact

import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@AllArgsConstructor
@NoArgsConstructor
class LoginInfoResp {
    @SerializedName("user_id")
    var userId = 0L
    @SerializedName("nickname")
    var nickname = ""
}
