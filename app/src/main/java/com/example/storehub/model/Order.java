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

    // Restored mock fields for compatibility with mock activities/fragments/adapters
    private String id;
    private String statusText;
    private String productName;
    private String productImage;
    private String productVariant;
    private int quantity;
    private double unitPrice;
    private double subtotal;
    private double discount;
    private double totalAmount;
    private String recipientName;
    private String recipientPhone;
    private String recipientAddress;
    private String createdDate;
    private String estimatedDeliveryDate;
    private String completedDate;
    private String cancelReason;
    private ArrayList<TimelineStep> timeline;

    public Order() {
        items = new ArrayList<>();
        timeline = new ArrayList<>();
    }

    public Order(String id, String status, String statusText, String productName, String productImage,
                 String productVariant, int quantity, double unitPrice, double shippingFee, double discount,
                 String recipientName, String recipientPhone, String recipientAddress, String createdDate,
                 String estimatedDeliveryDate) {
        this.id = id;
        this.status = status;
        this.statusText = statusText;
        this.productName = productName;
        this.productImage = productImage;
        this.productVariant = productVariant;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = unitPrice * quantity;
        this.shippingFee = (long) shippingFee;
        this.discount = discount;
        this.totalAmount = this.subtotal + shippingFee - discount;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.recipientAddress = recipientAddress;
        this.createdDate = createdDate;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.timeline = new ArrayList<>();
        this.items = new ArrayList<>();
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

    public String getStatusText() {
        return statusText != null ? statusText : "";
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
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

    public String getCreatedDate() {
        return createdDate != null ? createdDate : "";
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getEstimatedDeliveryDate() {
        return estimatedDeliveryDate != null ? estimatedDeliveryDate : "";
    }

    public void setEstimatedDeliveryDate(String estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public String getCompletedDate() {
        return completedDate != null ? completedDate : "";
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
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

    public static class TimelineStep implements Serializable {
        private String title;
        private String time;
        private String description;
        private boolean isCompleted;
        private boolean isCurrent;

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
}
