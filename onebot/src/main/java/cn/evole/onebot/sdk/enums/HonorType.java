package cn.evole.onebot.sdk.enums;

/**
 * Description:群聊荣誉
 * Author: cnlimiter
 * Date: 2022/9/14 17:17
 * Version: 1.0
 */
public enum HonorType {
    TALKATIVE("talkative"),//龙王
    PERFORMER("performer"),//群聊之火
    LEGENF("legend"),//群聊炽焰
    STRONG_NEWBIE("strong_newbie"),//冒尖小春笋
    EMOTION("emotion"),//快乐之源
    ALL("all"),//所有
    ;

    public String value;

    HonorType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
