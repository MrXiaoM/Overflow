package cn.evole.onebot.sdk.entity;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Anonymous {

    @SerializedName( "id")
    public long id;

    @SerializedName( "name")
    public String name;

    @SerializedName( "flag")
    public String flag;

}
