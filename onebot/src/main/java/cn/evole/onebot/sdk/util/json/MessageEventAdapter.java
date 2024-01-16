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
        String message = gson.toJson(obj.get("message"));
        String rawMessage = obj.get("raw_message").getAsString();
        int font = obj.get("font").getAsInt();
        switch (messageType) {
            case "group": {
                int messageId = obj.get("message_id").getAsInt();
                String subType = obj.get("sub_type").getAsString();
                long groupId = obj.get("group_id").getAsLong();
                Anonymous anonymous = gson.fromJson(obj.get("anonymous"), Anonymous.class);
                GroupMessageEvent.GroupSender sender = gson.fromJson(obj.get("sender"), GroupMessageEvent.GroupSender.class);
                e = new GroupMessageEvent(messageId, subType, groupId, anonymous, sender);
                e.setMessageType(messageType);
                e.setUserId(userId);
                e.setMessage(message);
                e.setRawMessage(rawMessage);
                e.setFont(font);
                break;
            }
            case "private": {
                int messageId = obj.get("message_id").getAsInt();
                String subType = obj.get("sub_type").getAsString();
                PrivateMessageEvent.PrivateSender sender = gson.fromJson(obj.get("sender"), PrivateMessageEvent.PrivateSender.class);
                e = new PrivateMessageEvent(messageId, subType, sender);
                e.setMessageType(messageType);
                e.setUserId(userId);
                e.setMessage(message);
                e.setRawMessage(rawMessage);
                e.setFont(font);
                break;
            }
            case "guild": {
                String messageId = obj.get("message_id").getAsString();
                String subType = obj.get("sub_type").getAsString();
                String guildId = obj.get("guild_id").getAsString();
                String channelId = obj.get("channel_id").getAsString();
                String selfTinyId = obj.get("self_tiny_id").getAsString();
                GuildMessageEvent.Sender sender = gson.fromJson(obj.get("sender"), GuildMessageEvent.Sender.class);
                e = new GuildMessageEvent(messageId, subType, guildId, channelId, selfTinyId, sender);
                e.setMessageType(messageType);
                e.setUserId(userId);
                e.setMessage(message);
                e.setRawMessage(rawMessage);
                e.setFont(font);
                break;
            }
        }
        return e;
    }
}
