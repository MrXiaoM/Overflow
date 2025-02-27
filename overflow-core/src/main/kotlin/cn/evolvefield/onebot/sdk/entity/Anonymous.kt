package cn.evolvefield.onebot.sdk.entity

import com.google.gson.annotations.SerializedName

class Anonymous {
    @SerializedName("id")
    var id = 0L
    @SerializedName("name")
    var name: String = ""
    @SerializedName("flag")
    var flag: String = ""
}
