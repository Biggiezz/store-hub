package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class UpdateStatusRequest {
    @SerializedName("orderId")
    private String orderId;

    @SerializedName("status")
    private String status;

    public UpdateStatusRequest(String orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
