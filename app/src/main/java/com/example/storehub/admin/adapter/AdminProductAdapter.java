package com.example.storehub.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storehub.R;
import com.example.storehub.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {
    public interface OnProductClickListener { void onProductClick(Product product); }

    private final List<Product> products = new ArrayList<>();
    private final OnProductClickListener listener;

    public AdminProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Product> data) {
        products.clear();
        if (data != null) products.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product_preview, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.category.setText(product.getCategory());
        holder.name.setText(product.getName());
        holder.price.setText(NumberFormat.getNumberInstance(new Locale("vi", "VN"))
                .format(product.getPriceAsLong()) + "đ");
        Glide.with(holder.image)
                .load(product.getImage())
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .centerCrop()
                .into(holder.image);
        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
    }

    @Override public int getItemCount() { return products.size(); }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView category, name, price;
        ProductViewHolder(@NonNull View view) {
            super(view);
            image = view.findViewById(R.id.ivAdminProduct);
            category = view.findViewById(R.id.tvAdminProductCategory);
            name = view.findViewById(R.id.tvAdminProductName);
            price = view.findViewById(R.id.tvAdminProductPrice);
        }
    }
}
