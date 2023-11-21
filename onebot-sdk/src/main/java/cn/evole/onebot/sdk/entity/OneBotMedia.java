package cn.evole.onebot.sdk.entity;


import cn.evole.onebot.sdk.util.BotUtils;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 13:51
 * Version: 1.0
 */
public class OneBotMedia {
    private final String file;
    private final Boolean cache;
    private final Boolean proxy;
    private final Integer timeout;

    /**
     * 构造函数
     *
     * @param builder {@link Builder}
     */
    public OneBotMedia(Builder builder) {
        this.file = builder.file;
        this.cache = builder.cache;
        this.proxy = builder.proxy;
        this.timeout = builder.timeout;
    }

    /**
     * @return media code params
     */
    public String escape() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("file=").append(this.file);
        if (this.cache != null) {
            stringBuilder.append(",cache=").append(this.cache ? 1 : 0);
        }
        if (this.proxy != null) {
            stringBuilder.append(",proxy=").append(this.proxy ? 1 : 0);
        }
        if (this.timeout != null) {
            stringBuilder.append(",timeout=").append(this.timeout);
        }
        return stringBuilder.toString();
    }

    /**
     * 构造器
     */
    public static class Builder {
        private String file = "";
        private Boolean cache;
        private Boolean proxy;
        private Integer timeout;

        /**
         * @param file 文件
         * @return {@link Builder}
         */
        public Builder file(String file) {
            this.file = BotUtils.escape(file);
            return this;
        }

        /**
         * @param cache 缓存
         * @return {@link Builder}
         */
        public Builder cache(boolean cache) {
            this.cache = cache;
            return this;
        }

        /**
         * @param proxy 代理
         * @return {@link Builder}
         */
        public Builder proxy(boolean proxy) {
            this.proxy = proxy;
            return this;
        }

        /**
         * @param timeout 超时
         * @return {@link Builder}
         */
        public Builder timeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * @return {@link OneBotMedia}
         */
        public OneBotMedia build() {
            return new OneBotMedia(this);
        }

    }
}
