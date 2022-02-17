package com.utracx.api.model.rest.vehicleregister_update;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UpRegister implements Serializable {

    @SerializedName("vehicle_registration")
    @Expose
    private String vehicle_registration;

    public String getVehicle_registration() {
        return vehicle_registration;
    }

    public void setVehicle_registration(String vehicle_registration) {
        this.vehicle_registration = vehicle_registration;
    }
}
