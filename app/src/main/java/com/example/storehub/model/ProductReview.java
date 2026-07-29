package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class ProductReview implements java.io.Serializable {
    // Mã định danh nhận xét dạng Object
    @SerializedName("id")
    public Object id;

    // Mã MongoDB ID dạng chuỗi của nhận xét
    @SerializedName("_id")
    public String mongoId;

    // Họ tên của khách hàng viết nhận xét
    @SerializedName("customer_name")
    public String customerName;

    // Họ tên khách hàng (trường bổ trợ tương thích)
    @SerializedName("customerName")
    public String altCustomerName;

    // Ngày giờ tạo nhận xét
    @SerializedName("created_at")
    public String createdAt;

    // Ngày giờ tạo nhận xét (trường bổ trợ tương thích)
    @SerializedName("createdAt")
    public String altCreatedAt;

    // Đường dẫn hình ảnh đại diện khách hàng
    @SerializedName("customerImage")
    public String customerImage;

    // Đường dẫn hình ảnh đại diện khách hàng (trường bổ trợ tương thích)
    @SerializedName("customer_image")
    public String altCustomerImage;

    // Điểm đánh giá (1-5 sao)
    @SerializedName("rating")
    public float rating;

    // Nội dung nhận xét/đánh giá
    @SerializedName("content")
    public String content;

    public ProductReview() {
    }

    public String getId() {
        if (id != null) return String.valueOf(id);
        if (mongoId != null) return mongoId;
        return "";
    }

    public String getCustomerName() {
        return customerName != null ? customerName : (altCustomerName != null ? altCustomerName : "");
    }

    public String getCustomerImage() {
        return customerImage != null && !customerImage.isEmpty() ? customerImage : (altCustomerImage != null ? altCustomerImage : "");
    }

    public String getCreatedAt() {
        return createdAt != null ? createdAt : (altCreatedAt != null ? altCreatedAt : "");
    }

    // Nội dung phản hồi của quản trị viên (Admin)
    @SerializedName("replyContent")
    public String replyContent;

    // Thời điểm quản trị viên phản hồi
    @SerializedName("replyCreatedAt")
    public String replyCreatedAt;

    public String getReplyContent() {
        return replyContent != null ? replyContent : "";
    }

    public String getReplyCreatedAt() {
        return replyCreatedAt != null ? replyCreatedAt : "";
    }
}
