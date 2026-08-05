package com.nguyenmanhphuc.storehubapp.admin;

import android.graphics.Color;
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
    private ProgressBar progressBar;
    private Call<Response<RevenueData>> activityCall;
    private int currentPage = 1;
    private String selectedTabFilter = "all"; // "all", "order", "product", "user"

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

        updateTabUi();

        ImageView imgBack = findViewById(R.id.imgBack);
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

        btnPreviousPage.setOnClickListener(v -> goToPage(currentPage - 1));
        btnNextPage.setOnClickListener(v -> goToPage(currentPage + 1));
        tvPage1.setOnClickListener(v -> goToPage(readPage(tvPage1)));
        tvPage2.setOnClickListener(v -> goToPage(readPage(tvPage2)));
        tvPage3.setOnClickListener(v -> goToPage(readPage(tvPage3)));
        tvPage4.setOnClickListener(v -> goToPage(readPage(tvPage4)));
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
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (rvRecentActivities != null) rvRecentActivities.setVisibility(View.GONE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);
        if (layoutPagination != null) layoutPagination.setVisibility(View.GONE);

        activityCall = new HttpResquest().callAPI().getRevenueStatsWithLimit(2, 0);
        activityCall.enqueue(new Callback<Response<RevenueData>>() {
            @Override
            public void onResponse(@NonNull Call<Response<RevenueData>> call,
                                   @NonNull retrofit2.Response<Response<RevenueData>> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allActivities.clear();
                    List<RecentActivity> activities = response.body().getData().getRecentActivities();
                    if (activities != null) {
                        allActivities.addAll(activities);
                    }
                }
                filterActivities();
            }

            @Override
            public void onFailure(@NonNull Call<Response<RevenueData>> call, @NonNull Throwable t) {
                if (!call.isCanceled()) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    allActivities.clear();
                    filterActivities();
                }
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

        for (RecentActivity activity : allActivities) {
            String title = activity.getTitle() == null ? "" : activity.getTitle();
            String detail = activity.getDetail() == null ? "" : activity.getDetail();
            String type = activity.getType() == null ? "" : activity.getType().toLowerCase(Locale.getDefault());

            if ("order".equals(selectedTabFilter) && !type.startsWith("order")) {
                continue;
            }
            if ("product".equals(selectedTabFilter) && !type.startsWith("product")) {
                continue;
            }
            if ("user".equals(selectedTabFilter) && (!type.startsWith("user") && !type.startsWith("login"))) {
                continue;
            }

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

            filteredActivities.add(activity);
        }

        currentPage = 1;
        renderPage();
    }

    private void renderPage() {
        int totalItems = filteredActivities.size();
        int totalPages = getTotalPages();
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        int fromIndex = Math.min((currentPage - 1) * ITEMS_PER_PAGE, totalItems);
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, totalItems);
        adapter.updateData(filteredActivities.subList(fromIndex, toIndex));

        boolean isEmpty = totalItems == 0;
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvRecentActivities.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        updatePaginationUi(totalPages);
    }

    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil(filteredActivities.size() / (double) ITEMS_PER_PAGE));
    }

    private void goToPage(int page) {
        int totalPages = getTotalPages();
        if (page < 1 || page > totalPages || page == currentPage) {
            return;
        }
        currentPage = page;
        renderPage();
    }

    private int readPage(TextView pageView) {
        try {
            return Integer.parseInt(pageView.getText().toString());
        } catch (NumberFormatException e) {
            return currentPage;
        }
    }

    private void updatePaginationUi(int totalPages) {
        layoutPagination.setVisibility(totalPages > 1 ? View.VISIBLE : View.GONE);
        if (totalPages <= 1) {
            return;
        }

        int startPage = Math.max(1, currentPage - 2);
        if (startPage + 3 > totalPages) {
            startPage = Math.max(1, totalPages - 3);
        }

        tvPage1.setText(String.valueOf(startPage));
        tvPage2.setText(String.valueOf(startPage + 1));
        tvPage3.setText(String.valueOf(startPage + 2));
        tvPage4.setText(String.valueOf(startPage + 3));

        tvPage1.setVisibility(View.VISIBLE);
        tvPage2.setVisibility(startPage + 1 <= totalPages ? View.VISIBLE : View.GONE);
        tvPage3.setVisibility(startPage + 2 <= totalPages ? View.VISIBLE : View.GONE);
        tvPage4.setVisibility(startPage + 3 <= totalPages ? View.VISIBLE : View.GONE);

        setPageStyle(tvPage1, readPage(tvPage1) == currentPage);
        setPageStyle(tvPage2, readPage(tvPage2) == currentPage);
        setPageStyle(tvPage3, readPage(tvPage3) == currentPage);
        setPageStyle(tvPage4, readPage(tvPage4) == currentPage);

        btnPreviousPage.setEnabled(currentPage > 1);
        btnPreviousPage.setAlpha(currentPage > 1 ? 1.0f : 0.4f);
        btnNextPage.setEnabled(currentPage < totalPages);
        btnNextPage.setAlpha(currentPage < totalPages ? 1.0f : 0.4f);
    }

    private void setPageStyle(TextView pageView, boolean active) {
        pageView.setBackgroundResource(active ? R.drawable.bg_pagination_active : R.drawable.bg_pagination_inactive);
        pageView.setTextColor(active ? Color.WHITE : Color.parseColor("#29362F"));
    }

    @Override
    protected void onDestroy() {
        if (activityCall != null) {
            activityCall.cancel();
        }
        super.onDestroy();
    }
}
