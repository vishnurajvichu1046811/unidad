package com.utracx.model.map;

import android.os.Parcel;
import android.os.Parcelable;

public class InfoWindowViewModel implements Parcelable {
    public static final Creator<InfoWindowViewModel> CREATOR = new Creator<InfoWindowViewModel>() {
        @Override
        public InfoWindowViewModel createFromParcel(Parcel in) {
            return new InfoWindowViewModel(in);
        }

        @Override
        public InfoWindowViewModel[] newArray(int size) {
            return new InfoWindowViewModel[size];
        }
    };
    private long time;
    private double speed;
    private String address;

    public InfoWindowViewModel(long time, double speed, String address) {
        this.time = time;
        this.speed = speed;
        this.address = address;
    }

    private InfoWindowViewModel(Parcel in) {
        time = in.readLong();
        speed = in.readDouble();
        address = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(time);
        dest.writeDouble(speed);
        dest.writeString(address);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}