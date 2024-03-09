package cn.evole.onebot.sdk.util.json;

import cn.evole.onebot.sdk.response.misc.ClientsResp;
import cn.evole.onebot.sdk.util.JsonHelper;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;

public class ClientsAdapter extends JsonHelper implements JsonDeserializer<ClientsResp.Clients> {

    public ClientsResp.Clients deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject obj = json.getAsJsonObject();
        ClientsResp.Clients clients = new ClientsResp.Clients();
        clients.appId = obj.get("app_id").getAsLong();
        clients.deviceName = obj.get("device_name").getAsString();
        clients.deviceKind = obj.get("device_kind").getAsString();
        clients.loginTime = ignorable(obj, "login_time", 0);
        clients.loginPlatform = ignorable(obj, "login_platform", 0);
        clients.location = ignorable(obj, "location", null);
        clients.guid = ignorable(obj, "guid", null);
        return clients;
    }
}
