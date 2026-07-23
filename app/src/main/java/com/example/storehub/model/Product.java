package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Product {
    // Mã MongoDB ID của sản phẩm
    @SerializedName("_id")
    private String _id;

    // Mã ID bổ trợ phục vụ tương thích với Mock data
    @SerializedName("id")
    private String id;

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
    private String category;

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

    // Danh sách các tùy chọn màu sắc biến thể của sản phẩm
    @SerializedName("colors")
    private List<ProductColor> colors = new ArrayList<>();

    // Danh sách các nhận xét/đánh giá sản phẩm của người dùng
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
        String rawImg = image != null ? image : (imageUrl != null ? imageUrl : "");
        return com.example.storehub.utils.ImageUtils.getCorrectedImageUrl(rawImg, com.example.storehub.services.HttpResquest.BASE_URL);
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

    public static class ProductColor {
        // Mã định danh màu sắc dạng Object
        @SerializedName("id")
        public Object id;

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

        public ProductColor(Object id, String name, String hex, boolean isDefault) {
            this.id = id;
            this.name = name;
            this.hex = hex;
            this.isDefault = isDefault;
        }

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

    public static class ProductReview implements java.io.Serializable {
        // Mã định danh nhận xét dạng Object
        @SerializedName("id")
        public Object id;

        // Mã MongoDB ID dạng chuỗi của nhận xét
        @SerializedName("_id")
        public String mongoId;

        // Họ tên của khách hàng viết nhận xét
        @SerializedName("customer_name")
        public String customerName;

        // Họ tên khách hàng (trường bổ trợ tương thích)
        @SerializedName("customerName")
        public String altCustomerName;

        // Ngày giờ tạo nhận xét
        @SerializedName("created_at")
        public String createdAt;

        // Ngày giờ tạo nhận xét (trường bổ trợ tương thích)
        @SerializedName("createdAt")
        public String altCreatedAt;

        // Đường dẫn hình ảnh đại diện khách hàng
        @SerializedName("customerImage")
        public String customerImage;

        // Đường dẫn hình ảnh đại diện khách hàng (trường bổ trợ tương thích)
        @SerializedName("customer_image")
        public String altCustomerImage;

        // Điểm đánh giá (1-5 sao)
        @SerializedName("rating")
        public float rating;

        // Nội dung nhận xét/đánh giá
        @SerializedName("content")
        public String content;

        public ProductReview() {
        }

        public String getId() {
            if (id != null) return String.valueOf(id);
            if (mongoId != null) return mongoId;
            return "";
        }

        public String getCustomerName() {
            return customerName != null ? customerName : (altCustomerName != null ? altCustomerName : "");
        }

        public String getCustomerImage() {
            return customerImage != null && !customerImage.isEmpty() ? customerImage : (altCustomerImage != null ? altCustomerImage : "");
        }

        public String getCreatedAt() {
            return createdAt != null ? createdAt : (altCreatedAt != null ? altCreatedAt : "");
        }

        // Nội dung phản hồi của quản trị viên (Admin)
        @SerializedName("replyContent")
        public String replyContent;

        // Thời điểm quản trị viên phản hồi
        @SerializedName("replyCreatedAt")
        public String replyCreatedAt;

        public String getReplyContent() {
            return replyContent != null ? replyContent : "";
        }

        public String getReplyCreatedAt() {
            return replyCreatedAt != null ? replyCreatedAt : "";
        }

        public static class AddRequest {
            @SerializedName("productId")
            private String productId;

            @SerializedName("customerName")
            private String customerName;

            @SerializedName("customerImage")
            private String customerImage;

            @SerializedName("rating")
            private float rating;

            @SerializedName("content")
            private String content;

            @SerializedName("orderId")
            private String orderId;

            public AddRequest() {
            }

            public AddRequest(String productId, String customerName, String customerImage, float rating, String content, String orderId) {
                this.productId = productId;
                this.customerName = customerName;
                this.customerImage = customerImage;
                this.rating = rating;
                this.content = content;
                this.orderId = orderId;
            }

            public AddRequest(String productId, String customerName, String customerImage, float rating, String content) {
                this(productId, customerName, customerImage, rating, content, null);
            }

            public AddRequest(String productId, String customerName, float rating, String content) {
                this(productId, customerName, "", rating, content, null);
            }

            public String getProductId() { return productId; }
            public String getCustomerName() { return customerName; }
            public String getCustomerImage() { return customerImage; }
            public float getRating() { return rating; }
            public String getContent() { return content; }
            public String getOrderId() { return orderId; }
        }

        public static class ReplyRequest {
            @SerializedName("productId")
            private final String productId;

            @SerializedName("reviewId")
            private final String reviewId;

            @SerializedName("replyContent")
            private final String replyContent;

            public ReplyRequest(String productId, String reviewId, String replyContent) {
                this.productId = productId;
                this.reviewId = reviewId;
                this.replyContent = replyContent;
            }

            public String getProductId() { return productId; }
            public String getReviewId() { return reviewId; }
            public String getReplyContent() { return replyContent; }
        }
    }
}
