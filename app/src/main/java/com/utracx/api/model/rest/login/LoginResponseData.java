package com.utracx.api.model.rest.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginResponseData {

    @SerializedName("code")
    @Expose
    private Double code;
    @SerializedName("data")
    @Expose
    private LoginData loginData;

    public Double getCode() {
        return code;
    }

    public void setCode(Double code) {
        this.code = code;
    }

    public LoginData getLoginData() {
        return loginData;
    }

    public void setLoginData(LoginData loginData) {
        this.loginData = loginData;
    }

}