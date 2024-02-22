package cn.evole.onebot.sdk.util.json;


import cn.evole.onebot.sdk.entity.Anonymous;
import cn.evole.onebot.sdk.event.message.GroupMessageEvent;
import cn.evole.onebot.sdk.event.message.GuildMessageEvent;
import cn.evole.onebot.sdk.event.message.MessageEvent;
import cn.evole.onebot.sdk.event.message.PrivateMessageEvent;
import com.google.gson.*;

import java.lang.reflect.Type;

public class MessageEventAdapter implements JsonDeserializer<MessageEvent> {
    Gson gson = new Gson();

    public MessageEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        MessageEvent e = null;
        JsonObject obj = json.getAsJsonObject();
        String messageType = obj.get("message_type").getAsString();
        long userId = obj.get("user_id").getAsLong();
        JsonElement messageElement = obj.get("message");
        String message = messageElement.isJsonPrimitive() ? messageElement.getAsJsonPrimitive().getAsString() : gson.toJson(messageElement);
        String rawMessage = obj.get("raw_message").getAsString();
        int font = obj.get("font").getAsInt();
        switch (messageType) {
            case "group": {
                e = groupMessageEvent(new JsonsObject(obj));
                break;
            }
            case "private": {
                e = privateMessageEvent(new JsonsObject(obj));
                break;
            }
            case "guild": {
                e = guildMessageEvent(new JsonsObject(obj));
                break;
            }
        }
        if (e != null){
            e.setMessageType(messageType);
            e.setUserId(userId);
            e.setMessage(message);
            e.setRawMessage(rawMessage);
            e.setFont(font);
        }
        return e;
    }

    private MessageEvent groupMessageEvent(JsonsObject obj) {
        int messageId = obj.optInt("message_id");
        String subType = obj.optString("sub_type");
        long groupId = obj.optLong("group_id");
        Anonymous anonymous = gson.fromJson(obj.get().get("anonymous"), Anonymous.class);
        GroupMessageEvent.GroupSender sender = gson.fromJson(obj.get().get("sender"), GroupMessageEvent.GroupSender.class);
        return new GroupMessageEvent(messageId, subType, groupId, anonymous, sender);
    }

    private MessageEvent privateMessageEvent(JsonsObject obj) {
        int messageId = obj.optInt("message_id");
        String subType = obj.optString("sub_type");
        PrivateMessageEvent.PrivateSender sender = gson.fromJson(obj.get().get("sender"), PrivateMessageEvent.PrivateSender.class);
        return new PrivateMessageEvent(messageId, subType, sender);
    }

    private MessageEvent guildMessageEvent(JsonsObject obj) {
        String messageId = obj.optString("message_id");
        String subType = obj.optString("sub_type");
        String guildId = obj.optString("guild_id");
        String channelId = obj.optString("channel_id");
        String selfTinyId = obj.optString("self_tiny_id");
        GuildMessageEvent.GuildSender sender = gson.fromJson(obj.get().get("sender"), GuildMessageEvent.GuildSender.class);
        return new GuildMessageEvent(messageId, subType, guildId, channelId, selfTinyId, sender);
    }
}
