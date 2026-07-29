package com.example.storehub.model.response;

import java.io.Serializable;

public class TimelineStep implements Serializable {
    // Tiêu đề của bước trạng thái đơn hàng (ví dụ: "Chờ xác nhận", "Đang giao hàng")
    private final String title;

    // Thời gian xảy ra bước trạng thái này (dạng chuỗi)
    private final String time;

    // Mô tả chi tiết hành động hoặc trạng thái (ví dụ: "Đơn hàng đã được bàn giao cho đơn vị vận chuyển")
    private final String description;

    // Trạng thái đã hoàn thành bước này hay chưa
    private final boolean isCompleted;

    // Trạng thái có phải là bước hiện tại của đơn hàng hay không
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
