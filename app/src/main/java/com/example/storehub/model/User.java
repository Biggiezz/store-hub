package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class User {
    // Mã MongoDB ID dạng chuỗi của tài khoản
    @SerializedName("_id")
    private String id;
    // Họ và tên người dùng
    private String name;
    // Địa chỉ email người dùng
    private String email;
    // Số điện thoại người dùng
    private String phone;
    // Vai trò phân quyền người dùng (customer, admin, superadmin)
    private String role;
    // Đường dẫn hình ảnh đại diện (avatar) của người dùng
    private String image;
    // Địa chỉ giao nhận hàng của người dùng
    private String address;
    // Thời điểm thay đổi mật khẩu lần gần nhất
    private String changePasswordDate;
    // Thời điểm hoạt động cuối cùng của người dùng (phục vụ hiển thị Admin)
    private String lastActive;
    private String password;

    public User() {
    }

    public User(String id, String name, String email, String phone, String role, String image, String address,String lastActive) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.image = image;
        this.address = address;
        this.lastActive = lastActive;
        this.changePasswordDate = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getImage() {
        return com.example.storehub.utils.ImageUtils.getCorrectedImageUrl(image, com.example.storehub.services.HttpResquest.BASE_URL);
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLastActive() {
        return lastActive != null ? lastActive : "Vừa xong";
    }

    public void setLastActive(String lastActive) {
        this.lastActive = lastActive;
    }

    public boolean isSuperAdmin() {
        if (role == null) return false;
        String r = role.trim().toLowerCase();
        return r.equals("superadmin") || r.equals("super admin") || r.equals("quản trị viên tối cao");
    }

    public boolean isAdmin() {
        if (role == null) return false;
        String r = role.trim().toLowerCase();
        return r.equals("admin") || r.equals("quản lý cửa hàng") || isSuperAdmin();
    }

    public boolean canManage(User targetUser) {
        if (targetUser == null) return true;
        if (this.isSuperAdmin()) return true;
        if (this.isAdmin()) {
            return !targetUser.isSuperAdmin();
        }
        return false;
    }

    public String getChangePasswordDate() {
        return changePasswordDate;
    }

    public void setChangePasswordDate(String changePasswordDate) {
        this.changePasswordDate = changePasswordDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static class LoginRequest {
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

    public static class RegisterRequest {
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

    public static class LoginResponse {
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
}
