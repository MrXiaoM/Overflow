package cn.evole.onebot.sdk.response.misc;

import cn.evole.onebot.sdk.util.json.ClientsAdapter;
import com.google.gson.annotations.JsonAdapter;
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
    @JsonAdapter(ClientsAdapter.class)
    public static class Clients {
        public long appId;
        public String deviceName;
        public String deviceKind;
        public long loginTime = 0;
        public long loginPlatform = 0;
        public String location = "";
        public String guid = "";
    }

}
