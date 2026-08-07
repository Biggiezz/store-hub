package com.nguyenmanhphuc.storehubapp.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.nguyenmanhphuc.storehubapp.admin.adapter.AdminOrderAdapter;
import com.nguyenmanhphuc.storehubapp.model.Order;
import com.nguyenmanhphuc.storehubapp.model.request.UpdateStatusRequest;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.services.ApiServices;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;

import java.util.ArrayList;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.util.Locale;


import retrofit2.Call;
import retrofit2.Callback;

public class AdminOrdersActivity extends AppCompatActivity implements AdminOrderAdapter.OnOrderClickListener {

    private RecyclerView rvAdminOrders;
    private TextView tvEmptyState;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdminOrderAdapter adapter;
    private ApiServices apiService;
    private Call<Response<ArrayList<Order>>> ordersCall;
    private SharedPreferencesManager preferencesManager;
    private boolean resumedOnce;
    
    private EditText etSearchOrders;
    private TextView tvFilterAll, tvFilterPending, tvFilterConfirmed, tvFilterShipping, tvFilterCompleted, tvFilterCancelled;
    private final ArrayList<Order> allOrders = new ArrayList<>();
    private String selectedStatusFilter = "all";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_orders_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Bind back button
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Bind Views
        rvAdminOrders = findViewById(R.id.rvAdminOrders);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        
        // Bind Search & Filter Views
        etSearchOrders = findViewById(R.id.etSearchOrders);
        tvFilterAll = findViewById(R.id.tvFilterAll);
        tvFilterPending = findViewById(R.id.tvFilterPending);
        tvFilterConfirmed = findViewById(R.id.tvFilterConfirmed);
        tvFilterShipping = findViewById(R.id.tvFilterShipping);
        tvFilterCompleted = findViewById(R.id.tvFilterCompleted);
        tvFilterCancelled = findViewById(R.id.tvFilterCancelled);

        // Set Tab Click Listeners
        tvFilterAll.setOnClickListener(v -> selectFilterTab("all"));
        tvFilterPending.setOnClickListener(v -> selectFilterTab("Chờ xác nhận"));
        tvFilterConfirmed.setOnClickListener(v -> selectFilterTab("Đã xác nhận"));
        tvFilterShipping.setOnClickListener(v -> selectFilterTab("Đang giao"));
        tvFilterCompleted.setOnClickListener(v -> selectFilterTab("Đã hoàn thành"));
        tvFilterCancelled.setOnClickListener(v -> selectFilterTab("Đã hủy"));

