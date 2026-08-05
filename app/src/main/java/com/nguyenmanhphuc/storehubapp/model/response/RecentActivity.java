package com.nguyenmanhphuc.storehubapp.model.response;

import com.nguyenmanhphuc.storehubapp.model.CartItem;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RecentActivity {
    // Loại hoạt động (ví dụ: "order", "user")
    private String type;
    // Tiêu đề/Nội dung hoạt động (ví dụ: "Có đơn hàng mới #123")
    private String title;
    // Chi tiết bổ sung của hoạt động
    private String detail;
    // Thời điểm hoạt động diễn ra
    private String createdAt;
    @SerializedName("products")
    private ArrayList<CartItem> products;
    @SerializedName("customerName")
    private String customerName;
    @SerializedName("customerPhone")
    private String customerPhone;
    @SerializedName("paymentMethod")
    private String paymentMethod;
    @SerializedName("totalAmount")
    private long totalAmount;
    @SerializedName("productImage")
    private String productImage;
    @SerializedName("productStock")
    private Integer productStock;
    @SerializedName("productPrice")
    private Long productPrice;
    @SerializedName("productStatus")
    private String productStatus;

    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getDetail() { return detail; }
    public String getCreatedAt() { return createdAt; }
    public ArrayList<CartItem> getProducts() { return products; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getPaymentMethod() { return paymentMethod; }
    public long getTotalAmount() { return totalAmount; }
    public String getProductImage() { return productImage; }
    public Integer getProductStock() { return productStock; }
    public Long getProductPrice() { return productPrice; }
    public String getProductStatus() { return productStatus; }
}
