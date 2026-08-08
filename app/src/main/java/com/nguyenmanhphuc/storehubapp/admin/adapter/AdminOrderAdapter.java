package com.nguyenmanhphuc.storehubapp.admin.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.model.Order;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private final Context context;
    private final ArrayList<Order> orders = new ArrayList<>();
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onViewDetailsClick(Order order);
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

    private String getLocalizedStatus(String status) {
        if (status == null) return "";
        if ("Chờ xác nhận".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status) || "Chờ xử lý".equalsIgnoreCase(status)) {
            return context.getString(R.string.status_pending);
        }
        if ("Đã xác nhận".equalsIgnoreCase(status) || "Confirmed".equalsIgnoreCase(status)) {
            return context.getString(R.string.xml_da_xac_nhan);
        }
        if ("Đã rời kho".equalsIgnoreCase(status) || "Left Warehouse".equalsIgnoreCase(status)) {
            return context.getString(R.string.status_dispatched);
        }
        if ("Đang giao hàng".equalsIgnoreCase(status) || "Shipping".equalsIgnoreCase(status)) {
            return context.getString(R.string.status_shipping);
        }
        if ("Đã giao hàng".equalsIgnoreCase(status) || "Đã hoàn thành".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
            return context.getString(R.string.status_completed);
        }
        if ("Đã hủy".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
            return context.getString(R.string.status_cancelled);
        }
        return status;
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCode, tvOrderPaymentMethod, tvOrderDate, tvRecipientNamePhone, tvRecipientAddress, tvItemsCount, tvTotalPrice, tvOrderStatus;
        MaterialButton btnViewDetails, btnUpdateStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvOrderPaymentMethod = itemView.findViewById(R.id.tvOrderPaymentMethod);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvRecipientNamePhone = itemView.findViewById(R.id.tvRecipientNamePhone);
            tvRecipientAddress = itemView.findViewById(R.id.tvRecipientAddress);
            tvItemsCount = itemView.findViewById(R.id.tvItemsCount);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewOrderDetails);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }

        public void bind(Order order) {
            tvOrderCode.setText(order.getOrderCode() != null ? order.getOrderCode() : itemView.getContext().getString(R.string.no_order_code));
            tvOrderPaymentMethod.setText("ZaloPay".equalsIgnoreCase(order.getPaymentMethod()) ? "ZaloPay" : "COD");
            
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
                tvRecipientNamePhone.setText(itemView.getContext().getString(R.string.no_recipient_info_order));
            } else {
                tvRecipientNamePhone.setText(name + "  •  " + phone);
            }

            String address = order.getRecipientAddress();
            tvRecipientAddress.setText(address.isEmpty() ? itemView.getContext().getString(R.string.no_address_order) : address);

            // Bind items count
            int itemsCount = order.getItems() != null ? order.getItems().size() : 0;
            tvItemsCount.setText(itemView.getContext().getString(R.string.items_count_prefix, itemsCount));

            // Bind total price
            long totalPrice = (long) order.getTotalPrice();
            tvTotalPrice.setText(formatPrice(totalPrice));

            // Bind status
            String status = order.getStatus();
            tvOrderStatus.setText(status != null ? getLocalizedStatus(status) : itemView.getContext().getString(R.string.status_pending));

            // Update status tag background depending on status
            if ("Đã giao hàng".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status) || "Đã hoàn thành".equalsIgnoreCase(status)) {
                tvOrderStatus.setBackgroundResource(R.drawable.bg_status_completed);
                tvOrderStatus.setTextColor(Color.parseColor("#2E7D32"));
            } else if ("Đã hủy".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status) || "cancel".equalsIgnoreCase(status)) {
                tvOrderStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                tvOrderStatus.setTextColor(Color.parseColor("#C62828"));
            } else if ("Chờ xác nhận".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status) || "Chờ xử lý".equalsIgnoreCase(status)) {
                tvOrderStatus.setBackgroundResource(R.drawable.bg_status_pending);
                tvOrderStatus.setTextColor(Color.parseColor("#B78103"));
            } else {
                tvOrderStatus.setBackgroundResource(R.drawable.bg_status_shipping);
                tvOrderStatus.setTextColor(Color.parseColor("#1565C0"));
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailsClick(order);
                }
            });
            btnViewDetails.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetailsClick(order);
                }
            });
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
