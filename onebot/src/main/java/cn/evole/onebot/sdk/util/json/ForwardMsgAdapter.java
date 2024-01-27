package cn.evole.onebot.sdk.util.json;


import cn.evole.onebot.sdk.response.group.ForwardMsgResp;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static cn.evole.onebot.sdk.Data.appName;

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
            if (appName.equalsIgnoreCase("shamrock")) {
                // OpenShamrock
                int time = obj.optInt("time");
                String messageType = obj.optString("message_type");
                int messageId = obj.optInt("message_id");
                int realId = obj.optInt("real_id");
                ForwardMsgResp.Sender sender = gson.fromJson(obj.optJSONObject("sender"), ForwardMsgResp.Sender.class);
                String message = gson.toJson(obj.getJsonElement("message"));
                long peerId = obj.optLong("peer_id");
                long targetId = obj.optLong("target_id");
                nodes.add(new ForwardMsgResp.Node(time, messageType, messageId, realId, peerId, targetId, sender, message));
            } if (appName.equalsIgnoreCase("go-cqhttp")) {
                // go-cqhttp
                String message = gson.toJson(obj.getJsonElement("content"));
                ForwardMsgResp.Sender sender = gson.fromJson(obj.optJSONObject("sender"), ForwardMsgResp.Sender.class);
                int time = obj.optInt("time");
                nodes.add(new ForwardMsgResp.Node(time, "", 0, 0, 0, 0, sender, message));
            } else {
                // 其它标准 Onebot 实现
                long userId = obj.optLong("user_id");
                String nickname = obj.optString("nickname");
                ForwardMsgResp.Sender sender = new ForwardMsgResp.Sender(userId, nickname, "", 0, "");
                String message = gson.toJson(obj.getJsonElement("content"));
                nodes.add(new ForwardMsgResp.Node(0, "", 0, 0, 0, 0, sender, message));
            }
        }
        return new ForwardMsgResp(nodes);
    }
}
