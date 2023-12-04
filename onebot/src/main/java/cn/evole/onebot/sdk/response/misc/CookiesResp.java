package cn.evole.onebot.sdk.response.misc;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * Created on 2023/12/4.
 *
 * @author MrXiaoM
 */
@Data
public class CookiesResp {

    @SerializedName("cookies")
    public String cookies;

}
