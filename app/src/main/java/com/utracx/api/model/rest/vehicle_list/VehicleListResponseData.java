package com.utracx.api.model.rest.vehicle_list;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class VehicleListResponseData {

    @SerializedName("code")
    @Expose
    private Double code;

    @SerializedName("data")
    @Expose
    private ArrayList<VehicleInfo> data = null;

    public Double getCode() {
        return code;
    }

    public void setCode(Double code) {
        this.code = code;
    }

    public List<VehicleInfo> getData() {
        return data;
    }

    public void setData(List<VehicleInfo> data) {
        this.data = new ArrayList<>(data);
    }
}
