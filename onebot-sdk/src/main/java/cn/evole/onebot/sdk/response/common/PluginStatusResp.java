package cn.evole.onebot.sdk.response.common;

import com.google.gson.annotations.Expose;
import lombok.Data;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/11 19:58
 * Version: 1.0
 */
@Data
public class PluginStatusResp {
    @Expose
    boolean app_initialized =  true;
    @Expose boolean app_enabled =  true;
    PluginsGood plugins_good = new PluginsGood();
    @Expose boolean app_good =  true;
    @Expose boolean online =  true;
    @Expose boolean good = true;
}
