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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity {

    public static ArrayList<Product> preloadedProducts = null;
    public static ArrayList<News> preloadedNews = null;

    private ViewPager2 sliderBanner;
    private TextView dotOne, dotTwo, dotThree;
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private RecyclerView rvNews;
    private NewsAdapter newsAdapter;
    private MaterialButton btnViewAllProducts;
    private MaterialButton btnProducts;

    private MaterialButton btnPhone;
    private MaterialButton btnComputer;
    private MaterialButton btnHeadphone;
    private ArrayList<Product> allProductsList = new ArrayList<>();
    private ArrayList<News> newsList;

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
        newsAdapter.setMultiTypeEnabled(false); // Ở Trang chủ chỉ dùng các dòng tin tức nằm ngang thông thường (item_news_standard)
        rvNews.setAdapter(newsAdapter);

        // Initialize navigation buttons
        btnViewAllProducts = findViewById(R.id.btnViewAllProducts);
        btnProducts = findViewById(R.id.btnProducts);

        if (btnViewAllProducts != null) {
            btnViewAllProducts.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProductsActivity.class);
                startActivity(intent);
            });
        }

        if (btnProducts != null) {
            btnProducts.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProductsActivity.class);
                startActivity(intent);
            });
        }

        // Initialize category buttons
        btnPhone = findViewById(R.id.btnPhone);
        btnComputer = findViewById(R.id.btnComputer);
        btnHeadphone = findViewById(R.id.btnHeadphone);

        if (btnPhone != null) {
            btnPhone.setOnClickListener(v -> updateCategorySelection("Điện thoại"));
        }
        if (btnComputer != null) {
            btnComputer.setOnClickListener(v -> updateCategorySelection("Laptop"));
        }
        if (btnHeadphone != null) {
            btnHeadphone.setOnClickListener(v -> updateCategorySelection("Tai nghe"));
        }

        // Load products & news from server or use preloaded data
        if (preloadedProducts != null) {
            allProductsList = preloadedProducts;
            preloadedProducts = null;
            updateCategorySelection("Điện thoại");
        } else {
            fetchProducts();
        }

        if (preloadedNews != null) {
            newsAdapter.updateData(preloadedNews);
            preloadedNews = null;
        } else {
            fetchNews();
        }
    }

    private void updateCategorySelection(String activeCategory) {
        int activeBgColor = Color.parseColor("#14291F"); // dark_green
        int activeTextColor = Color.parseColor("#FFFFFF"); // white

        int inactiveBgColor = Color.parseColor("#F1E3D7");
        int inactiveTextColor = Color.parseColor("#41413F"); // text_primary

        if (btnPhone != null) {
            if ("Điện thoại".equals(activeCategory)) {
                btnPhone.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeBgColor));
                btnPhone.setTextColor(activeTextColor);
            } else {
                btnPhone.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveBgColor));
                btnPhone.setTextColor(inactiveTextColor);
            }
        }

        if (btnComputer != null) {
            if ("Laptop".equals(activeCategory)) {
                btnComputer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeBgColor));
                btnComputer.setTextColor(activeTextColor);
            } else {
                btnComputer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveBgColor));
                btnComputer.setTextColor(inactiveTextColor);
            }
        }

        if (btnHeadphone != null) {
            if ("Tai nghe".equals(activeCategory)) {
                btnHeadphone.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeBgColor));
                btnHeadphone.setTextColor(activeTextColor);
            } else {
                btnHeadphone.setBackgroundTintList(android.content.res.ColorStateList.valueOf(inactiveBgColor));
                btnHeadphone.setTextColor(inactiveTextColor);
            }
        }

        filterProductsByCategory(activeCategory);
    }

    private void filterProductsByCategory(String category) {
        ArrayList<Product> filteredList = new ArrayList<>();
        for (Product product : allProductsList) {
            if (category != null && category.equals(product.getCategory())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateData(filteredList);
    }

    private void fetchProducts() {
        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().getListProduct(1, 50).enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(Call<Response<ArrayList<Product>>> call, retrofit2.Response<Response<ArrayList<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<ArrayList<Product>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        allProductsList = apiResponse.getData();
                        updateCategorySelection("Điện thoại");
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
        httpResquest.callAPI().getListNews(1, 5).enqueue(new Callback<Response<ArrayList<News>>>() {
            @Override
            public void onResponse(Call<Response<ArrayList<News>>> call, retrofit2.Response<Response<ArrayList<News>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Response<ArrayList<News>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        newsList = apiResponse.getData();
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