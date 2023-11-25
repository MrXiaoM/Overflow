package cn.evole.onebot.sdk.response.common;

import cn.evole.onebot.sdk.action.ActionData;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/9 23:22
 * Version: 1.0
 */
public class AsyncStarted extends ActionData<String> {
    public AsyncStarted(){
        this.setRetCode(1);
        this.setStatus("async");
        this.setData(null);
    }
}
