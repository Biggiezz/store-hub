package com.example.storehub.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storehub.R;
import com.example.storehub.model.CartItem;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartChangeListener {
        void onQuantityChange(CartItem cartItem, int newQuantity);

        void onDeleteItem(CartItem cartItem);
    }

    private final Context context;
    private List<CartItem> listCartItems;
    private OnCartChangeListener listener;

    public CartAdapter(Context context) {
        this.context = context;
        this.listCartItems = new ArrayList<>();
    }

    public void setOnCartChangeListener(OnCartChangeListener listener) {
        this.listener = listener;
    }

    public void updateData(List<CartItem> list) {
        this.listCartItems = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_product, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = listCartItems.get(position);

        holder.tvProductName.setText(item.getProductName());

        if (!TextUtils.isEmpty(item.getColorName())) {
            holder.layoutVariant.setVisibility(View.VISIBLE);
            holder.tvVariant.setText("Màu: " + item.getColorName());
            if (!TextUtils.isEmpty(item.getColorHex())) {
                holder.viewColorPreview.setVisibility(View.VISIBLE);
                holder.viewColorPreview.setBackground(createColorCircleDrawable(item.getColorHex()));
            } else {
                holder.viewColorPreview.setVisibility(View.GONE);
            }
        } else {
            holder.layoutVariant.setVisibility(View.GONE);
        }

        holder.tvProductPrice.setText(formatPrice(item.getTotalItemPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        Glide.with(context)
                .load(item.getProductImage())
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(holder.ivProduct);

        holder.btnDecrease.setOnClickListener(v -> {
            if (listener != null) {
                int newQty = item.getQuantity() - 1;
                listener.onQuantityChange(item, newQty);
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            if (listener != null) {
                int newQty = item.getQuantity() + 1;
                listener.onQuantityChange(item, newQty);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteItem(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listCartItems.size();
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                new Locale("vi", "VN")
        );
        return formatter.format(price);
    }

    private android.graphics.drawable.GradientDrawable createColorCircleDrawable(String hexColor) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        int color = parseColorSafely(hexColor);
        drawable.setColor(color);
        if (isLightColor(color)) {
            drawable.setStroke(Math.round(1 * context.getResources().getDisplayMetrics().density), android.graphics.Color.parseColor("#DDDDDD"));
        }
        return drawable;
    }

    private int parseColorSafely(String color) {
        try {
            if (android.text.TextUtils.isEmpty(color)) return android.graphics.Color.TRANSPARENT;
            if (!color.startsWith("#")) color = "#" + color;
            return android.graphics.Color.parseColor(color);
        } catch (Exception ignored) {
            return android.graphics.Color.TRANSPARENT;
        }
    }

    private boolean isLightColor(int color) {
        double luminance = (0.299 * android.graphics.Color.red(color) + 0.587 * android.graphics.Color.green(color) + 0.114 * android.graphics.Color.blue(color)) / 255;
        return luminance > 0.85;
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivProduct;
        TextView tvProductName, tvVariant, tvProductPrice, btnDecrease, tvQuantity, btnIncrease;
        ImageButton btnDelete;
        View layoutVariant, viewColorPreview;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvVariant = itemView.findViewById(R.id.tvVariant);
            layoutVariant = itemView.findViewById(R.id.layoutVariant);
            viewColorPreview = itemView.findViewById(R.id.viewColorPreview);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
