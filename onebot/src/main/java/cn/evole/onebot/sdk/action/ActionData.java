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
    public String status;
    @SerializedName("retcode")
    public int retCode;
    @SerializedName("data")
    public T data;
    @SerializedName("echo")
    public String echo;
}
