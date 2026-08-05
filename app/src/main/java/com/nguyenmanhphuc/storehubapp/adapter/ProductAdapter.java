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
        holder.tvProductCategory.setText(product.getCategory() != null ? product.getCategory().getName() : "");
        holder.tvProductName.setText(product.getName());

        if (product.getStock() <= 0) {
            holder.tvProductStatus.setVisibility(View.VISIBLE);
            holder.tvProductStatus.setText("HẾT HÀNG");
        } else {
            holder.tvProductStatus.setVisibility(View.GONE);
        }

        try {
            double priceValue = Double.parseDouble(product.getPrice());
            holder.tvProductPrice.setText(String.format("%,.0fđ", priceValue).replace(',', '.'));
        } catch (NumberFormatException e) {
            holder.tvProductPrice.setText(product.getPrice() + "đ");
        }

        Glide.with(context)
                .load(product.getImage())
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
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
