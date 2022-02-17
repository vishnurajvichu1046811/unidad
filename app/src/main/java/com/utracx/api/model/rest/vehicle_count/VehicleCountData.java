package com.utracx.api.model.rest.vehicle_count;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VehicleCountData {

    @SerializedName("idle_count")
    @Expose
    private Integer sleepCount;

    @SerializedName("halt_count")
    @Expose
    private Integer haltCount;

    @Expose
    @SerializedName("running_count")
    private Integer movingCount;

    @Expose
    @SerializedName("inactive_count")
    private Integer inactiveCount;

    public Integer getSleepCount() {
        return sleepCount;
    }

    public void setSleepCount(Integer sleepCount) {
        this.sleepCount = sleepCount;
    }

    public Integer getHaltCount() {
        return haltCount;
    }

    public void setHaltCount(Integer haltCount) {
        this.haltCount = haltCount;
    }

    public Integer getMovingCount() {
        return movingCount;
    }

    public void setMovingCount(Integer movingCount) {
        this.movingCount = movingCount;
    }

    public Integer getInactiveCount() {
        return inactiveCount;
    }

    public void setInactiveCount(Integer inactiveCount) {
        this.inactiveCount = inactiveCount;
    }
}