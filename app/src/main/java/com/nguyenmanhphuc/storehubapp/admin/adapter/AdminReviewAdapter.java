package com.nguyenmanhphuc.storehubapp.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.adapter.ReviewMediaAdapter;
import com.nguyenmanhphuc.storehubapp.model.ProductReview;
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
        public String productImage;

        public ReviewWithProduct(ProductReview review, String productName, String productId, String productImage) {
            this.review = review;
            this.productName = productName;
            this.productId = productId;
            this.productImage = productImage;
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
        RecyclerView rvReviewMedia;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            rvReviewMedia = itemView.findViewById(R.id.rvReviewMedia);
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

        protected void bindCommon(ReviewWithProduct item, ShapeableImageView imgAvatar, TextView tvCustomerName, TextView tvReviewTime, TextView tvProductName, TextView tvRating, TextView tvReviewContent, TextView tvSeeMore, ImageButton btnMoreMenu) {
            ProductReview review = item.review;
            tvCustomerName.setText(review.getCustomerName().isEmpty() ? context.getString(R.string.customer) : review.getCustomerName());

            String dateStr = review.getCreatedAt();
            if (dateStr != null && dateStr.contains("T")) {
                dateStr = dateStr.split("T")[0];
            }
            tvReviewTime.setText(dateStr != null ? dateStr : "");

            tvProductName.setText(item.productName != null ? item.productName : context.getString(R.string.nav_products));
            tvRating.setText(getStarString(review.rating));

            String contentText = review.content != null ? review.content : "";
            tvReviewContent.setText(contentText);

            if (tvSeeMore != null) {
                if (contentText.length() > 120) {
                    tvSeeMore.setVisibility(View.VISIBLE);
                    tvReviewContent.setMaxLines(4);
                    tvSeeMore.setText(context.getString(R.string.see_more));
                    tvSeeMore.setOnClickListener(v -> {
                        if (tvReviewContent.getMaxLines() == 4) {
                            tvReviewContent.setMaxLines(Integer.MAX_VALUE);
                            tvSeeMore.setText(context.getString(R.string.see_less));
                        } else {
                            tvReviewContent.setMaxLines(4);
                            tvSeeMore.setText(context.getString(R.string.see_more));
                        }
                    });
                } else {
                    tvSeeMore.setVisibility(View.GONE);
                }
            }

            if (btnMoreMenu != null) {
                btnMoreMenu.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(context, btnMoreMenu);
                    popup.getMenu().add(context.getString(R.string.action_reply_edit));
                    popup.getMenu().add(context.getString(R.string.action_hide_review));
                    popup.getMenu().add(context.getString(R.string.action_delete_review));
                    popup.setOnMenuItemClickListener(menuItem -> {
                        String title = menuItem.getTitle().toString();
                        if (title.equals(context.getString(R.string.action_reply_edit))) {
                            if (listener != null) {
                                listener.onReplyClick(item);
                            }
                        } else {
                            Toast.makeText(context, context.getString(R.string.feature_under_development, title), Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    });
                    popup.show();
                });
            }

            // Bind media files (images/videos)
            if (review.getMedia() != null && !review.getMedia().isEmpty()) {
                rvReviewMedia.setVisibility(View.VISIBLE);
                rvReviewMedia.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
                ReviewMediaAdapter mediaAdapter = new ReviewMediaAdapter(context, review.getMedia());
                rvReviewMedia.setAdapter(mediaAdapter);
            } else {
                rvReviewMedia.setVisibility(View.GONE);
            }

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
        TextView tvSeeMore;
        ImageButton btnMoreMenu;
        MaterialButton btnReply;

        public UnansweredViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvReviewTime = itemView.findViewById(R.id.tvReviewTime);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            tvSeeMore = itemView.findViewById(R.id.tvSeeMore);
            btnMoreMenu = itemView.findViewById(R.id.btnMoreMenu);
            btnReply = itemView.findViewById(R.id.btnReply);
        }

        @Override
        public void bind(ReviewWithProduct item) {
            bindCommon(item, imgAvatar, tvCustomerName, tvReviewTime, tvProductName, tvRating, tvReviewContent, tvSeeMore, btnMoreMenu);
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
        TextView tvSeeMore;
        ImageButton btnMoreMenu;
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
            tvSeeMore = itemView.findViewById(R.id.tvSeeMore);
            btnMoreMenu = itemView.findViewById(R.id.btnMoreMenu);
            tvAdminName = itemView.findViewById(R.id.tvAdminName);
            tvReplyTime = itemView.findViewById(R.id.tvReplyTime);
            tvAdminReply = itemView.findViewById(R.id.tvAdminReply);
            btnEditReply = itemView.findViewById(R.id.btnEditReply);
        }

        @Override
        public void bind(ReviewWithProduct item) {
            bindCommon(item, imgAvatar, tvCustomerName, tvReviewTime, tvProductName, tvRating, tvReviewContent, tvSeeMore, btnMoreMenu);

            ProductReview review = item.review;
            tvAdminName.setText(context.getString(R.string.shop_reply));
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
