import cn.evolvefield.onebot.sdk.response.group.ForwardMsgResp
import cn.evolvefield.onebot.sdk.util.JsonHelper.gson
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import net.mamoe.mirai.message.data.ForwardMessage
import net.mamoe.mirai.message.data.MessageChain
import org.junit.jupiter.api.Test
import top.mrxiaom.overflow.contact.RemoteBot
import top.mrxiaom.overflow.contact.RemoteBot.Companion.asRemoteBot
import top.mrxiaom.overflow.internal.message.OnebotMessages

class ForwardMessageTest {
    private val mockBot = object : RemoteBot {
        override val appName: String
            get() = "test"
        override val appVersion: String
            get() = "V1.0.0"
        override val noPlatform: Boolean
            get() = false

        override suspend fun executeAction(actionPath: String, params: String?): String {
            TODO("Not yet implemented")
        }

        override fun sendRawWebSocketMessage(message: String) {
            TODO("Not yet implemented")
        }

        override fun sendRawWebSocketMessage(message: ByteArray) {
            TODO("Not yet implemented")
        }

        override suspend fun getMsg(messageId: Int): MessageChain? {
            TODO("Not yet implemented")
        }

    }

    @Test
    //仅用于将OnebotMessages.kt#425行的代码做测试，防止出现bug
    fun testForwardCast() {
        val it = Json.decodeFromString<JsonObject>(
        """
            {
                "id": "7436377759204399490",
                "content": [
                    {
                        "self_id": 3405637452,
                        "user_id": 485184047,
                        "time": 1731415740,
                        "message_id": 726892516,
                        "message_seq": 726892516,
                        "real_id": 726892516,
                        "message_type": "group",
                        "sender": {
                            "user_id": 485184047,
                            "nickname": "上亦下心",
                            "card": ""
                        },
                        "raw_message": "1",
                        "font": 14,
                        "sub_type": "normal",
                        "message": [
                            {
                                "type": "text",
                                "data": {
                                    "text": "1"
                                }
                            }
                        ],
                        "message_format": "array",
                        "post_type": "message",
                        "group_id": 284840486
                    },
                    {
                        "self_id": 3405637452,
                        "user_id": 485184047,
                        "time": 1731415746,
                        "message_id": 1835533293,
                        "message_seq": 1835533293,
                        "real_id": 1835533293,
                        "message_type": "group",
                        "sender": {
                            "user_id": 485184047,
                            "nickname": "上亦下心",
                            "card": ""
                        },
                        "raw_message": "1",
                        "font": 14,
                        "sub_type": "normal",
                        "message": [
                            {
                                "type": "text",
                                "data": {
                                    "text": "1"
                                }
                            }
                        ],
                        "message_format": "array",
                        "post_type": "message",
                        "group_id": 284840486
                    }
                ]
            }
        """
        )

        val resp = runBlocking {
            gson.fromJson(
                it.toString(),
                ForwardMsgResp::class.java
            )
        }
        val result = runBlocking {
            resp.message.map {
                val msg = OnebotMessages.deserializeFromOneBot(mockBot, it.message)
                ForwardMessage.Node(
                    it.sender!!.userId,
                    it.time,
                    it.sender!!.nickname.takeIf(String::isNotEmpty) ?: "QQ用户",
                    msg
                )
            }
        }
        println(result)
    }
}