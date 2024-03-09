package cn.evole.onebot.sdk.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Supplier;

public abstract class JsonHelper {
    public static final Gson gson = new Gson();
    public static int ignorable(JsonObject obj, String key, int def) {
        JsonElement element = obj.get(key);
        if (element == null || !element.isJsonPrimitive()) return def;
        try {
            return Integer.parseInt(element.getAsString());
        } catch (NumberFormatException ignored) {
            return def;
        }
    }
    public static long ignorable(JsonObject obj, String key, long def) {
        JsonElement element = obj.get(key);
        if (element == null || !element.isJsonPrimitive()) return def;
        try {
            return Long.parseLong(element.getAsString());
        } catch (NumberFormatException ignored) {
            return def;
        }
    }
    public static float ignorable(JsonObject obj, String key, float def) {
        JsonElement element = obj.get(key);
        if (element == null || !element.isJsonPrimitive()) return def;
        try {
            return Float.parseFloat(element.getAsString());
        } catch (NumberFormatException ignored) {
            return def;
        }
    }
    public static double ignorable(JsonObject obj, String key, double def) {
        JsonElement element = obj.get(key);
        if (element == null || !element.isJsonPrimitive()) return def;
        try {
            return Double.parseDouble(element.getAsString());
        } catch (NumberFormatException ignored) {
            return def;
        }
    }
    public static String ignorable(JsonObject obj, String key, String def) {
        JsonElement element = obj.get(key);
        if (element == null || !element.isJsonPrimitive()) return def;
        return element.getAsString();
    }
    public static JsonObject ignorableObject(JsonObject obj, String key, Supplier<JsonObject> def) {
        JsonElement element = obj.get(key);
        if (element == null || !element.isJsonObject()) return def.get();
        return element.getAsJsonObject();
    }
    public static JsonArray ignorableArray(JsonObject obj, String key, Supplier<JsonArray> def) {
        JsonElement element = obj.get(key);
        if (element == null || !element.isJsonArray()) return def.get();
        return element.getAsJsonArray();
    }
    public static String forceString(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element == null) return "";
        if (element.isJsonPrimitive()) return element.getAsString();
        return gson.toJson(element);
    }
    public static <T> T fromJson(JsonObject obj, String key, Class<T> type) {
        return gson.fromJson(obj.get(key), type);
    }
}
