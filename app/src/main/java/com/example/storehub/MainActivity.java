package com.example.storehub;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.storehub.adapter.NewsAdapter;
import com.example.storehub.adapter.ProductAdapter;
import com.example.storehub.adapter.SlideShowAdapter;
import com.example.storehub.model.News;
import com.example.storehub.model.Product;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 sliderBanner;
    private TextView dotOne, dotTwo, dotThree;
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private RecyclerView rvNews;
    private NewsAdapter newsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SlideShow ViewPager2 & Adapter
        sliderBanner = findViewById(R.id.sliderBanner);
        SlideShowAdapter adapter = new SlideShowAdapter(this);
        sliderBanner.setAdapter(adapter);

        // Initialize Indicator dots
        dotOne = findViewById(R.id.dotOne);
        dotTwo = findViewById(R.id.dotTwo);
        dotThree = findViewById(R.id.dotThree);

        // Update indicator dots dynamically on page changes
        sliderBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
            }
        });

        // Initialize RecyclerView for products
        rvProducts = findViewById(R.id.rvProducts);
        productAdapter = new ProductAdapter(this);
        rvProducts.setAdapter(productAdapter);

        // Initialize RecyclerView for news
        rvNews = findViewById(R.id.rvNews);
        newsAdapter = new NewsAdapter(this);
        rvNews.setAdapter(newsAdapter);

        // Load products & news from server
        fetchProducts();
        fetchNews();

        // Setup bottom nav news button click to open NewsActivity
        findViewById(R.id.btnNews).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NewsActivity.class);
            startActivity(intent);
            // Disable default transition to simulate tab changes
            overridePendingTransition(0, 0);//tắt hiệu ứng chuyển màn hình giữa các màn hình
        });
    }

    private void fetchProducts() {
        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().getListProduct().enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(Call<Response<ArrayList<Product>>> call, retrofit2.Response<Response<ArrayList<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<ArrayList<Product>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        ArrayList<Product> products = apiResponse.getData();
                        productAdapter.updateData(products);
                    } else {
                        Log.e("MainActivity", "Server response error: " + apiResponse.getMessage());
                    }
                } else {
                    Log.e("MainActivity", "Failed to fetch products: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error fetching products", t);
            }
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
                        newsAdapter.updateData(newsList);
                    } else {
                        Log.e("MainActivity", "Server news response error: " + apiResponse.getMessage());
                    }
                } else {
                    Log.e("MainActivity", "Failed to fetch news: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<News>>> call, @NonNull Throwable t) {
                Log.e("MainActivity", "Error fetching news", t);
            }
        });
    }

    private void updateIndicators(int position) {
        int activeColor = Color.parseColor("#FFFFFF");
        int inactiveColor = Color.parseColor("#B8B7B2");

        dotOne.setTextColor(position == 0 ? activeColor : inactiveColor);
        dotTwo.setTextColor(position == 1 ? activeColor : inactiveColor);
        dotThree.setTextColor(position == 2 ? activeColor : inactiveColor);
    }
}