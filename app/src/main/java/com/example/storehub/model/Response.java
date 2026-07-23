package com.example.storehub.model;

public class Response<T> {
    // Mã trạng thái phản hồi HTTP (ví dụ: 200, 400, 404, 500)
    private int code;
    // Dữ liệu nội dung phản hồi (có kiểu Generic T)
    private T data;
    // Thông điệp phản hồi từ máy chủ (ví dụ: "Thành công", "Lỗi")
    private String message;
    // Thông tin phân trang (nếu có)
    private Pagination pagination;
    // Token xác thực JWT gửi kèm khi đăng ký/đăng nhập thành công
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
