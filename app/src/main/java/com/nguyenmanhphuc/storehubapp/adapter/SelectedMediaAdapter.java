package com.nguyenmanhphuc.storehubapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.R;

import java.util.ArrayList;

public class SelectedMediaAdapter extends RecyclerView.Adapter<SelectedMediaAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Uri> mediaUris;
    private final OnItemRemoveListener removeListener;

    public interface OnItemRemoveListener {
        void onItemRemove(int position);
    }

    public SelectedMediaAdapter(Context context, ArrayList<Uri> mediaUris, OnItemRemoveListener removeListener) {
        this.context = context;
        this.mediaUris = mediaUris;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_selected_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = mediaUris.get(position);

        // Load image or video thumbnail using Glide
        Glide.with(context)
                .load(uri)
                .centerCrop()
                .into(holder.ivThumbnail);

        // Check if the URI points to a video
        boolean isVideo = false;
        String type = context.getContentResolver().getType(uri);
        if (type != null && type.startsWith("video")) {
            isVideo = true;
        } else {
            String path = uri.toString().toLowerCase();
            if (path.endsWith(".mp4") || path.endsWith(".3gp") || path.endsWith(".mkv") || path.endsWith(".webm")) {
                isVideo = true;
            }
        }

        holder.ivPlayIcon.setVisibility(isVideo ? View.VISIBLE : View.GONE);

        holder.btnRemove.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && removeListener != null) {
                removeListener.onItemRemove(currentPos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaUris.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        ImageView ivPlayIcon;
        ImageView btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
