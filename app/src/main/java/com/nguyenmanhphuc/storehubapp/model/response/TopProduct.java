package com.nguyenmanhphuc.storehubapp.model.response;

public class TopProduct {
    // Tên sản phẩm bán chạy
    private String name;
    // Đường dẫn hình ảnh sản phẩm
    private String image;
    // Số lượng sản phẩm đã được bán ra
    private int soldCount;

    public String getName() { return name; }
    public String getImage() { return image; }
    public int getSoldCount() { return soldCount; }
}
