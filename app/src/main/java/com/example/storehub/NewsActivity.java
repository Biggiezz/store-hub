package com.example.storehub;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.adapter.NewsAdapter;
import com.example.storehub.model.News;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;


public class NewsActivity extends AppCompatActivity {

    private RecyclerView rvAllNews;
    private NewsAdapter newsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        rvAllNews = findViewById(R.id.rvAllNews);

        newsAdapter = new NewsAdapter(this);
        rvAllNews.setAdapter(newsAdapter);

        // Thiết lập sự kiện click cho Bottom Navigation & Nút Quay lại
        setupBottomNavigation();

        findViewById(R.id.btnBackNews).setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        // Tải toàn bộ bài viết từ API Server
        fetchNews();
    }


    private void setupBottomNavigation() {
        // Nút "Trang chủ" -> quay lại MainActivity
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            finish();
            overridePendingTransition(0, 0);
        });

        findViewById(R.id.btnProducts).setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Sản phẩm đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnCart).setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng Giỏ hàng đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnNews).setOnClickListener(v -> {

        });
    }


    private void fetchNews() {
        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().getListNews().enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(Call<Response<ArrayList<News>>> call, retrofit2.Response<Response<ArrayList<News>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<ArrayList<News>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        ArrayList<News> newsList = apiResponse.getData();
                        // Cập nhật danh sách bài viết vào adapter
                        newsAdapter.updateData(newsList);
                    } else {
                        Log.e("NewsActivity", "Lỗi dữ liệu trả về từ server: " + apiResponse.getMessage());
                    }
                } else {
                    Log.e("NewsActivity", "Không thể lấy tin tức: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                Log.e("NewsActivity", "Lỗi kết nối khi tải tin tức", t);
                Toast.makeText(NewsActivity.this, "Không thể kết nối đến máy chủ!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
