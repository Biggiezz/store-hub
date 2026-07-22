package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class ProductReview {
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

    public String getCreatedAt() {
        return createdAt != null ? createdAt : (altCreatedAt != null ? altCreatedAt : "");
    }

    public static class AddRequest {
        @SerializedName("productId")
        private String productId;

        @SerializedName("customerName")
        private String customerName;

        @SerializedName("rating")
        private float rating;

        @SerializedName("content")
        private String content;

        public AddRequest() {
        }

        public AddRequest(String productId, String customerName, float rating, String content) {
            this.productId = productId;
            this.customerName = customerName;
            this.rating = rating;
            this.content = content;
        }

        public String getProductId() { return productId; }
        public String getCustomerName() { return customerName; }
        public float getRating() { return rating; }
        public String getContent() { return content; }
    }
}
