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
import com.example.storehub.model.Post;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;
    private PostItemListener listener;

    public interface PostItemListener {
        void onDeleteClick(Post post);
        void onItemClick(Post post);
    }

    public PostAdapter(Context context, List<Post> postList, PostItemListener listener) {
        this.context = context;
        this.postList = postList;
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
        Post post = postList.get(position);
        holder.txtTitle.setText(post.getTitle());
        holder.txtContent.setText(post.getContent());

        Glide.with(context)
                .load(post.getImage())
                .placeholder(R.drawable.ic_avatar)
                .into(holder.imgPost);

        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(post));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(post));
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void updateData(List<Post> newList) {
        this.postList = newList;
        notifyDataSetChanged();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPost, btnDelete;
        TextView txtTitle, txtContent;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPost = itemView.findViewById(R.id.imgPost);
            btnDelete = itemView.findViewById(R.id.btnDeletePost);
            txtTitle = itemView.findViewById(R.id.txtPostTitle);
            txtContent = itemView.findViewById(R.id.txtPostContent);
        }
    }
}
