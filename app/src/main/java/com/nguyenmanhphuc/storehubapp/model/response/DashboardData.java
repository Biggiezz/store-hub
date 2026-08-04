package com.nguyenmanhphuc.storehubapp.model.response;

import com.google.gson.annotations.SerializedName;

public class DashboardData {
    // Tổng doanh số bán hàng (doanh thu)
    @SerializedName("totalSales")
    private long totalSales;

    // Tổng số lượng sản phẩm đã bán ra
    @SerializedName("totalSalesCount")
    private int totalSalesCount;

    // Trạng thái tăng trưởng doanh số (ví dụ: "+15% so với tháng trước")
    @SerializedName("salesStatus")
    private String salesStatus;

    // Tổng số lượng người dùng hệ thống
    @SerializedName("totalUsers")
    private int totalUsers;

    // Trạng thái tăng trưởng người dùng (ví dụ: "+2% tuần này")
    @SerializedName("usersStatus")
    private String usersStatus;

    // Tổng số sản phẩm trong hệ thống
    @SerializedName("totalProducts")
    private int totalProducts;

    // Trạng thái số lượng sản phẩm (ví dụ: "Đã đồng bộ")
    @SerializedName("productsStatus")
    private String productsStatus;

    // Tổng số đơn hàng trong hệ thống
    @SerializedName("totalOrders")
    private int totalOrders;

    // Số đơn hàng đang chờ xác nhận
    @SerializedName("pendingOrders")
    private int pendingOrders;

    public long getTotalSales() { return totalSales; }
    public int getTotalSalesCount() { return totalSalesCount; }
    public String getSalesStatus() { return salesStatus; }
    public int getTotalUsers() { return totalUsers; }
    public String getUsersStatus() { return usersStatus; }
    public int getTotalProducts() { return totalProducts; }
    public String getProductsStatus() { return productsStatus; }
    public int getTotalOrders() { return totalOrders; }
    public int getPendingOrders() { return pendingOrders; }
}
