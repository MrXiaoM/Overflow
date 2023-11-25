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
    private List<TextDetection> texts;

    @SerializedName("language")
    private String language;

    @Data
    private static class TextDetection {

        /**
         * 文本
         */
        @SerializedName("text")
        private String text;

        /**
         * 置信度
         */
        @SerializedName("confidence")
        private int confidence;

        /**
         * 坐标
         */
        @SerializedName("coordinates")
        private int[][] coordinates;

    }

}
