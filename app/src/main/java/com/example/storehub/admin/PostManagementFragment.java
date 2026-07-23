package com.example.storehub.admin;

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

import com.example.storehub.NewsDetailActivity;
import com.example.storehub.R;
import com.example.storehub.adapter.PostManagementAdapter;
import com.example.storehub.model.ApiMessageResponse;
import com.example.storehub.model.News;
import com.example.storehub.model.Response;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class PostManagementFragment extends Fragment implements PostManagementAdapter.OnPostActionListener {

    private RecyclerView rvPosts;
    private PostManagementAdapter adapter;
    private FloatingActionButton fabAdd;
    private ApiServices apiServices;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiServices = new HttpResquest().callAPI();
        rvPosts = view.findViewById(R.id.rvPosts);
        fabAdd = view.findViewById(R.id.fabAdd);

        adapter = new PostManagementAdapter(getContext(), this);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setAdapter(adapter);

        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddNewsActivity.class));
        });

        loadNews();

        view.findViewById(R.id.btnRefresh).setOnClickListener(v -> loadNews());
    }

    private void loadNews() {
        apiServices.getListNews(1, 100).enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(Call<Response<ArrayList<News>>> call, retrofit2.Response<Response<ArrayList<News>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.updateData(response.body().getData());
                }
            }

            @Override
            public void onFailure(Call<Response<ArrayList<News>>> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDelete(News news) {
        apiServices.deleteNews(news.get_id()).enqueue(new Callback<ApiMessageResponse>() {
            @Override
            public void onResponse(Call<ApiMessageResponse> call, retrofit2.Response<ApiMessageResponse> response) {
                if (response.isSuccessful()) {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "Đã xóa bài viết", Toast.LENGTH_SHORT).show();
                    }
                    loadNews();
                }
            }

            @Override
            public void onFailure(Call<ApiMessageResponse> call, Throwable t) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(News news) {
        Intent intent = new Intent(getContext(), NewsDetailActivity.class);
        intent.putExtra("news_item", news);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNews();
    }
}
