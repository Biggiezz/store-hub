package com.example.storehub.model;

public class Product {
    private String _id;
    private String name;
    private String price;
    private String image;
    private String category;
    private String description;

    public Product(String _id, String name, String price, String image, String category, String description) {
        this._id = _id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.category = category;
        this.description = description;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
