package com.example.storehub.model.request;

import com.google.gson.annotations.SerializedName;

public class CancelOrderRequest {
    // Mã ID đơn hàng cần hủy
    @SerializedName("orderId")
    private String orderId;

    // Lý do hủy đơn hàng
    @SerializedName("reason")
    private String reason;

    public CancelOrderRequest(String orderId) {
        this.orderId = orderId;
        this.reason = "";
    }

    public CancelOrderRequest(String orderId, String reason) {
        this.orderId = orderId;
        this.reason = reason;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
