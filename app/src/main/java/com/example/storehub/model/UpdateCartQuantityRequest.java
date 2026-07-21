package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class UpdateCartQuantityRequest {
    @SerializedName("cartItemId")
    private String cartItemId;

    @SerializedName("quantity")
    private int quantity;

    public UpdateCartQuantityRequest(String cartItemId, int quantity) {
        this.cartItemId = cartItemId;
        this.quantity = quantity;
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
