package com.example.storehub.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import com.example.storehub.R;
import com.example.storehub.admin.adapter.AdminOrderAdapter;
import com.example.storehub.model.Order;
import com.example.storehub.model.Order.UpdateStatusRequest;
import com.example.storehub.model.Response;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class AdminOrdersActivity extends AppCompatActivity implements AdminOrderAdapter.OnOrderClickListener {

    private RecyclerView rvAdminOrders;
    private TextView tvEmptyState;
    private ProgressBar progressBar;

    private AdminOrderAdapter adapter;
    private ApiServices apiService;
    private Call<Response<ArrayList<Order>>> ordersCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_orders_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Bind Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Bind Views
        rvAdminOrders = findViewById(R.id.rvAdminOrders);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar);

        // RecyclerView Config
        rvAdminOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(this, this);
        rvAdminOrders.setAdapter(adapter);

        apiService = new HttpResquest().callAPI();

        // Load Initial Data
        loadOrders();
    }

    private void loadOrders() {
        setLoading(true);
        if (ordersCall != null) {
            ordersCall.cancel();
        }

        // Passing null as userId retrieves all customer orders
        ordersCall = apiService.getOrders(null);
        ordersCall.enqueue(new Callback<Response<ArrayList<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Order>>> call, @NonNull retrofit2.Response<Response<ArrayList<Order>>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<Order> orderList = response.body().getData();
                    if (orderList == null || orderList.isEmpty()) {
                        tvEmptyState.setVisibility(View.VISIBLE);
                        adapter.updateData(new ArrayList<>());
                    } else {
                        tvEmptyState.setVisibility(View.GONE);
                        adapter.updateData(orderList);
                    }
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
            }
        });
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onUpdateStatusClick(Order order) {
        if (order == null || order.getId() == null) return;

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
                updateStatus(order.getId(), newStatus);
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.create().show();
    }

    private void updateStatus(String orderId, String newStatus) {
        setLoading(true);
        UpdateStatusRequest request = new UpdateStatusRequest(orderId, newStatus);
        apiService.updateOrderStatus(request).enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Order>> call,
                                   @NonNull retrofit2.Response<Response<Order>> response) {
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

    @Override
    protected void onDestroy() {
        if (ordersCall != null) {
            ordersCall.cancel();
        }
        super.onDestroy();
    }
}
