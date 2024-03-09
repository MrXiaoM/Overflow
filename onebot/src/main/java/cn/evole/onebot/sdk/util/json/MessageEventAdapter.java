package cn.evole.onebot.sdk.util.json;


import cn.evole.onebot.sdk.entity.Anonymous;
import cn.evole.onebot.sdk.event.message.GroupMessageEvent;
import cn.evole.onebot.sdk.event.message.GuildMessageEvent;
import cn.evole.onebot.sdk.event.message.MessageEvent;
import cn.evole.onebot.sdk.event.message.PrivateMessageEvent;
import cn.evole.onebot.sdk.util.JsonHelper;
import com.google.gson.*;

import java.lang.reflect.Type;

public class MessageEventAdapter extends JsonHelper implements JsonDeserializer<MessageEvent> {
    public MessageEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        MessageEvent e = null;
        JsonObject obj = json.getAsJsonObject();
        String postType = obj.get("post_type").getAsString();
        String subType = obj.get("sub_type").getAsString();
        long time = obj.get("time").getAsLong();
        long selfId = obj.get("self_id").getAsLong();
        String messageType = obj.get("message_type").getAsString();
        long userId = obj.get("user_id").getAsLong();
        String message = forceString(obj, "message");
        String rawMessage = ignorable(obj, "raw_message", "");
        int font = ignorable(obj, "font", 0);

        switch (messageType) {
            case "group": {
                e = groupMessageEvent(obj, subType);
                break;
            }
            case "private": {
                e = privateMessageEvent(obj, subType);
                break;
            }
            case "guild": { // go-cqhttp, OpenShamrock
                e = guildMessageEvent(obj, subType);
                break;
            }
        }
        if (e != null) {
            e.setPostType(postType);
            e.setTime(time);
            e.setSelfId(selfId);
            e.setMessageType(messageType);
            e.setUserId(userId);
            e.setMessage(message);
            e.setRawMessage(rawMessage);
            e.setFont(font);
        }
        return e;
    }

    private MessageEvent groupMessageEvent(JsonObject obj, String subType) {
        int messageId = MsgAdapter.getMessageId(obj);
        long groupId = obj.get("group_id").getAsLong();
        Anonymous anonymous = fromJson(obj, "anonymous", Anonymous.class);
        GroupMessageEvent.GroupSender sender = fromJson(obj, "sender", GroupMessageEvent.GroupSender.class);
        return new GroupMessageEvent(messageId, subType, groupId, anonymous, sender);
    }

    private MessageEvent privateMessageEvent(JsonObject obj, String subType) {
        int messageId = MsgAdapter.getMessageId(obj);
        int tempSource = ignorable(obj, "temp_source", Integer.MIN_VALUE); // OpenShamrock
        PrivateMessageEvent.PrivateSender sender = fromJson(obj, "sender", PrivateMessageEvent.PrivateSender.class);
        long groupId = ignorable(obj, "group_id", sender != null ? sender.groupId : 0); // OpenShamrock
        String fromNick = ignorable(obj, "from_nick", sender != null ? sender.nickname : ""); // OpenShamrock
        return new PrivateMessageEvent(messageId, subType, sender, groupId, tempSource, fromNick);
    }

    private MessageEvent guildMessageEvent(JsonObject obj, String subType) {
        String messageId = obj.get("message_id").getAsString();
        String guildId = obj.get("guild_id").getAsString();
        String channelId = obj.get("channel_id").getAsString();
        String selfTinyId = obj.get("self_tiny_id").getAsString();
        GuildMessageEvent.GuildSender sender = fromJson(obj, "sender", GuildMessageEvent.GuildSender.class);
        return new GuildMessageEvent(messageId, subType, guildId, channelId, selfTinyId, sender);
    }

    public static class PrivateSender implements JsonDeserializer<PrivateMessageEvent.PrivateSender> {
        @Override
        public PrivateMessageEvent.PrivateSender deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            PrivateMessageEvent.PrivateSender sender = new PrivateMessageEvent.PrivateSender();
            sender.userId = obj.get("user_id").getAsLong();
            sender.groupId = ignorable(obj, "group_id", 0);
            sender.nickname = obj.get("nickname").getAsString();
            sender.sex = ignorable(obj, "sex", "unknown");
            sender.age = ignorable(obj, "age", 0);
            return sender;
        }
    }
}
