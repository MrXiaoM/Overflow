package cn.evolvefield.onebot.sdk.entity

import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data

@Data
@AllArgsConstructor
class Anonymous {
    @SerializedName("id")
    var id = 0L
    @SerializedName("name")
    var name: String = ""
    @SerializedName("flag")
    var flag: String = ""
}
