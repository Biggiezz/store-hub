package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class AdminStats {

    public static class DashboardData {
        @SerializedName("totalSales")
        private long totalSales;

        @SerializedName("totalSalesCount")
        private int totalSalesCount;

        @SerializedName("salesStatus")
        private String salesStatus;

        @SerializedName("totalUsers")
        private int totalUsers;

        @SerializedName("usersStatus")
        private String usersStatus;

        @SerializedName("totalProducts")
        private int totalProducts;

        @SerializedName("productsStatus")
        private String productsStatus;

        public long getTotalSales() { return totalSales; }
        public int getTotalSalesCount() { return totalSalesCount; }
        public String getSalesStatus() { return salesStatus; }
        public int getTotalUsers() { return totalUsers; }
        public String getUsersStatus() { return usersStatus; }
        public int getTotalProducts() { return totalProducts; }
        public String getProductsStatus() { return productsStatus; }
    }

    public static class RevenueData {
        @SerializedName("totalRevenue")
        private long totalRevenue;

        @SerializedName("totalOrders")
        private int totalOrders;

        @SerializedName("dailyStats")
        private ArrayList<DailyStat> dailyStats;

        @SerializedName("labels")
        private ArrayList<String> labels;

        @SerializedName("topProducts")
        private ArrayList<TopProduct> topProducts;

        @SerializedName("recentActivities")
        private ArrayList<RecentActivity> recentActivities;

        public long getTotalRevenue() { return totalRevenue; }
        public int getTotalOrders() { return totalOrders; }
        public ArrayList<DailyStat> getDailyStats() { return dailyStats; }
        public ArrayList<String> getLabels() { return labels; }
        public ArrayList<TopProduct> getTopProducts() { return topProducts; }
        public ArrayList<RecentActivity> getRecentActivities() { return recentActivities; }
    }

    public static class TopProduct {
        private String name;
        private String image;
        private int soldCount;

        public String getName() { return name; }
        public String getImage() { return image; }
        public int getSoldCount() { return soldCount; }
    }

    public static class RecentActivity {
        private String type;
        private String title;
        private String createdAt;

        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getCreatedAt() { return createdAt; }
    }

    public static class DailyStat {
        @SerializedName("index")
        private int index;

        @SerializedName("label")
        private String label;

        @SerializedName("revenue")
        private float revenue;

        public int getIndex() { return index; }
        public String getLabel() { return label; }
        public float getRevenue() { return revenue; }
    }
}
