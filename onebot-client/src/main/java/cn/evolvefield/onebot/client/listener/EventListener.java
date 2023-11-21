package cn.evolvefield.onebot.client.listener;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 16:11
 * Version: 1.0
 */
public interface EventListener<T> {

        /**
         * 监听到消息
         *
         * @param t 消息实体
         */
        void onMessage(T t);
}
