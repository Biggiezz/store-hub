package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {
    // Mã MongoDB ID dạng chuỗi của đơn hàng
    @SerializedName("_id")
    private String orderId;

    // Mã code đơn hàng (ví dụ: SH-12345)
    @SerializedName("orderCode")
    private String orderCode;

    // Danh sách các sản phẩm đặt mua trong đơn hàng
    @SerializedName("items")
    private ArrayList<CartItem> items;

    // Trạng thái đơn hàng (Chờ xác nhận, Đang chuẩn bị hàng, Đang giao hàng, Đã giao hàng, Đã hoàn thành, Đã hủy)
    @SerializedName("status")
    private String status;

    // Tổng số tiền đơn hàng trước phí ship
    @SerializedName("totalPrice")
    private long totalPrice;

    // Phí vận chuyển
    @SerializedName("shippingFee")
    private long shippingFee;

    // Thời điểm tạo đơn hàng
    @SerializedName("createdAt")
    private String createdAt;

    // Trường ID bổ trợ phục vụ tương thích với Mock data
    private String id;
    // Tên sản phẩm chính (bổ trợ phục vụ tương thích viết đánh giá)
    private String productName = "";
    // Hình ảnh sản phẩm chính (bổ trợ phục vụ tương thích viết đánh giá)
    private String productImage = "";
    // Tên phân loại màu sắc sản phẩm chính (bổ trợ phục vụ tương thích viết đánh giá)
    private String productVariant = "";

    // Họ tên người nhận hàng
    @SerializedName("receiverName")
    private String recipientName;

    // Số điện thoại người nhận hàng
    @SerializedName("receiverPhone")
    private String recipientPhone;

    // Địa chỉ giao nhận hàng
    @SerializedName("deliveryAddress")
    private String recipientAddress;

    // Thời điểm xác nhận đơn hàng
    @SerializedName("confirmedAt")
    private String confirmedAt;

    // Thời điểm đơn hàng nhập kho/đóng gói xong
    @SerializedName("warehouseAt")
    private String warehouseAt;

    // Thời điểm đơn hàng bắt đầu giao hàng
    @SerializedName("deliveringAt")
    private String deliveringAt;

    // Thời điểm đơn hàng được giao thành công / hoàn thành
    @SerializedName("completedAt")
    private String completedAt;

    // Lý do hủy đơn hàng (nếu có)
    @SerializedName("cancelReason")
    private String cancelReason;

    // Trạng thái đã được người dùng nhận xét/đánh giá sản phẩm hay chưa
    @SerializedName("isReviewed")
    private boolean isReviewed;

    // Danh sách các bước dòng thời gian giao nhận đơn hàng (Timeline)
    private ArrayList<TimelineStep> timeline;

    public Order() {
        items = new ArrayList<>();
        timeline = new ArrayList<>();
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

    // Getters and Setters for mock fields
    public String getId() {
        return id != null ? id : (orderId != null ? orderId : "");
    }

    public void setId(String id) {
        this.id = id;
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

    public String getProductVariant() {
        return productVariant != null ? productVariant : "";
    }

    public void setProductVariant(String productVariant) {
        this.productVariant = productVariant;
    }

    public String getRecipientName() {
        return recipientName != null ? recipientName : "";
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone != null ? recipientPhone : "";
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getRecipientAddress() {
        return recipientAddress != null ? recipientAddress : "";
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public String getCancelReason() {
        return cancelReason != null ? cancelReason : "";
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public ArrayList<TimelineStep> getTimeline() {
        return timeline != null ? timeline : new ArrayList<>();
    }

    public void setTimeline(ArrayList<TimelineStep> timeline) {
        this.timeline = timeline;
    }

    public String getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(String confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public String getWarehouseAt() {
        return warehouseAt;
    }

    public void setWarehouseAt(String warehouseAt) {
        this.warehouseAt = warehouseAt;
    }

    public String getDeliveringAt() {
        return deliveringAt;
    }

    public void setDeliveringAt(String deliveringAt) {
        this.deliveringAt = deliveringAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isReviewed() {
        return isReviewed;
    }

    public void setReviewed(boolean reviewed) {
        isReviewed = reviewed;
    }

    public static class TimelineStep implements Serializable {
        private final String title;
        private final String time;
        private final String description;
        private final boolean isCompleted;
        private final boolean isCurrent;

        public TimelineStep(String title, String time, String description, boolean isCompleted, boolean isCurrent) {
            this.title = title;
            this.time = time;
            this.description = description;
            this.isCompleted = isCompleted;
            this.isCurrent = isCurrent;
        }

        public String getTitle() { return title; }
        public String getTime() { return time; }
        public String getDescription() { return description; }
        public boolean isCompleted() { return isCompleted; }
        public boolean isCurrent() { return isCurrent; }
    }

    public static class CancelOrderRequest {
        @SerializedName("orderId")
        private String orderId;

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

    public static class UpdateStatusRequest {
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
}
