package cn.evole.onebot.sdk.response.misc;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * @author cnlimiter
 */
@Data
public class OcrResp {

    @SerializedName("texts")
    public List<TextDetection> texts;

    @SerializedName("language")
    public String language;

    @Data
    private static class TextDetection {

        /**
         * 文本
         */
        @SerializedName("text")
        public String text;

        /**
         * 置信度
         */
        @SerializedName("confidence")
        public int confidence;

        /**
         * 坐标
         */
        @SerializedName("coordinates")
        public int[][] coordinates;

    }

}
