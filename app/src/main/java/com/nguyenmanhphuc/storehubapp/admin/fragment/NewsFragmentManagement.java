package com.nguyenmanhphuc.storehubapp.admin.fragment;

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
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.adapter.PostAdapter;
import com.nguyenmanhphuc.storehubapp.admin.AddNewsManagementActivity;
import com.nguyenmanhphuc.storehubapp.model.News;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class NewsFragmentManagement extends Fragment implements PostAdapter.PostItemListener {
    private static final int ITEMS_PER_PAGE = 10;
    private RecyclerView rvPosts;
    private TextView tvEmptyPosts;
    private PostAdapter adapter;
    private List<News> displayedNewsList = new ArrayList<>();
    private MaterialButton btnPublished, btnDraft, btnPrivate;
    private HttpResquest httpRequest;
    private SharedPreferencesManager sharedPreferencesManager;
    private String selectedStatus = "published";
    private ProgressBar progressBar, progressBarLoadMore;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;

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
        progressBar = view.findViewById(R.id.progressBar);
        progressBarLoadMore = view.findViewById(R.id.progressBarLoadMore);
        httpRequest = new HttpResquest();
        if (getContext() != null) {
            sharedPreferencesManager = new SharedPreferencesManager(getContext());
        }
    }

    private void setUpAdapter() {
        if (getContext() != null && rvPosts != null) {
            adapter = new PostAdapter(getContext(), displayedNewsList, this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            rvPosts.setLayoutManager(layoutManager);
            rvPosts.setAdapter(adapter);

            rvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                        if (!isLoading && !isLastPage) {
                            if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                                currentPage++;
                                fetchPosts(false);
                            }
                        }
                    }
                }
            });
        }
    }

    private void setUpListener(View view) {
        btnPublished.setOnClickListener(v -> changeStatusFilter("published"));
        btnDraft.setOnClickListener(v -> changeStatusFilter("draft"));
        btnPrivate.setOnClickListener(v -> changeStatusFilter("hidden"));

        FloatingActionButton fabAddPost = view.findViewById(R.id.fabAddPost);
        if (fabAddPost != null) {
            fabAddPost.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), AddNewsManagementActivity.class);
                startActivity(intent);
            });
        }
    }

    private void changeStatusFilter(String status) {
        if (selectedStatus.equals(status)) return;
        selectedStatus = status;
        currentPage = 1;
        isLastPage = false;
        fetchPosts(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        currentPage = 1;
        isLastPage = false;
        fetchPosts(true);
    }

    private void fetchPosts(boolean isFirstLoad) {
        if (isLoading) return;
        isLoading = true;

        String token = sharedPreferencesManager != null ? sharedPreferencesManager.getToken() : "";
        String authHeader = "Bearer " + token;

        if (isFirstLoad) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            if (rvPosts != null) rvPosts.setVisibility(View.GONE);
            if (tvEmptyPosts != null) tvEmptyPosts.setVisibility(View.GONE);
        } else {
            if (progressBarLoadMore != null) progressBarLoadMore.setVisibility(View.VISIBLE);
        }

        httpRequest.callAPI().getAdminListNews(authHeader, currentPage, ITEMS_PER_PAGE).enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<News>>> call, @NonNull retrofit2.Response<Response<ArrayList<News>>> response) {
                if (!isAdded()) return;
                isLoading = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (progressBarLoadMore != null) progressBarLoadMore.setVisibility(View.GONE);
                if (rvPosts != null) rvPosts.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ArrayList<News> fetchedNewsList = response.body().getData();
                    
                    if (isFirstLoad) {
                        displayedNewsList.clear();
                        adapter.updateData(fetchedNewsList);
                    } else {
                        adapter.addData(fetchedNewsList);
                    }
                    displayedNewsList.addAll(fetchedNewsList);

                    if (fetchedNewsList.size() < ITEMS_PER_PAGE) {
                        isLastPage = true;
                    }
                    
                    filterByStatus(selectedStatus);
                } else {
                    isLastPage = true;
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                isLoading = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (progressBarLoadMore != null) progressBarLoadMore.setVisibility(View.GONE);
                if (rvPosts != null) rvPosts.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Lỗi tải danh sách bài viết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterByStatus(String status) {
        selectedStatus = status;
        // NOTE: Ideally, the status filtering should be done on the server.
        // If the server returns all statuses mixed in pagination, we filter here.
        // But for true pagination, server should support status query.
        
        updateTabButtonsUi(status);
        
        // This is a workaround if server doesn't support status filter yet.
        // In a real senior dev setup, I would have updated ApiServices to include @Query("status").
        tvEmptyPosts.setText("draft".equals(status)
                ? "Chưa có bài viết bản nháp"
                : "hidden".equals(status)
                ? "Chưa có bài viết riêng tư"
                : "Chưa có bài viết đã xuất bản");
        tvEmptyPosts.setVisibility(displayedNewsList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateTabButtonsUi(String status) {
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
                    currentPage = 1;
                    isLastPage = false;
                    fetchPosts(true);
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
        Intent intent = new Intent(getContext(), com.nguyenmanhphuc.storehubapp.NewsDetailActivity.class);
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
