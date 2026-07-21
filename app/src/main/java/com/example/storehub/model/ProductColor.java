package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class ProductColor {
    @SerializedName("id")
    public Object id;

    @SerializedName("_id")
    public String mongoId;

    @SerializedName("name")
    public String name;

    @SerializedName("hex")
    public String hex;

    @SerializedName("isDefault")
    public boolean isDefault;

    public ProductColor() {
    }

    public ProductColor(Object id, String name, String hex, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.hex = hex;
        this.isDefault = isDefault;
    }

    public String getId() {
        return id != null ? String.valueOf(id) : (mongoId != null ? mongoId : "");
    public String getId() {
        if (id != null) return String.valueOf(id);
        if (mongoId != null) return mongoId;
        return "";
    }

    public void setId(Object id) {
        this.id = id;
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
