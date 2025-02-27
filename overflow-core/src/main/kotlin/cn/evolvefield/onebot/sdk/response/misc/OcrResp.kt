package cn.evolvefield.onebot.sdk.response.misc

import com.google.gson.annotations.SerializedName

class OcrResp {
    @SerializedName("texts")
    var texts: List<TextDetection> = mutableListOf()

    @SerializedName("language")
    var language: String = ""

    class TextDetection {
        /**
         * 文本
         */
        @SerializedName("text")
        var text: String = ""

        /**
         * 置信度
         */
        @SerializedName("confidence")
        var confidence = 0

        /**
         * 坐标
         */
        @SerializedName("coordinates")
        var coordinates: Array<IntArray> = arrayOf()
    }
}
