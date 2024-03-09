package cn.evole.onebot.sdk.util.json;


import cn.evole.onebot.sdk.response.group.ForwardMsgResp;
import cn.evole.onebot.sdk.util.JsonHelper;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ForwardMsgAdapter extends JsonHelper implements JsonDeserializer<ForwardMsgResp> {
    public ForwardMsgResp deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        List<ForwardMsgResp.Node> nodes = new ArrayList<>();
        JsonObject jsonObj = json.getAsJsonObject();
        JsonElement messagesArray;
        messagesArray = jsonObj.get("messages");
        if (messagesArray == null) {
            messagesArray = jsonObj.get("message"); // Lagrange
        }
        for (JsonElement jsonElement : messagesArray.getAsJsonArray()) {
            JsonObject obj = jsonElement.getAsJsonObject();
            if (obj.has("data")) {
                obj = obj.get("data").getAsJsonObject();
            }
            int time = ignorable(obj, "time", 0); // OpenShamrock
            String messageType = ignorable(obj, "message_type", ""); // OpenShamrock
            int messageId = ignorable(obj, "message_id", 0); // OpenShamrock
            int realId = ignorable(obj, "real_id", 0); // OpenShamrock
            ForwardMsgResp.Sender sender = fromJson(obj, "sender", ForwardMsgResp.Sender.class); // OpenShamrock
            if (sender == null) { // Lagrange
                long userId = ignorable(obj, "user_id", 0);
                String nickName = ignorable(obj, "nickname", "");
                sender = new ForwardMsgResp.Sender(userId, nickName, "unknown", 0, "");
            }
            String message;
            if (obj.has("content")) {
                message = forceString(obj, "content"); // Lagrange
            } else {
                message = forceString(obj, "message"); // go-cqhttp, OpenShamrock
            }
            long peerId = ignorable(obj, "peer_id", 0); // OpenShamrock
            long targetId = ignorable(obj, "target_id", 0); // OpenShamrock
            nodes.add(new ForwardMsgResp.Node(time, messageType, messageId, realId, peerId, targetId, sender, message));
        }
        return new ForwardMsgResp(nodes);
    }
}
