package com.nguyenmanhphuc.storehubapp.model;

import com.google.gson.annotations.SerializedName;

public class CartItem implements java.io.Serializable {
    // Mã MongoDB ID dạng chuỗi của CartItem
    @SerializedName("_id")
    private String mongoId;

    // ID của sản phẩm
    @SerializedName("productId")
    private String productId;

    // Tên của sản phẩm
    @SerializedName("productName")
    private String productName;

    // Đường dẫn hình ảnh sản phẩm
    @SerializedName("productImage")
    private String productImage;

    // ID của biến thể màu sắc đã chọn
    @SerializedName("colorId")
    private String colorId;

    // Tên màu sắc của biến thể (ví dụ: Đen, Trắng)
    @SerializedName("colorName")
    private String colorName;

    @SerializedName("colorHex")
    private String colorHex;

    // Giá sản phẩm tại giỏ hàng (kiểu Object hỗ trợ nhiều kiểu số/chuỗi)
    @SerializedName("price")
    private Object rawPrice;

    // Số lượng đặt hàng (mặc định là 1)
    @SerializedName("quantity")
    private int quantity = 1;

    public CartItem() {
    }

    public String getId() {
        return mongoId != null ? mongoId : "";
    }

    public String getProductId() {
        if (productId != null && !productId.isEmpty()) {
            return productId;
        }
        return "";
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName != null ? productName : "";
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage != null ? productImage : "";
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getColorId() {
        return colorId;
    }

    public void setColorId(String colorId) {
        this.colorId = colorId;
    }

    public String getColorName() {
        return colorName != null ? colorName : "";
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
    }

    public String getColorHex() {
        return colorHex != null ? colorHex : "";
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public long getPrice() {
        if (rawPrice == null) return 0L;
        try {
            if (rawPrice instanceof Number) {
                return ((Number) rawPrice).longValue();
            }
            return (long) Double.parseDouble(rawPrice.toString().replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0L;
        }
    }

    public void setPrice(Object price) {
        this.rawPrice = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getTotalItemPrice() {
        return getPrice() * quantity;
    }
}
