package com.nguyenmanhphuc.storehubapp.model;

import com.google.gson.annotations.SerializedName;

public class ProductColor {
    // Mã MongoDB ID dạng chuỗi của màu sắc
    @SerializedName("_id")
    public String mongoId;

    // Tên màu sắc hiển thị (ví dụ: Xanh dương)
    @SerializedName("name")
    public String name;

    // Mã màu dạng HEX (ví dụ: #0000FF)
    @SerializedName("hex")
    public String hex;

    // Trạng thái màu mặc định cho sản phẩm
    @SerializedName("isDefault")
    public boolean isDefault;

    public ProductColor() {
    }

    public ProductColor(String mongoId, String name, String hex, boolean isDefault) {
        this.mongoId = mongoId;
        this.name = name;
        this.hex = hex;
        this.isDefault = isDefault;
    }

    public String getId() {
        return mongoId != null ? mongoId : "";
    }

    public String getMongoId() {
        return mongoId;
    }

    public void setMongoId(String mongoId) {
        this.mongoId = mongoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}
