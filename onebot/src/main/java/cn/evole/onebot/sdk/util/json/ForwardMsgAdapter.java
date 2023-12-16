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
        JsonArray messagesArray = jsonObj.get("messages").getAsJsonArray();
        for (JsonElement jsonElement : messagesArray) {
            JsonsObject obj = new JsonsObject(jsonElement.getAsJsonObject());
            int time = obj.optInt("time");
            String messageType = obj.optString("message_type");
            int messageId = obj.optInt("message_id");
            int realId = obj.optInt("real_id");
            ForwardMsgResp.Sender sender = gson.fromJson(obj.optJSONObject("anonymous"), ForwardMsgResp.Sender.class);
            String message;
            if (obj.getJsonElement("message").isJsonArray()) {
                message = gson.toJson(obj.optJSONArray("message"));
            } else {
                message = obj.optString("message");
            }
            long peerId = obj.optLong("peer_id");
            long targetId = obj.optLong("target_id");
            nodes.add(new ForwardMsgResp.Node(time, messageType, messageId, realId, peerId, targetId, sender, message));
        }
        return new ForwardMsgResp(nodes);
    }
}
