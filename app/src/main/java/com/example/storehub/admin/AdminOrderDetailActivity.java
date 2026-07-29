package com.example.storehub.admin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.R;
import com.example.storehub.adapter.OrderProductAdapter;
import com.example.storehub.model.CartItem;
import com.example.storehub.model.Order;
import com.example.storehub.model.response.Response;
import com.example.storehub.model.request.UpdateStatusRequest;
import com.example.storehub.model.User;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class AdminOrderDetailActivity extends AppCompatActivity {
    private static final String EXTRA_ORDER_ID = "admin_order_id";

    private final String[] statusOptions = {
            "Chờ xác nhận",
            "Đã xác nhận",
            "Đã rời kho",
            "Đang giao hàng",
            "Đã giao hàng",
            "Đã hoàn thành",
            "Đã hủy"
    };

    private TextView orderCodeView, createdAtView, statusView, receiverView;
    private TextView addressView, itemCountView, subtotalView, shippingFeeView;
    private TextView totalView, cancelReasonView;
    private MaterialButton updateStatusButton;
    private ProgressBar progressBar;
    private OrderProductAdapter productAdapter;
    private ApiServices apiService;
    private SharedPreferencesManager preferencesManager;
    private Call<Response<Order>> currentCall;
    private String orderId;
    private Order currentOrder;

    public static Intent createIntent(Context context, String orderId) {
        return new Intent(context, AdminOrderDetailActivity.class)
                .putExtra(EXTRA_ORDER_ID, orderId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminOrderDetailRoot),
                (view, insets) -> {
                    Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                });

        preferencesManager = new SharedPreferencesManager(this);
        if (!hasAdminAccess()) {
            Toast.makeText(this, "Bạn không có quyền xem đơn hàng này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        if (orderId == null || orderId.isBlank()) {
            Toast.makeText(this, "Mã đơn hàng không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = new HttpResquest().callAPI();
        initUi();
        loadOrderDetail();
    }

    private void initUi() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        orderCodeView = findViewById(R.id.tvAdminDetailOrderCode);
        createdAtView = findViewById(R.id.tvAdminDetailCreatedAt);
        statusView = findViewById(R.id.tvAdminDetailStatus);
        receiverView = findViewById(R.id.tvAdminDetailReceiver);
        addressView = findViewById(R.id.tvAdminDetailAddress);
        itemCountView = findViewById(R.id.tvAdminDetailItemCount);
        subtotalView = findViewById(R.id.tvAdminDetailSubtotal);
        shippingFeeView = findViewById(R.id.tvAdminDetailShippingFee);
        totalView = findViewById(R.id.tvAdminDetailTotal);
        cancelReasonView = findViewById(R.id.tvAdminDetailCancelReason);
        updateStatusButton = findViewById(R.id.btnAdminDetailUpdateStatus);
        progressBar = findViewById(R.id.progressAdminOrderDetail);

        RecyclerView productsView = findViewById(R.id.rvAdminOrderDetailProducts);
        productsView.setLayoutManager(new LinearLayoutManager(this));
        productsView.setNestedScrollingEnabled(false);
        productAdapter = new OrderProductAdapter(this);
        productsView.setAdapter(productAdapter);

        updateStatusButton.setOnClickListener(v -> showStatusDialog());
    }

    private void loadOrderDetail() {
        setLoading(true);
        if (currentCall != null) currentCall.cancel();
        currentCall = apiService.getAdminOrderDetail(getAuthHeader(), orderId);
        currentCall.enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Order>> call,
                                   @NonNull retrofit2.Response<Response<Order>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    currentOrder = response.body().getData();
                    bindOrder(currentOrder);
                } else {
                    handleProtectedApiError(response.code(), "Không thể tải chi tiết đơn hàng");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Order>> call, @NonNull Throwable throwable) {
                if (call.isCanceled()) return;
                setLoading(false);
                Toast.makeText(AdminOrderDetailActivity.this,
                        "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindOrder(Order order) {
        orderCodeView.setText(order.getOrderCode().isEmpty()
                ? "Đơn hàng"
                : order.getOrderCode());
        createdAtView.setText("Ngày đặt: " + formatDate(order.getCreatedAt()));

        String status = order.getStatus();
        statusView.setText(status);
        statusView.setBackgroundResource("Đã hủy".equals(status)
                ? R.drawable.bg_order_status_cancelled
                : R.drawable.bg_status_dark);

        String receiver = order.getRecipientName();
        String phone = order.getRecipientPhone();
        receiverView.setText(receiver.isEmpty() && phone.isEmpty()
                ? "Chưa có thông tin người nhận"
                : receiver + (phone.isEmpty() ? "" : "  •  " + phone));
        addressView.setText(order.getRecipientAddress().isEmpty()
                ? "Chưa có địa chỉ giao hàng"
                : order.getRecipientAddress());

        productAdapter.updateData(order.getItems());
        int totalQuantity = 0;
        long subtotal = 0L;
        for (CartItem item : order.getItems()) {
            totalQuantity += item.getQuantity();
            subtotal += item.getTotalItemPrice();
        }
        if (subtotal == 0L) subtotal = order.getTotalPrice();

        long shippingFee = order.getShippingFee();
        itemCountView.setText(totalQuantity + " sản phẩm");
        subtotalView.setText(formatPrice(subtotal));
        shippingFeeView.setText(formatPrice(shippingFee));
        totalView.setText(formatPrice(subtotal + shippingFee));

        if ("Đã hủy".equals(status)) {
            cancelReasonView.setVisibility(View.VISIBLE);
            String reason = order.getCancelReason();
            cancelReasonView.setText("Lý do hủy: " +
                    (reason.isEmpty() ? "Không có thông tin" : reason));
        } else {
            cancelReasonView.setVisibility(View.GONE);
        }
    }

    private void showStatusDialog() {
        if (currentOrder == null) return;
        int currentIndex = -1;
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equals(currentOrder.getStatus())) {
                currentIndex = i;
                break;
            }
        }

        final int[] selectedIndex = {currentIndex};
        new AlertDialog.Builder(this)
                .setTitle("Cập nhật trạng thái " + currentOrder.getOrderCode())
                .setSingleChoiceItems(statusOptions, currentIndex,
                        (dialog, which) -> selectedIndex[0] = which)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    if (selectedIndex[0] >= 0) {
                        updateOrderStatus(statusOptions[selectedIndex[0]]);
                    }
                })
                .show();
    }

    private void updateOrderStatus(String status) {
        setLoading(true);
        UpdateStatusRequest request = new UpdateStatusRequest(orderId, status);
        currentCall = apiService.updateAdminOrderStatus(
                getAuthHeader(), orderId, request);
        currentCall.enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Order>> call,
                                   @NonNull retrofit2.Response<Response<Order>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    currentOrder = response.body().getData();
                    bindOrder(currentOrder);
                    Toast.makeText(AdminOrderDetailActivity.this,
                            response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    handleProtectedApiError(response.code(), "Không thể cập nhật trạng thái");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Order>> call, @NonNull Throwable throwable) {
                if (call.isCanceled()) return;
                setLoading(false);
                Toast.makeText(AdminOrderDetailActivity.this,
                        "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
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

    private void handleProtectedApiError(int statusCode, String fallbackMessage) {
        String message = statusCode == 401 || statusCode == 403
                ? "Phiên đăng nhập không hợp lệ hoặc bạn không có quyền"
                : fallbackMessage;
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (statusCode == 401 || statusCode == 403) finish();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        updateStatusButton.setEnabled(!loading);
    }

    private String formatPrice(long value) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN"))
                .format(value) + "đ";
    }

    private String formatDate(String value) {
        try {
            return DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy")
                    .withZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .format(Instant.parse(value));
        } catch (Exception ignored) {
            return value;
        }
    }

    @Override
    protected void onDestroy() {
        if (currentCall != null) currentCall.cancel();
        super.onDestroy();
    }
}
