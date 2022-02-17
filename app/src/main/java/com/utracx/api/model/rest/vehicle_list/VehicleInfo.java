package com.utracx.api.model.rest.vehicle_list;

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

import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity(tableName = "vehicles_table")
public class  VehicleInfo implements Parcelable {

    @Ignore
    public static final Creator<VehicleInfo> CREATOR = new Creator<VehicleInfo>() {
        @Override
        public VehicleInfo createFromParcel(Parcel in) {
            return new VehicleInfo(in);
        }

        @Override
        public VehicleInfo[] newArray(int size) {
            return new VehicleInfo[size];
        }
    };

    @SerializedName("id")
    @Expose
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @SerializedName("last_updated_data")
    @Expose
    @Embedded
    private LastUpdatedData lastUpdatedData;

    @Ignore
    public boolean isEditClicked;

    @SerializedName("vehicle_registration")
    @Expose
    @ColumnInfo(name = "vehicle_registration")
    private String vehicleRegistration;

    @SerializedName("vehicle_type")
    @Expose
    @ColumnInfo(name = "vehicle_type")
    private String vehicleType;

    @SerializedName("emergency_alert_count")
    @Expose
    @ColumnInfo(name = "emergency_alert_count")
    private int emergencyAlertCount;

    @SerializedName("alert_count")
    @Expose
    @ColumnInfo(name = "alert_count")
    private int alertCount;

    @SerializedName("overspeed_alert_count")
    @Expose
    @ColumnInfo(name = "overspeed_alert_count")
    private int overspeedAlertCount;

    @SerializedName("main_power_removal_alert_count")
    @Expose
    @ColumnInfo(name = "main_power_removal_alert_count")
    private int mainPowerRemovalAlertCount;

    @SerializedName("wire_cut_alert_count")
    @Expose
    @ColumnInfo(name = "wire_cut_alert_count")
    private int wireCutAlertCount;

    @SerializedName("route_required")
    @Expose
    @ColumnInfo(name = "route_required")
    private boolean routeRequired = false;
    @Ignore
    private int othersAlertCount;

    @SerializedName("use_google_api")
    @Expose
    @ColumnInfo(name = "use_google_api")
    private boolean useGoogleApi;


    public VehicleInfo() {
    }

    @Ignore
    protected VehicleInfo(@NonNull Parcel in) {
        this.id = in.readString();
        this.lastUpdatedData = in.readParcelable(LastUpdatedData.class.getClassLoader());
        this.vehicleRegistration = in.readString();
        this.vehicleType = in.readString();

        this.emergencyAlertCount = in.readInt();
        this.alertCount = in.readInt();
        this.overspeedAlertCount = in.readInt();
        this.wireCutAlertCount = in.readInt();
        this.othersAlertCount = in.readInt();
        this.mainPowerRemovalAlertCount = in.readInt();
        this.routeRequired = in.readInt() == 1;
        this.useGoogleApi = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
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
        dest.writeParcelable(lastUpdatedData, flags);
        dest.writeString(vehicleRegistration);
        dest.writeString(vehicleType);
        dest.writeInt(emergencyAlertCount);
        dest.writeInt(alertCount);
        dest.writeInt(overspeedAlertCount);
        dest.writeInt(wireCutAlertCount);
        dest.writeInt(othersAlertCount);
        dest.writeInt(mainPowerRemovalAlertCount);
        dest.writeInt(routeRequired ? 1 : -1);
        dest.writeValue(useGoogleApi);
    }

    @NotNull
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LastUpdatedData getLastUpdatedData() {
        return lastUpdatedData;
    }

    public void setLastUpdatedData(LastUpdatedData d) {
        this.lastUpdatedData = d;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public int getEmergencyAlertCount() {
        return emergencyAlertCount;
    }

    public void setEmergencyAlertCount(int emergencyAlertCount) {
        this.emergencyAlertCount = emergencyAlertCount;
    }

    public int getOverspeedAlertCount() {
        return overspeedAlertCount;
    }

    public void setOverspeedAlertCount(int overspeedAlertCount) {
        this.overspeedAlertCount = overspeedAlertCount;
    }

    public int getMainPowerRemovalAlertCount() {
        return mainPowerRemovalAlertCount;
    }

    public void setMainPowerRemovalAlertCount(int mainPowerRemovalAlertCount) {
        this.mainPowerRemovalAlertCount = mainPowerRemovalAlertCount;
    }

    public int getWireCutAlertCount() {
        return wireCutAlertCount;
    }

    public void setWireCutAlertCount(int wireCutAlertCount) {
        this.wireCutAlertCount = wireCutAlertCount;
    }

    public int getOthersAlertCount() {
        return othersAlertCount;
    }

    public void setOthersCount(int othersAlertCount) {
        this.othersAlertCount = othersAlertCount;
    }

    public boolean isRouteRequired() {
        return routeRequired;
    }

    public void setRouteRequired(boolean routeRequired) {
        this.routeRequired = routeRequired;
    }

    public int getAlertCount() {
        return alertCount;
    }

    public void setAlertCount(int alertCount) {
        this.alertCount = alertCount;
    }

    public boolean getUseGoogleApi() {
        return useGoogleApi;
    }

    public void setUseGoogleApi(boolean useGoogleApi) {
        this.useGoogleApi = useGoogleApi;
    }
}