package cn.evole.onebot.sdk.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Anonymous {

    @SerializedName( "id")
    private long id;

    @SerializedName( "name")
    private String name;

    @SerializedName( "flag")
    private String flag;

}
