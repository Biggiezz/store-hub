package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Product {
    @SerializedName("_id")
    private String _id;

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("price")
    private Object rawPrice; // handle String, Double, Long seamlessly

    @SerializedName("image")
    private String image;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("category")
    private String category;

    @SerializedName("description")
    private String description;

    @SerializedName("rating")
    private float rating;

    @SerializedName("reviewCount")
    private int reviewCount;

    @SerializedName("review_count")
    private int altReviewCount;

    @SerializedName("stock")
    private int stock;

    @SerializedName("colors")
    private List<ProductColor> colors = new ArrayList<>();

    @SerializedName("reviews")
    private List<ProductReview> reviews = new ArrayList<>();

    public Product() {
    }

    public Product(String _id, String name, String price, String image, String category, String description) {
        this._id = _id;
        this.name = name;
        this.rawPrice = price;
        this.image = image;
        this.category = category;
        this.description = description;
    }

    public String get_id() {
        return _id != null ? _id : (id != null ? id : "");
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getId() {
        return get_id();
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return rawPrice == null ? "0" : String.valueOf(rawPrice);
    }

    public long getPriceAsLong() {
        if (rawPrice == null) return 0L;
        try {
            if (rawPrice instanceof Number) {
                return ((Number) rawPrice).longValue();
            }
            return (long) Double.parseDouble(rawPrice.toString().replaceAll("[^0-9.]", ""));
        } catch (Exception e) {
            return 0L;
        }
    }

    public void setPrice(String price) {
        this.rawPrice = price;
    }

    public String getImage() {
        return image != null ? image : (imageUrl != null ? imageUrl : "");
    }

    public String getImageUrl() {
        return getImage();
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category != null ? category : "";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount > 0 ? reviewCount : altReviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public List<ProductColor> getColors() {
        return colors != null ? colors : new ArrayList<>();
    }

    public void setColors(List<ProductColor> colors) {
        this.colors = colors;
    }

    public List<ProductReview> getReviews() {
        return reviews != null ? reviews : new ArrayList<>();
    }

    public void setReviews(List<ProductReview> reviews) {
        this.reviews = reviews;
    }
}
