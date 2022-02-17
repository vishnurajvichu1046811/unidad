package com.utracx.api.model.rest.firebase;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FirebaseRequestBody {

    @SerializedName("username")
    @Expose
    String username;

    @Expose
    @SerializedName("password")
    String password;

    @Expose
    @SerializedName("registration_id")
    String firebaseID;

    public FirebaseRequestBody(@NonNull String username, @NonNull String password,
                               @NonNull String firebaseID) {
        this.username = username;
        this.password = password;
        this.firebaseID = firebaseID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirebaseID() {
        return firebaseID;
    }

    public void setFirebaseID(String firebaseID) {
        this.firebaseID = firebaseID;
    }
}
