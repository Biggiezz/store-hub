package com.example.storehub.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storehub.R;
import com.example.storehub.model.CartItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.ViewHolder> {

    private final Context context;
    private List<CartItem> listItems;

    public OrderProductAdapter(Context context) {
        this.context = context;
        this.listItems = new ArrayList<>();
    }

    public void updateData(List<CartItem> items) {
        this.listItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = listItems.get(position);

        holder.tvProductName.setText(item.getProductName());

        if (TextUtils.isEmpty(item.getColorName())) {
            holder.tvProductColor.setVisibility(View.GONE);
        } else {
            holder.tvProductColor.setVisibility(View.VISIBLE);
            holder.tvProductColor.setText("Phân loại: " + item.getColorName());
        }

        holder.tvProductQty.setText("Số lượng: " + item.getQuantity());
        holder.tvProductPrice.setText(formatPrice(item.getPrice()));

        Glide.with(context)
                .load(item.getProductImage())
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        TextView tvProductColor;
        TextView tvProductQty;
        TextView tvProductPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductColor = itemView.findViewById(R.id.tvProductColor);
            tvProductQty = itemView.findViewById(R.id.tvProductQty);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
        }
    }
}
