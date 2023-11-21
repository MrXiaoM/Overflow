package cn.evole.onebot.sdk.response.common;

import com.google.gson.annotations.Expose;
import lombok.Data;

/**
 * Description:
 * Author: cnlimiter
 * Date: 2022/10/12 13:59
 * Version: 1.0
 */
@Data
public class VersionInfo {

    @Expose
    String coolq_directory = "";
    @Expose String coolq_edition = "pro";
    @Expose String plugin_version = "0.2.0";
    @Expose int plugin_build_number = 99;
    @Expose String plugin_build_configuration = "release";
    @Expose String app_ = "onebot-sdk";
    @Expose String protocol_version = "v11";
}
