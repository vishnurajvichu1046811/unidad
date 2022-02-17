package com.utracx.api.model.rest.vehicle_count;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VehicleCountResponseData {

    @SerializedName("code")
    @Expose
    private Integer code;

    @SerializedName("data")
    @Expose
    private VehicleCountData data;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public VehicleCountData getData() {
        return data;
    }

    public void setData(VehicleCountData data) {
        this.data = data;
    }

}
