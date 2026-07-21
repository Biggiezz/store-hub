package com.example.storehub.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storehub.OrderDetailActivity;
import com.example.storehub.R;
import com.example.storehub.model.Order;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<Order> orderList;
    private OnOrderCancelClickListener cancelClickListener;

    public interface OnOrderCancelClickListener {
        void onCancelClick(Order order);
    }

    public OrderAdapter(Context context, OnOrderCancelClickListener cancelClickListener) {
        this.context = context;
        this.orderList = new ArrayList<>();
        this.cancelClickListener = cancelClickListener;
    }

    public void setOrders(ArrayList<Order> list) {
        this.orderList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderId.setText("Mã đơn: " + order.getId());
        holder.tvProductName.setText(order.getProductName());
        holder.tvProductQuantity.setText("Số lượng: " + order.getQuantity());

        DecimalFormat formatter = new DecimalFormat("#,###đ");
        holder.tvOrderTotal.setText(formatter.format(order.getTotalAmount()));

        // Tải ảnh sản phẩm bằng Glide
        Glide.with(context)
                .load(order.getProductImage())
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(holder.ivProductImage);

        // Thiết lập trạng thái đơn hàng (Badge status)
        switch (order.getStatus()) {
            case "shipping":
            case "processing":
                holder.tvOrderStatusBadge.setText("Đang giao hàng");
                holder.tvOrderStatusBadge.setBackgroundResource(R.drawable.bg_status_delivering);
                holder.tvOrderStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
                holder.btnCancelOrder.setVisibility(View.VISIBLE);
                break;
            case "completed":
                holder.tvOrderStatusBadge.setText("Đã hoàn thành");
                holder.tvOrderStatusBadge.setBackgroundResource(R.drawable.bg_status_completed);
                holder.tvOrderStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.dark_green));
                holder.btnCancelOrder.setVisibility(View.GONE);
                break;
            case "cancelled":
                holder.tvOrderStatusBadge.setText("Đã hủy");
                holder.tvOrderStatusBadge.setBackgroundResource(R.drawable.bg_status_cancelled);
                holder.tvOrderStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.dark_green));
                holder.btnCancelOrder.setVisibility(View.GONE);
                break;
            default:
                holder.tvOrderStatusBadge.setText(order.getStatusText());
                holder.tvOrderStatusBadge.setBackgroundResource(R.drawable.bg_status_delivering);
                holder.btnCancelOrder.setVisibility(View.GONE);
                break;
        }

        // Sự kiện nút Hủy đơn
        holder.btnCancelOrder.setOnClickListener(v -> {
            if (cancelClickListener != null) {
                cancelClickListener.onCancelClick(order);
            }
        });

        // Sự kiện Mở Chi tiết đơn hàng
        holder.btnOrderDetail.setOnClickListener(v -> openOrderDetail(order));
        holder.itemView.setOnClickListener(v -> openOrderDetail(order));
    }

    private void openOrderDetail(Order order) {
        Intent intent = new Intent(context, OrderDetailActivity.class);
        intent.putExtra("order_item", order);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderStatusBadge, tvProductName, tvProductQuantity, tvOrderTotal;
        ShapeableImageView ivProductImage;
        MaterialButton btnCancelOrder, btnOrderDetail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderStatusBadge = itemView.findViewById(R.id.tvOrderStatusBadge);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
            btnOrderDetail = itemView.findViewById(R.id.btnOrderDetail);
        }
    }
}
