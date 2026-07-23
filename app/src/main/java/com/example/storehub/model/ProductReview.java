package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class ProductReview implements java.io.Serializable {
    @SerializedName("id")
    public Object id;

    @SerializedName("_id")
    public String mongoId;

    @SerializedName("customer_name")
    public String customerName;

    @SerializedName("customerName")
    public String altCustomerName;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("createdAt")
    public String altCreatedAt;

    @SerializedName("customerImage")
    public String customerImage;

    @SerializedName("customer_image")
    public String altCustomerImage;

    @SerializedName("rating")
    public float rating;

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

    @SerializedName("replyContent")
    public String replyContent;

    @SerializedName("replyCreatedAt")
    public String replyCreatedAt;

    public String getReplyContent() {
        return replyContent != null ? replyContent : "";
    }

    public String getReplyCreatedAt() {
        return replyCreatedAt != null ? replyCreatedAt : "";
    }

    public static class AddRequest {
        @SerializedName("productId")
        private String productId;

        @SerializedName("customerName")
        private String customerName;

        @SerializedName("customerImage")
        private String customerImage;

        @SerializedName("rating")
        private float rating;

        @SerializedName("content")
        private String content;

        public AddRequest() {
        }

        public AddRequest(String productId, String customerName, String customerImage, float rating, String content) {
            this.productId = productId;
            this.customerName = customerName;
            this.customerImage = customerImage;
            this.rating = rating;
            this.content = content;
        }

        public AddRequest(String productId, String customerName, float rating, String content) {
            this(productId, customerName, "", rating, content);
        }

        public String getProductId() { return productId; }
        public String getCustomerName() { return customerName; }
        public String getCustomerImage() { return customerImage; }
        public float getRating() { return rating; }
        public String getContent() { return content; }
    }

    public static class ReplyRequest {
        @SerializedName("productId")
        private String productId;

        @SerializedName("reviewId")
        private String reviewId;

        @SerializedName("replyContent")
        private String replyContent;

        public ReplyRequest(String productId, String reviewId, String replyContent) {
            this.productId = productId;
            this.reviewId = reviewId;
            this.replyContent = replyContent;
        }

        public String getProductId() { return productId; }
        public String getReviewId() { return reviewId; }
        public String getReplyContent() { return replyContent; }
    }
}
