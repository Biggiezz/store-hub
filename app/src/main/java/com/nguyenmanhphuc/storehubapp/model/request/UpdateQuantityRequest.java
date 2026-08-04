package com.nguyenmanhphuc.storehubapp.model.request;

import com.google.gson.annotations.SerializedName;

public class UpdateQuantityRequest {
    // Mã ID của CartItem cần cập nhật
    @SerializedName("cartItemId")
    private String cartItemId;

    // Số lượng sản phẩm mới cập nhật
    @SerializedName("quantity")
    private int quantity;

    public UpdateQuantityRequest(String cartItemId, int quantity) {
        this.cartItemId = cartItemId;
        this.quantity = quantity;
    }

    public String getCartItemId() {
        return cartItemId;
    }

    public int getQuantity() {
        return quantity;
    }
}
