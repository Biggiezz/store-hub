package com.example.storehub.admin.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.R;
import com.example.storehub.adapter.PostAdapter;
import com.example.storehub.admin.AddNewsManagementActivity;
import com.example.storehub.model.News;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class NewsFragmentManagement extends Fragment implements PostAdapter.PostItemListener {

    private RecyclerView rvPosts;
    private TextView tvEmptyPosts;
    private PostAdapter adapter;
    private List<News> newsList = new ArrayList<>();
    private MaterialButton btnPublished, btnDraft, btnPrivate;
    private HttpResquest httpRequest;
    private SharedPreferencesManager sharedPreferencesManager;
    private String selectedStatus = "published";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUi(view);
        setUpAdapter();
        setUpListener(view);
    }

    private void initUi(View view) {
        rvPosts = view.findViewById(R.id.rvPosts);
        tvEmptyPosts = view.findViewById(R.id.tvEmptyPosts);
        btnPublished = view.findViewById(R.id.btnPublished);
        btnDraft = view.findViewById(R.id.btnDraft);
        btnPrivate = view.findViewById(R.id.btnPrivate);
        httpRequest = new HttpResquest();
        if (getContext() != null) {
            sharedPreferencesManager = new SharedPreferencesManager(getContext());
        }
    }

    private void setUpAdapter() {
        if (getContext() != null && rvPosts != null) {
            adapter = new PostAdapter(getContext(), newsList, this);
            rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
            rvPosts.setAdapter(adapter);
        }
    }

    private void setUpListener(View view) {
        btnPublished.setOnClickListener(v -> filterByStatus("published"));
        btnDraft.setOnClickListener(v -> filterByStatus("draft"));
        btnPrivate.setOnClickListener(v -> filterByStatus("hidden"));

        FloatingActionButton fabAddPost = view.findViewById(R.id.fabAddPost);
        if (fabAddPost != null) {
            fabAddPost.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddNewsManagementActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchPosts();
    }

    private void fetchPosts() {
        String token = sharedPreferencesManager != null ? sharedPreferencesManager.getToken() : "";
        String authHeader = "Bearer " + token;

        httpRequest.callAPI().getAdminListNews(authHeader, 1, 50).enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<News>>> call, @NonNull retrofit2.Response<Response<ArrayList<News>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    newsList = response.body().getData();
                    filterByStatus(selectedStatus);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi tải danh sách bài viết", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filterByStatus(String status) {
        selectedStatus = status;
        List<News> filteredNews = new ArrayList<>();
        for (News news : newsList) {
            if (status.equals(news.getStatus())) filteredNews.add(news);
        }
        if (adapter != null) adapter.updateData(filteredNews);
        tvEmptyPosts.setText("draft".equals(status)
                ? "Chưa có bài viết bản nháp"
                : "hidden".equals(status)
                ? "Chưa có bài viết riêng tư"
                : "Chưa có bài viết đã xuất bản");
        tvEmptyPosts.setVisibility(filteredNews.isEmpty() ? View.VISIBLE : View.GONE);

        int activeBackground = Color.parseColor("#14291F");
        int inactiveBackground = Color.parseColor("#F1E3D7");
        int activeText = Color.WHITE;
        int inactiveText = Color.parseColor("#41413F");
        MaterialButton[] buttons = {btnPublished, btnDraft, btnPrivate};
        String[] statuses = {"published", "draft", "hidden"};
        for (int i = 0; i < buttons.length; i++) {
            boolean active = statuses[i].equals(status);
            buttons[i].setBackgroundTintList(ColorStateList.valueOf(
                    active ? activeBackground : inactiveBackground));
            buttons[i].setTextColor(active ? activeText : inactiveText);
        }
    }

    @Override
    public void onDeleteClick(News news) {
        if (getContext() == null || news == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa bài viết này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteNews(news.get_id()))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteNews(String id) {
        if (id == null || id.isEmpty()) return;
        String token = sharedPreferencesManager != null ? sharedPreferencesManager.getToken() : "";
        String authHeader = "Bearer " + token;

        httpRequest.callAPI().deleteAdminNews(authHeader, id).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Void>> call, @NonNull retrofit2.Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Đã xóa bài viết thành công", Toast.LENGTH_SHORT).show();
                    fetchPosts();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Void>> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Lỗi khi xóa bài viết", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onItemClick(News news) {
        if (getContext() == null || news == null) return;
        Intent intent = new Intent(getContext(), com.example.storehub.NewsDetailActivity.class);
        intent.putExtra("news_item", news);
        intent.putExtra("is_admin", true);
        startActivity(intent);
    }

    @Override
    public void onEditClick(News news) {
        if (getContext() == null || news == null) return;
        Intent intent = new Intent(getContext(), AddNewsManagementActivity.class);
        intent.putExtra("edit_news", news);
        startActivity(intent);
    }
}
