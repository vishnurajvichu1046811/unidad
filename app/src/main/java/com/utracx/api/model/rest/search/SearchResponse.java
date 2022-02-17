
package com.utracx.api.model.rest.search;

import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SearchResponse implements Serializable, Parcelable {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("data")
    @Expose
    private List<Datum> data = null;
    public final static Creator<SearchResponse> CREATOR = new Creator<SearchResponse>() {


        @SuppressWarnings({
                "unchecked"
        })
        public SearchResponse createFromParcel(android.os.Parcel in) {
            return new SearchResponse(in);
        }

        public SearchResponse[] newArray(int size) {
            return (new SearchResponse[size]);
        }

    };
    private final static long serialVersionUID = -4048591897726448193L;

    protected SearchResponse(android.os.Parcel in) {
        this.status = ((String) in.readValue((String.class.getClassLoader())));
        this.code = ((Integer) in.readValue((Integer.class.getClassLoader())));
        in.readList(this.data, (Datum.class.getClassLoader()));
    }

    public SearchResponse() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public List<Datum> getData() {
        return data;
    }

    public void setData(List<Datum> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SearchResponse.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null) ? "<null>" : this.status));
        sb.append(',');
        sb.append("code");
        sb.append('=');
        sb.append(((this.code == null) ? "<null>" : this.code));
        sb.append(',');
        sb.append("data");
        sb.append('=');
        sb.append(((this.data == null) ? "<null>" : this.data));
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
        result = ((result * 31) + ((this.code == null) ? 0 : this.code.hashCode()));
        result = ((result * 31) + ((this.data == null) ? 0 : this.data.hashCode()));
        result = ((result * 31) + ((this.status == null) ? 0 : this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SearchResponse) == false) {
            return false;
        }
        SearchResponse rhs = ((SearchResponse) other);
        return ((((this.code == rhs.code) || ((this.code != null) && this.code.equals(rhs.code))) && ((this.data == rhs.data) || ((this.data != null) && this.data.equals(rhs.data)))) && ((this.status == rhs.status) || ((this.status != null) && this.status.equals(rhs.status))));
    }

    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeValue(status);
        dest.writeValue(code);
        dest.writeList(data);
    }

    public int describeContents() {
        return 0;
    }

}
