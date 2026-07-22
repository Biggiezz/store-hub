package com.example.storehub.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storehub.R;
import com.example.storehub.model.User;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private final Context context;
    private List<User> userList;
    private OnUserClickListener listener;
    private User currentUser;

    public UserManagementAdapter(Context context) {
        this.context = context;
        this.userList = new ArrayList<>();
    }

    public UserManagementAdapter(Context context, User currentUser) {
        this.context = context;
        this.currentUser = currentUser;
        this.userList = new ArrayList<>();
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        notifyDataSetChanged();
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<User> list) {
        this.userList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUserName.setText(user.getName());

        String role = !TextUtils.isEmpty(user.getRole()) ? user.getRole() : "Khách hàng";
        holder.tvUserRole.setText(role);

        // Đánh dấu huy hiệu nổi bật cho Super Admin
        if (user.isSuperAdmin()) {
            holder.tvUserRole.setBackgroundResource(R.drawable.bg_badge_super_admin);
            holder.tvUserRole.setTextColor(Color.WHITE);
        } else {
            holder.tvUserRole.setBackgroundResource(R.drawable.bg_badge_role);
            holder.tvUserRole.setTextColor(Color.parseColor("#675C53"));
        }

        holder.tvUserEmail.setText(user.getEmail());

        String lastActive = user.getLastActive();
        if (!lastActive.startsWith("Hoạt động")) {
            lastActive = "Hoạt động: " + lastActive;
        }
        holder.tvUserLastActive.setText(lastActive);

        Glide.with(context)
                .load(user.getImage())
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .into(holder.ivUserAvatar);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivUserAvatar;
        TextView tvUserName, tvUserRole, tvUserEmail, tvUserLastActive;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserLastActive = itemView.findViewById(R.id.tvUserLastActive);
        }
    }
}
