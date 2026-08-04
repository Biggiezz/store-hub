package com.nguyenmanhphuc.storehubapp.model.request;

import com.google.gson.annotations.SerializedName;

public class AddToCartRequest {
    // Mã ID sản phẩm thêm vào giỏ hàng
    @SerializedName("productId")
    private Object productId;

    // Mã ID màu sắc đã chọn của sản phẩm
    @SerializedName("colorId")
    private Object colorId;

    // Số lượng sản phẩm thêm mới
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

    public Object getColorId() {
        return colorId;
    }

    public int getQuantity() {
        return quantity;
    }
}
