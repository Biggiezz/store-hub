package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class AddReviewRequest {
    @SerializedName("productId")
    private String productId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("rating")
    private float rating;

    @SerializedName("content")
    private String content;

    public AddReviewRequest() {
    }

    public AddReviewRequest(String productId, String customerName, float rating, String content) {
        this.productId = productId;
        this.customerName = customerName;
        this.rating = rating;
        this.content = content;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
