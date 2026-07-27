package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {
    @SerializedName("_id")
    private String orderId;

    @SerializedName("orderCode")
    private String orderCode;

    @SerializedName("items")
    private ArrayList<CartItem> items;

    @SerializedName("status")
    private String status;

    @SerializedName("totalPrice")
    private long totalPrice;

    @SerializedName("shippingFee")
    private long shippingFee;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("receiverName")
    private String recipientName;

    @SerializedName("receiverPhone")
    private String recipientPhone;

    @SerializedName("deliveryAddress")
    private String recipientAddress;

    @SerializedName("cancelReason")
    private String cancelReason;

    public Order() {
        items = new ArrayList<>();
    }

    public String getOrderId() {
        return orderId != null ? orderId : "";
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode != null ? orderCode : "";
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public ArrayList<CartItem> getItems() {
        return items != null ? items : new ArrayList<>();
    }

    public void setItems(ArrayList<CartItem> items) {
        this.items = items;
    }

    public String getStatus() {
        return status != null ? status : "Đang giao hàng";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public long getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(long shippingFee) {
        this.shippingFee = shippingFee;
    }

    public String getCreatedAt() {
        return createdAt != null ? createdAt : "";
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getRecipientName() {
        return recipientName != null ? recipientName : "";
    }

    public String getRecipientPhone() {
        return recipientPhone != null ? recipientPhone : "";
    }

    public String getRecipientAddress() {
        return recipientAddress != null ? recipientAddress : "";
    }

    public String getCancelReason() {
        return cancelReason != null ? cancelReason : "";
    }

    public static class UpdateStatusRequest {
        @SerializedName("orderId")
        private final String orderId;

        @SerializedName("status")
        private final String status;

        public UpdateStatusRequest(String orderId, String status) {
            this.orderId = orderId;
            this.status = status;
        }
    }
}
