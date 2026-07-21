package com.example.storehub;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.adapter.PostAdapter;
import com.example.storehub.model.Post;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class PostFragment extends Fragment implements PostAdapter.PostItemListener {

    private RecyclerView rvPosts;
    private PostAdapter adapter;
    private List<Post> postList = new ArrayList<>();
    private HttpResquest httpRequest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        rvPosts = view.findViewById(R.id.rvPosts);
        FloatingActionButton fabAddPost = view.findViewById(R.id.fabAddPost);

        httpRequest = new HttpResquest();
        adapter = new PostAdapter(getContext(), postList, this);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setAdapter(adapter);

        fabAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddPostActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchPosts();
    }

    private void fetchPosts() {
        httpRequest.callAPI().getListPost().enqueue(new Callback<Response<ArrayList<Post>>>() {
            @Override
            public void onResponse(Call<Response<ArrayList<Post>>> call, retrofit2.Response<Response<ArrayList<Post>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    postList = response.body().getData();
                    adapter.updateData(postList);
                }
            }

            @Override
            public void onFailure(Call<Response<ArrayList<Post>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi tải bài viết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteClick(Post post) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này?")
                .setPositiveButton("Xóa", (dialog, which) -> deletePost(post.getId()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deletePost(String id) {
        httpRequest.callAPI().deletePost(id).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(Call<Response<Void>> call, retrofit2.Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã xóa bài viết", Toast.LENGTH_SHORT).show();
                    fetchPosts();
                }
            }

            @Override
            public void onFailure(Call<Response<Void>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi xóa bài viết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(Post post) {
        // Có thể mở màn hình cập nhật nếu cần
    }
}
