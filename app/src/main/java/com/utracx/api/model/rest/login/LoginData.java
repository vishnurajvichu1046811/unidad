package com.utracx.api.model.rest.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginData implements Serializable {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("mobile")
    @Expose
    private String mobile;

    @SerializedName("full_name")
    @Expose
    private String fullname;

    @SerializedName("address")
    @Expose
    private String address;

    @SerializedName("email")
    @Expose
    private String email;

    @SerializedName("password")
    @Expose
    private String password;

    @SerializedName("role_code")
    @Expose
    private String role_code;

    @SerializedName("vehicle_registration")
    @Expose
    private String vehicle_registration;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRole_code() {
        return role_code;
    }

    public void setRole_code(String role_code) {
        this.role_code = role_code;
    }

    public String getVehicle_registration() {
        return vehicle_registration;
    }

    public void setVehicle_registration(String vehicle_registration) {
        this.vehicle_registration = vehicle_registration;
    }
}
