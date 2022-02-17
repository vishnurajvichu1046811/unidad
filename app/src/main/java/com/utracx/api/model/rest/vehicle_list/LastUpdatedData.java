package com.utracx.api.model.rest.vehicle_list;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Ignore;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import static com.utracx.util.ConstantVariables.SPEED_ROUND;
import static com.utracx.util.helper.MathHelper.round;

public class LastUpdatedData implements Parcelable {

    @Ignore
    public transient static final Creator<LastUpdatedData> CREATOR = new Creator<LastUpdatedData>() {
        @Override
        public LastUpdatedData createFromParcel(Parcel in) {
            return new LastUpdatedData(in);
        }

        @Override
        public LastUpdatedData[] newArray(int size) {
            return new LastUpdatedData[size];
        }
    };

    @ColumnInfo(name = "address")
    private transient String address;

    @SerializedName("ignition")
    @Expose
    @ColumnInfo(name = "ignition")
    private String ignition;

    @SerializedName("vehicle_mode")
    @Expose
    @ColumnInfo(name = "vehicle_mode")
    private String vehicleMode;

    @SerializedName("latitude")
    @Expose
    @ColumnInfo(name = "latitude")
    private Double latitude;

    @SerializedName("gnss_fix")
    @Expose
    @ColumnInfo(name = "gnss_fix")
    private Integer gnssFix;

    @SerializedName("gsm_signal_strength")
    @Expose
    @ColumnInfo(name = "gsm_signal_strength")
    private Integer gsmSignalStrength;

    @SerializedName("speed")
    @Expose
    @ColumnInfo(name = "speed")
    private Double speed;

    @SerializedName("longitude")
    @Expose
    @ColumnInfo(name = "longitude")
    private Double longitude;

    @SerializedName("source_date")
    @Expose
    @ColumnInfo(name = "source_date")
    private Long sourceDate;

    @SerializedName("packet_type")
    @Expose
    @ColumnInfo(name = "packet_type")
    private String packetType;

    @SerializedName("serial_no")
    @Expose
    @ColumnInfo(name = "serial_no")
    private String serialNumber;

    @SerializedName("imei_no")
    @Expose
    @ColumnInfo(name = "imei_no")
    private String imeiNo;

    public LastUpdatedData() {
    }

    @Ignore
    private LastUpdatedData(Parcel in) {
        address = in.readString();
        ignition = in.readString();
        vehicleMode = in.readString();
        latitude = in.readDouble();
        if (in.readByte() == 0) {
            gnssFix = null;
        } else {
            gnssFix = in.readInt();
        }
        speed = in.readDouble();
        longitude = in.readDouble();
        if (in.readByte() == 0) {
            sourceDate = null;
        } else {
            sourceDate = in.readLong();
        }
        packetType = in.readString();
        serialNumber = in.readString();
        imeiNo = in.readString();
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(ignition);
        dest.writeString(vehicleMode);
        dest.writeDouble(latitude);
        if (gnssFix == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(gnssFix == null ? -1 : gnssFix);
        }

        dest.writeDouble(speed == null ? 0.0 : speed);
        dest.writeDouble(longitude);
        if (sourceDate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(sourceDate == null ? 0L : sourceDate);
        }
        dest.writeString(packetType);
        dest.writeString(serialNumber);
        dest.writeString(imeiNo);
    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getGnssFix() {
        return gnssFix;
    }

    public void setGnssFix(Integer gnssFix) {
        this.gnssFix = gnssFix;
    }

    public Double getSpeed() {
        return round(speed, SPEED_ROUND);
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getIgnition() {
        return ignition;
    }

    public void setIgnition(String ignition) {
        this.ignition = ignition;
    }

    public Long getSourceDate() {
        return sourceDate;
    }

    public void setSourceDate(Long sourceDate) {
        this.sourceDate = sourceDate;
    }

    public Integer getGsmSignalStrength() {
        return gsmSignalStrength;
    }

    public void setGsmSignalStrength(Integer gsmSignalStrength) {
        this.gsmSignalStrength = gsmSignalStrength;
    }

    public String getPacketType() {
        return packetType;
    }

    public void setPacketType(String packetType) {
        this.packetType = packetType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getImeiNo() {
        return imeiNo;
    }

    public void setImeiNo(String imeiNo) {
        this.imeiNo = imeiNo;
    }
}