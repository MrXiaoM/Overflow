package cn.evole.onebot.sdk.event.notice.misc;

import cn.evole.onebot.sdk.event.notice.NoticeEvent;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Created on 2022/7/8.
 *
 * @author cnlimiter
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class OtherClientStatusNoticeEvent extends NoticeEvent {

    @SerializedName("online")
    private boolean online;

    @SerializedName("client")
    private Clients client;

    /**
     * 设备对象
     */
    @Data
    public static class Clients {

        @SerializedName("app_id")
        private long appId;

        @SerializedName("platform")
        private String platform;

        @SerializedName("device_name")
        private String deviceName;

        @SerializedName("device_kind")
        private String deviceKind;

    }

}
