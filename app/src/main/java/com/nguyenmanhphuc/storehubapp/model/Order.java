package com.nguyenmanhphuc.storehubapp.model;

import com.nguyenmanhphuc.storehubapp.model.response.TimelineStep;
import com.google.gson.annotations.SerializedName;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.Gson;
import java.io.Serializable;
import java.io.ObjectStreamException;
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

    @SerializedName("discount")
    private long discount;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    // Thời điểm tạo đơn hàng
    @SerializedName("createdAt")
    private String createdAt;

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

    // Reference toi tai khoan khach hang (backend tra ve "user")
    // Danh dau transient de khong tham gia Java Serialization (JsonElement khong Serializable)
    @SerializedName("user")
    private transient JsonElement rawUser;

    // ID nguoi dung duoc giai ma tu rawUser, luu thanh String de truyen qua Intent an toan
    private String resolvedUserId = "";

    // Thông tin User mới nhất được gán sau khi fetch từ API
    private User populatedUser;

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

    @SerializedName("appTransId")
    private String appTransId;

    @SerializedName("zpTransId")
    private String zpTransId;

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

    public long getDiscount() {
        return discount;
    }

    public void setDiscount(long discount) {
        this.discount = discount;
    }

    public String getPaymentMethod() {
        return paymentMethod != null ? paymentMethod : "COD";
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCreatedAt() {
        return createdAt != null ? createdAt : "";
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return getOrderId();
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

    public User getUser() {
        return populatedUser;
    }

    public void setPopulatedUser(User user) {
        this.populatedUser = user;
    }

    public String getUserIdString() {
        // Neu da giai ma roi thi dung luon
        if (resolvedUserId != null && !resolvedUserId.isEmpty()) return resolvedUserId;
        // Giai ma tu rawUser (chi co khi vua duoc Gson parse, truoc khi serialize qua Intent)
        resolvedUserId = extractIdFromElement(rawUser);
        return resolvedUserId;
    }

    private String extractIdFromElement(JsonElement elem) {
        if (elem == null || elem.isJsonNull()) return "";
        try {
            if (elem.isJsonPrimitive()) return elem.getAsString();
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                if (obj.has("_id") && !obj.get("_id").isJsonNull()) return obj.get("_id").getAsString();
                if (obj.has("id")  && !obj.get("id").isJsonNull())  return obj.get("id").getAsString();
            }
        } catch (Exception ignored) {}
        return "";
    }

    /**
     * Duoc goi ngay sau khi Gson parse xong (truoc khi object co the bi serialize).
     * Dam bao resolvedUserId duoc giai ma truoc khi rawUser bi mat khi truyen qua Intent.
     */
    public Order resolveFields() {
        resolvedUserId = extractIdFromElement(rawUser);
        return this;
    }

    public String getRecipientName() {
        User u = getUser();
        if (u != null && u.getName() != null && !u.getName().trim().isEmpty()) {
            return u.getName().trim();
        }
        return recipientName != null ? recipientName : "";
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        User u = getUser();
        if (u != null && u.getPhone() != null && !u.getPhone().trim().isEmpty()) {
            return u.getPhone().trim();
        }
        return recipientPhone != null ? recipientPhone : "";
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getRecipientAddress() {
        User u = getUser();
        if (u != null && u.getAddress() != null && !u.getAddress().trim().isEmpty()) {
            return u.getAddress().trim();
        }
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

    public String getAppTransId() {
        return appTransId != null ? appTransId : "";
    }

    public void setAppTransId(String appTransId) {
        this.appTransId = appTransId;
    }

    public String getZpTransId() {
        return zpTransId != null ? zpTransId : "";
    }

    public void setZpTransId(String zpTransId) {
        this.zpTransId = zpTransId;
    }
}
