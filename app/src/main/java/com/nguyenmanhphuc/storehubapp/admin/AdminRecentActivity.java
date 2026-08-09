package com.nguyenmanhphuc.storehubapp.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.admin.adapter.RecentActivityAdapter;
import com.nguyenmanhphuc.storehubapp.model.response.RecentActivity;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.model.response.RevenueData;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.DateTimeUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class AdminRecentActivity extends AppCompatActivity {
    private static final int ITEMS_PER_PAGE = 5;
    private MaterialCardView cardFromDate, cardToDate;
    private TextView tvFromDate, tvToDate, tvPage1, tvPage2, tvPage3, tvPage4;
    private TextView tvFilterAll, tvFilterOrder, tvFilterProduct, tvFilterEmployee;
    private EditText edtSearchOrder;
    private RecyclerView rvRecentActivities;
    private View layoutEmpty, layoutPagination;
    private ImageButton btnPreviousPage, btnNextPage;
    private final RecentActivityAdapter adapter = new RecentActivityAdapter(activity ->
            startActivity(RecentActivityDetailActivity.createIntent(this, activity)));
    private final ArrayList<RecentActivity> allActivities = new ArrayList<>();
    private final ArrayList<RecentActivity> filteredActivities = new ArrayList<>();
    private final ArrayList<RecentActivity> displayedActivities = new ArrayList<>();
    private ProgressBar progressBar;
    private Call<Response<ArrayList<RecentActivity>>> activityCall;
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;
    private String selectedTabFilter = "all"; // "all", "order", "product", "user"
    private SwipeRefreshLayout swipeRefreshLayout;
    private com.nguyenmanhphuc.storehubapp.services.ApiServices apiServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutRecent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initUi();
        apiServices = new HttpResquest().callAPI();
        loadActivities();

        edtSearchOrder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterActivities();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initUi() {
        cardFromDate = findViewById(R.id.cardFromDate);
        cardToDate = findViewById(R.id.cardToDate);
        edtSearchOrder = findViewById(R.id.edtSearchOrder);
        rvRecentActivities = findViewById(R.id.rvRecentActivities);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        layoutPagination = findViewById(R.id.layoutPagination);
        progressBar = findViewById(R.id.progressBar);

        tvFromDate = findViewById(R.id.tvFromDate);
        tvToDate = findViewById(R.id.tvToDate);
        tvPage1 = findViewById(R.id.tvPage1);
        tvPage2 = findViewById(R.id.tvPage2);
        tvPage3 = findViewById(R.id.tvPage3);
        tvPage4 = findViewById(R.id.tvPage4);
        btnPreviousPage = findViewById(R.id.btnPreviousPage);
        btnNextPage = findViewById(R.id.btnNextPage);

        tvFilterAll = findViewById(R.id.tvFilterAll);
        tvFilterOrder = findViewById(R.id.tvFilterOrder);
        tvFilterProduct = findViewById(R.id.tvFilterProduct);
        tvFilterEmployee = findViewById(R.id.tvFilterEmployee);

        if (tvFilterAll != null) tvFilterAll.setOnClickListener(v -> selectTab("all"));
        if (tvFilterOrder != null) tvFilterOrder.setOnClickListener(v -> selectTab("order"));
        if (tvFilterProduct != null) tvFilterProduct.setOnClickListener(v -> selectTab("product"));
        if (tvFilterEmployee != null) tvFilterEmployee.setOnClickListener(v -> selectTab("user"));

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.dark_green));
            swipeRefreshLayout.setOnRefreshListener(this::loadActivities);
        }

        updateTabUi();

        ImageView imgBack = findViewById(R.id.btnBack);
        imgBack.setOnClickListener(v -> finish());

        rvRecentActivities.setLayoutManager(new LinearLayoutManager(this));
        rvRecentActivities.setAdapter(adapter);

        cardFromDate.setOnClickListener(v -> DateTimeUtils.showDatePicker(this, tvFromDate));

        cardToDate.setOnClickListener(v -> DateTimeUtils.showDatePicker(this, tvToDate));

        TextWatcher dateFilterWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterActivities();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        tvFromDate.addTextChangedListener(dateFilterWatcher);
        tvToDate.addTextChangedListener(dateFilterWatcher);

        if (layoutPagination != null) {
            layoutPagination.setVisibility(View.GONE);
        }

        androidx.core.widget.NestedScrollView nestedScrollView = findViewById(R.id.nestedScrollViewRecent);
        if (nestedScrollView != null) {
            nestedScrollView.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollY > oldScrollY && currentPage < totalPages && !isLoading) {
                    if (v.getChildAt(0) != null) {
                        int diff = v.getChildAt(0).getMeasuredHeight() - (v.getHeight() + scrollY);
                        if (diff <= 600) {
                            loadMoreActivities();
                        }
                    }
                }
            });
        }
    }

    private void selectTab(String tab) {
        selectedTabFilter = tab;
        updateTabUi();
        filterActivities();
    }

    private void updateTabUi() {
        updateTabButton(tvFilterAll, "all");
        updateTabButton(tvFilterOrder, "order");
        updateTabButton(tvFilterProduct, "product");
        updateTabButton(tvFilterEmployee, "user");
    }

    private void updateTabButton(TextView tv, String tab) {
        if (tv == null) return;
        boolean selected = tab.equals(selectedTabFilter);
        tv.setBackgroundResource(selected ? R.drawable.bg_order_filter_selected : R.drawable.bg_order_filter);
        tv.setTextColor(ContextCompat.getColor(this, selected ? R.color.text_button : R.color.dark_green));
    }

    private void loadActivities() {
        if (activityCall != null) activityCall.cancel();
        currentPage = 1;
        isLoading = true;
        boolean isSwipeRefreshing = swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing();
        if (!isSwipeRefreshing) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        }
        if (rvRecentActivities != null) rvRecentActivities.setVisibility(View.GONE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);

        String typeParam = selectedTabFilter.equals("all") ? "" : selectedTabFilter;
        String token = HttpResquest.authorizationHeader(this);
        
        activityCall = apiServices.getRecentActivities(token, currentPage, 15, typeParam);
        activityCall.enqueue(new Callback<Response<ArrayList<RecentActivity>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<RecentActivity>>> call,
                                   @NonNull retrofit2.Response<Response<ArrayList<RecentActivity>>> response) {
                isLoading = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allActivities.clear();
                    allActivities.addAll(response.body().getData());
                    totalPages = response.body().getPagination() != null ? response.body().getPagination().getTotalPages() : 1;
                } else {
                    allActivities.clear();
                }
                filterActivities();
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<RecentActivity>>> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    isLoading = false;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    allActivities.clear();
                    filterActivities();
                }
            }
        });
    }

    private void loadMoreActivities() {
        isLoading = true;
        currentPage++;
        
        String typeParam = selectedTabFilter.equals("all") ? "" : selectedTabFilter;
        String token = HttpResquest.authorizationHeader(this);
        
        apiServices.getRecentActivities(token, currentPage, 15, typeParam).enqueue(new Callback<Response<ArrayList<RecentActivity>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<RecentActivity>>> call, @NonNull retrofit2.Response<Response<ArrayList<RecentActivity>>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ArrayList<RecentActivity> moreData = response.body().getData();
                    allActivities.addAll(moreData);
                    filterActivities();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<RecentActivity>>> call, @NonNull Throwable t) {
                isLoading = false;
                currentPage--;
            }
        });
    }

    private Date parseDateString(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    private void filterActivities() {
        String keyword = edtSearchOrder.getText().toString().trim().toLowerCase(Locale.getDefault());
        String fromDateStr = tvFromDate.getText().toString().trim();
        String toDateStr = tvToDate.getText().toString().trim();

        Date fromDate = parseDateString(fromDateStr);
        Date toDate = parseDateString(toDateStr);

        if (fromDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(fromDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            fromDate = cal.getTime();
        }

        if (toDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(toDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            toDate = cal.getTime();
        }

        filteredActivities.clear();
        filteredActivities.addAll(allActivities);
        // We still support local keyword/date filtering on the data we have
        ArrayList<RecentActivity> searchResults = new ArrayList<>();
        
        for (RecentActivity activity : filteredActivities) {
            String title = activity.getTitle() == null ? "" : activity.getTitle();
            String detail = activity.getDetail() == null ? "" : activity.getDetail();

            boolean matchesKeyword = keyword.isEmpty()
                    || title.toLowerCase(Locale.getDefault()).contains(keyword)
                    || detail.toLowerCase(Locale.getDefault()).contains(keyword);

            if (!matchesKeyword) {
                continue;
            }

            Date activityDate = DateTimeUtils.parseISO(activity.getCreatedAt());
            if (activityDate != null) {
                if (fromDate != null && activityDate.before(fromDate)) {
                    continue;
                }
                if (toDate != null && activityDate.after(toDate)) {
                    continue;
                }
            } else {
                if (fromDate != null || toDate != null) {
                    continue;
                }
            }

            searchResults.add(activity);
        }

        adapter.updateData(searchResults);
        boolean isEmpty = searchResults.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvRecentActivities.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        if (activityCall != null) {
            activityCall.cancel();
        }
        super.onDestroy();
    }
}
