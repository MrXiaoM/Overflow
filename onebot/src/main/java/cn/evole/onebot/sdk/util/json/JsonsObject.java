package cn.evole.onebot.sdk.util.json;

import com.google.gson.*;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/6/12 7:54
 * Version: 1.0
 */
public class JsonsObject {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final JsonObject jsonObject;

    public JsonsObject(String text){
        this.jsonObject = parse(text);
    }

    public JsonsObject(JsonObject jsonObject){
        this.jsonObject = jsonObject;
    }

    public static JsonsObject parse(JsonObject json){
        return new JsonsObject(json);
    }


    private JsonObject parse(String text) {
        JsonElement elm = getJsonElement(text);
        if (elm == null) {
            return new JsonObject();
        }
        return elm.getAsJsonObject();
    }

    public JsonObject get(){
        if (jsonObject != null) return jsonObject;
        return new JsonObject();
    }

    public JsonElement getFromString(String string, boolean log) {
        if (string == null) {
            return null;
        }
        try {
            return JsonParser.parseString(string);
        } catch (Exception e) {
            if (log) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public JsonElement getJsonElement(String string) {
        return getFromString(string, true);
    }

    public boolean has(String key){
        return this.jsonObject.has(key);
    }

    public String optString(String key){
        return this.optString(key, "");
    }

    public String optString(String key, String defaultValue){
         return this.jsonObject.has(key) ? jsonObject.get(key).getAsString() : defaultValue;
    }

    public Number optNumber(String key) {
        return this.optNumber(key, null);
    }

    public Number optNumber(String key, Number defaultValue) {
        return this.jsonObject.has(key) ? jsonObject.get(key).getAsNumber() : defaultValue;
    }

    public long optLong(String key) {
        return this.optLong(key, 0);
    }

    public long optLong(String key, long defaultValue){
        return this.jsonObject.has(key) ? jsonObject.get(key).getAsLong() : defaultValue;
    }

    public double optDouble(String key) {
        return this.optDouble(key, 0d);
    }

    public double optDouble(String key, double defaultValue){
        return this.jsonObject.has(key) ? jsonObject.get(key).getAsDouble() : defaultValue;
    }

    public short optShort(String key) {
        return this.optShort(key, (short) 0);
    }

    public short optShort(String key, short defaultValue){
        return this.jsonObject.has(key) ? jsonObject.get(key).getAsShort() : defaultValue;
    }

    public byte optByte(String key) {
        return this.optByte(key, (byte) 0);
    }

    public byte optByte(String key, byte defaultValue){
        return this.jsonObject.has(key) ? jsonObject.get(key).getAsByte() : defaultValue;
    }

    public boolean optBool(String key) {
        return this.optBool(key, true);
    }

    public boolean optBool(String key, boolean defaultValue){
        return this.jsonObject.has(key) ? jsonObject.get(key).getAsBoolean() : defaultValue;
    }

    public int optInt(String key) {
        return this.optInt(key, 0);
    }

    public int optInt(String key, int defaultValue) {
        final Number val = this.optNumber(key, null);
        if (val == null) {
            return defaultValue;
        }
        return val.intValue();
    }

    public JsonObject optJSONObject(String key){
        return this.optJSONObject(key, new JsonObject());
    }

    public JsonObject optJSONObject(String key, JsonObject defaultValue){
        return this.jsonObject.has(key) ? jsonObject.get(key).getAsJsonObject() : defaultValue;
    }

    public JsonArray optJSONArray(String key) {
        return this.optJSONArray(key, new JsonArray());
    }

    public JsonArray optJSONArray(String key, JsonArray defaultValue) {
        return this.jsonObject.has(key) ? jsonObject.get(key).getAsJsonArray() : defaultValue;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }

    public String toPrettyString() {
        return gson.toJson(jsonObject);
    }
}
