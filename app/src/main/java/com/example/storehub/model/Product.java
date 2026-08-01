package com.example.storehub.model;

import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.ImageUtils;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Product {
    // Mã MongoDB ID của sản phẩm
    @SerializedName("_id")
    private String _id;

    // Tên của sản phẩm
    @SerializedName("name")
    private String name;

    // Đơn giá sản phẩm (dạng Object hỗ trợ nhiều kiểu số/chuỗi)
    @SerializedName("price")
    private Object rawPrice;

    // Đường dẫn hình ảnh chính của sản phẩm
    @SerializedName("image")
    private String image;

    // Đường dẫn hình ảnh phụ bổ trợ phục vụ tương thích
    @SerializedName("image_url")
    private String imageUrl;

    // Phân loại danh mục sản phẩm
    @SerializedName("category")
    private Category category;

    // Mô tả thông tin chi tiết sản phẩm
    @SerializedName("description")
    private String description;

    // Điểm đánh giá trung bình (ví dụ: 4.5)
    @SerializedName("rating")
    private float rating;

    // Tổng số lượng lượt nhận xét/đánh giá sản phẩm
    @SerializedName("reviewCount")
    private int reviewCount;

    // Số lượng sản phẩm còn trong kho
    @SerializedName("stock")
    private int stock;

    // Số lượng sản phẩm đã bán
    @SerializedName(value = "sold", alternate = {"soldCount", "soldQuantity"})
    private int sold;

    @SerializedName("isActive")
    private boolean isActive;

    // Danh sách các tùy chọn màu sắc biến thể của sản phẩm
    @SerializedName("colors")
    private List<ProductColor> colors = new ArrayList<>();

    // Danh sách các nhận xét/đánh giá sản phẩm của người dùng
    @SerializedName("reviews")
    private List<ProductReview> reviews = new ArrayList<>();

    public Product() {
    }

    public Product(String _id, String name, String price, String image, Category category, String description) {
        this._id = _id;
        this.name = name;
        this.rawPrice = price;
        this.image = image;
        this.category = category;
        this.description = description;
    }

    public String get_id() {
        return _id != null ? _id : "";
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
        String rawImg = image != null ? image : (imageUrl != null ? imageUrl : "");
        return ImageUtils.getCorrectedImageUrl(rawImg, HttpResquest.BASE_URL);
    }

    public String getImageUrl() {
        return getImage();
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
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
        return reviewCount;
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

    public int getSold() {
        return sold;
    }

    public void setSold(int sold) {
        this.sold = sold;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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
