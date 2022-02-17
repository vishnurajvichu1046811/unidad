package com.utracx.api.model.rest.login;

import java.io.Serializable;

public class ProfileUpdateResponse implements Serializable {

    public String status;
    public int code;
    public String data;


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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

