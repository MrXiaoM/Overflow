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
    public List<Clients> clients;

    @Data
    public static class Clients {

        @SerializedName("app_id")
        public long appId;

        @SerializedName("device_name")
        public String deviceName;

        @SerializedName("device_kind")
        public String deviceKind;

    }

}
