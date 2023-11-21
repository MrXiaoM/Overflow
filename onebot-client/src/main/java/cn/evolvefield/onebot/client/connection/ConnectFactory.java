package cn.evolvefield.onebot.client.connection;

import cn.evolvefield.onebot.client.config.BotConfig;
import cn.evolvefield.onebot.client.core.Bot;
import cn.evolvefield.onebot.client.handler.ActionHandler;
import org.java_websocket.WebSocket;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static cn.evolvefield.onebot.client.connection.WSClient.log;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/1 17:01
 * Version: 1.0
 */
public class ConnectFactory {
    private final BotConfig config;
    private final BlockingQueue<String> queue;
    private final ActionHandler actionHandler;
    public WSClient ws;
    /**
     *
     * @param config 配置
     * @param queue 队列消息
     */
    public ConnectFactory(BotConfig config, BlockingQueue<String> queue){
        this.config = config;
        this.queue = queue;
        this.actionHandler = new ActionHandler();
        try {
            this.ws = createWebsocketClient();
        }catch (NullPointerException e){
            log.error("▌ §c连接错误，请检查服务端是否开启 §a┈━═☆");
        }
    }



    /**
     * 创建websocket客户端(支持cqhttp和mirai类型)
     * @return 连接示例
     */
    public WSClient createWebsocketClient(){
        StringBuilder builder = new StringBuilder();
        WSClient ws = null;
        if (config.isMiraiHttp()){
            builder.append(config.getUrl());
            builder.append("/all");
            builder.append("?verifyKey=");
            if (config.isAccessToken()) {
                builder.append(config.getToken());
            }
            builder.append("&qq=");
            builder.append(config.getBotId());
        }
        else {
            builder.append(config.getUrl());
            if (config.isAccessToken()) {
                builder.append("?access_token=");
                builder.append(config.getToken());
            }
        }
        String url = builder.toString();
        try {
            ws = new WSClient(URI.create(url), queue, actionHandler);
            ws.connect();
        }catch (Exception e){
            log.error("▌ §c{}连接错误，请检查服务端是否开启 §a┈━═☆", url);
        }
        return ws;
    }

    public void stop(){
        ws.close();
    }


}
