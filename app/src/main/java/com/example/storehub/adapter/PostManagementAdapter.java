package com.example.storehub.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storehub.R;
import com.example.storehub.model.News;

import java.util.ArrayList;

public class PostManagementAdapter extends RecyclerView.Adapter<PostManagementAdapter.ViewHolder> {

    private final Context context;
    private ArrayList<News> listNews;
    private OnPostActionListener listener;

    public interface OnPostActionListener {
        void onDelete(News news);
        void onClick(News news);
    }

    public PostManagementAdapter(Context context, OnPostActionListener listener) {
        this.context = context;
        this.listNews = new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(ArrayList<News> list) {
        this.listNews = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_management, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        News news = listNews.get(position);
        holder.tvNewsTitle.setText(news.getTitle());
        holder.tvNewsDesc.setText(news.getContent());
        holder.tvNewsTime.setText(news.getCreatedAt());
        holder.tvStatus.setText(news.getStatus());

        Glide.with(context)
                .load(news.getImage())
                .placeholder(R.drawable.ic_new)
                .error(R.drawable.ic_new)
                .into(holder.ivNewsImage);

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(news);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(news);
        });
    }

    @Override
    public int getItemCount() {
        return listNews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivNewsImage, btnDelete;
        TextView tvNewsTitle, tvNewsDesc, tvNewsTime, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivNewsImage = itemView.findViewById(R.id.ivNewsImage);
            tvNewsTitle = itemView.findViewById(R.id.tvNewsTitle);
            tvNewsDesc = itemView.findViewById(R.id.tvNewsDesc);
            tvNewsTime = itemView.findViewById(R.id.tvNewsTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
