package cn.evolvefield.onebot.sdk.util.json

import cn.evolvefield.onebot.sdk.response.misc.ClientsResp
import cn.evolvefield.onebot.sdk.util.ignorable
import cn.evolvefield.onebot.sdk.util.long
import cn.evolvefield.onebot.sdk.util.string
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class ClientsAdapter : JsonDeserializer<ClientsResp.Clients> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ClientsResp.Clients {
        val obj = json.asJsonObject
        return ClientsResp.Clients().apply {
            appId = obj.long("app_id")
            deviceName = obj.string("device_name")
            deviceKind = obj.string("device_kind")
            loginTime = obj.ignorable("login_time", 0L)
            loginPlatform = obj.ignorable("login_platform", 0L)
            location = obj.ignorable("location", "")
            guid = obj.ignorable("guid", "")
        }
    }
}
