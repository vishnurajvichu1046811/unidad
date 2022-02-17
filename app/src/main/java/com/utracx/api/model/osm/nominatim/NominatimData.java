package com.utracx.api.model.osm.nominatim;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NominatimData implements Parcelable {

    public final static Creator<NominatimData> CREATOR = new Creator<NominatimData>() {

        @SuppressWarnings({
                "unchecked"
        })
        public NominatimData createFromParcel(Parcel in) {
            return new NominatimData(in);
        }

        public NominatimData[] newArray(int size) {
            return (new NominatimData[size]);
        }

    };

    @SerializedName("lat")
    @Expose
    private String lat;

    @SerializedName("lon")
    @Expose
    private String lon;

    @SerializedName("postcode")
    @Expose
    private String postcode;

    @SerializedName("display_name")
    @Expose
    private String displayName;

    @SerializedName("address")
    @Expose
    private Address address;

    private NominatimData(Parcel in) {
        this.lat = in.readString();
        this.lon = in.readString();
        this.postcode = in.readString();
        this.displayName = in.readString();
        this.address = ((Address) in.readValue((Address.class.getClassLoader())));
    }

    public NominatimData() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(lat);
        dest.writeValue(lon);
        dest.writeValue(postcode);
        dest.writeValue(displayName);
        dest.writeValue(address);
    }

    public int describeContents() {
        return 0;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }
}
