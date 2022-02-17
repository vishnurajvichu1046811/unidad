
package com.utracx.api.model.rest.search;

import java.io.Serializable;


import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Datum implements Serializable, Parcelable {

    @SerializedName("device_type_id")
    @Expose
    private Object deviceTypeId;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("fw_version")
    @Expose
    private Object fwVersion;
    @SerializedName("fw_update_status")
    @Expose
    private Object fwUpdateStatus;
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("imei_no")
    @Expose
    private String imeiNo;
    @SerializedName("vendor")
    @Expose
    private String vendor;
    @SerializedName("distributor")
    @Expose
    private String distributor;
    @SerializedName("dealer")
    @Expose
    private String dealer;
    @SerializedName("sub_dealer")
    @Expose
    private Object subDealer;
    @SerializedName("fleet")
    @Expose
    private Object fleet;
    @SerializedName("meta_d")
    @Expose
    private MetaD metaD;
    @SerializedName("active_ind")
    @Expose
    private Object activeInd;
    @SerializedName("last_updated_time")
    @Expose
    private Object lastUpdatedTime;
    @SerializedName("enable_notification_api")
    @Expose
    private Boolean enableNotificationApi;
    @SerializedName("device_type")
    @Expose
    private String deviceType;
    @SerializedName("device_type_name")
    @Expose
    private String deviceTypeName;
    @SerializedName("created_by")
    @Expose
    private String createdBy;
    @SerializedName("created_time")
    @Expose
    private Long createdTime;
    @SerializedName("modified_by")
    @Expose
    private String modifiedBy;
    @SerializedName("modified_time")
    @Expose
    private Object modifiedTime;
    public final static Creator<Datum> CREATOR = new Creator<Datum>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Datum createFromParcel(android.os.Parcel in) {
            return new Datum(in);
        }

        public Datum[] newArray(int size) {
            return (new Datum[size]);
        }

    };
    private final static long serialVersionUID = 4338701225597062435L;

    protected Datum(android.os.Parcel in) {
        this.deviceTypeId = ((Object) in.readValue((Object.class.getClassLoader())));
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.fwVersion = ((Object) in.readValue((Object.class.getClassLoader())));
        this.fwUpdateStatus = ((Object) in.readValue((Object.class.getClassLoader())));
        this.text = ((String) in.readValue((String.class.getClassLoader())));
        this.imeiNo = ((String) in.readValue((String.class.getClassLoader())));
        this.vendor = ((String) in.readValue((String.class.getClassLoader())));
        this.distributor = ((String) in.readValue((String.class.getClassLoader())));
        this.dealer = ((String) in.readValue((String.class.getClassLoader())));
        this.subDealer = ((Object) in.readValue((Object.class.getClassLoader())));
        this.fleet = ((Object) in.readValue((Object.class.getClassLoader())));
        this.metaD = ((MetaD) in.readValue((MetaD.class.getClassLoader())));
        this.activeInd = ((Object) in.readValue((Object.class.getClassLoader())));
        this.lastUpdatedTime = ((Object) in.readValue((Object.class.getClassLoader())));
        this.enableNotificationApi = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.deviceType = ((String) in.readValue((String.class.getClassLoader())));
        this.deviceTypeName = ((String) in.readValue((String.class.getClassLoader())));
        this.createdBy = ((String) in.readValue((String.class.getClassLoader())));
        this.createdTime = ((Long) in.readValue((Long.class.getClassLoader())));
        this.modifiedBy = ((String) in.readValue((String.class.getClassLoader())));
        this.modifiedTime = ((Object) in.readValue((Object.class.getClassLoader())));
    }

    public Datum() {
    }

    public Object getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(Object deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getFwVersion() {
        return fwVersion;
    }

    public void setFwVersion(Object fwVersion) {
        this.fwVersion = fwVersion;
    }

    public Object getFwUpdateStatus() {
        return fwUpdateStatus;
    }

    public void setFwUpdateStatus(Object fwUpdateStatus) {
        this.fwUpdateStatus = fwUpdateStatus;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImeiNo() {
        return imeiNo;
    }

    public void setImeiNo(String imeiNo) {
        this.imeiNo = imeiNo;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDistributor() {
        return distributor;
    }

    public void setDistributor(String distributor) {
        this.distributor = distributor;
    }

    public String getDealer() {
        return dealer;
    }

    public void setDealer(String dealer) {
        this.dealer = dealer;
    }

    public Object getSubDealer() {
        return subDealer;
    }

    public void setSubDealer(Object subDealer) {
        this.subDealer = subDealer;
    }

    public Object getFleet() {
        return fleet;
    }

    public void setFleet(Object fleet) {
        this.fleet = fleet;
    }

    public MetaD getMetaD() {
        return metaD;
    }

    public void setMetaD(MetaD metaD) {
        this.metaD = metaD;
    }

    public Object getActiveInd() {
        return activeInd;
    }

    public void setActiveInd(Object activeInd) {
        this.activeInd = activeInd;
    }

    public Object getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Object lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public Boolean getEnableNotificationApi() {
        return enableNotificationApi;
    }

    public void setEnableNotificationApi(Boolean enableNotificationApi) {
        this.enableNotificationApi = enableNotificationApi;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceTypeName() {
        return deviceTypeName;
    }

    public void setDeviceTypeName(String deviceTypeName) {
        this.deviceTypeName = deviceTypeName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Object getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Object modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Datum.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("deviceTypeId");
        sb.append('=');
        sb.append(((this.deviceTypeId == null) ? "<null>" : this.deviceTypeId));
        sb.append(',');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("fwVersion");
        sb.append('=');
        sb.append(((this.fwVersion == null) ? "<null>" : this.fwVersion));
        sb.append(',');
        sb.append("fwUpdateStatus");
        sb.append('=');
        sb.append(((this.fwUpdateStatus == null) ? "<null>" : this.fwUpdateStatus));
        sb.append(',');
        sb.append("text");
        sb.append('=');
        sb.append(((this.text == null) ? "<null>" : this.text));
        sb.append(',');
        sb.append("imeiNo");
        sb.append('=');
        sb.append(((this.imeiNo == null) ? "<null>" : this.imeiNo));
        sb.append(',');
        sb.append("vendor");
        sb.append('=');
        sb.append(((this.vendor == null) ? "<null>" : this.vendor));
        sb.append(',');
        sb.append("distributor");
        sb.append('=');
        sb.append(((this.distributor == null) ? "<null>" : this.distributor));
        sb.append(',');
        sb.append("dealer");
        sb.append('=');
        sb.append(((this.dealer == null) ? "<null>" : this.dealer));
        sb.append(',');
        sb.append("subDealer");
        sb.append('=');
        sb.append(((this.subDealer == null) ? "<null>" : this.subDealer));
        sb.append(',');
        sb.append("fleet");
        sb.append('=');
        sb.append(((this.fleet == null) ? "<null>" : this.fleet));
        sb.append(',');
        sb.append("metaD");
        sb.append('=');
        sb.append(((this.metaD == null) ? "<null>" : this.metaD));
        sb.append(',');
        sb.append("activeInd");
        sb.append('=');
        sb.append(((this.activeInd == null) ? "<null>" : this.activeInd));
        sb.append(',');
        sb.append("lastUpdatedTime");
        sb.append('=');
        sb.append(((this.lastUpdatedTime == null) ? "<null>" : this.lastUpdatedTime));
        sb.append(',');
        sb.append("enableNotificationApi");
        sb.append('=');
        sb.append(((this.enableNotificationApi == null) ? "<null>" : this.enableNotificationApi));
        sb.append(',');
        sb.append("deviceType");
        sb.append('=');
        sb.append(((this.deviceType == null) ? "<null>" : this.deviceType));
        sb.append(',');
        sb.append("deviceTypeName");
        sb.append('=');
        sb.append(((this.deviceTypeName == null) ? "<null>" : this.deviceTypeName));
        sb.append(',');
        sb.append("createdBy");
        sb.append('=');
        sb.append(((this.createdBy == null) ? "<null>" : this.createdBy));
        sb.append(',');
        sb.append("createdTime");
        sb.append('=');
        sb.append(((this.createdTime == null) ? "<null>" : this.createdTime));
        sb.append(',');
        sb.append("modifiedBy");
        sb.append('=');
        sb.append(((this.modifiedBy == null) ? "<null>" : this.modifiedBy));
        sb.append(',');
        sb.append("modifiedTime");
        sb.append('=');
        sb.append(((this.modifiedTime == null) ? "<null>" : this.modifiedTime));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',') {
            sb.setCharAt((sb.length() - 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result * 31) + ((this.deviceType == null) ? 0 : this.deviceType.hashCode()));
        result = ((result * 31) + ((this.fleet == null) ? 0 : this.fleet.hashCode()));
        result = ((result * 31) + ((this.modifiedTime == null) ? 0 : this.modifiedTime.hashCode()));
        result = ((result * 31) + ((this.deviceTypeId == null) ? 0 : this.deviceTypeId.hashCode()));
        result = ((result * 31) + ((this.subDealer == null) ? 0 : this.subDealer.hashCode()));
        result = ((result * 31) + ((this.deviceTypeName == null) ? 0 : this.deviceTypeName.hashCode()));
        result = ((result * 31) + ((this.distributor == null) ? 0 : this.distributor.hashCode()));
        result = ((result * 31) + ((this.metaD == null) ? 0 : this.metaD.hashCode()));
        result = ((result * 31) + ((this.fwUpdateStatus == null) ? 0 : this.fwUpdateStatus.hashCode()));
        result = ((result * 31) + ((this.imeiNo == null) ? 0 : this.imeiNo.hashCode()));
        result = ((result * 31) + ((this.createdBy == null) ? 0 : this.createdBy.hashCode()));
        result = ((result * 31) + ((this.vendor == null) ? 0 : this.vendor.hashCode()));
        result = ((result * 31) + ((this.activeInd == null) ? 0 : this.activeInd.hashCode()));
        result = ((result * 31) + ((this.dealer == null) ? 0 : this.dealer.hashCode()));
        result = ((result * 31) + ((this.createdTime == null) ? 0 : this.createdTime.hashCode()));
        result = ((result * 31) + ((this.fwVersion == null) ? 0 : this.fwVersion.hashCode()));
        result = ((result * 31) + ((this.lastUpdatedTime == null) ? 0 : this.lastUpdatedTime.hashCode()));
        result = ((result * 31) + ((this.modifiedBy == null) ? 0 : this.modifiedBy.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.text == null) ? 0 : this.text.hashCode()));
        result = ((result * 31) + ((this.enableNotificationApi == null) ? 0 : this.enableNotificationApi.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Datum) == false) {
            return false;
        }
        Datum rhs = ((Datum) other);
        return ((((((((((((((((((((((this.deviceType == rhs.deviceType) || ((this.deviceType != null) && this.deviceType.equals(rhs.deviceType))) && ((this.fleet == rhs.fleet) || ((this.fleet != null) && this.fleet.equals(rhs.fleet)))) && ((this.modifiedTime == rhs.modifiedTime) || ((this.modifiedTime != null) && this.modifiedTime.equals(rhs.modifiedTime)))) && ((this.deviceTypeId == rhs.deviceTypeId) || ((this.deviceTypeId != null) && this.deviceTypeId.equals(rhs.deviceTypeId)))) && ((this.subDealer == rhs.subDealer) || ((this.subDealer != null) && this.subDealer.equals(rhs.subDealer)))) && ((this.deviceTypeName == rhs.deviceTypeName) || ((this.deviceTypeName != null) && this.deviceTypeName.equals(rhs.deviceTypeName)))) && ((this.distributor == rhs.distributor) || ((this.distributor != null) && this.distributor.equals(rhs.distributor)))) && ((this.metaD == rhs.metaD) || ((this.metaD != null) && this.metaD.equals(rhs.metaD)))) && ((this.fwUpdateStatus == rhs.fwUpdateStatus) || ((this.fwUpdateStatus != null) && this.fwUpdateStatus.equals(rhs.fwUpdateStatus)))) && ((this.imeiNo == rhs.imeiNo) || ((this.imeiNo != null) && this.imeiNo.equals(rhs.imeiNo)))) && ((this.createdBy == rhs.createdBy) || ((this.createdBy != null) && this.createdBy.equals(rhs.createdBy)))) && ((this.vendor == rhs.vendor) || ((this.vendor != null) && this.vendor.equals(rhs.vendor)))) && ((this.activeInd == rhs.activeInd) || ((this.activeInd != null) && this.activeInd.equals(rhs.activeInd)))) && ((this.dealer == rhs.dealer) || ((this.dealer != null) && this.dealer.equals(rhs.dealer)))) && ((this.createdTime == rhs.createdTime) || ((this.createdTime != null) && this.createdTime.equals(rhs.createdTime)))) && ((this.fwVersion == rhs.fwVersion) || ((this.fwVersion != null) && this.fwVersion.equals(rhs.fwVersion)))) && ((this.lastUpdatedTime == rhs.lastUpdatedTime) || ((this.lastUpdatedTime != null) && this.lastUpdatedTime.equals(rhs.lastUpdatedTime)))) && ((this.modifiedBy == rhs.modifiedBy) || ((this.modifiedBy != null) && this.modifiedBy.equals(rhs.modifiedBy)))) && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id)))) && ((this.text == rhs.text) || ((this.text != null) && this.text.equals(rhs.text)))) && ((this.enableNotificationApi == rhs.enableNotificationApi) || ((this.enableNotificationApi != null) && this.enableNotificationApi.equals(rhs.enableNotificationApi))));
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeValue(deviceTypeId);
        dest.writeValue(id);
        dest.writeValue(fwVersion);
        dest.writeValue(fwUpdateStatus);
        dest.writeValue(text);
        dest.writeValue(imeiNo);
        dest.writeValue(vendor);
        dest.writeValue(distributor);
        dest.writeValue(dealer);
        dest.writeValue(subDealer);
        dest.writeValue(fleet);
        dest.writeValue(metaD);
        dest.writeValue(activeInd);
        dest.writeValue(lastUpdatedTime);
        dest.writeValue(enableNotificationApi);
        dest.writeValue(deviceType);
        dest.writeValue(deviceTypeName);
        dest.writeValue(createdBy);
        dest.writeValue(createdTime);
        dest.writeValue(modifiedBy);
        dest.writeValue(modifiedTime);
    }

    public int describeContents() {
        return 0;
    }

}
