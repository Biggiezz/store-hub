package com.example.storehub.model;

public class Response<T> {
    private int code;
    private T data;
    private String message;
    private Pagination pagination;
    private String token;

    public Response(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public Response(int code, T data, String message, Pagination pagination) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.pagination = pagination;
    }

    public Response(int code, T data, String message, Pagination pagination, String token) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.pagination = pagination;
        this.token = token;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
