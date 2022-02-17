package com.utracx.model.map;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class CustomMapPoint implements Parcelable {
    public static final Creator<CustomMapPoint> CREATOR = new Creator<CustomMapPoint>() {
        @Override
        public CustomMapPoint createFromParcel(Parcel in) {
            return new CustomMapPoint(in);
        }

        @Override
        public CustomMapPoint[] newArray(int size) {
            return new CustomMapPoint[size];
        }
    };
    private LatLng mapPoint;
    private boolean isMarkerPoint;
    private InfoWindowViewModel infoWindow;
    private String markerType;
    private double speed;

    public CustomMapPoint(LatLng mapPoint, InfoWindowViewModel infoWindow, Boolean isMarkerPoint, String markerType) {
        this.mapPoint = mapPoint;
        this.infoWindow = infoWindow;
        this.isMarkerPoint = isMarkerPoint;
        this.markerType = markerType;
        this.speed = speed;
    }

    private CustomMapPoint(Parcel in) {
        this.mapPoint = in.readParcelable(LatLng.class.getClassLoader());
        this.infoWindow = in.readParcelable(InfoWindowViewModel.class.getClassLoader());
        this.isMarkerPoint = in.readByte() != 0;
        this.markerType = in.readString();
        this.speed = in.readByte();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mapPoint, flags);
        dest.writeParcelable(infoWindow, flags);
        dest.writeByte((byte) (isMarkerPoint ? 1 : 0));
        dest.writeString(markerType);
        dest.writeDouble(speed);
    }

    public LatLng getMapPoint() {
        return mapPoint;
    }

    public void setMapPoint(LatLng mapPoint) {
        this.mapPoint = mapPoint;
    }

    public InfoWindowViewModel getInfoWindow() {
        return infoWindow;
    }

    public void setInfoWindow(InfoWindowViewModel infoWindow) {
        this.infoWindow = infoWindow;
    }

    public boolean isMarkerPoint() {
        return isMarkerPoint;
    }

    public void setMarkerPoint(boolean markerPoint) {
        isMarkerPoint = markerPoint;
    }

    public String getMarkerType() {
        return markerType;
    }

    public void setMarkerType(String markerType) {
        this.markerType = markerType;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
