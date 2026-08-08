package com.nguyenmanhphuc.storehubapp;

import com.nguyenmanhphuc.storehubapp.R;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nguyenmanhphuc.storehubapp.adapter.NewsAdapter;
import com.nguyenmanhphuc.storehubapp.model.News;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class NewsActivity extends AppCompatActivity {
    private static final int LIMIT = 5;

    private RecyclerView rvAllNews;
    private ImageView btnBackNews;
    private NewsAdapter newsAdapter;
    private Call<Response<ArrayList<News>>> currentCall;
    private int loadGeneration = 0;
    private int currentPage = 1;
    private ProgressBar progressBarNews;
    private ProgressBar progressBarNewsLoadMore;

    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isLoading = false;
    private boolean hasReachedEnd = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_news);

        View root = findViewById(android.R.id.content);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initUi();
        setUpAdapter();
        setUpListener();

        loadNews(1);
    }

    private void initUi() {
        rvAllNews = findViewById(R.id.rvAllNews);
        btnBackNews = findViewById(R.id.btnBackNews);
        progressBarNews = findViewById(R.id.progressBarNews);
        progressBarNewsLoadMore = findViewById(R.id.progressBarNewsLoadMore);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.dark_green));
            swipeRefreshLayout.setOnRefreshListener(() -> {
                currentPage = 1;
                hasReachedEnd = false;
                loadNews(1);
            });
        }
    }

    private void setUpAdapter() {
        newsAdapter = new NewsAdapter(this);
        if (rvAllNews != null) {
            rvAllNews.setLayoutManager(new LinearLayoutManager(this));
            rvAllNews.setAdapter(newsAdapter);
        }
    }

    private void setUpListener() {
        if (btnBackNews != null) {
            btnBackNews.setOnClickListener(v -> finish());
        }



        if (rvAllNews != null) {
            rvAllNews.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
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
                    showLoadFailure(requestGeneration, getString(R.string.toast_news_list_failed), null);
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
                showLoadFailure(requestGeneration, getString(R.string.toast_news_load_error), t);
            }
        });
    }

    private void showLoadFailure(int requestGeneration, String message, Throwable error) {
        if (isFinishing() || isDestroyed() || requestGeneration != loadGeneration) return;
        if (progressBarNews != null) progressBarNews.setVisibility(View.GONE);
        if (progressBarNewsLoadMore != null) progressBarNewsLoadMore.setVisibility(View.GONE);
        if (rvAllNews != null) rvAllNews.setVisibility(View.VISIBLE);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        if (!com.nguyenmanhphuc.storehubapp.utils.NetworkUtils.isNetworkAvailable(this)) {
            com.nguyenmanhphuc.storehubapp.utils.NetworkUtils.showNoNetworkToast(this);
        } else if (error != null) {
            Toast.makeText(this, this.getString(R.string.toast_khong_the_ket_noi_den_may_chu), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (currentCall != null) currentCall.cancel();
        newsAdapter = null;
        super.onDestroy();
    }
}
