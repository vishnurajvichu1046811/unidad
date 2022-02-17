
package com.utracx.api.model.rest.search;

import java.io.Serializable;
import android.os.Parcelable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MetaD implements Serializable, Parcelable
{

    @SerializedName("fleet")
    @Expose
    private Object fleet;
    @SerializedName("vehicle_registration")
    @Expose
    private String vehicleRegistration;
    @SerializedName("vendor")
    @Expose
    private String vendor;
    @SerializedName("dealer")
    @Expose
    private String dealer;
    @SerializedName("distributor")
    @Expose
    private String distributor;
    public final static Creator<MetaD> CREATOR = new Creator<MetaD>() {


        @SuppressWarnings({
            "unchecked"
        })
        public MetaD createFromParcel(android.os.Parcel in) {
            return new MetaD(in);
        }

        public MetaD[] newArray(int size) {
            return (new MetaD[size]);
        }

    }
    ;
    private final static long serialVersionUID = 673799314877497579L;

    protected MetaD(android.os.Parcel in) {
        this.fleet = ((Object) in.readValue((Object.class.getClassLoader())));
        this.vehicleRegistration = ((String) in.readValue((String.class.getClassLoader())));
        this.vendor = ((String) in.readValue((String.class.getClassLoader())));
        this.dealer = ((String) in.readValue((String.class.getClassLoader())));
        this.distributor = ((String) in.readValue((String.class.getClassLoader())));
    }

    public MetaD() {
    }

    public Object getFleet() {
        return fleet;
    }

    public void setFleet(Object fleet) {
        this.fleet = fleet;
    }

    public String getVehicleRegistration() {
        return vehicleRegistration;
    }

    public void setVehicleRegistration(String vehicleRegistration) {
        this.vehicleRegistration = vehicleRegistration;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDealer() {
        return dealer;
    }

    public void setDealer(String dealer) {
        this.dealer = dealer;
    }

    public String getDistributor() {
        return distributor;
    }

    public void setDistributor(String distributor) {
        this.distributor = distributor;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(MetaD.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("fleet");
        sb.append('=');
        sb.append(((this.fleet == null)?"<null>":this.fleet));
        sb.append(',');
        sb.append("vehicleRegistration");
        sb.append('=');
        sb.append(((this.vehicleRegistration == null)?"<null>":this.vehicleRegistration));
        sb.append(',');
        sb.append("vendor");
        sb.append('=');
        sb.append(((this.vendor == null)?"<null>":this.vendor));
        sb.append(',');
        sb.append("dealer");
        sb.append('=');
        sb.append(((this.dealer == null)?"<null>":this.dealer));
        sb.append(',');
        sb.append("distributor");
        sb.append('=');
        sb.append(((this.distributor == null)?"<null>":this.distributor));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+((this.dealer == null)? 0 :this.dealer.hashCode()));
        result = ((result* 31)+((this.fleet == null)? 0 :this.fleet.hashCode()));
        result = ((result* 31)+((this.vehicleRegistration == null)? 0 :this.vehicleRegistration.hashCode()));
        result = ((result* 31)+((this.distributor == null)? 0 :this.distributor.hashCode()));
        result = ((result* 31)+((this.vendor == null)? 0 :this.vendor.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MetaD) == false) {
            return false;
        }
        MetaD rhs = ((MetaD) other);
        return ((((((this.dealer == rhs.dealer)||((this.dealer!= null)&&this.dealer.equals(rhs.dealer)))&&((this.fleet == rhs.fleet)||((this.fleet!= null)&&this.fleet.equals(rhs.fleet))))&&((this.vehicleRegistration == rhs.vehicleRegistration)||((this.vehicleRegistration!= null)&&this.vehicleRegistration.equals(rhs.vehicleRegistration))))&&((this.distributor == rhs.distributor)||((this.distributor!= null)&&this.distributor.equals(rhs.distributor))))&&((this.vendor == rhs.vendor)||((this.vendor!= null)&&this.vendor.equals(rhs.vendor))));
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeValue(fleet);
        dest.writeValue(vehicleRegistration);
        dest.writeValue(vendor);
        dest.writeValue(dealer);
        dest.writeValue(distributor);
    }

    public int describeContents() {
        return  0;
    }

}
