package com.nguyenmanhphuc.storehubapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.model.ProductReview;

import java.util.ArrayList;
import java.util.List;

public class ProductReviewAdapter extends RecyclerView.Adapter<ProductReviewAdapter.ReviewViewHolder> {

    private final ArrayList<ProductReview> reviewsList = new ArrayList<>();
    private boolean showAllReviews;

    public ProductReviewAdapter() {}

    public void updateData(List<ProductReview> newList) {
        reviewsList.clear();
        if (newList != null) {
            reviewsList.addAll(newList);
        }
        showAllReviews = false;
        notifyDataSetChanged();
    }

    public void showAll() {
        if (!showAllReviews) {
            showAllReviews = true;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ProductReview review = reviewsList.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return showAllReviews ? reviewsList.size() : Math.min(2, reviewsList.size());
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewerName, tvReviewDate, tvReviewContent;
        RatingBar ratingReview;
        RecyclerView rvReviewMedia;
        View layoutAdminReply;
        TextView tvReplyTime, tvAdminReply;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            ratingReview = itemView.findViewById(R.id.ratingReview);
            rvReviewMedia = itemView.findViewById(R.id.rvReviewMedia);
            layoutAdminReply = itemView.findViewById(R.id.layoutAdminReply);
            tvReplyTime = itemView.findViewById(R.id.tvReplyTime);
            tvAdminReply = itemView.findViewById(R.id.tvAdminReply);
        }

        public void bind(ProductReview review) {
            tvReviewerName.setText(review.getCustomerName().isEmpty() ? itemView.getContext().getString(R.string.customer) : review.getCustomerName());
            
            String dateStr = review.getCreatedAt();
            if (dateStr != null && dateStr.contains("T")) {
                dateStr = dateStr.split("T")[0];
            }
            tvReviewDate.setText(dateStr != null ? dateStr : "");
            
            ratingReview.setRating(review.rating);
            tvReviewContent.setText(review.content != null ? review.content : "");

            // Bind media files (images/videos)
            if (review.getMedia() != null && !review.getMedia().isEmpty()) {
                rvReviewMedia.setVisibility(View.VISIBLE);
                rvReviewMedia.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                ReviewMediaAdapter mediaAdapter = new ReviewMediaAdapter(itemView.getContext(), review.getMedia());
                rvReviewMedia.setAdapter(mediaAdapter);
            } else {
                rvReviewMedia.setVisibility(View.GONE);
            }

            // Bind admin reply
            if (review.getReplyContent() != null && !review.getReplyContent().isEmpty()) {
                layoutAdminReply.setVisibility(View.VISIBLE);
                tvAdminReply.setText(review.getReplyContent());
                tvReplyTime.setText(review.getReplyCreatedAt());
            } else {
                layoutAdminReply.setVisibility(View.GONE);
            }
        }
    }
}
