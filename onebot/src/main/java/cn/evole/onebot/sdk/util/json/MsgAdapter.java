package cn.evole.onebot.sdk.util.json;

import cn.evole.onebot.sdk.response.group.GetMsgResp;
import com.google.gson.*;

import java.lang.reflect.Type;

public class MsgAdapter implements JsonDeserializer<GetMsgResp> {
    Gson gson = new Gson();

    public GetMsgResp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonObject obj = json.getAsJsonObject();

        int messageId = obj.get("message_id").getAsInt();
        int realId = obj.get("real_id").getAsInt();
        GetMsgResp.Sender sender = gson.fromJson(obj.get("sender"), GetMsgResp.Sender.class);
        int time = obj.get("time").getAsInt();
        String message;
        if (obj.get("message").isJsonArray()) {
            message = gson.toJson(obj.get("message").getAsJsonArray());
        } else {
            message = obj.get("message").getAsString();
        }
        String rawMessage = obj.has("raw_message") ? obj.get("raw_message").getAsString() : "";
        long peerId = obj.has("peer_id") ? obj.get("peer_id").getAsLong() : 0;
        long groupId = obj.has("group_id") ? obj.get("group_id").getAsLong() : 0;
        long targetId = obj.has("target_id") ? obj.get("target_id").getAsLong() : 0;

        return new GetMsgResp(messageId, realId, sender, time, message, rawMessage, peerId, groupId, targetId);
    }
}
