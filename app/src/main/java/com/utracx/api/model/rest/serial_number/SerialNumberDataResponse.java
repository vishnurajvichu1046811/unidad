package com.utracx.api.model.rest.serial_number;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SerialNumberDataResponse {

    @SerializedName("code")
    @Expose
    private Integer code;

    @SerializedName("data")
    @Expose
    private SerialNumberData data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public SerialNumberData getData() {
        return data;
    }

    public void setData(SerialNumberData data) {
        this.data = data;
    }

}