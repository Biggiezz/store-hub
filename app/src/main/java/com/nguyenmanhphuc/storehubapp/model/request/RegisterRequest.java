package com.nguyenmanhphuc.storehubapp.model.request;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    // Họ tên đầy đủ khi đăng ký
    @SerializedName("name")
    private final String name;

    // Email dùng đăng ký tài khoản mới (phải là duy nhất)
    @SerializedName("email")
    private final String email;

    // Số điện thoại liên hệ
    @SerializedName("phone")
    private final String phone;

    // Mật khẩu mong muốn
    @SerializedName("password")
    private final String password;

    public RegisterRequest(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }
}
