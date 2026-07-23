package com.example.storehub.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storehub.ProductDetailActivity;
import com.example.storehub.R;
import com.example.storehub.model.Product;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private final Context context;
    private ArrayList<Product> listProduct;

    public ProductAdapter(Context context) {
        this.context = context;
        this.listProduct = new ArrayList<>();
    }

    public void updateData(ArrayList<Product> list) {
        this.listProduct = list;
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
        holder.tvProductName.setText(product.getName());

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
                .into(holder.imgProduct);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            String pid = product.get_id();
            if (pid == null || pid.isEmpty()) pid = product.getId();
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
        TextView tvProductName;
        TextView tvProductPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
        }
    }
}
