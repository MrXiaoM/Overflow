package cn.evole.onebot.sdk.entity;

import cn.evole.onebot.sdk.enums.MsgTypeEnum;
import lombok.Data;

import java.util.Map;

/**
 * Project: onebot-sdk
 * Author: cnlimiter
 * Date: 2023/2/10 1:30
 * Description:
 */
@Data
public class ArrayMsg {
    public MsgTypeEnum type;

    public Map<String, String> data;
}
