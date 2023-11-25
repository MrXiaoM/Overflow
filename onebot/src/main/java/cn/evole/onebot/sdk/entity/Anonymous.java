package cn.evole.onebot.sdk.entity;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Anonymous {

    @SerializedName( "id")
    public long id;

    @SerializedName( "name")
    public String name;

    @SerializedName( "flag")
    public String flag;

}
