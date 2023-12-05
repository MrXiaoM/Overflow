package cn.evole.onebot.sdk.util.json;

import cn.evole.onebot.sdk.response.group.GetMsgResp;
import cn.evole.onebot.sdk.response.misc.ClientsResp;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ClientsAdapter implements JsonDeserializer<ClientsResp.Clients> {

    public ClientsResp.Clients deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject obj = json.getAsJsonObject();
        ClientsResp.Clients clients = new ClientsResp.Clients();
        clients.appId = obj.get("app_id").getAsLong();
        clients.deviceName = obj.get("device_name").getAsString();
        clients.deviceKind = obj.get("device_kind").getAsString();
        JsonElement loginTime = obj.get("login_time");
        JsonElement loginPlatform = obj.get("login_platform");
        JsonElement location = obj.get("location");
        JsonElement guid = obj.get("guid");
        if (loginTime != null) clients.loginTime = loginTime.getAsLong();
        if (loginPlatform != null) clients.loginPlatform = loginPlatform.getAsLong();
        if (location != null) clients.location = location.getAsString();
        if (guid != null) clients.guid = guid.getAsString();
        return clients;
    }
}
