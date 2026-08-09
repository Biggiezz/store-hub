package com.nguyenmanhphuc.storehubapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.utils.DateTimeUtils;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public interface OnUserDeleteListener {
        void onUserDelete(User user);
    }

    private final Context context;
    private List<User> userList;
    private OnUserClickListener listener;
    private OnUserDeleteListener deleteListener;
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

    public void setOnUserDeleteListener(OnUserDeleteListener listener) {
        this.deleteListener = listener;
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

        String role = !TextUtils.isEmpty(user.getRole()) ? user.getRole() : "customer";
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

        String rawLastActive = user.getLastActive();
        String lastActive = DateTimeUtils.getRelativeTime(rawLastActive, user.isOnline());

        if (lastActive.contains("Đang hoạt động")) {
            holder.tvUserLastActive.setTextColor(Color.parseColor("#2E7D32")); // Xanh lá cây
        } else {
            holder.tvUserLastActive.setTextColor(Color.parseColor("#000000")); // Màu đen
        }

        if (!lastActive.startsWith("Hoạt động")) {
            lastActive = "Hoạt động: " + lastActive;
        }
        holder.tvUserLastActive.setText(lastActive);

        Glide.with(context)
                .load(user.getImage())
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .thumbnail(Glide.with(context).load(user.getImage()).override(10))
                .into(holder.ivUserAvatar);

        // Hiển thị nút xóa nếu là Super Admin và không phải là chính mình
        if (currentUser != null && currentUser.isSuperAdmin() && !user.getId().equals(currentUser.getId())) {
            holder.btnDeleteUser.setVisibility(View.VISIBLE);
        } else {
            holder.btnDeleteUser.setVisibility(View.GONE);
        }

        holder.btnDeleteUser.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onUserDelete(user);
            }
        });

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
        ImageButton btnDeleteUser;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserRole = itemView.findViewById(R.id.tvUserRole);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserLastActive = itemView.findViewById(R.id.tvUserLastActive);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
