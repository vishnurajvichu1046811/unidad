package com.utracx.api.model.rest.about;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AboutResponseData implements Parcelable {

    public final static Parcelable.Creator<AboutResponseData> CREATOR = new Creator<AboutResponseData>() {


        @SuppressWarnings({
                "unchecked"
        })
        public AboutResponseData createFromParcel(Parcel in) {
            return new AboutResponseData(in);
        }

        public AboutResponseData[] newArray(int size) {
            return (new AboutResponseData[size]);
        }

    };

    @SerializedName("privacy_policy_url")
    @Expose
    private String privacyPolicyUrl;
    @SerializedName("terms_conditions_url")
    @Expose
    private String termsConditionsUrl;
    @SerializedName("homepage_url")
    @Expose
    private String homepageUrl;
    @SerializedName("contact_us_url")
    @Expose
    private String contactUsUrl;

    protected AboutResponseData(Parcel in) {
        this.privacyPolicyUrl = ((String) in.readValue((String.class.getClassLoader())));
        this.termsConditionsUrl = ((String) in.readValue((String.class.getClassLoader())));
        this.homepageUrl = ((String) in.readValue((String.class.getClassLoader())));
        this.contactUsUrl = ((String) in.readValue((String.class.getClassLoader())));
    }

    public AboutResponseData(@NonNull String homepageUrl, @NonNull String contactUsUrl,
                             @NonNull String privacyPolicyUrl, @NonNull String termsConditionsUrl) {
        this.homepageUrl = homepageUrl;
        this.contactUsUrl = contactUsUrl;
        this.privacyPolicyUrl = privacyPolicyUrl;
        this.termsConditionsUrl = termsConditionsUrl;
    }

    public String getPrivacyPolicyUrl() {
        return privacyPolicyUrl;
    }

    public void setPrivacyPolicyUrl(String privacyPolicyUrl) {
        this.privacyPolicyUrl = privacyPolicyUrl;
    }

    public String getTermsConditionsUrl() {
        return termsConditionsUrl;
    }

    public void setTermsConditionsUrl(String termsConditionsUrl) {
        this.termsConditionsUrl = termsConditionsUrl;
    }

    public String getHomepageUrl() {
        return homepageUrl;
    }

    public void setHomepageUrl(String homepageUrl) {
        this.homepageUrl = homepageUrl;
    }

    public String getContactUsUrl() {
        return contactUsUrl;
    }

    public void setContactUsUrl(String contactUsUrl) {
        this.contactUsUrl = contactUsUrl;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(privacyPolicyUrl);
        dest.writeValue(termsConditionsUrl);
        dest.writeValue(homepageUrl);
        dest.writeValue(contactUsUrl);
    }

    public int describeContents() {
        return 0;
    }

}
