package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class ProductColor {
    @SerializedName("id")
    public Long id;

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

    public ProductColor(Long id, String name, String hex, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.hex = hex;
        this.isDefault = isDefault;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
