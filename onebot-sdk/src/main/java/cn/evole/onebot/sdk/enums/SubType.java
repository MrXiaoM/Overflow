package cn.evole.onebot.sdk.enums;

/**
 * Description:提交类型
 * Author: cnlimiter
 * Date: 2022/9/14 17:17
 * Version: 1.0
 */
public enum SubType {
    POKE("poke"),//戳一戳
    ;

    private String value;

    SubType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
