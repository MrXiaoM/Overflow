import cn.evolvefield.onebot.client.config.BotConfig;
import cn.evolvefield.onebot.client.connection.ConnectFactory;
import cn.evolvefield.onebot.client.core.Bot;
import cn.evolvefield.onebot.client.handler.EventBus;
import cn.evolvefield.onebot.client.handler.Handler;
import cn.evolvefield.onebot.client.listener.SimpleEventListener;
import cn.evolvefield.onebot.client.listener.impl.GroupMessageEventListener;
import cn.evole.onebot.sdk.event.message.PrivateMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/2 19:06
 * Version: 1.0
 */
public class WebSocketClientTest {

    public static Logger logger = LoggerFactory.getLogger("test");


    public static void main1(String[] args) throws Exception {
        LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();//使用队列传输数据
        ConnectFactory service = new ConnectFactory(
                new BotConfig("ws://127.0.0.1:8080"), blockingQueue);//创建websocket客户端
        Bot bot = service.ws.createBot();
        EventBus dispatchers = new EventBus(blockingQueue);//创建事件分发器
        GroupMessageEventListener groupMessageListener = new GroupMessageEventListener();//自定义监听规则
        groupMessageListener.addHandler("test", new Handler<cn.evole.onebot.sdk.event.message.GroupMessageEvent>() {
            @Override
            public void handle(cn.evole.onebot.sdk.event.message.GroupMessageEvent groupMessage) {
                bot.sendGroupMsg(337631140, groupMessage.getMessage(), false);

            }
        });//匹配关键字监听
        dispatchers.addListener(groupMessageListener);//注册监听
        dispatchers.addListener(new SimpleEventListener<PrivateMessageEvent>() {//私聊监听
            @Override
            public void onMessage(PrivateMessageEvent privateMessage) {
                logger.info(privateMessage.toString());
            }
        });//快速监听

        dispatchers.stop();
        service.stop();
    }
}
