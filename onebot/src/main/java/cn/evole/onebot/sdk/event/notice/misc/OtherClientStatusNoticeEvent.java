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
    public boolean online;

    @SerializedName("client")
    private Clients client;

    /**
     * 设备对象
     */
    @Data
    public static class Clients {

        @SerializedName("app_id")
        public long appId;

        @SerializedName("platform")
        public String platform;

        @SerializedName("device_name")
        public String deviceName;

        @SerializedName("device_kind")
        public String deviceKind;

    }

}
