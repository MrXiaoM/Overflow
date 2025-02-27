package cn.evolvefield.onebot.sdk.response.misc

import cn.evolvefield.onebot.sdk.util.json.ClientsAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName

class ClientsResp {
    @SerializedName("clients")
    var clients: List<Clients> = mutableListOf()

    @JsonAdapter(ClientsAdapter::class)
    class Clients {
        var appId = 0L
        var deviceName = ""
        var deviceKind = ""
        var loginTime = 0L
        var loginPlatform = 0L
        var location = ""
        var guid = ""
    }
}
