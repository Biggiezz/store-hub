package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class ApiMessageResponse {
    @SerializedName("code")
    public int code;

    @SerializedName("message")
    public String message;

    public ApiMessageResponse() {
    }

    public ApiMessageResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
