import cn.evolvefield.onebot.client.config.BotConfig;
import cn.evolvefield.onebot.client.connection.ConnectFactory;
import cn.evolvefield.onebot.client.core.Bot;
import cn.evole.onebot.sdk.util.MsgUtils;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/2 19:35
 * Version: 1.0
 */
public class ApiTest {
    public static void main(String[] a) throws Exception {
        LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();//使用队列传输数据
        ConnectFactory service = new ConnectFactory(
                new BotConfig("ws://127.0.0.1:8080"),blockingQueue)
                ;//创建websocket客户端
        Bot bot = service.ws.createBot();
        var test =  bot.sendGroupMsg(337631140, MsgUtils.builder().text("123").build(), true);//发送群消息
        //GroupMemberInfoResp sender = bot.getGroupMemberInfo(720975019, 1812165465, false).getData();//获取响应的群成员信息
        System.out.println(test.getData().toString());//打印
        service.stop();
    }
}