        // Set Search Input Listener
        etSearchOrders.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndSearchOrders();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.dark_green));
            swipeRefreshLayout.setOnRefreshListener(this::loadOrders);
        }

        // RecyclerView Config
        rvAdminOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(this, this);
        rvAdminOrders.setAdapter(adapter);

        apiService = new HttpResquest().callAPI();
        preferencesManager = new SharedPreferencesManager(this);

        if (!hasAdminAccess()) {
            Toast.makeText(this, "Bạn không có quyền quản lý đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load Initial Data
        loadOrders();
    }

    private void loadOrders() {
        setLoading(true);
        if (ordersCall != null) {
            ordersCall.cancel();
        }

        ordersCall = apiService.getAdminOrders(getAuthHeader());
        ordersCall.enqueue(new Callback<Response<ArrayList<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Order>>> call, @NonNull retrofit2.Response<Response<ArrayList<Order>>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<Order> orderList = response.body().getData();
                    allOrders.clear();
                    if (orderList != null) {
                        allOrders.addAll(orderList);
                    }
                    filterAndSearchOrders();
                } else {
                    Toast.makeText(AdminOrdersActivity.this, "Không thể tải danh sách đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Order>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                setLoading(false);
                Log.e("AdminOrdersActivity", "Error loading orders", t);
                Toast.makeText(AdminOrdersActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                allOrders.clear();
                filterAndSearchOrders();
            }
        });
    }

    @Override
    public void onViewDetailsClick(Order order) {
        if (order == null || order.getOrderId().isEmpty()) return;
        Intent intent = AdminOrderDetailActivity.createIntent(this, order.getOrderId());
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (!loading) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (loading) {
            if (rvAdminOrders != null) rvAdminOrders.setVisibility(View.GONE);
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
        } else {
            if (rvAdminOrders != null) rvAdminOrders.setVisibility(View.VISIBLE);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    @Override
    public void onUpdateStatusClick(Order order) {
        if (order == null || order.getOrderId().isEmpty()) return;

        final String[] statusOptions = {"Đã xác nhận", "Đã rời kho", "Đang giao hàng", "Đã giao hàng"};

        // Find current selection index
        int currentIndex = -1;
        String currentStatus = order.getStatus();
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equalsIgnoreCase(currentStatus)) {
                currentIndex = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn trạng thái mới cho đơn hàng " + order.getOrderCode());

        final int[] selectedIndex = {currentIndex};
        builder.setSingleChoiceItems(statusOptions, currentIndex, (dialog, which) -> selectedIndex[0] = which);

        builder.setPositiveButton("Cập nhật", (dialog, which) -> {
            if (selectedIndex[0] >= 0 && selectedIndex[0] < statusOptions.length) {
                String newStatus = statusOptions[selectedIndex[0]];
                updateStatus(order.getOrderId(), newStatus);
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.create().show();
    }

    private void updateStatus(String orderId, String newStatus) {
        setLoading(true);
        UpdateStatusRequest request = new UpdateStatusRequest(orderId, newStatus);
        apiService.updateAdminOrderStatus(getAuthHeader(), orderId, request)
                .enqueue(new Callback<Response<Order>>() {
                    @Override
                    public void onResponse(@NonNull Call<Response<Order>> call, @NonNull retrofit2.Response<Response<Order>> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(AdminOrdersActivity.this, "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
                            loadOrders(); // Refresh lists
                        } else {
                            Toast.makeText(AdminOrdersActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Response<Order>> call, @NonNull Throwable t) {
                        setLoading(false);
                        Log.e("AdminOrdersActivity", "Error updating status", t);
                        Toast.makeText(AdminOrdersActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean hasAdminAccess() {
        User user = preferencesManager.getUser();
        if (user == null || preferencesManager.getToken() == null) return false;
        String role = user.getRole() == null
                ? ""
                : user.getRole().replaceAll("\\s+", "").toLowerCase();
        return "admin".equals(role) || "superadmin".equals(role);
    }

    private String getAuthHeader() {
        return "Bearer " + preferencesManager.getToken();
    }

    private void selectFilterTab(String filter) {
        selectedStatusFilter = filter;
        updateFilterTabsUi();
        filterAndSearchOrders();
    }

    private void updateFilterTabsUi() {
        updateTabStyle(tvFilterAll, "all");
        updateTabStyle(tvFilterPending, "Chờ xác nhận");
        updateTabStyle(tvFilterConfirmed, "Đã xác nhận");
        updateTabStyle(tvFilterShipping, "Đang giao");
        updateTabStyle(tvFilterCompleted, "Đã hoàn thành");
        updateTabStyle(tvFilterCancelled, "Đã hủy");
    }

    private void updateTabStyle(TextView tv, String status) {
        if (tv == null) return;
        boolean isSelected = status.equals(selectedStatusFilter);
        tv.setBackgroundResource(isSelected ? R.drawable.bg_order_filter_selected : R.drawable.bg_order_filter);
        tv.setTextColor(ContextCompat.getColor(this, isSelected ? R.color.text_button : R.color.dark_green));
    }

    private void filterAndSearchOrders() {
        String query = etSearchOrders != null ? etSearchOrders.getText().toString().trim().toLowerCase(Locale.getDefault()) : "";
        ArrayList<Order> filteredList = new ArrayList<>();

        for (Order order : allOrders) {
            // 1. Filter by Status
            boolean matchesStatus = false;
            String status = order.getStatus() != null ? order.getStatus() : "";
            
            if ("all".equals(selectedStatusFilter)) {
                matchesStatus = true;
            } else if ("Chờ xác nhận".equals(selectedStatusFilter)) {
                matchesStatus = "Chờ xác nhận".equalsIgnoreCase(status) 
                        || "pending".equalsIgnoreCase(status) 
                        || "Chờ xử lý".equalsIgnoreCase(status);
            } else if ("Đã xác nhận".equals(selectedStatusFilter)) {
                matchesStatus = "Đã xác nhận".equalsIgnoreCase(status) 
                        || "Đã rời kho".equalsIgnoreCase(status);
            } else if ("Đang giao".equals(selectedStatusFilter)) {
                matchesStatus = "Đang giao hàng".equalsIgnoreCase(status) 
                        || "shipping".equalsIgnoreCase(status);
            } else if ("Đã hoàn thành".equals(selectedStatusFilter)) {
                matchesStatus = "Đã giao hàng".equalsIgnoreCase(status) 
                        || "Đã hoàn thành".equalsIgnoreCase(status) 
                        || "completed".equalsIgnoreCase(status);
            } else if ("Đã hủy".equals(selectedStatusFilter)) {
                matchesStatus = "Đã hủy".equalsIgnoreCase(status) 
                        || "cancelled".equalsIgnoreCase(status) 
                        || "cancel".equalsIgnoreCase(status);
            }

            if (!matchesStatus) {
                continue;
            }

            // 2. Filter by Search Query
            boolean matchesQuery = true;
            if (!query.isEmpty()) {
                String orderCode = order.getOrderCode() != null ? order.getOrderCode().toLowerCase(Locale.getDefault()) : "";
                String recipientName = order.getRecipientName() != null ? order.getRecipientName().toLowerCase(Locale.getDefault()) : "";
                String recipientPhone = order.getRecipientPhone() != null ? order.getRecipientPhone().toLowerCase(Locale.getDefault()) : "";
                
                matchesQuery = orderCode.contains(query) 
                        || recipientName.contains(query) 
                        || recipientPhone.contains(query);
            }

            if (matchesQuery) {
                filteredList.add(order);
            }
        }

        // 3. Update UI
        if (filteredList.isEmpty()) {
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Không tìm thấy đơn hàng phù hợp");
            }
            if (rvAdminOrders != null) {
                rvAdminOrders.setVisibility(View.GONE);
            }
        } else {
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.GONE);
            }
            if (rvAdminOrders != null) {
                rvAdminOrders.setVisibility(View.VISIBLE);
            }
        }
        if (adapter != null) {
            adapter.updateData(filteredList);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (resumedOnce && hasAdminAccess()) loadOrders();
        resumedOnce = true;
    }

    @Override
    protected void onDestroy() {
        if (ordersCall != null) {
            ordersCall.cancel();
        }
        super.onDestroy();
    }
}
