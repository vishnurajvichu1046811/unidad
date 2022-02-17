package com.utracx.view.adapter.data;

import com.utracx.api.model.rest.vehicle_list.LastUpdatedData;

public class TripSummary {

    private double averageSpeed;
    private long startTime;
    private long endTime;
    private long duration;
    private double latitude;
    private double longitude;
    private String address;
    private double distance;
    private LastUpdatedData initialData;
    private LastUpdatedData lastUpdatedData;
    private String vehicleMode;

    public TripSummary() {
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVehicleMode() {
        return vehicleMode;
    }

    public void setVehicleMode(String vehicleMode) {
        this.vehicleMode = vehicleMode;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public LastUpdatedData getLastUpdatedData() {
        return lastUpdatedData;
    }

    public void setLastUpdatedData(LastUpdatedData lastUpdatedData) {
        this.lastUpdatedData = lastUpdatedData;
    }

    public LastUpdatedData getInitialData() {
        return initialData;
    }

    public void setInitialData(LastUpdatedData initialData) {
        this.initialData = initialData;
    }
}
