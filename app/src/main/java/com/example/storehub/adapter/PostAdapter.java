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

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<News> newsList;
    private PostItemListener listener;

    public interface PostItemListener {
        void onDeleteClick(News news);
        void onItemClick(News news);
        void onEditClick(News news);
    }

    public PostAdapter(Context context, List<News> newsList, PostItemListener listener) {
        this.context = context;
        this.newsList = newsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.txtTitle.setText(news.getTitle());
        holder.txtContent.setText(news.getContent());

        Glide.with(context)
                .load(news.getImage())
                .placeholder(R.drawable.ic_product)
                .into(holder.imgPost);

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(news));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(news));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(news));
    }

    @Override
    public int getItemCount() {
        return newsList != null ? newsList.size() : 0;
    }

    public void updateData(List<News> newList) {
        this.newsList = newList;
        notifyDataSetChanged();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPost, btnDelete, btnEdit;
        TextView txtTitle, txtContent;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPost = itemView.findViewById(R.id.imgPost);
            btnDelete = itemView.findViewById(R.id.btnDeletePost);
            btnEdit = itemView.findViewById(R.id.btnEditPost);
            txtTitle = itemView.findViewById(R.id.txtPostTitle);
            txtContent = itemView.findViewById(R.id.txtPostContent);
        }
    }
}
