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
    private TextView tvFilterAll, tvFilterPending, tvFilterConfirmed, tvFilterShipping, tvFilterCompleted, tvFilterCancelled, tvFilterDisputed;
    private final ArrayList<Order> allOrders = new ArrayList<>();
    private String selectedStatusFilter = "all";
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading;


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
        tvFilterDisputed = findViewById(R.id.tvFilterDisputed);

        // Set Tab Click Listeners
        tvFilterAll.setOnClickListener(v -> selectFilterTab("all"));
        tvFilterPending.setOnClickListener(v -> selectFilterTab("Chờ xác nhận"));
        tvFilterConfirmed.setOnClickListener(v -> selectFilterTab("Đã xác nhận"));
        tvFilterShipping.setOnClickListener(v -> selectFilterTab("Đang giao"));
        tvFilterCompleted.setOnClickListener(v -> selectFilterTab("Đã hoàn thành"));
        tvFilterCancelled.setOnClickListener(v -> selectFilterTab("Đã hủy"));
        if (tvFilterDisputed != null) {
            tvFilterDisputed.setOnClickListener(v -> selectFilterTab("Khiếu nại"));
        }

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
        rvAdminOrders.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy <= 0 || isLoading || currentPage >= totalPages) return;
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (manager instanceof LinearLayoutManager
                        && ((LinearLayoutManager) manager).findLastVisibleItemPosition() >= adapter.getItemCount() - 3) {
                    loadOrdersPage(currentPage + 1, true);
                }
            }
        });

        apiService = new HttpResquest().callAPI();
        preferencesManager = new SharedPreferencesManager(this);

        if (!hasAdminAccess()) {
            Toast.makeText(this, this.getString(R.string.toast_ban_khong_co_quyen_quan_ly_don_hang), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load Initial Data
        loadOrders();
    }

    private void loadOrders() {
        if (ordersCall != null) ordersCall.cancel();
        isLoading = false;
        currentPage = 1;
        totalPages = 1;
        loadOrdersPage(1, false);
    }

    private void loadOrdersPage(int page, boolean append) {
        if (isLoading) return;
        isLoading = true;
        if (!append) setLoading(true);
        if (ordersCall != null) ordersCall.cancel();

        ordersCall = apiService.getAdminOrders(getAuthHeader(), page, PAGE_SIZE);
        ordersCall.enqueue(new Callback<Response<ArrayList<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Order>>> call, @NonNull retrofit2.Response<Response<ArrayList<Order>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<Order> orderList = response.body().getData();
                    if (!append) allOrders.clear();
                    if (orderList != null) allOrders.addAll(orderList);
                    if (response.body().getPagination() != null) {
                        currentPage = response.body().getPagination().getCurrentPage();
                        totalPages = Math.max(currentPage, response.body().getPagination().getTotalPages());
                    } else {
                        currentPage = page;
                        totalPages = orderList != null && orderList.size() == PAGE_SIZE ? page + 1 : page;
                    }
                    isLoading = false;
                    setLoading(false);
                    filterAndSearchOrders();
                } else {
                    isLoading = false;
                    setLoading(false);
                    Toast.makeText(AdminOrdersActivity.this, AdminOrdersActivity.this.getString(R.string.toast_khong_the_tai_danh_sach_don_hang), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Order>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                isLoading = false;
                setLoading(false);
                Log.e("AdminOrdersActivity", "Error loading orders", t);
                Toast.makeText(AdminOrdersActivity.this, AdminOrdersActivity.this.getString(R.string.toast_loi_ket_noi_may_chu), Toast.LENGTH_SHORT).show();
                if (!append) allOrders.clear();
                filterAndSearchOrders();
            }
        });
    }

    private void fetchLatestUsersAndUpdateOrders(ArrayList<Order> orderList) {
        apiService.getListUsers(getAuthHeader(), 1, 1000, null, null).enqueue(new Callback<Response<ArrayList<User>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<User>>> call, @NonNull retrofit2.Response<Response<ArrayList<User>>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ArrayList<User> users = response.body().getData();
                    Log.d("AdminOrders", "Fetched " + users.size() + " users");
                    java.util.HashMap<String, User> userMap = new java.util.HashMap<>();
                    for (User u : users) {
                        if (u.getId() != null) {
                            userMap.put(u.getId(), u);
                        }
                    }
                    for (Order o : orderList) {
                        String uid = o.getUserIdString();
                        Log.d("AdminOrders", "Order " + o.getOrderCode() + " userId='" + uid + "'");
                        if (!uid.isEmpty() && userMap.containsKey(uid)) {
                            o.setPopulatedUser(userMap.get(uid));
                            Log.d("AdminOrders", "  → Mapped user: " + userMap.get(uid).getName());
                        }
                    }
                }
                allOrders.clear();
                if (orderList != null) {
                    allOrders.addAll(orderList);
                }
                filterAndSearchOrders();
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<User>>> call, @NonNull Throwable t) {
                setLoading(false);
                allOrders.clear();
                if (orderList != null) {
                    allOrders.addAll(orderList);
                }
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

    private String normalizeStatus(String status) {
        if (status == null) return "Chờ xác nhận";
        String s = status.trim().toLowerCase();
        if (s.contains("chờ xác nhận") || s.contains("pending") || s.contains("chờ xử lý")) {
            return "Chờ xác nhận";
        }
        if (s.contains("đã xác nhận") || s.contains("confirmed")) {
            return "Đã xác nhận";
        }
        if (s.contains("đã rời kho") || s.contains("left warehouse") || s.contains("dispatched")) {
            return "Đã rời kho";
        }
        if (s.contains("đang giao hàng") || s.contains("shipping") || s.contains("delivering")) {
            return "Đang giao hàng";
        }
        if (s.contains("đã hoàn thành") || s.contains("completed") || s.contains("done")) {
            return "Đã hoàn thành";
        }
        if (s.contains("đã hủy") || s.contains("cancelled") || s.contains("cancel")) {
            return "Đã hủy";
        }
        return "Chờ xác nhận";
    }

    private String getLocalizedStatus(String status) {
        if (status == null) return "";
        if ("Chờ xác nhận".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status) || "Chờ xử lý".equalsIgnoreCase(status)) {
            return getString(R.string.status_pending);
        }
        if ("Đã xác nhận".equalsIgnoreCase(status) || "Confirmed".equalsIgnoreCase(status)) {
            return getString(R.string.status_confirmed);
        }
        if ("Đã rời kho".equalsIgnoreCase(status) || "Left Warehouse".equalsIgnoreCase(status)) {
            return getString(R.string.status_dispatched);
        }
        if ("Đang giao hàng".equalsIgnoreCase(status) || "Shipping".equalsIgnoreCase(status)) {
            return getString(R.string.status_shipping);
        }
        if ("Đã giao hàng".equalsIgnoreCase(status) || "Đã hoàn thành".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
            return getString(R.string.status_completed);
        }
        if ("Đã hủy".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
            return getString(R.string.status_cancelled);
        }
        return status;
    }

    private static final String[] allStatuses = {
            "Chờ xác nhận",
            "Đã xác nhận",
            "Đã rời kho",
            "Đang giao hàng",
            "Đã hoàn thành",
            "Đã hủy"
    };

    private class StatusAdapter extends android.widget.ArrayAdapter<String> {
        private final int currentStatusIndex;
        private final int nextValidIndex;
        private final int cancelIndex;
        private final boolean isSuperAdmin;

        public StatusAdapter(android.content.Context context, String[] objects, int currentStatusIndex, int nextValidIndex, int cancelIndex, boolean isSuperAdmin) {
            super(context, android.R.layout.select_dialog_singlechoice, objects);
            this.currentStatusIndex = currentStatusIndex;
            this.nextValidIndex = nextValidIndex;
            this.cancelIndex = cancelIndex;
            this.isSuperAdmin = isSuperAdmin;
        }

        @Override
        public boolean isEnabled(int position) {
            if (position == cancelIndex) {
                return isSuperAdmin && (currentStatusIndex == 0 || currentStatusIndex == 1);
            }
            return position == nextValidIndex;
        }

        @androidx.annotation.NonNull
        @Override
        public android.view.View getView(int position, android.view.View convertView, @androidx.annotation.NonNull android.view.ViewGroup parent) {
            android.view.View view = super.getView(position, convertView, parent);
            android.widget.TextView textView = view.findViewById(android.R.id.text1);
            textView.setText(getLocalizedStatus(getItem(position)));

            if (position == currentStatusIndex) {
                textView.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                textView.setTypeface(null, android.graphics.Typeface.NORMAL);
            }

            if (isEnabled(position)) {
                textView.setTextColor(android.graphics.Color.parseColor("#1B2C24"));
                view.setAlpha(1.0f);
            } else {
                textView.setTextColor(android.graphics.Color.parseColor("#B0B0B0"));
                view.setAlpha(0.5f);
            }
            return view;
        }
    }

    @Override
    public void onUpdateStatusClick(Order order) {
        if (order == null || order.getOrderId().isEmpty()) return;

        String currentStatus = order.getStatus();
        
        int currentStatusIndex = -1;
        String normalized = normalizeStatus(currentStatus);
        for (int i = 0; i < allStatuses.length; i++) {
            if (allStatuses[i].equals(normalized)) {
                currentStatusIndex = i;
                break;
            }
        }

        int nextValidIndex = -1;
        if (currentStatusIndex >= 0 && currentStatusIndex < 4) {
            nextValidIndex = currentStatusIndex + 1;
        }

        int cancelIndex = 5;

        if (currentStatusIndex == 4 || currentStatusIndex == 5) {
            Toast.makeText(this, this.getString(R.string.toast_don_hang_da_o_trang_thai_cuoi_cung_khong), Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = new SharedPreferencesManager(this).getUser();
        boolean isSuperAdmin = currentUser != null && currentUser.isSuperAdmin();

        final int finalCurrentStatusIndex = currentStatusIndex;
        final int[] selectedIndex = {finalCurrentStatusIndex};
        StatusAdapter adapter = new StatusAdapter(this, allStatuses, finalCurrentStatusIndex, nextValidIndex, cancelIndex, isSuperAdmin);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(String.format(getString(R.string.choose_new_status_title), order.getOrderCode()));
        builder.setSingleChoiceItems(adapter, finalCurrentStatusIndex, (dialog, which) -> {
            selectedIndex[0] = which;
        });

        builder.setPositiveButton(getString(R.string.update), (dialog, which) -> {
            if (selectedIndex[0] >= 0 && selectedIndex[0] != finalCurrentStatusIndex) {
                updateStatus(order.getOrderId(), allStatuses[selectedIndex[0]]);
            } else {
                Toast.makeText(this, this.getString(R.string.toast_vui_long_chon_mot_trang_thai_moi_de_cap_), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), null);
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
                            Toast.makeText(AdminOrdersActivity.this, AdminOrdersActivity.this.getString(R.string.toast_cap_nhat_trang_thai_thanh_cong), Toast.LENGTH_SHORT).show();
                            loadOrders(); // Refresh lists
                        } else {
                            Toast.makeText(AdminOrdersActivity.this, AdminOrdersActivity.this.getString(R.string.toast_cap_nhat_that_bai), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Response<Order>> call, @NonNull Throwable t) {
                        setLoading(false);
                        Log.e("AdminOrdersActivity", "Error updating status", t);
                        Toast.makeText(AdminOrdersActivity.this, AdminOrdersActivity.this.getString(R.string.toast_loi_ket_noi_may_chu), Toast.LENGTH_SHORT).show();
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
        updateTabStyle(tvFilterDisputed, "Khiếu nại");
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
            } else if ("Khiếu nại".equals(selectedStatusFilter)) {
                matchesStatus = "Khiếu nại".equalsIgnoreCase(status)
                        || "disputed".equalsIgnoreCase(status);
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
                tvEmptyState.setText(getString(R.string.no_matching_orders));
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
        if (filteredList.isEmpty() && !isLoading && currentPage < totalPages) {
            loadOrdersPage(currentPage + 1, true);
        } else if (rvAdminOrders != null) {
            rvAdminOrders.post(() -> {
                if (!isLoading && currentPage < totalPages && !rvAdminOrders.canScrollVertically(1)) {
                    loadOrdersPage(currentPage + 1, true);
                }
            });
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
