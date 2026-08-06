package com.nguyenmanhphuc.storehubapp.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import android.widget.ImageView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nguyenmanhphuc.storehubapp.MainActivity;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.adapter.NewsAdapter;
import com.nguyenmanhphuc.storehubapp.model.News;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class NewsFragment extends Fragment {
    private static final int LIMIT = 5;

    private RecyclerView rvAllNews;
    private ImageView btnBackNews;
    private TextView btnPrevNewsPage, btnNewsPage1, btnNewsPage2, btnNewsPage3, btnNextNewsPage;
    private NewsAdapter newsAdapter;
    private Call<Response<ArrayList<News>>> currentCall;
    private int loadGeneration = 0;
    private int currentPage = 1;
    private int totalPages = 1;
    private ProgressBar progressBarNews;
    private ProgressBar progressBarNewsLoadMore;
    private View llPaginationNews;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isLoading = false;
    private boolean hasReachedEnd = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUi(view);
        setUpAdapter();
        setUpListener();
 
        loadNews(1);
    }

    private void initUi(View view) {
        rvAllNews = view.findViewById(R.id.rvAllNews);
        btnBackNews = view.findViewById(R.id.btnBackNews);
        btnPrevNewsPage = view.findViewById(R.id.btnPrevNewsPage);
        btnNewsPage1 = view.findViewById(R.id.btnNewsPage1);
        btnNewsPage2 = view.findViewById(R.id.btnNewsPage2);
        btnNewsPage3 = view.findViewById(R.id.btnNewsPage3);
        btnNextNewsPage = view.findViewById(R.id.btnNextNewsPage);
        progressBarNews = view.findViewById(R.id.progressBarNews);
        progressBarNewsLoadMore = view.findViewById(R.id.progressBarNewsLoadMore);
        llPaginationNews = view.findViewById(R.id.llPaginationNews);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.dark_green));
            swipeRefreshLayout.setOnRefreshListener(() -> {
                currentPage = 1;
                hasReachedEnd = false;
                loadNews(1);
            });
        }
    }

    private void setUpAdapter() {
        newsAdapter = new NewsAdapter(requireContext());
        if (rvAllNews != null) {
            rvAllNews.setAdapter(newsAdapter);
        }
    }

    private void setUpListener() {
        if (btnBackNews != null) {
            btnBackNews.setOnClickListener(v -> ((MainActivity) requireActivity()).showHome());
        }
 
        if (llPaginationNews != null) {
            llPaginationNews.setVisibility(View.GONE);
        }
 
        rvAllNews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                androidx.recyclerview.widget.LinearLayoutManager layoutManager =
                        (androidx.recyclerview.widget.LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    
                    if (!isLoading && !hasReachedEnd) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                                && firstVisibleItemPosition >= 0) {
                            loadNextPage();
                        }
                    }
                }
            }
        });
    }

    private void loadNextPage() {
        if (isLoading || hasReachedEnd) return;
        isLoading = true;
        currentPage++;
        loadNews(currentPage);
    }
 
    private void loadNews(int page) {
        if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
            if (progressBarNews != null && page == 1) progressBarNews.setVisibility(View.VISIBLE);
            if (rvAllNews != null && page == 1) rvAllNews.setVisibility(View.GONE);
        }
        if (progressBarNewsLoadMore != null && page > 1) progressBarNewsLoadMore.setVisibility(View.VISIBLE);
        if (llPaginationNews != null) llPaginationNews.setVisibility(View.GONE);
 
        isLoading = true;
        if (currentCall != null) currentCall.cancel();
        int requestGeneration = ++loadGeneration;
        currentCall = new HttpResquest().callAPI().getListNews(page, LIMIT);
        currentCall.enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<News>>> call, @NonNull retrofit2.Response<Response<ArrayList<News>>> response) {
                if (call.isCanceled() || newsAdapter == null || requestGeneration != loadGeneration) return;
                isLoading = false;
                if (progressBarNews != null) progressBarNews.setVisibility(View.GONE);
                if (progressBarNewsLoadMore != null) progressBarNewsLoadMore.setVisibility(View.GONE);
                if (rvAllNews != null) rvAllNews.setVisibility(View.VISIBLE);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
 
                if (response.isSuccessful() && response.body() != null
                        && response.body().getCode() == 200 && response.body().getData() != null) {
                    ArrayList<News> news = response.body().getData();
                    if (news.isEmpty() || news.size() < LIMIT) {
                        hasReachedEnd = true;
                    }
                    if (page == 1) {
                        newsAdapter.updateData(news);
                    } else {
                        newsAdapter.addData(news);
                    }
                } else {
                    showLoadFailure(requestGeneration, "Không thể tải danh sách tin tức", null);
                }
            }
 
            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                isLoading = false;
                if (progressBarNewsLoadMore != null) progressBarNewsLoadMore.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                showLoadFailure(requestGeneration, "Lỗi tải tin tức", t);
            }
        });
    }
 
    private void showLoadFailure(int requestGeneration, String message, Throwable error) {
        if (!isAdded() || requestGeneration != loadGeneration) return;
        if (progressBarNews != null) progressBarNews.setVisibility(View.GONE);
        if (progressBarNewsLoadMore != null) progressBarNewsLoadMore.setVisibility(View.GONE);
        if (rvAllNews != null) rvAllNews.setVisibility(View.VISIBLE);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        Log.e("NewsFragment", message, error);
        if (error != null) Toast.makeText(requireContext(), "Không thể kết nối đến máy chủ!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        if (currentCall != null) currentCall.cancel();
        newsAdapter = null;
        super.onDestroyView();
    }
}
