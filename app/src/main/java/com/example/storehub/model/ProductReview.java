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

    public String getCustomerName() {
        return customerName != null ? customerName : (altCustomerName != null ? altCustomerName : "");
    }

    public String getCreatedAt() {
        return createdAt != null ? createdAt : (altCreatedAt != null ? altCreatedAt : "");
    }
}
