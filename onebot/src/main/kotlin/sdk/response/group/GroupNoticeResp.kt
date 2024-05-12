package cn.evolvefield.onebot.sdk.response.group

import com.google.gson.annotations.SerializedName
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@AllArgsConstructor
@NoArgsConstructor
class GroupNoticeResp {
    @SerializedName("sender_id")
    var senderId = 0L

    @SerializedName("publish_time")
    var publishTime = 0L

    @SerializedName("message")
    var message: Message? = null

    @Data
    class Message {
        @SerializedName("text")
        var text: String = ""

        @SerializedName("images")
        var images: List<Image> = mutableListOf()
    }

    @Data
    class Image {
        @SerializedName("id")
        var id = ""

        @SerializedName("width")
        var width = "0"

        @SerializedName("height")
        var height = "0"
    }
}
