package com.example.storehub.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storehub.R;
import com.example.storehub.model.Product.ProductReview;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class AdminReviewAdapter extends RecyclerView.Adapter<AdminReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private final ArrayList<ReviewWithProduct> list = new ArrayList<>();
    private final OnReviewClickListener listener;

    public interface OnReviewClickListener {
        void onReplyClick(ReviewWithProduct review);
    }

    public static class ReviewWithProduct {
        public ProductReview review;
        public String productName;
        public String productId;

        public ReviewWithProduct(ProductReview review, String productName, String productId) {
            this.review = review;
            this.productName = productName;
            this.productId = productId;
        }
    }

    public AdminReviewAdapter(Context context, OnReviewClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updateData(ArrayList<ReviewWithProduct> newList) {
        list.clear();
        if (newList != null) {
            list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ReviewWithProduct item = list.get(position);
        if (item.review.getReplyContent() != null && !item.review.getReplyContent().isEmpty()) {
            return 1; // Answered
        } else {
            return 0; // Unanswered
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_review_answered, parent, false);
            return new AnsweredViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_review_unanswered, parent, false);
            return new UnansweredViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewWithProduct item = list.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    abstract class ReviewViewHolder extends RecyclerView.ViewHolder {
        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
        }
        public abstract void bind(ReviewWithProduct item);

        protected String getStarString(float rating) {
            int r = Math.round(rating);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                if (i < r) {
                    sb.append("★");
                } else {
                    sb.append("☆");
                }
            }
            return sb.toString();
        }

        protected void bindCommon(ReviewWithProduct item, ShapeableImageView imgAvatar, TextView tvCustomerName, TextView tvReviewTime, TextView tvProductName, TextView tvRating, TextView tvReviewContent) {
            ProductReview review = item.review;
            tvCustomerName.setText(review.getCustomerName().isEmpty() ? "Khách hàng" : review.getCustomerName());

            String dateStr = review.getCreatedAt();
            if (dateStr != null && dateStr.contains("T")) {
                dateStr = dateStr.split("T")[0];
            }
            tvReviewTime.setText(dateStr != null ? dateStr : "");

            tvProductName.setText(item.productName != null ? item.productName : "Sản phẩm");
            tvRating.setText(getStarString(review.rating));
            tvReviewContent.setText(review.content != null ? review.content : "");

            String avatarUrl = review.getCustomerImage();
            Glide.with(context)
                    .load(avatarUrl != null && !avatarUrl.isEmpty() ? avatarUrl : R.drawable.ic_avatar)
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .into(imgAvatar);
        }
    }

    class UnansweredViewHolder extends ReviewViewHolder {
        ShapeableImageView imgAvatar;
        TextView tvCustomerName, tvReviewTime, tvProductName, tvRating, tvReviewContent;
        MaterialButton btnReply;

        public UnansweredViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvReviewTime = itemView.findViewById(R.id.tvReviewTime);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            btnReply = itemView.findViewById(R.id.btnReply);
        }

        @Override
        public void bind(ReviewWithProduct item) {
            bindCommon(item, imgAvatar, tvCustomerName, tvReviewTime, tvProductName, tvRating, tvReviewContent);
            btnReply.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReplyClick(item);
                }
            });
        }
    }

    class AnsweredViewHolder extends ReviewViewHolder {
        ShapeableImageView imgAvatar;
        TextView tvCustomerName, tvReviewTime, tvProductName, tvRating, tvReviewContent;
        TextView tvAdminName, tvReplyTime, tvAdminReply;
        MaterialButton btnEditReply;

        public AnsweredViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvReviewTime = itemView.findViewById(R.id.tvReviewTime);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            tvAdminName = itemView.findViewById(R.id.tvAdminName);
            tvReplyTime = itemView.findViewById(R.id.tvReplyTime);
            tvAdminReply = itemView.findViewById(R.id.tvAdminReply);
            btnEditReply = itemView.findViewById(R.id.btnEditReply);
        }

        @Override
        public void bind(ReviewWithProduct item) {
            bindCommon(item, imgAvatar, tvCustomerName, tvReviewTime, tvProductName, tvRating, tvReviewContent);

            ProductReview review = item.review;
            tvAdminName.setText("Phản hồi từ Cửa hàng");
            tvReplyTime.setText(review.getReplyCreatedAt());
            tvAdminReply.setText(review.getReplyContent());

            btnEditReply.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReplyClick(item);
                }
            });
        }
    }
}
