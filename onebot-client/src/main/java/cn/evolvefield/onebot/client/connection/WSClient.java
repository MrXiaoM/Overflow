package cn.evolvefield.onebot.client.connection;

import cn.evolvefield.onebot.client.core.Bot;
import cn.evolvefield.onebot.client.handler.ActionHandler;
import cn.evole.onebot.sdk.util.json.JsonsObject;
import com.google.gson.JsonSyntaxException;
import lombok.val;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.BlockingQueue;

/**
 * Project: onebot-client
 * Author: cnlimiter
 * Date: 2023/4/4 2:20
 * Description:
 */
public class WSClient extends WebSocketClient {
    public static final Logger log = LoggerFactory.getLogger("WSClient");
    private static final String META_EVENT = "meta_event_type";
    private final static String API_RESULT_KEY = "echo";
    private static final String FAILED_STATUS = "failed";
    private static final String RESULT_STATUS_KEY = "status";
    private static final String HEART_BEAT = "heartbeat";
    private static final String LIFE_CYCLE = "lifecycle";
    private final BlockingQueue<String> queue;
    private final ActionHandler actionHandler;

    public WSClient(URI uri, BlockingQueue<String> queue, ActionHandler actionHandler) {
        super(uri);
        this.queue = queue;
        this.actionHandler = actionHandler;
    }

    public Bot createBot(){
        return new Bot(this, actionHandler);
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("▌ §c已连接到服务器 §a┈━═☆");
    }

    @Override
    public void onMessage(String message) {
        try {
            val jsonObject = new JsonsObject(message);

            if (message != null && !HEART_BEAT.equals(jsonObject.optString(META_EVENT)) ) {//过滤心跳
                log.debug("接收到原始消息{}", jsonObject.toString());
                if (jsonObject.has(API_RESULT_KEY)) {
                    if (FAILED_STATUS.equals(jsonObject.optString(RESULT_STATUS_KEY))) {
                        log.debug("请求失败: {}", jsonObject.optString("wording"));
                    } else
                        actionHandler.onReceiveActionResp(jsonObject);//请求执行
                } else if (!queue.offer(message)){//事件监听
                    log.error("监听错误: {}", message);
                }
            }
        } catch (
                JsonSyntaxException e) {
            log.error("Json语法错误:{}", message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("▌ §c服务器因{}已关闭",  reason);
    }

    @Override
    public void onError(Exception ex) {
        log.error("▌ §c出现错误{}或未连接§a┈━═☆",  ex.getLocalizedMessage());
    }
}
