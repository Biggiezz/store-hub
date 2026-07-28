package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class CartItem implements java.io.Serializable {
    // Mã MongoDB ID dạng chuỗi của CartItem
    @SerializedName("_id")
    private String mongoId;

    // Mã ID định dạng chung (kiểu Object hỗ trợ String/Number)
    @SerializedName("id")
    private Object id;

    // ID của sản phẩm
    @SerializedName("productId")
    private String productId;

    // ID tham chiếu đến sản phẩm (phòng trường hợp Schema Server trả về dạng product)
    @SerializedName("product")
    private String productRefId;

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

    // Giá sản phẩm tại giỏ hàng (kiểu Object hỗ trợ nhiều kiểu số/chuỗi)
    @SerializedName("price")
    private Object rawPrice;

    // Số lượng đặt hàng (mặc định là 1)
    @SerializedName("quantity")
    private int quantity = 1;

    public CartItem() {
    }

    public String getId() {
        if (mongoId != null) return mongoId;
        if (id != null) return String.valueOf(id);
        return "";
    }

    public String getProductId() {
        if (productId != null && !productId.isEmpty()) {
            return productId;
        }
        if (productRefId != null && !productRefId.isEmpty()) {
            return productRefId;
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

    public static class AddToCartRequest {
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

        public Object getProductId() { return productId; }
        public Object getColorId() { return colorId; }
        public int getQuantity() { return quantity; }
    }

    public static class UpdateQuantityRequest {
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

        public String getCartItemId() { return cartItemId; }
        public int getQuantity() { return quantity; }
    }
}
