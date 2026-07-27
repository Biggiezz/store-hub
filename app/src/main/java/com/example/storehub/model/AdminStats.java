package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class AdminStats {

    public static class DashboardData {
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

    public static class RevenueData {
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

    public static class TopProduct {
        // Tên sản phẩm bán chạy
        private String name;
        // Đường dẫn hình ảnh sản phẩm
        private String image;
        // Số lượng sản phẩm đã được bán ra
        private int soldCount;

        public String getName() { return name; }
        public String getImage() { return image; }
        public int getSoldCount() { return soldCount; }
    }

    public static class RecentActivity {
        // Loại hoạt động (ví dụ: "order", "user")
        private String type;
        // Tiêu đề/Nội dung hoạt động (ví dụ: "Có đơn hàng mới #123")
        private String title;
        // Thời điểm hoạt động diễn ra
        private String createdAt;

        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getCreatedAt() { return createdAt; }
    }

    public static class DailyStat {
        // Vị trí/Thứ tự của ngày trong tuần hoặc tháng (0-6)
        @SerializedName("index")
        private int index;

        // Nhãn ngày (ví dụ: "T2", "T3")
        @SerializedName("label")
        private String label;

        // Doanh thu của ngày tương ứng
        @SerializedName("revenue")
        private float revenue;

        public int getIndex() { return index; }
        public String getLabel() { return label; }
        public float getRevenue() { return revenue; }
    }
}
