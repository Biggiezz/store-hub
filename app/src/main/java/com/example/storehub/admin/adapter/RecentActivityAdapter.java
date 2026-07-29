package com.example.storehub.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import android.content.res.ColorStateList;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.R;
import com.example.storehub.model.response.RecentActivity;
import com.example.storehub.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {
    private final ArrayList<RecentActivity> activities = new ArrayList<>();
    private final OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onActivityClick(RecentActivity activity);
    }

    public RecentActivityAdapter() {
        this(null);
    }

    public RecentActivityAdapter(OnActivityClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<RecentActivity> newActivities) {
        activities.clear();
        if (newActivities != null) {
            activities.addAll(newActivities);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentActivity activity = activities.get(position);
        holder.bind(activity, position == activities.size() - 1);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onActivityClick(activity);
        });
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivActivityIcon;
        private final TextView tvActivityTitle;
        private final TextView tvActivityDetail;
        private final TextView tvActivityTime;
        private final View viewDivider;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivActivityIcon = itemView.findViewById(R.id.ivActivityIcon);
            tvActivityTitle = itemView.findViewById(R.id.tvActivityTitle);
            tvActivityDetail = itemView.findViewById(R.id.tvActivityDetail);
            tvActivityTime = itemView.findViewById(R.id.tvActivityTime);
            viewDivider = itemView.findViewById(R.id.viewDivider);
        }

        void bind(RecentActivity activity, boolean isLast) {
            int iconResource = RecentActivityAdapter.getIcon(activity.getType());

            ivActivityIcon.setImageResource(iconResource);
            ivActivityIcon.setBackgroundTintList(ColorStateList.valueOf(RecentActivityAdapter.getIconBackground(activity.getType())));
            ivActivityIcon.setContentDescription(activity.getTitle());
            tvActivityTitle.setText(activity.getTitle());
            tvActivityDetail.setText(activity.getDetail() != null ? activity.getDetail() : "");
            tvActivityTime.setText(DateTimeUtils.formatISOToLocal(activity.getCreatedAt(), "dd/MM/yyyy HH:mm"));
            viewDivider.setVisibility(isLast ? View.GONE : View.VISIBLE);
        }
    }

    public static int getIcon(String type) {
            if ("order_created".equals(type)) {
                return R.drawable.ic_order_shipping;
            }
            if ("order_completed".equals(type)) {
                return R.drawable.ic_check;
            }
            if ("order_cancelled".equals(type)) {
                return R.drawable.ic_order_cancelled;
            }
            if ("product_created".equals(type)) {
                return R.drawable.ic_products;
            }
            if ("login_admin".equals(type) || "login_customer".equals(type)) {
                return R.drawable.ic_user_check;
            }
            if ("user_created".equals(type)) {
                return R.drawable.ic_users;
            }
            return R.drawable.ic_check_done;
        }

    public static int getIconBackground(String type) {
            if ("order_cancelled".equals(type)) {
                return Color.parseColor("#F9D8D8");
            }
            if ("order_created".equals(type)) {
                return Color.parseColor("#E6E3DD");
            }
            if ("product_created".equals(type)) {
                return Color.parseColor("#E6E3DD");
            }
            if ("login_admin".equals(type) || "login_customer".equals(type)) {
                return Color.parseColor("#DDE8C0");
            }
            if ("user_created".equals(type)) {
                return Color.parseColor("#E4EAD0");
            }
            return Color.parseColor("#DDE8C8");
        }
}
