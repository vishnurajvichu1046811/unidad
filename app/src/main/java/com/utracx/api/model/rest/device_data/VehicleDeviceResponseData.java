package com.utracx.api.model.rest.device_data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class VehicleDeviceResponseData {

    @SerializedName("code")
    @Expose
    private Double code;

    @SerializedName("data")
    @Expose
    private ArrayList<DeviceData> data = null;

    public Double getCode() {
        return code;
    }

    public void setCode(Double code) {
        this.code = code;
    }

    public ArrayList<DeviceData> getData() {
        return data;
    }

    public void setData(ArrayList<DeviceData> data) {
        this.data = data;
    }

}