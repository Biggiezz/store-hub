package com.example.storehub.admin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.R;
import com.example.storehub.model.Order;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private final Context context;
    private final ArrayList<Order> orders = new ArrayList<>();
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onUpdateStatusClick(Order order);
    }

    public AdminOrderAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void updateData(ArrayList<Order> newOrders) {
        orders.clear();
        if (newOrders != null) {
            orders.addAll(newOrders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCode, tvOrderDate, tvRecipientNamePhone, tvRecipientAddress, tvItemsCount, tvTotalPrice, tvOrderStatus;
        MaterialButton btnUpdateStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvRecipientNamePhone = itemView.findViewById(R.id.tvRecipientNamePhone);
            tvRecipientAddress = itemView.findViewById(R.id.tvRecipientAddress);
            tvItemsCount = itemView.findViewById(R.id.tvItemsCount);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }

        public void bind(Order order) {
            tvOrderCode.setText(order.getOrderCode() != null ? order.getOrderCode() : "Không có mã");
            
            // Format created date or fallback
            String dateStr = order.getCreatedAt();
            if (dateStr != null && dateStr.contains("T")) {
                // Split date from ISO format
                dateStr = dateStr.split("T")[0];
            }
            tvOrderDate.setText(dateStr != null ? dateStr : "");

            // Bind recipient details
            String name = order.getRecipientName();
            String phone = order.getRecipientPhone();
            if (name.isEmpty() && phone.isEmpty()) {
                tvRecipientNamePhone.setText("Chưa có thông tin nhận hàng");
            } else {
                tvRecipientNamePhone.setText(name + "  •  " + phone);
            }

            String address = order.getRecipientAddress();
            tvRecipientAddress.setText(address.isEmpty() ? "Chưa có địa chỉ" : address);

            // Bind items count
            int itemsCount = order.getItems() != null ? order.getItems().size() : 0;
            tvItemsCount.setText("Sản phẩm: " + itemsCount + " món");

            // Bind total price
            long totalPrice = (long) order.getTotalPrice();
            tvTotalPrice.setText(formatPrice(totalPrice));

            // Bind status
            String status = order.getStatus();
            tvOrderStatus.setText(status != null ? status : "Chờ xác nhận");

            // Update status tag background depending on status
            if ("Đã giao hàng".equals(status)) {
                tvOrderStatus.setBackgroundResource(R.drawable.bg_status_dark); // standard dark active
            } else if ("Đã hủy".equals(status)) {
                tvOrderStatus.setBackgroundResource(R.drawable.bg_order_status_cancelled);
            } else {
                tvOrderStatus.setBackgroundResource(R.drawable.bg_timeline_active);
            }

            // Click listener
            btnUpdateStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpdateStatusClick(order);
                }
            });
        }

        private String formatPrice(long price) {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(price);
        }
    }
}
