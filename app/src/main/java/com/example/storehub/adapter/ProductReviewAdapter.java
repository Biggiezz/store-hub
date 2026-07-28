package com.example.storehub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storehub.R;
import com.example.storehub.model.Product.ProductReview;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ProductReviewAdapter extends RecyclerView.Adapter<ProductReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private final ArrayList<ProductReview> reviewsList = new ArrayList<>();

    public ProductReviewAdapter(Context context) {
        this.context = context;
    }

    public void updateData(List<ProductReview> newList) {
        reviewsList.clear();
        if (newList != null) {
            reviewsList.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ProductReview review = reviewsList.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviewsList.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgReviewAvatar;
        TextView tvReviewerName, tvReviewDate, tvReviewContent;
        RatingBar ratingReview;
        View layoutAdminReply;
        TextView tvReplyTime, tvAdminReply;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgReviewAvatar = itemView.findViewById(R.id.imgReviewAvatar);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            ratingReview = itemView.findViewById(R.id.ratingReview);
            layoutAdminReply = itemView.findViewById(R.id.layoutAdminReply);
            tvReplyTime = itemView.findViewById(R.id.tvReplyTime);
            tvAdminReply = itemView.findViewById(R.id.tvAdminReply);
        }

        public void bind(ProductReview review) {
            tvReviewerName.setText(review.getCustomerName().isEmpty() ? "Khách hàng" : review.getCustomerName());
            
            String dateStr = review.getCreatedAt();
            if (dateStr != null && dateStr.contains("T")) {
                dateStr = dateStr.split("T")[0];
            }
            tvReviewDate.setText(dateStr != null ? dateStr : "");
            
            ratingReview.setRating(review.rating);
            tvReviewContent.setText(review.content != null ? review.content : "");

            String avatarUrl = review.getCustomerImage();
            Glide.with(context)
                    .load(avatarUrl != null && !avatarUrl.isEmpty() ? avatarUrl : R.drawable.ic_avatar)
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .into(imgReviewAvatar);

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
