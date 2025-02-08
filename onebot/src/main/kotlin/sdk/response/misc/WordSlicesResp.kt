package cn.evolvefield.onebot.sdk.response.misc

import com.google.gson.annotations.SerializedName

class WordSlicesResp {
    @SerializedName("slices")
    var slices: List<String> = mutableListOf()
}
