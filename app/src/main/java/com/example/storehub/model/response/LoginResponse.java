package com.example.storehub.model.response;

import com.example.storehub.model.User;
import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    // Mã HTTP phản hồi (ví dụ: 200, 400, 404, 500)
    @SerializedName("code")
    private int code;

    // Thông điệp trạng thái (ví dụ: "Đăng nhập thành công")
    @SerializedName("message")
    private String message;

    // Token xác thực JWT gửi kèm
    @SerializedName("token")
    private String token;

    // Thông tin chi tiết của tài khoản người dùng đăng nhập
    @SerializedName("data")
    private User data;

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public User getData() { return data; }
}
