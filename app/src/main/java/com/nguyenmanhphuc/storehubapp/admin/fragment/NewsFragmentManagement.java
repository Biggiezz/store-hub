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

    private RecyclerView rvPosts;
    private TextView tvEmptyPosts;
    private PostAdapter adapter;
    private List<News> newsList = new ArrayList<>();
    private MaterialButton btnPublished, btnDraft, btnPrivate;
    private HttpResquest httpRequest;
    private SharedPreferencesManager sharedPreferencesManager;
    private String selectedStatus = "published";
    private ProgressBar progressBar;

    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

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

            rvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null && dy > 0) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                        if (hasMoreData && !isLoading) {
                            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                                    && firstVisibleItemPosition >= 0) {
                                currentPage++;
                                fetchPosts(currentPage, true);
                            }
                        }
                    }
                }
            });
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
        currentPage = 1;
        hasMoreData = true;
        fetchPosts(1, false);
    }

    private void fetchPosts(int page, boolean isLoadMore) {
        if (isLoading) return;
        isLoading = true;

        String token = sharedPreferencesManager != null ? sharedPreferencesManager.getToken() : "";
        String authHeader = "Bearer " + token;

        // Chỉ hiển thị loading ở lần tải đầu tiên (danh sách rỗng)
        if (!isLoadMore) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            if (rvPosts != null) rvPosts.setVisibility(View.GONE);
            if (tvEmptyPosts != null) tvEmptyPosts.setVisibility(View.GONE);
        } else {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        }

        httpRequest.callAPI().getAdminListNews(authHeader, page, PAGE_SIZE, selectedStatus).enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<News>>> call, @NonNull retrofit2.Response<Response<ArrayList<News>>> response) {
                if (!isAdded()) return;
                isLoading = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (rvPosts != null) rvPosts.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ArrayList<News> serverNews = response.body().getData();
                    if (!isLoadMore) {
                        newsList.clear();
                    }

                    if (serverNews.size() < PAGE_SIZE) {
                        hasMoreData = false;
                    } else {
                        hasMoreData = true;
                    }

                    newsList.addAll(serverNews);
                    if (adapter != null) adapter.updateData(newsList);
                } else {
                    Toast.makeText(getContext(), "Không nhận được phản hồi từ máy chủ", Toast.LENGTH_SHORT).show();
                }

                tvEmptyPosts.setText("draft".equals(selectedStatus)
                        ? "Chưa có bài viết bản nháp"
                        : "hidden".equals(selectedStatus)
                        ? "Chưa có bài viết riêng tư"
                        : "Chưa có bài viết đã xuất bản");
                tvEmptyPosts.setVisibility(newsList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                isLoading = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (rvPosts != null) rvPosts.setVisibility(View.VISIBLE);
                Toast.makeText(getContext(), "Lỗi tải danh sách bài viết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterByStatus(String status) {
        selectedStatus = status;
        currentPage = 1;
        hasMoreData = true;

        int activeBackground = Color.parseColor("#14291F");
        int inactiveBackground = Color.parseColor("#F5F3F0");
        int activeText = Color.WHITE;
        int inactiveText = Color.parseColor("#8A8077");
        int strokeActiveColor = Color.parseColor("#14291F");
        int strokeInactiveColor = Color.parseColor("#D5D2CD");

        MaterialButton[] buttons = {btnPublished, btnDraft, btnPrivate};
        String[] statuses = {"published", "draft", "hidden"};
        for (int i = 0; i < buttons.length; i++) {
            boolean active = statuses[i].equals(status);
            buttons[i].setBackgroundTintList(ColorStateList.valueOf(
                    active ? activeBackground : inactiveBackground));
            buttons[i].setTextColor(active ? activeText : inactiveText);
            buttons[i].setStrokeColor(ColorStateList.valueOf(
                    active ? strokeActiveColor : strokeInactiveColor));
        }

        fetchPosts(1, false);
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
