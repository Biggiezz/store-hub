package com.nguyenmanhphuc.storehubapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Category implements Serializable {
    @SerializedName("_id")
    private String _id;

    @SerializedName("name")
    private String name;

    @SerializedName("image")
    private String image;

    @SerializedName("isActive")
    private boolean isActive = true;

    public Category() {
    }

    public Category(String _id, String name) {
        this._id = _id;
        this.name = name;
    }

    public String get_id() {
        return _id != null ? _id : "";
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        if (name == null) return "";
        try {
            java.util.Locale current = java.util.Locale.getDefault();
            if ("en".equalsIgnoreCase(current.getLanguage())) {
                if ("Tai nghe".equalsIgnoreCase(name)) return "Headphones";
                if ("Điện thoại".equalsIgnoreCase(name)) return "Phones";
                if ("Máy tính".equalsIgnoreCase(name)) return "Laptops";
                if ("Đồng hồ".equalsIgnoreCase(name)) return "Watches";
            }
        } catch (Exception ignored) {}
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image != null ? image : "";
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
