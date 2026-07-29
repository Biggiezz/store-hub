package com.example.storehub.model.request;

import com.google.gson.annotations.SerializedName;

public class AddReviewRequest {
    // Mã ID sản phẩm được đánh giá
    @SerializedName("productId")
    private String productId;

    // Tên của khách hàng gửi đánh giá
    @SerializedName("customerName")
    private String customerName;

    // Đường dẫn ảnh đại diện của khách hàng gửi đánh giá
    @SerializedName("customerImage")
    private String customerImage;

    // Điểm số đánh giá (từ 1 đến 5 sao)
    @SerializedName("rating")
    private float rating;

    // Nội dung chi tiết của đánh giá
    @SerializedName("content")
    private String content;

    // Mã ID đơn hàng liên quan đến đánh giá này (để tránh đánh giá trùng lặp)
    @SerializedName("orderId")
    private String orderId;

    public AddReviewRequest() {
    }

    public AddReviewRequest(String productId, String customerName, String customerImage, float rating, String content, String orderId) {
        this.productId = productId;
        this.customerName = customerName;
        this.customerImage = customerImage;
        this.rating = rating;
        this.content = content;
        this.orderId = orderId;
    }

    public AddReviewRequest(String productId, String customerName, String customerImage, float rating, String content) {
        this(productId, customerName, customerImage, rating, content, null);
    }

    public AddReviewRequest(String productId, String customerName, float rating, String content) {
        this(productId, customerName, "", rating, content, null);
    }

    public String getProductId() { return productId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerImage() { return customerImage; }
    public float getRating() { return rating; }
    public String getContent() { return content; }
    public String getOrderId() { return orderId; }
}
