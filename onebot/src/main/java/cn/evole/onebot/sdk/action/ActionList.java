package cn.evole.onebot.sdk.action;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.LinkedList;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/9/14 17:40
 * Version: 1.0
 */
@Data
public class ActionList<T>{

    @SerializedName("status")
    public String status;

    @SerializedName("retcode")
    public int retCode;

    @SerializedName("data")
    public LinkedList<T> data;

}
