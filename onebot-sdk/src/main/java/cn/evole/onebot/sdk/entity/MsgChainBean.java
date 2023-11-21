package cn.evole.onebot.sdk.entity;

import lombok.Data;

import java.util.Map;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 9:10
 * Version: 1.0
 */
@Data
public class MsgChainBean {
    private String type;

    private Map<String, String> data;
}
