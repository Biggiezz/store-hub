package com.example.storehub.admin;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.storehub.BaseActivity;
import com.example.storehub.R;
import com.example.storehub.admin.fragment.AdminHomeFragment;
import com.example.storehub.admin.fragment.NewsFragmentManagement;
import com.example.storehub.admin.fragment.ProductsFragmentManagement;
import com.example.storehub.admin.fragment.StatsManagerFragment;
import com.google.android.material.button.MaterialButton;

public class HomePageManagementActivity extends BaseActivity {

    public static final String TAB_HOME = "home";
    public static final String TAB_PRODUCTS = "products";
    public static final String TAB_NEWS = "news";
    public static final String TAB_USERS = "users";
    public static final String TAB_STATS = "stats";
    private static final String STATE_TAB = "selected_admin_tab";

    private MaterialButton btnHome, btnProducts, btnNews, btnUsers, btnStats;
    private String currentTabTag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUi();
        setUpListener();

        if (savedInstanceState == null) {
            showTab(TAB_HOME, new AdminHomeFragment());
        } else {
            currentTabTag = savedInstanceState.getString(STATE_TAB, TAB_HOME);
            updateBottomNavigation(currentTabTag);
        }
    }

    private void initUi() {
        btnHome = findViewById(R.id.btnHome);
        btnProducts = findViewById(R.id.btnProducts);
        btnNews = findViewById(R.id.btnNews);
        btnUsers = findViewById(R.id.btnUsers);
        btnStats = findViewById(R.id.btnStats);
    }

    private void setUpListener() {
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                if (TAB_HOME.equals(currentTabTag)) {
                    return;
                }
                showTab(TAB_HOME, new AdminHomeFragment());
            });
        }

        if (btnProducts != null) {
            btnProducts.setOnClickListener(v -> {
                if (TAB_PRODUCTS.equals(currentTabTag)) {
                    return;
                }
                showTab(TAB_PRODUCTS, new ProductsFragmentManagement());
            });
        }

        if (btnNews != null) {
            btnNews.setOnClickListener(v -> {
                if (TAB_NEWS.equals(currentTabTag)) {
                    return;
                }
                showTab(TAB_NEWS, new NewsFragmentManagement());
            });
        }

        if (btnStats != null) {
            btnStats.setOnClickListener(v -> {
                if (TAB_STATS.equals(currentTabTag)) {
                    return;
                }
                showTab(TAB_STATS, new StatsManagerFragment());
            });
        }
    }

    private void showTab(String tabTag, Fragment fragment) {
        currentTabTag = tabTag;
        updateBottomNavigation(tabTag);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentAdminContainer, fragment, tabTag)
                .commit();
    }

    private void updateBottomNavigation(String activeTab) {
        int activeBackground = ContextCompat.getColor(this, R.color.bottom_nav_active);
        int activeContent = Color.parseColor("#756E67");
        int inactiveContent = Color.parseColor("#AAA49D");

        MaterialButton[] buttons = {btnHome, btnProducts, btnNews, btnUsers, btnStats};
        String[] tabs = {TAB_HOME, TAB_PRODUCTS, TAB_NEWS, TAB_USERS, TAB_STATS};
        for (int i = 0; i < buttons.length; i++) {
            boolean isActive = tabs[i].equals(activeTab);
            buttons[i].setBackgroundTintList(ColorStateList.valueOf(
                    isActive ? activeBackground : Color.TRANSPARENT));
            buttons[i].setTextColor(isActive ? activeContent : inactiveContent);
            buttons[i].setIconTint(ColorStateList.valueOf(isActive ? activeContent : inactiveContent));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_TAB, currentTabTag);
        super.onSaveInstanceState(outState);
    }
}
