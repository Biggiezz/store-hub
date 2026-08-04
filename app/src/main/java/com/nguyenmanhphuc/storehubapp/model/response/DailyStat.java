package com.nguyenmanhphuc.storehubapp.model.response;

import com.google.gson.annotations.SerializedName;

public class DailyStat {
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
