package com.utracx.api.model.osm.nominatim;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Address implements Parcelable {

    public final static Creator<Address> CREATOR = new Creator<Address>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        public Address[] newArray(int size) {
            return (new Address[size]);
        }

    };
    @SerializedName("county")
    @Expose
    private String county;
    @SerializedName("state_district")
    @Expose
    private String stateDistrict;
    @SerializedName("state")
    @Expose
    private String state;

    protected Address(Parcel in) {
        this.county = ((String) in.readValue((String.class.getClassLoader())));
        this.stateDistrict = ((String) in.readValue((String.class.getClassLoader())));
        this.state = ((String) in.readValue((String.class.getClassLoader())));
    }

    public Address() {
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getStateDistrict() {
        return stateDistrict;
    }

    public void setStateDistrict(String stateDistrict) {
        this.stateDistrict = stateDistrict;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(county);
        dest.writeValue(stateDistrict);
        dest.writeValue(state);
    }

    public int describeContents() {
        return 0;
    }

}
