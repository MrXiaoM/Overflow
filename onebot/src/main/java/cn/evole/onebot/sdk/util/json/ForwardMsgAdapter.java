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
            JsonObject obj = jsonElement.getAsJsonObject();
            int time = obj.get("time").getAsInt();
            String messageType = obj.get("message_type").getAsString();
            int messageId = obj.get("message_id").getAsInt();
            int realId = obj.get("real_id").getAsInt();
            ForwardMsgResp.Sender sender = gson.fromJson(obj.get("anonymous"), ForwardMsgResp.Sender.class);
            String message;
            if (obj.get("message").isJsonArray()) {
                message = gson.toJson(obj.get("message").getAsJsonArray());
            } else {
                message = obj.get("message").getAsString();
            }
            long peerId = obj.get("peer_id").getAsLong();
            long targetId = obj.get("target_id").getAsLong();
            nodes.add(new ForwardMsgResp.Node(time, messageType, messageId, realId, peerId, targetId, sender, message));
        }
        return new ForwardMsgResp(nodes);
    }
}
