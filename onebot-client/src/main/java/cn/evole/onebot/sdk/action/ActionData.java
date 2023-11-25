package cn.evole.onebot.sdk.action;


import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/13 22:05
 * Version: 1.0
 */
@Data
public class ActionData<T> {

    @SerializedName("status")
    private String status;
    @SerializedName("retcode")
    private int retCode;
    @SerializedName("data")
    private T data;
    @SerializedName("echo")
    private String echo;
}
