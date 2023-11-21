package cn.evole.onebot.sdk.response.misc;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * @author cnlimiter
 */
@Data
public class ClientsResp {

    @SerializedName("clients")
    private List<Clients> clients;

    @Data
    private static class Clients {

        @SerializedName("app_id")
        private long appId;

        @SerializedName("device_name")
        private String deviceName;

        @SerializedName("device_kind")
        private String deviceKind;

    }

}
