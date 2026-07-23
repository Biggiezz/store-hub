package com.example.storehub.admin;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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

import com.example.storehub.R;
import com.example.storehub.admin.adapter.AdminReviewAdapter;
import com.example.storehub.admin.adapter.AdminReviewAdapter.ReviewWithProduct;
import com.example.storehub.model.Product;
import com.example.storehub.model.Product.ProductReview;
import com.example.storehub.model.Response;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class ManagementReviewsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvReviews;
    private TextView tvEmptyState;
    private TextView tvFilterAll, tvFilterUnanswered, tvFilterAnswered;

    private AdminReviewAdapter adapter;
    private final ArrayList<ReviewWithProduct> allReviews = new ArrayList<>();
    private final ArrayList<ReviewWithProduct> filteredReviews = new ArrayList<>();
    
    private int currentTab = 0; // 0: Tất cả, 1: Chưa trả lời, 2: Đã trả lời
    private ApiServices apiServices;

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
        tvFilterAll = findViewById(R.id.tvFilterAll);
        tvFilterUnanswered = findViewById(R.id.tvFilterUnanswered);
        tvFilterAnswered = findViewById(R.id.tvFilterAnswered);
    }

    private void setupRecyclerView() {
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminReviewAdapter(this, item -> {
            Intent intent = new Intent(ManagementReviewsActivity.this, ReplyReviewActivity.class);
            intent.putExtra("review_item", item.review);
            intent.putExtra("product_name", item.productName);
            intent.putExtra("product_id", item.productId);
            startActivity(intent);
        });
        rvReviews.setAdapter(adapter);
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
        apiServices.getListProduct(1, 1000, "").enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull retrofit2.Response<Response<ArrayList<Product>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allReviews.clear();
                    ArrayList<Product> products = response.body().getData();
                    
                    for (Product product : products) {
                        if (product.getReviews() != null) {
                            for (ProductReview review : product.getReviews()) {
                                allReviews.add(new ReviewWithProduct(review, product.getName(), product.getId()));
                            }
                        }
                    }
                    filterAndDisplayReviews();
                } else {
                    Toast.makeText(ManagementReviewsActivity.this, "Không thể tải đánh giá từ server", Toast.LENGTH_SHORT).show();
                    tvEmptyState.setVisibility(View.VISIBLE);
                    rvReviews.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull Throwable t) {
                Toast.makeText(ManagementReviewsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvReviews.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadReviews();
    }
}