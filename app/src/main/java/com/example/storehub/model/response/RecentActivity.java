package com.example.storehub.model.response;

public class RecentActivity {
    // Loại hoạt động (ví dụ: "order", "user")
    private String type;
    // Tiêu đề/Nội dung hoạt động (ví dụ: "Có đơn hàng mới #123")
    private String title;
    // Chi tiết bổ sung của hoạt động
    private String detail;
    // Thời điểm hoạt động diễn ra
    private String createdAt;

    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getDetail() { return detail; }
    public String getCreatedAt() { return createdAt; }
}
