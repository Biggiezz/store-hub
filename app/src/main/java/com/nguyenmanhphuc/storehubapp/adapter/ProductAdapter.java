package com.nguyenmanhphuc.storehubapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.ProductDetailActivity;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.model.Product;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final Context context;
    private ArrayList<Product> listProduct;

    public ProductAdapter(Context context) {
        this.context = context;
        this.listProduct = new ArrayList<>();
    }

    public void updateData(ArrayList<Product> list) {
        this.listProduct = new ArrayList<>();
        if (list != null) {
            for (Product p : list) {
                boolean isActive = p.isActive();
                if (isActive) {
                    this.listProduct.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = listProduct.get(position);
        holder.tvProductCategory.setText(product.getCategory() != null ? getLocalizedCategoryName(product.getCategory().getName()) : "");
        holder.tvProductName.setText(product.getName());

        if (product.getStock() <= 0) {
            holder.tvProductStatus.setVisibility(View.VISIBLE);
            holder.tvProductStatus.setText(context.getString(R.string.out_of_stock).toUpperCase(java.util.Locale.getDefault()));
        } else {
            holder.tvProductStatus.setVisibility(View.GONE);
        }

        try {
            double priceValue = Double.parseDouble(product.getPrice());
            holder.tvProductPrice.setText(String.format(context.getString(R.string.price_format_float), priceValue).replace(',', '.'));
        } catch (NumberFormatException e) {
            holder.tvProductPrice.setText(product.getPrice() + context.getString(R.string.currency_suffix));
        }

        String imgUrl = product.getImage();
        Log.d("ProductAdapter", "Loading image: " + imgUrl);

        Glide.with(context)
                .load(product.getImage())
                .placeholder(R.drawable.ic_products)
                .error(R.drawable.ic_products)
                .listener(new com.bumptech.glide.request.RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                        Log.e("ProductAdapter", "Image load failed for URL: " + product.getImage() + " (Product: " + product.getName() + ")", e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.imgProduct);

        holder.tvSold.setText(context.getString(R.string.sold_label) + " " + product.getSold());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            String pid = product.get_id();
            Log.d("ProductAdapter", "Opening detail for ID: " + pid);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, pid);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listProduct.size();
    }

    private String getLocalizedCategoryName(String rawName) {
        if (rawName == null) return "";
        String lower = rawName.trim().toLowerCase();
        if (lower.contains("điện thoại") || lower.contains("phone")) {
            return context.getString(R.string.category_phones);
        } else if (lower.contains("máy tính") || lower.contains("computer") || lower.contains("laptop")) {
            return context.getString(R.string.category_computers);
        } else if (lower.contains("tai nghe") || lower.contains("headphone") || lower.contains("earphone")) {
            return context.getString(R.string.category_headphones);
        } else if (lower.contains("đồng hồ") || lower.contains("watch")) {
            return context.getString(R.string.category_watches);
        }
        return rawName;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductCategory, tvProductName, tvProductPrice, tvProductStatus;
        TextView tvSold;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductStatus = itemView.findViewById(R.id.tvProductStatus);
            tvSold = itemView.findViewById(R.id.tvSold);
        }
    }
}
