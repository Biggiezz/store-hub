package com.nguyenmanhphuc.storehubapp.admin;

import android.app.AlertDialog;
import android.content.Context;
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

import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.adapter.OrderProductAdapter;
import com.nguyenmanhphuc.storehubapp.model.CartItem;
import com.nguyenmanhphuc.storehubapp.model.Order;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.model.request.UpdateStatusRequest;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.services.ApiServices;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.DataCache;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.nguyenmanhphuc.storehubapp.model.Product;
import com.google.android.material.button.MaterialButton;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.content.ContextCompat;

public class AdminOrderDetailActivity extends AppCompatActivity {
    public static final String EXTRA_ORDER_ID = "order_id";

    private TextView orderCodeView, createdAtView, statusView, receiverView;
    private TextView addressView, itemCountView, subtotalView, shippingFeeView;
    private TextView totalView, cancelReasonView;
    private MaterialButton updateStatusButton;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OrderProductAdapter productAdapter;
    private ApiServices apiService;
    private SharedPreferencesManager preferencesManager;
    private Call<Response<Order>> currentCall;
    private String orderId;
    private Order currentOrder;
    private TextView disputeReasonView;

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
            Toast.makeText(this, this.getString(R.string.toast_ban_khong_co_quyen_xem_don_hang_nay), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        if (orderId == null || orderId.isBlank()) {
            Toast.makeText(this, this.getString(R.string.toast_ma_don_hang_khong_hop_le), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = new HttpResquest().callAPI();
        initUi();
        loadOrderDetail();
    }

    private void initUi() {
        ImageView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

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
        disputeReasonView = findViewById(R.id.tvAdminDetailDisputeReason);
        updateStatusButton = findViewById(R.id.btnAdminDetailUpdateStatus);
        progressBar = findViewById(R.id.progressAdminOrderDetail);

        RecyclerView productsView = findViewById(R.id.rvAdminOrderDetailProducts);
        productsView.setLayoutManager(new LinearLayoutManager(this));
        productsView.setNestedScrollingEnabled(false);
        productAdapter = new OrderProductAdapter(this);
        productAdapter.setOnItemClickListener(item -> showEditQuantityDialog(item));
        productsView.setAdapter(productAdapter);

        updateStatusButton.setOnClickListener(v -> showStatusDialog());
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.dark_green));
            swipeRefreshLayout.setOnRefreshListener(this::loadOrderDetail);
        }
    }

    private void showEditQuantityDialog(CartItem item) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_quantity, null);
        android.widget.EditText edtQuantity = dialogView.findViewById(R.id.edtEditQuantity);
        edtQuantity.setText(String.valueOf(item.getQuantity()));

        new AlertDialog.Builder(this)
                .setTitle(String.format(getString(R.string.edit_quantity_title), item.getProductName()))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.update), (dialog, which) -> {
                    String qtyStr = edtQuantity.getText().toString().trim();
                    if (!qtyStr.isEmpty()) {
                        int newQty = Integer.parseInt(qtyStr);
                        if (newQty != item.getQuantity()) {
                            updateOrderItemQuantity(item, newQty);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void updateOrderItemQuantity(CartItem item, int newQty) {
        // Trong thực tế, cần có API riêng để update quantity của item trong order.
        // Ở đây giả lập bằng cách update trực tiếp vào object currentOrder nếu backend hỗ trợ 
        // hoặc gọi API update status để trigger logic update (nếu backend có hỗ trợ).
        
        // Tuy nhiên, theo yêu cầu: "Khi sửa số lượng sản phẩm... sẽ cập nhật lại Tồn kho"
        // Ta sẽ thực hiện update Tồn kho của sản phẩm đó.
        
        int diff = item.getQuantity() - newQty; // Nếu mới < cũ -> diff dương -> trả lại kho
        item.setQuantity(newQty);
        
        // Tìm sản phẩm để update stock
        apiService.getProductDetail(item.getProductId()).enqueue(new Callback<Response<Product>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Product>> call, @NonNull retrofit2.Response<Response<Product>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Product product = response.body().getData();
                    int newStock = product.getStock() + diff;
                    updateProductStock(product, newStock);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Product>> call, @NonNull Throwable t) {
                Toast.makeText(AdminOrderDetailActivity.this, AdminOrderDetailActivity.this.getString(R.string.toast_loi_khi_lay_thong_tin_san_pham), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProductStock(Product product, int newStock) {
        MediaType textType = MediaType.parse("text/plain");
        RequestBody name = RequestBody.create(textType, product.getName());
        RequestBody price = RequestBody.create(textType, product.getPrice());
        RequestBody category = RequestBody.create(textType, product.getCategory() != null ? product.getCategory().get_id() : "");
        RequestBody description = RequestBody.create(textType, product.getDescription());
        RequestBody stock = RequestBody.create(textType, String.valueOf(newStock));
        RequestBody sold = RequestBody.create(textType, String.valueOf(product.getSold()));
        RequestBody isActive = RequestBody.create(textType, String.valueOf(product.isActive()));
        RequestBody colors = RequestBody.create(textType, new com.google.gson.Gson().toJson(product.getColors()));

        String token = HttpResquest.authorizationHeader(this);
        apiService.updateProduct(token, product.get_id(), name, price, category, description, stock, sold, isActive, colors, null)
                .enqueue(new Callback<Response<Product>>() {
                    @Override
                    public void onResponse(@NonNull Call<Response<Product>> call, @NonNull retrofit2.Response<Response<Product>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminOrderDetailActivity.this, AdminOrderDetailActivity.this.getString(R.string.toast_da_cap_nhat_so_luong_va_ton_kho), Toast.LENGTH_SHORT).show();
                            bindOrder(currentOrder); // Refresh UI
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Response<Product>> call, @NonNull Throwable t) {
                        Toast.makeText(AdminOrderDetailActivity.this, AdminOrderDetailActivity.this.getString(R.string.toast_loi_cap_nhat_ton_kho), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOrderDetail() {
        setLoading(true);
        if (currentCall != null) currentCall.cancel();
        currentCall = apiService.getAdminOrderDetail(getAuthHeader(), orderId);
        currentCall.enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Order>> call,
                                   @NonNull retrofit2.Response<Response<Order>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    currentOrder = response.body().getData();
                    fetchLatestUserAndBind(currentOrder);
                } else {
                    setLoading(false);
                    handleProtectedApiError(response.code(), getString(R.string.load_order_detail_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Order>> call, @NonNull Throwable throwable) {
                if (call.isCanceled()) return;
                setLoading(false);
                Toast.makeText(AdminOrderDetailActivity.this, AdminOrderDetailActivity.this.getString(R.string.toast_loi_ket_noi_server), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLatestUserAndBind(Order order) {
        String userId = order.getUserIdString();
        Log.d("AdminOrderDetail", "userId from order: '" + userId + "'");
        if (!userId.isEmpty()) {
            apiService.getUserById(getAuthHeader(), userId).enqueue(new Callback<Response<User>>() {
                @Override
                public void onResponse(@NonNull Call<Response<User>> call, @NonNull retrofit2.Response<Response<User>> response) {
                    setLoading(false);
                    Log.d("AdminOrderDetail", "getUserById response code: " + response.code());
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        User u = response.body().getData();
                        Log.d("AdminOrderDetail", "Fetched user name: " + u.getName());
                        order.setPopulatedUser(u);
                    }
                    bindOrder(order);
                }

                @Override
                public void onFailure(@NonNull Call<Response<User>> call, @NonNull Throwable t) {
                    setLoading(false);
                    Log.e("AdminOrderDetail", "getUserById failed: " + t.getMessage());
                    bindOrder(order);
                }
            });
        } else {
            // userId rỗng: có thể backend populate object vào field khác, thử fallback qua email/name từ receiverName
            Log.w("AdminOrderDetail", "userId is empty, binding with order data only");
            setLoading(false);
            bindOrder(order);
        }
    }

    private void bindOrder(Order order) {
        orderCodeView.setText(order.getOrderCode().isEmpty()
                ? getString(R.string.order_label)
                : order.getOrderCode());
        createdAtView.setText(String.format(getString(R.string.order_date_prefix), formatDate(order.getCreatedAt())));

        String status = order.getStatus();
        statusView.setText(getLocalizedStatus(status));
        statusView.setBackgroundResource(("Đã hủy".equals(status) || "Khiếu nại".equalsIgnoreCase(status))
                ? R.drawable.bg_order_status_cancelled
                : R.drawable.bg_status_dark);

        String receiver = order.getRecipientName();
        String phone = order.getRecipientPhone();
        User u = order.getUser();
        String email = (u != null && u.getEmail() != null && !u.getEmail().isEmpty()) ? "  •  " + u.getEmail() : "";
        receiverView.setText(receiver.isEmpty() && phone.isEmpty()
                ? getString(R.string.no_recipient_info)
                : receiver + (phone.isEmpty() ? "" : "  •  " + phone) + email);
        addressView.setText(order.getRecipientAddress().isEmpty()
                ? getString(R.string.no_shipping_address)
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
        itemCountView.setText(String.format(getString(R.string.item_count_suffix), totalQuantity));
        subtotalView.setText(formatPrice(subtotal));
        shippingFeeView.setText(formatPrice(shippingFee));
        totalView.setText(formatPrice(subtotal + shippingFee));

        if ("Đã hủy".equals(status)) {
            cancelReasonView.setVisibility(View.VISIBLE);
            String reason = order.getCancelReason();
            cancelReasonView.setText(String.format(getString(R.string.cancel_reason_prefix),
                    reason.isEmpty() ? getString(R.string.no_info) : reason));
        } else {
            cancelReasonView.setVisibility(View.GONE);
        }

        if ("Khiếu nại".equalsIgnoreCase(status)) {
            if (disputeReasonView != null) {
                disputeReasonView.setVisibility(View.VISIBLE);
                String reason = order.getDisputeReason();
                disputeReasonView.setText("Lý do khiếu nại: " + (reason.isEmpty() ? getString(R.string.no_info) : reason));
            }
        } else {
            if (disputeReasonView != null) {
                disputeReasonView.setVisibility(View.GONE);
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
        if (s.contains("đã giao hàng") || s.contains("delivered")) {
            return "Đã giao hàng";
        }
        if (s.contains("khiếu nại") || s.contains("disputed") || s.contains("dispute") || s.contains("complain")) {
            return "Khiếu nại";
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
        if ("Đã giao hàng".equalsIgnoreCase(status) || "Delivered".equalsIgnoreCase(status)) {
            return "Đã giao hàng";
        }
        if ("Khiếu nại".equalsIgnoreCase(status) || "Disputed".equalsIgnoreCase(status)) {
            return "Khiếu nại";
        }
        if ("Đã hoàn thành".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
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
            "Đã giao hàng",
            "Khiếu nại",
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
            String targetStatus = getItem(position);
            String normCurrent = normalizeStatus(currentOrder != null ? currentOrder.getStatus() : "");
            String normTarget = normalizeStatus(targetStatus);
            
            if (normCurrent.equals(normTarget)) return false;
            
            if ("Đã hủy".equals(normTarget)) {
                return isSuperAdmin && ("Chờ xác nhận".equals(normCurrent) || "Đã xác nhận".equals(normCurrent) || "Khiếu nại".equals(normCurrent));
            }
            
            if ("Chờ xác nhận".equals(normCurrent) && "Đã xác nhận".equals(normTarget)) return true;
            if ("Đã xác nhận".equals(normCurrent) && "Đã rời kho".equals(normTarget)) return true;
            if ("Đã rời kho".equals(normCurrent) && "Đang giao hàng".equals(normTarget)) return true;
            if ("Đang giao hàng".equals(normCurrent) && ("Đã giao hàng".equals(normTarget) || "Đã hoàn thành".equals(normTarget))) return true;
            if ("Đã giao hàng".equals(normCurrent) && ("Đã hoàn thành".equals(normTarget) || "Khiếu nại".equals(normTarget))) return true;
            if ("Khiếu nại".equals(normCurrent) && ("Đang giao hàng".equals(normTarget) || "Đã hoàn thành".equals(normTarget))) return true;
            
            return false;
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

    private void showStatusDialog() {
        if (currentOrder == null) return;
        String currentStatus = currentOrder.getStatus();

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

        if ("Đã hoàn thành".equals(normalized) || "Đã hủy".equals(normalized)) {
            Toast.makeText(this, this.getString(R.string.toast_don_hang_da_o_trang_thai_cuoi_cung_khong), Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = preferencesManager.getUser();
        boolean isSuperAdmin = currentUser != null && currentUser.isSuperAdmin();

        final int finalCurrentStatusIndex = currentStatusIndex;
        final int[] selectedIndex = {finalCurrentStatusIndex};
        StatusAdapter adapter = new StatusAdapter(this, allStatuses, finalCurrentStatusIndex, -1, -1, isSuperAdmin);

        new AlertDialog.Builder(this)
                .setTitle(String.format(getString(R.string.update_status_title), currentOrder.getOrderCode()))
                .setSingleChoiceItems(adapter, finalCurrentStatusIndex, (dialog, which) -> {
                    selectedIndex[0] = which;
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.update), (dialog, which) -> {
                    if (selectedIndex[0] >= 0 && selectedIndex[0] != finalCurrentStatusIndex) {
                        updateOrderStatus(allStatuses[selectedIndex[0]]);
                    } else {
                        Toast.makeText(this, this.getString(R.string.toast_vui_long_chon_mot_trang_thai_moi_de_cap_), Toast.LENGTH_SHORT).show();
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
                    // Xóa cache đơn hàng phía user → lần sau vào sẽ thấy trạng thái mới
                    DataCache.get().invalidate("user_orders");
                    Toast.makeText(AdminOrderDetailActivity.this,
                            response.body().getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    handleProtectedApiError(response.code(), getString(R.string.update_status_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Order>> call, @NonNull Throwable throwable) {
                if (call.isCanceled()) return;
                setLoading(false);
                Toast.makeText(AdminOrderDetailActivity.this, AdminOrderDetailActivity.this.getString(R.string.toast_loi_ket_noi_server), Toast.LENGTH_SHORT).show();
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
                ? getString(R.string.invalid_session_or_no_permission)
                : fallbackMessage;
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (statusCode == 401 || statusCode == 403) finish();
    }

    private void setLoading(boolean loading) {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.GONE);
            updateStatusButton.setEnabled(true);
            if (!loading) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        updateStatusButton.setEnabled(!loading);
        if (!loading && swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private String formatPrice(long value) {
        return NumberFormat.getNumberInstance(new Locale("vi", "VN"))
                .format(value) + getString(R.string.admin_price_suffix);
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
