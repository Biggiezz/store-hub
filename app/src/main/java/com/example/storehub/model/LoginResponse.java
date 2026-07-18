package com.example.storehub.model;

public class LoginResponse {
    private int code;
    private String message;
    private String token;
    private User data;

    public LoginResponse(int code, String message, String token, User data) {
        this.code = code;
        this.message = message;
        this.token = token;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getData() {
        return data;
    }

    public void setData(User data) {
        this.data = data;
    }
}
