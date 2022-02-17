package com.utracx.api.model.rest.device_data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.utracx.api.model.rest.vehicle_list.AlertHistoryData;
import com.utracx.api.model.rest.vehicle_list.LastUpdatedData;

import java.util.Objects;

@Entity(tableName = "device_updates")
public class DeviceData implements Parcelable {

    @Ignore
    public static final Creator<DeviceData> CREATOR = new Creator<DeviceData>() {
        @Override
        public DeviceData createFromParcel(Parcel in) {
            return new DeviceData(in);
        }

        @Override
        public DeviceData[] newArray(int size) {
            return new DeviceData[size];
        }
    };
    @SerializedName("id")
    @Expose
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    private String id;
    @SerializedName("d")
    @Expose
    @Embedded
    private LastUpdatedData d;


    public DeviceData(@NonNull String id) {
        this.id = id;
    }

    @Ignore
    protected DeviceData(@NonNull Parcel in) {
        id = Objects.requireNonNull(in.readString());
        d = in.readParcelable(LastUpdatedData.class.getClassLoader());

    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public LastUpdatedData getD() {
        return d;
    }

    public void setD(LastUpdatedData d) {
        this.d = d;
    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeParcelable(d, flags);
    }

    @Ignore
    @Override
    public boolean equals(Object objectToCompare) {
        if (objectToCompare == null || getClass() != objectToCompare.getClass()) {
            return false;
        }

        if (this == objectToCompare) {
            return true;
        }

        DeviceData that = (DeviceData) objectToCompare;

        return this.id.equalsIgnoreCase(that.id);
    }

    @Ignore
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
