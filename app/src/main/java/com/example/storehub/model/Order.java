package com.example.storehub.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Order implements Serializable {
    private String id;
    private String status; // "processing", "shipping", "completed", "cancelled"
    private String statusText; // "Chờ xác nhận", "Đang giao hàng", "Đã hoàn thành", "Đã hủy"
    private String productName;
    private String productImage;
    private String productVariant;
    private int quantity;
    private double unitPrice;
    private double subtotal;
    private double shippingFee;
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
        this.timeline = new ArrayList<>();
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
        this.shippingFee = shippingFee;
        this.discount = discount;
        this.totalAmount = this.subtotal + shippingFee - discount;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.recipientAddress = recipientAddress;
        this.createdDate = createdDate;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
        this.timeline = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }

    public String getProductVariant() { return productVariant; }
    public void setProductVariant(String productVariant) { this.productVariant = productVariant; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }

    public String getRecipientAddress() { return recipientAddress; }
    public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(String estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }

    public String getCompletedDate() { return completedDate; }
    public void setCompletedDate(String completedDate) { this.completedDate = completedDate; }

    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

    public ArrayList<TimelineStep> getTimeline() { return timeline; }
    public void setTimeline(ArrayList<TimelineStep> timeline) { this.timeline = timeline; }

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
