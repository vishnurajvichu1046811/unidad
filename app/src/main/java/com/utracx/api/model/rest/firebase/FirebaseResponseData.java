package com.utracx.api.model.rest.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FirebaseResponseData {

    @SerializedName("code")
    @Expose
    private Integer code;

    @SerializedName("data")
    @Expose
    private Object data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
