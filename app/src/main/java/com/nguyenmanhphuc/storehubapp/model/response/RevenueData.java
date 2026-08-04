package com.nguyenmanhphuc.storehubapp.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class RevenueData {
    // Tổng doanh thu thống kê
    @SerializedName("totalRevenue")
    private long totalRevenue;

    // Tổng số lượng đơn hàng
    @SerializedName("totalOrders")
    private int totalOrders;

    // Danh sách thống kê doanh thu theo ngày
    @SerializedName("dailyStats")
    private ArrayList<DailyStat> dailyStats;

    // Danh sách các nhãn thời gian tương ứng (ví dụ: Thứ 2, Thứ 3...)
    @SerializedName("labels")
    private ArrayList<String> labels;

    // Danh sách các sản phẩm bán chạy nhất
    @SerializedName("topProducts")
    private ArrayList<TopProduct> topProducts;

    // Danh sách các hoạt động gần đây
    @SerializedName("recentActivities")
    private ArrayList<RecentActivity> recentActivities;

    public long getTotalRevenue() { return totalRevenue; }
    public int getTotalOrders() { return totalOrders; }
    public ArrayList<DailyStat> getDailyStats() { return dailyStats; }
    public ArrayList<String> getLabels() { return labels; }
    public ArrayList<TopProduct> getTopProducts() { return topProducts; }
    public ArrayList<RecentActivity> getRecentActivities() { return recentActivities; }
}
