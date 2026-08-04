package com.nguyenmanhphuc.storehubapp.model.request;

import com.google.gson.annotations.SerializedName;

public class ReplyReviewRequest {
    // Mã ID sản phẩm chứa nhận xét cần phản hồi
    @SerializedName("productId")
    private final String productId;

    // Mã ID nhận xét cần phản hồi
    @SerializedName("reviewId")
    private final String reviewId;

    // Nội dung phản hồi từ phía Admin
    @SerializedName("replyContent")
    private final String replyContent;

    public ReplyReviewRequest(String productId, String reviewId, String replyContent) {
        this.productId = productId;
        this.reviewId = reviewId;
        this.replyContent = replyContent;
    }

    public String getProductId() { return productId; }
    public String getReviewId() { return reviewId; }
    public String getReplyContent() { return replyContent; }
}
