package com.nguyenmanhphuc.storehubapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nguyenmanhphuc.storehubapp.R;

public class SlideShowAdapter extends RecyclerView.Adapter<SlideShowAdapter.ViewHolder> {
    private final int[] imgs = {
            R.drawable.img_slide1, R.drawable.img_slide2, R.drawable.img_slide3};

    private final int[] titleRes = {
            R.string.slide_title_1, R.string.slide_title_2, R.string.slide_title_3};

    private final int[] descRes = {
            R.string.slide_desc_1, R.string.slide_desc_2, R.string.slide_desc_3};

    private final Context context;

    public SlideShowAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_silder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imageView.setImageResource(imgs[position]);
        holder.titleView.setText(context.getString(titleRes[position]));
        holder.descriptionView.setText(context.getString(descRes[position]));
    }

    @Override
    public int getItemCount() {
        return imgs.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView descriptionView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgSlider);
            titleView = itemView.findViewById(R.id.tvSliderTitle);
            descriptionView = itemView.findViewById(R.id.tvSliderDescription);
        }
    }
}
