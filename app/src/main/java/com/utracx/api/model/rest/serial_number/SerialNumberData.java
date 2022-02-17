package com.utracx.api.model.rest.serial_number;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SerialNumberData {


    @SerializedName("serial_no")
    @Expose
    private String serialNo;

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

}