package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class AddToCartRequest {
    @SerializedName("productId")
    private Object productId; // string or long

    @SerializedName("colorId")
    private Object colorId;

    @SerializedName("quantity")
    private int quantity;

    public AddToCartRequest(Object productId, Object colorId, int quantity) {
        this.productId = productId;
        this.colorId = colorId;
        this.quantity = quantity;
    }

    public Object getProductId() {
        return productId;
    }

    public void setProductId(Object productId) {
        this.productId = productId;
    }

    public Object getColorId() {
        return colorId;
    }

    public void setColorId(Object colorId) {
        this.colorId = colorId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
