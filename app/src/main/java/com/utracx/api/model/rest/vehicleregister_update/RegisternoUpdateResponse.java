package com.utracx.api.model.rest.vehicleregister_update;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.utracx.api.model.rest.login.LoginData;

import java.io.Serializable;

public class RegisternoUpdateResponse implements Serializable {

    public String status;
    public int code;

    @SerializedName("data")
    @Expose
    private LoginData loginData;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public LoginData getLoginData() {
        return loginData;
    }

    public void setLoginData(LoginData loginData) {
        this.loginData = loginData;
    }

}

