package com.utracx.database.datamodel;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "nominatim_osm", primaryKeys = {"lat", "lon"})
public class NominatimEntitiy {

    @ColumnInfo(name = "lat")
    private double latitude;

    @ColumnInfo(name = "lon")
    private double longitude;

    @ColumnInfo(name = "address")
    private String resolvedAddress;

    public NominatimEntitiy(double latitude, double longitude, String resolvedAddress) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.resolvedAddress = resolvedAddress;
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

    public String getResolvedAddress() {
        return resolvedAddress;
    }

    public void setResolvedAddress(String resolvedAddress) {
        this.resolvedAddress = resolvedAddress;
    }
}
