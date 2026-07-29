package com.example.storehub.model.request;

import com.google.gson.annotations.SerializedName;

public class UpdateStatusRequest {
    // Mã ID đơn hàng cần cập nhật trạng thái
    @SerializedName("orderId")
    private String orderId;

    // Trạng thái mới cần cập nhật cho đơn hàng (ví dụ: "pending", "shipping", "completed", "cancelled")
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
