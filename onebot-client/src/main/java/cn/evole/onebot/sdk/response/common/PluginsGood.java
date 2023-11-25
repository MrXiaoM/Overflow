package cn.evole.onebot.sdk.response.common;

import com.google.gson.annotations.Expose;
import lombok.Data;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/12 14:04
 * Version: 1.0
 */
@Data
public class PluginsGood {
   @Expose
   boolean websocket  = true;
   @Expose
   boolean eventDataPatcher  = true;

}
