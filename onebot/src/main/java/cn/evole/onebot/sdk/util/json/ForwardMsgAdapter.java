package cn.evole.onebot.sdk.util.json;


import cn.evole.onebot.sdk.response.group.ForwardMsgResp;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ForwardMsgAdapter implements JsonDeserializer<ForwardMsgResp> {
    Gson gson = new Gson();

    public ForwardMsgResp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        List<ForwardMsgResp.Node> nodes = new ArrayList<>();
        JsonObject jsonObj = json.getAsJsonObject();
        JsonElement messagesArray;
        messagesArray = jsonObj.get("messages");
        if (messagesArray == null) {
            messagesArray = jsonObj.get("message");
        }
        for (JsonElement jsonElement : messagesArray.getAsJsonArray()) {
            JsonsObject obj = new JsonsObject(jsonElement.getAsJsonObject());
            if (obj.has("data")) {
                obj = new JsonsObject(obj.getJsonElement("data").getAsJsonObject());
            }
            int time = obj.optInt("time");
            String messageType = obj.optString("message_type");
            int messageId = obj.optInt("message_id");
            int realId = obj.optInt("real_id");
            ForwardMsgResp.Sender sender = gson.fromJson(obj.optJSONObject("anonymous"), ForwardMsgResp.Sender.class);
            String message = gson.toJson(obj.getJsonElement("message"));
            long peerId = obj.optLong("peer_id");
            long targetId = obj.optLong("target_id");
            nodes.add(new ForwardMsgResp.Node(time, messageType, messageId, realId, peerId, targetId, sender, message));
        }
        return new ForwardMsgResp(nodes);
    }
}
