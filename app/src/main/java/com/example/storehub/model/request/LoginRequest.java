package com.example.storehub.model.request;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    // Email dùng đăng nhập
    @SerializedName("email")
    private final String email;

    // Mật khẩu đăng nhập
    @SerializedName("password")
    private final String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
}
