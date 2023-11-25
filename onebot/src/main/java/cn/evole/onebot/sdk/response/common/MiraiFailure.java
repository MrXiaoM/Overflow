package cn.evole.onebot.sdk.response.common;

import cn.evole.onebot.sdk.action.ActionData;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/9 23:26
 * Version: 1.0
 */
public class MiraiFailure extends ActionData<String> {
    public MiraiFailure(){
        this.setStatus("failed");
        this.setRetCode(102);
        this.setData(null);
    }
}
