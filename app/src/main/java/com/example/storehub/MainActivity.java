package com.example.storehub;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.storehub.adapter.NewsAdapter;
import com.example.storehub.adapter.ProductAdapter;
import com.example.storehub.adapter.SlideShowAdapter;
import com.example.storehub.fragment.CartFragment;
import com.example.storehub.fragment.NewsFragment;
import com.example.storehub.fragment.OderFragment;
import com.example.storehub.fragment.ProductsFragment;
import com.example.storehub.model.News;
import com.example.storehub.model.Product;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends BaseActivity {

    public static final String EXTRA_OPEN_TAB = "open_tab";
    public static final String TAB_HOME = "home";
    public static final String TAB_PRODUCTS = "products";
    public static final String TAB_NEWS = "news";
    public static final String TAB_CART = "cart";
    public static final String TAB_ORDERS = "orders";
    private static final String STATE_TAB = "selected_tab";
    public static ArrayList<Product> preloadedProducts = null;
    public static boolean shouldOpenCartOnResume = false;
    public static ArrayList<News> preloadedNews = null;
    private ViewPager2 sliderBanner;
    private TextView dotOne, dotTwo, dotThree;
    private RecyclerView rvProducts, rvNews;
    private ProductAdapter productAdapter;
    private NewsAdapter newsAdapter;
    private MaterialButton btnViewAllProducts, btnHome, btnProducts, btnCart, btnNews, btnProfile, btnPhone, btnComputer, btnHeadphone;
    private ArrayList<Product> allProductsList = new ArrayList<>();
    private ArrayList<News> newsList;
    private String selectedTab = TAB_HOME;

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

        initUi();
        setUpAdapter();
        setUpListener();

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

        if (savedInstanceState != null) openTab(savedInstanceState.getString(STATE_TAB, TAB_HOME));
        else handleRequestedTab(getIntent());
    }

    private void initUi() {
        // Initialize SlideShow ViewPager2
        // Set click listener on avatar to open profile screen
        findViewById(R.id.imgAvatar).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Initialize SlideShow ViewPager2 & Adapter
        sliderBanner = findViewById(R.id.sliderBanner);

        // Initialize Indicator dots
        dotOne = findViewById(R.id.dotOne);
        dotTwo = findViewById(R.id.dotTwo);
        dotThree = findViewById(R.id.dotThree);

        // Initialize RecyclerView for products
        rvProducts = findViewById(R.id.rvProducts);

        // Initialize RecyclerView for news
        rvNews = findViewById(R.id.rvNews);

        // Initialize navigation buttons
        btnViewAllProducts = findViewById(R.id.btnViewAllProducts);
        btnHome = findViewById(R.id.btnHome);
        btnProducts = findViewById(R.id.btnProducts);
        btnCart = findViewById(R.id.btnCart);
        btnNews = findViewById(R.id.btnNews);
        btnProfile = findViewById(R.id.btnProfile);

        // Initialize category buttons
        btnPhone = findViewById(R.id.btnPhone);
        btnComputer = findViewById(R.id.btnComputer);
        btnHeadphone = findViewById(R.id.btnHeadphone);
    }

    private void setUpAdapter() {
        SlideShowAdapter adapter = new SlideShowAdapter(this);
        sliderBanner.setAdapter(adapter);

        productAdapter = new ProductAdapter(this);
        rvProducts.setAdapter(productAdapter);

        newsAdapter = new NewsAdapter(this);
        newsAdapter.setMultiTypeEnabled(false);
        rvNews.setAdapter(newsAdapter);
    }

    private void setUpListener() {
        // Update indicator dots dynamically on page changes
        sliderBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
            }
        });

        if (btnViewAllProducts != null) {
            btnViewAllProducts.setOnClickListener(v -> showProducts());
        }

        btnHome.setOnClickListener(v -> showHome());
        btnProducts.setOnClickListener(v -> showProducts());
        btnCart.setOnClickListener(v -> showCart());
        btnNews.setOnClickListener(v -> showNews());
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (findViewById(R.id.fragmentContainer).getVisibility() == View.VISIBLE) {
                    showHome();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        if (btnPhone != null) {
            btnPhone.setOnClickListener(v -> updateCategorySelection("Điện thoại"));
        }
        if (btnComputer != null) {
            btnComputer.setOnClickListener(v -> updateCategorySelection("Laptop"));
        }
        if (btnHeadphone != null) {
            btnHeadphone.setOnClickListener(v -> updateCategorySelection("Tai nghe"));
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleRequestedTab(intent);
    }

    private void handleRequestedTab(Intent intent) {
        openTab(intent.getStringExtra(EXTRA_OPEN_TAB));
    }

    private void openTab(String tab) {
        if (TAB_PRODUCTS.equals(tab)) showProducts();
        else if (TAB_NEWS.equals(tab)) showNews();
        else if (TAB_ORDERS.equals(tab)) showOder();
        else if (TAB_CART.equals(tab)) showCart();
        else if (TAB_HOME.equals(tab)) showHome();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(STATE_TAB, selectedTab);
        super.onSaveInstanceState(outState);
    }

    public void showHome() {
        selectedTab = TAB_HOME;
        findViewById(R.id.mainScrollView).setVisibility(View.VISIBLE);
        findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
        updateBottomNavigation(btnHome);
    }

    private void showProducts() {
        selectedTab = TAB_PRODUCTS;
        findViewById(R.id.mainScrollView).setVisibility(View.GONE);
        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
        if (getSupportFragmentManager().findFragmentByTag("products") == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ProductsFragment(), "products")
                    .commit();
        }
        updateBottomNavigation(btnProducts);
    }

    public void showCart() {
        selectedTab = TAB_CART;
        findViewById(R.id.mainScrollView).setVisibility(View.GONE);
        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new CartFragment(), "cart")
                .commit();
        updateBottomNavigation(btnCart);
    }

    public void showOder() {
        selectedTab = TAB_CART;
        findViewById(R.id.mainScrollView).setVisibility(View.GONE);
        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new OderFragment(), "oder")
                .commit();
        updateBottomNavigation(btnCart);
        View bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }

    private void showNews() {
        selectedTab = TAB_NEWS;
        findViewById(R.id.mainScrollView).setVisibility(View.GONE);
        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
        if (getSupportFragmentManager().findFragmentByTag("news") == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new NewsFragment(), "news")
                    .commit();
        }
        updateBottomNavigation(btnNews);
    }

    private void updateBottomNavigation(MaterialButton activeButton) {
        View bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
        int inactiveColor = ContextCompat.getColor(this, android.R.color.transparent);
        int activeColor = ContextCompat.getColor(this, R.color.bottom_nav_active);
        int inactiveContentColor = Color.parseColor("#AAA49D");
        int activeContentColor = Color.parseColor("#756E67");

        for (MaterialButton button : new MaterialButton[]{btnHome, btnProducts, btnCart, btnNews, btnProfile}) {
            boolean isActive = button == activeButton;
            button.setBackgroundTintList(ColorStateList.valueOf(isActive ? activeColor : inactiveColor));
            button.setTextColor(isActive ? activeContentColor : inactiveContentColor);
            button.setIconTint(ColorStateList.valueOf(isActive ? activeContentColor : inactiveContentColor));
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
        httpResquest.callAPI().getListProduct(1, 50, "").enqueue(new Callback<Response<ArrayList<Product>>>() {
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
