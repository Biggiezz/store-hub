package com.nguyenmanhphuc.storehubapp.admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.content.ContextCompat;

import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.admin.adapter.AdminReviewAdapter;
import com.nguyenmanhphuc.storehubapp.admin.adapter.AdminReviewAdapter.ReviewWithProduct;
import com.nguyenmanhphuc.storehubapp.model.Product;
import com.nguyenmanhphuc.storehubapp.model.ProductReview;
import com.nguyenmanhphuc.storehubapp.model.Pagination;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.ApiServices;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class ManagementReviewsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvReviews;
    private TextView tvEmptyState;
    private TextView tvFilterAll, tvFilterUnanswered, tvFilterAnswered;

    private AdminReviewAdapter adapter;
    private final ArrayList<ReviewWithProduct> allReviews = new ArrayList<>();
    private final ArrayList<ReviewWithProduct> filteredReviews = new ArrayList<>();
    
    private ProgressBar progressBar;
    private int currentTab = 0; // 0: Tất cả, 1: Chưa trả lời, 2: Đã trả lời
    private ApiServices apiServices;

    private SwipeRefreshLayout swipeRefreshLayout;
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading;
    private boolean resumedOnce;
    private Call<Response<ArrayList<Product>>> reviewsCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_management_reviews);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.management_reviews_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiServices = new HttpResquest().callAPI();
        initUi();
        setupListeners();
        setupRecyclerView();
        
        loadReviews();
    }

    private void initUi() {
        btnBack = findViewById(R.id.btnBack);
        rvReviews = findViewById(R.id.rvReviews);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar);
        tvFilterAll = findViewById(R.id.tvFilterAll);
        tvFilterUnanswered = findViewById(R.id.tvFilterUnanswered);
        tvFilterAnswered = findViewById(R.id.tvFilterAnswered);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.dark_green));
            swipeRefreshLayout.setOnRefreshListener(this::loadReviews);
        }
    }

    private void setupRecyclerView() {
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminReviewAdapter(this, item -> {
            Intent intent = new Intent(ManagementReviewsActivity.this, ReplyReviewActivity.class);
            intent.putExtra("review_item", item.review);
            intent.putExtra("product_name", item.productName);
            intent.putExtra("product_id", item.productId);
            intent.putExtra("product_image", item.productImage);
            startActivity(intent);
        });
        rvReviews.setAdapter(adapter);
        rvReviews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0 || isLoading || currentPage >= totalPages) return;
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (manager instanceof LinearLayoutManager
                        && ((LinearLayoutManager) manager).findLastVisibleItemPosition() >= adapter.getItemCount() - 3) {
                    loadReviewsPage(currentPage + 1, true);
                }
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        tvFilterAll.setOnClickListener(v -> {
            currentTab = 0;
            updateTabStyles();
            filterAndDisplayReviews();
        });

        tvFilterUnanswered.setOnClickListener(v -> {
            currentTab = 1;
            updateTabStyles();
            filterAndDisplayReviews();
        });

        tvFilterAnswered.setOnClickListener(v -> {
            currentTab = 2;
            updateTabStyles();
            filterAndDisplayReviews();
        });

        // Initialize active tab style
        updateTabStyles();
    }

    private void updateTabStyles() {
        // Set all to inactive style
        tvFilterAll.setBackgroundResource(R.drawable.bg_admin_chip);
        tvFilterAll.setTextColor(Color.parseColor("#5D615E"));

        tvFilterUnanswered.setBackgroundResource(R.drawable.bg_admin_chip);
        tvFilterUnanswered.setTextColor(Color.parseColor("#5D615E"));

        tvFilterAnswered.setBackgroundResource(R.drawable.bg_admin_chip);
        tvFilterAnswered.setTextColor(Color.parseColor("#5D615E"));

        // Highlight active tab
        if (currentTab == 0) {
            tvFilterAll.setBackgroundResource(R.drawable.bg_admin_chip_active);
            tvFilterAll.setTextColor(Color.WHITE);
        } else if (currentTab == 1) {
            tvFilterUnanswered.setBackgroundResource(R.drawable.bg_admin_chip_active);
            tvFilterUnanswered.setTextColor(Color.WHITE);
        } else if (currentTab == 2) {
            tvFilterAnswered.setBackgroundResource(R.drawable.bg_admin_chip_active);
            tvFilterAnswered.setTextColor(Color.WHITE);
        }
    }

    private void loadReviews() {
        if (reviewsCall != null) reviewsCall.cancel();
        isLoading = false;
        currentPage = 1;
        totalPages = 1;
        loadReviewsPage(1, false);
    }

    private void loadReviewsPage(int page, boolean append) {
        if (isLoading) return;
        isLoading = true;
        boolean isSwipeRefreshing = swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing();
        if (!append && !isSwipeRefreshing) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        }
        if (!append) {
            if (rvReviews != null) rvReviews.setVisibility(View.GONE);
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
        }

        if (reviewsCall != null) reviewsCall.cancel();
        reviewsCall = apiServices.getListProduct(page, PAGE_SIZE, "", false, "");
        reviewsCall.enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull retrofit2.Response<Response<ArrayList<Product>>> response) {
                if (isFinishing() || isDestroyed()) return;
                isLoading = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    if (!append) allReviews.clear();
                    ArrayList<Product> products = response.body().getData();
                    Pagination pagination = response.body().getPagination();
                    if (pagination != null) {
                        currentPage = pagination.getCurrentPage();
                        totalPages = Math.max(currentPage, pagination.getTotalPages());
                    } else {
                        currentPage = page;
                        totalPages = products.size() == PAGE_SIZE ? page + 1 : page;
                    }
                    
                    for (Product product : products) {
                        if (product.getReviews() != null) {
                            for (ProductReview review : product.getReviews()) {
                                allReviews.add(new ReviewWithProduct(review, product.getName(), product.get_id(), product.getImage()));
                            }
                        }
                    }
                    filterAndDisplayReviews();
                } else {
                    Toast.makeText(ManagementReviewsActivity.this, ManagementReviewsActivity.this.getString(R.string.toast_khong_the_tai_danh_gia_tu_server), Toast.LENGTH_SHORT).show();
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvReviews.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                if (call.isCanceled()) return;
                isLoading = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(ManagementReviewsActivity.this, String.format(getString(R.string.connection_error_prefix), t.getMessage()), Toast.LENGTH_SHORT).show();
                tvEmptyState.setVisibility(View.VISIBLE);
                rvReviews.setVisibility(View.GONE);
            }
        });
    }

    private void filterAndDisplayReviews() {
        filteredReviews.clear();
        
        if (currentTab == 0) {
            // Tất cả
            filteredReviews.addAll(allReviews);
        } else if (currentTab == 1) {
            // Chưa trả lời
            for (ReviewWithProduct r : allReviews) {
                if (r.review.getReplyContent() == null || r.review.getReplyContent().isEmpty()) {
                    filteredReviews.add(r);
                }
            }
        } else if (currentTab == 2) {
            // Đã trả lời
            for (ReviewWithProduct r : allReviews) {
                if (r.review.getReplyContent() != null && !r.review.getReplyContent().isEmpty()) {
                    filteredReviews.add(r);
                }
            }
        }

        adapter.updateData(filteredReviews);

        if (filteredReviews.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvReviews.setVisibility(View.GONE);
            if (!isLoading && currentPage < totalPages) {
                loadReviewsPage(currentPage + 1, true);
            }
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvReviews.setVisibility(View.VISIBLE);
            rvReviews.post(() -> {
                if (!isLoading && currentPage < totalPages && !rvReviews.canScrollVertically(1)) {
                    loadReviewsPage(currentPage + 1, true);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resumedOnce) loadReviews();
        resumedOnce = true;
    }

    @Override
    protected void onDestroy() {
        if (reviewsCall != null) reviewsCall.cancel();
        super.onDestroy();
    }
}
