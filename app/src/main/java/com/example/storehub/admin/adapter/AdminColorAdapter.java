package com.example.storehub.admin.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.R;
import com.example.storehub.model.ProductColor;

import java.util.List;

public class AdminColorAdapter extends RecyclerView.Adapter<AdminColorAdapter.ColorViewHolder> {
    private final List<ProductColor> colors;

    public AdminColorAdapter(List<ProductColor> colors) {
        this.colors = colors;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
        View view = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_color_circle, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        ProductColor color = colors.get(position);
        try {
            String hex = color.getHex();
            if (hex != null) {
                if (!hex.startsWith("#")) {
                    hex = "#" + hex;
                }
                holder.viewColor.setBackgroundColor(android.graphics.Color.parseColor(hex));
            } else {
                holder.viewColor.setBackgroundColor(android.graphics.Color.LTGRAY);
            }
        } catch (Exception e) {
            holder.viewColor.setBackgroundColor(android.graphics.Color.LTGRAY);
        }
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        View viewColor;

        ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColor = itemView.findViewById(R.id.viewColor);
        }
    }
}
