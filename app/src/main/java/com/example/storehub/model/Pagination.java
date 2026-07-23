package com.example.storehub.model;

public class Pagination {
    // Tổng số lượng sản phẩm/bản ghi
    private int totalProducts;
    // Trang hiện tại đang hiển thị
    private int currentPage;
    // Tổng số trang phân trang
    private int totalPages;
    // Số lượng sản phẩm/bản ghi tối đa trên một trang
    private int limit;

    public Pagination(int totalProducts, int currentPage, int totalPages, int limit) {
        this.totalProducts = totalProducts;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.limit = limit;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
