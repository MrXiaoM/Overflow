import cn.evole.onebot.sdk.util.MsgUtils
import cn.evolvefield.onebot.client.config.BotConfig
import cn.evolvefield.onebot.client.connection.ConnectFactory

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/2 19:35
 * Version: 1.0
 */
object ApiTest {
    @JvmStatic
    suspend fun main(args: Array<String>) {
        val service = ConnectFactory.create(
            BotConfig("ws://127.0.0.1:8080")
        ) //创建websocket客户端
        val ws = service.ws ?: throw IllegalStateException("未连接到 Onebot")
        val bot = ws.createBot()

        val test = bot.sendGroupMsg(337631140, MsgUtils.builder().text("123").build(), true) //发送群消息
        //GroupMemberInfoResp sender = bot.getGroupMemberInfo(720975019, 1812165465, false).getData();//获取响应的群成员信息
        println(test.data.toString()) //打印

        service.stop()
    }
}