package com.example.storehub;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.adapter.OrderProductAdapter;
import com.example.storehub.model.CancelOrderRequest;
import com.example.storehub.model.CartItem;
import com.example.storehub.model.Order;
import com.example.storehub.model.Response;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.DateTimeUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Giao diện chi tiết đơn hàng đang vận chuyển kết nối dữ liệu thật.
 */
public class ShippingOrderDetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Order order;
    private View btnCancelOrder;
    private RecyclerView rvOrderProducts;
    private OrderProductAdapter adapter;
    private TextView tvSubtotal, tvShippingFee, tvTotal, tvOrderDetailCode;
    private TextView tvShippingNamePhone, tvShippingAddress;
    private TextView tvStatusTitle, tvStatusBadge, tvEstimatedDelivery;
    private ApiServices apiService;
    private Call<Response<ArrayList<CartItem>>> cartCall;
    private static final long DEFAULT_SHIPPING_FEE = 40000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_shipping);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.orderDetailRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        initUi();
        setUpAdapter();
        setUpListener();

        apiService = new HttpResquest().callAPI();

        // Nhận dữ liệu Order từ Intent
        order = (Order) getIntent().getSerializableExtra("order_data");
        if (order != null) {
            bindOrderData(order);
        } else {
            // Fallback load các sản phẩm từ cart nếu đi trực tiếp (hoặc phòng hờ)
            loadOrderProductsFallback();
        }

    }

    private void initUi() {
        toolbar = findViewById(R.id.toolbar);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        tvOrderDetailCode = findViewById(R.id.tvOrderDetailCode);
        tvShippingNamePhone = findViewById(R.id.tvShippingNamePhone);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);
        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvEstimatedDelivery = findViewById(R.id.tvEstimatedDelivery);
        rvOrderProducts = findViewById(R.id.rvOrderProducts);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
    }

    private void setUpListener() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
        if (btnCancelOrder != null) {
            btnCancelOrder.setOnClickListener(v -> {
                if (order != null) {
                    showCancelOrderDialog(order);
                }
            });
        }
    }

    private void setUpAdapter() {
        rvOrderProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderProductAdapter(this);
        rvOrderProducts.setAdapter(adapter);
    }

    private void bindOrderData(Order order) {
        if (tvOrderDetailCode != null) {
            tvOrderDetailCode.setText("Mã đơn: " + order.getOrderCode());
        }

        String status = order.getStatus();
        if (status == null || status.isEmpty()) {
            status = "Chờ xác nhận";
        }
        if (tvStatusTitle != null) {
            tvStatusTitle.setText("▰  " + status);
        }
        if (tvStatusBadge != null) {
            tvStatusBadge.setText(status);
        }
        if (tvEstimatedDelivery != null) {
            tvEstimatedDelivery.setText("Dự kiến giao: " + DateTimeUtils.calculateVNEstimatedDelivery(order.getCreatedAt()));
        }

        updateTimeline(order);

        if (tvShippingNamePhone != null) {
            String name = order.getRecipientName();
            String phone = order.getRecipientPhone();
            if (name.isEmpty() && phone.isEmpty()) {
                tvShippingNamePhone.setText("Không có thông tin người nhận");
            } else {
                tvShippingNamePhone.setText(name + "  •  " + phone);
            }
        }

        if (tvShippingAddress != null) {
            String address = order.getRecipientAddress();
            if (address.isEmpty()) {
                tvShippingAddress.setText("Chưa cung cấp địa chỉ nhận hàng");
            } else {
                tvShippingAddress.setText(address);
            }
        }

        adapter.updateData(order.getItems());

        long subtotal = 0;
        for (CartItem item : order.getItems()) {
            subtotal += item.getTotalItemPrice();
        }

        long shippingFee = order.getShippingFee();
        long total = subtotal + shippingFee;

        if (tvSubtotal != null) tvSubtotal.setText(formatPrice(subtotal));
        if (tvShippingFee != null) tvShippingFee.setText(formatPrice(shippingFee));
        if (tvTotal != null) tvTotal.setText(formatPrice(total));

        TextView tvVoucher = findViewById(R.id.tvVoucher);
        if (tvVoucher != null) tvVoucher.setText("-" + formatPrice(0));
    }

    private void loadOrderProductsFallback() {
        if (cartCall != null) {
            cartCall.cancel();
        }

        cartCall = apiService.getCart();
        cartCall.enqueue(new Callback<Response<ArrayList<CartItem>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<CartItem>>> call,
                                   @NonNull retrofit2.Response<Response<ArrayList<CartItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ArrayList<CartItem> items = response.body().getData();
                    adapter.updateData(items);

                    long subtotal = 0;
                    for (CartItem item : items) {
                        subtotal += item.getTotalItemPrice();
                    }
                    long total = subtotal + DEFAULT_SHIPPING_FEE;

                    if (tvSubtotal != null) tvSubtotal.setText(formatPrice(subtotal));
                    if (tvShippingFee != null)
                        tvShippingFee.setText(formatPrice(DEFAULT_SHIPPING_FEE));
                    if (tvTotal != null) tvTotal.setText(formatPrice(total));
                } else {
                    Toast.makeText(ShippingOrderDetailActivity.this, "Không thể lấy danh sách sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<CartItem>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                Log.e("ShippingOrderDetail", "Error loading order products", t);
                Toast.makeText(ShippingOrderDetailActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    private void showCancelOrderDialog(final Order order) {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_cancel_order);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        final android.widget.RadioGroup rgCancelReasons = dialog.findViewById(R.id.rgCancelReasons);
        final android.widget.EditText edtCancelNote = dialog.findViewById(R.id.edtCancelNote);
        android.widget.ImageView btnCloseDialog = dialog.findViewById(R.id.btnCloseDialog);
        com.google.android.material.button.MaterialButton btnDismissCancel = dialog.findViewById(R.id.btnDismissCancel);
        com.google.android.material.button.MaterialButton btnConfirmCancel = dialog.findViewById(R.id.btnConfirmCancel);

        if (btnCloseDialog != null) {
            btnCloseDialog.setOnClickListener(v -> dialog.dismiss());
        }
        if (btnDismissCancel != null) {
            btnDismissCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnConfirmCancel != null) {
            btnConfirmCancel.setOnClickListener(v -> {
                String reason = "";
                int checkedId = rgCancelReasons != null ? rgCancelReasons.getCheckedRadioButtonId() : -1;
                if (checkedId != -1) {
                    android.widget.RadioButton rb = dialog.findViewById(checkedId);
                    if (rb != null) {
                        reason = rb.getText().toString();
                    }
                }

                String note = edtCancelNote != null ? edtCancelNote.getText().toString().trim() : "";
                if (!note.isEmpty()) {
                    if (reason.isEmpty()) {
                        reason = note;
                    } else {
                        reason += " - Ghi chú: " + note;
                    }
                }

                if (reason.isEmpty()) {
                    Toast.makeText(this, "Vui lòng chọn hoặc nhập lý do hủy", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                executeCancelOrder(order, reason);
            });
        }

        dialog.show();
    }

    private void executeCancelOrder(Order order, String reason) {
        CancelOrderRequest request = new CancelOrderRequest(order.getOrderId(), reason);
        apiService.cancelOrder(request).enqueue(new retrofit2.Callback<Response<Order>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Response<Order>> call,
                                   @NonNull retrofit2.Response<Response<Order>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ShippingOrderDetailActivity.this, "Đã hủy đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                    // Go back to Orders list fragment by finishing
                    finish();
                } else {
                    Toast.makeText(ShippingOrderDetailActivity.this, "Không thể hủy đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Response<Order>> call, @NonNull Throwable t) {
                Toast.makeText(ShippingOrderDetailActivity.this, "Không kết nối được máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTimeline(Order order) {
        LinearLayout layoutConfirmed = findViewById(R.id.layoutStepConfirmed);
        LinearLayout layoutWarehouse = findViewById(R.id.layoutStepWarehouse);
        LinearLayout layoutDelivering = findViewById(R.id.layoutStepDelivering);
        LinearLayout layoutCompleted = findViewById(R.id.layoutStepCompleted);

        ImageView ivConfirmed = findViewById(R.id.ivStepConfirmed);
        ImageView ivWarehouse = findViewById(R.id.ivStepWarehouse);
        ImageView ivDelivering = findViewById(R.id.ivStepDelivering);
        ImageView ivCompleted = findViewById(R.id.ivStepCompleted);

        TextView tvConfirmed = findViewById(R.id.tvStepConfirmed);
        TextView tvWarehouse = findViewById(R.id.tvStepWarehouse);
        TextView tvDelivering = findViewById(R.id.tvStepDelivering);
        TextView tvCompleted = findViewById(R.id.tvStepCompleted);

        if (layoutConfirmed == null || layoutWarehouse == null || layoutDelivering == null || layoutCompleted == null) {
            return;
        }

        // Set default / pending state first
        layoutConfirmed.setAlpha(0.4f);
        layoutWarehouse.setAlpha(0.4f);
        layoutDelivering.setAlpha(0.4f);
        layoutCompleted.setAlpha(0.4f);

        ivConfirmed.setBackgroundResource(R.drawable.bg_timeline_pending);
        ivConfirmed.setImageDrawable(null);
        ivWarehouse.setBackgroundResource(R.drawable.bg_timeline_pending);
        ivWarehouse.setImageDrawable(null);
        ivDelivering.setBackgroundResource(R.drawable.bg_timeline_pending);
        ivDelivering.setImageDrawable(null);
        ivCompleted.setBackgroundResource(R.drawable.bg_timeline_pending);
        ivCompleted.setImageDrawable(null);

        tvConfirmed.setText("Đã xác nhận");
        tvWarehouse.setText("Đã rời kho");
        tvDelivering.setText("Đang giao hàng\nĐơn hàng đang được shipper vận chuyển đến bạn.");
        tvCompleted.setText("Đã giao hàng");

        String status = order != null ? order.getStatus() : "Chờ xác nhận";
        if (status == null) status = "Chờ xác nhận";

        // Setup dates dynamically if available
        String confirmedTime = (order != null && order.getConfirmedAt() != null) ? DateTimeUtils.formatISOToVN(order.getConfirmedAt(), "HH:mm  •  dd/MM/yyyy") : "";
        String warehouseTime = (order != null && order.getWarehouseAt() != null) ? DateTimeUtils.formatISOToVN(order.getWarehouseAt(), "HH:mm  •  dd/MM/yyyy") : "";
        String deliveringTime = (order != null && order.getDeliveringAt() != null) ? DateTimeUtils.formatISOToVN(order.getDeliveringAt(), "HH:mm  •  dd/MM/yyyy") : "";
        String completedTime = (order != null && order.getCompletedAt() != null) ? DateTimeUtils.formatISOToVN(order.getCompletedAt(), "HH:mm  •  dd/MM/yyyy") : "";

        if ("Đã xác nhận".equalsIgnoreCase(status)) {
            // Step 1 active
            layoutConfirmed.setAlpha(1.0f);
            ivConfirmed.setBackgroundResource(R.drawable.bg_timeline_active);
            ivConfirmed.setImageResource(R.drawable.ic_check);
            if (!confirmedTime.isEmpty()) {
                tvConfirmed.setText("Đã xác nhận\n" + confirmedTime);
            }
        } else if ("Đã rời kho".equalsIgnoreCase(status)) {
            // Step 1 completed, Step 2 active
            layoutConfirmed.setAlpha(1.0f);
            ivConfirmed.setBackgroundResource(R.drawable.bg_timeline_done);
            ivConfirmed.setImageResource(R.drawable.ic_order_done);
            if (!confirmedTime.isEmpty()) {
                tvConfirmed.setText("Đã xác nhận\n" + confirmedTime);
            }

            layoutWarehouse.setAlpha(1.0f);
            ivWarehouse.setBackgroundResource(R.drawable.bg_timeline_active);
            ivWarehouse.setImageResource(R.drawable.ic_check);
            if (!warehouseTime.isEmpty()) {
                tvWarehouse.setText("Đã rời kho\n" + warehouseTime);
            }
        } else if ("Đang giao hàng".equalsIgnoreCase(status)) {
            // Step 1, 2 completed, Step 3 active
            layoutConfirmed.setAlpha(1.0f);
            ivConfirmed.setBackgroundResource(R.drawable.bg_timeline_done);
            ivConfirmed.setImageResource(R.drawable.ic_order_done);
            if (!confirmedTime.isEmpty()) {
                tvConfirmed.setText("Đã xác nhận\n" + confirmedTime);
            }

            layoutWarehouse.setAlpha(1.0f);
            ivWarehouse.setBackgroundResource(R.drawable.bg_timeline_done);
            ivWarehouse.setImageResource(R.drawable.ic_order_done);
            if (!warehouseTime.isEmpty()) {
                tvWarehouse.setText("Đã rời kho\n" + warehouseTime);
            }

            layoutDelivering.setAlpha(1.0f);
            ivDelivering.setBackgroundResource(R.drawable.bg_timeline_active);
            ivDelivering.setImageResource(R.drawable.ic_order_shipping);
            if (!deliveringTime.isEmpty()) {
                tvDelivering.setText("Đang giao hàng\n" + deliveringTime + "\nĐơn hàng đang được shipper vận chuyển đến bạn.");
            }
        } else if ("Đã giao hàng".equalsIgnoreCase(status) || "Đã hoàn thành".equalsIgnoreCase(status)) {
            // All steps completed
            layoutConfirmed.setAlpha(1.0f);
            ivConfirmed.setBackgroundResource(R.drawable.bg_timeline_done);
            ivConfirmed.setImageResource(R.drawable.ic_order_done);
            if (!confirmedTime.isEmpty()) {
                tvConfirmed.setText("Đã xác nhận\n" + confirmedTime);
            }

            layoutWarehouse.setAlpha(1.0f);
            ivWarehouse.setBackgroundResource(R.drawable.bg_timeline_done);
            ivWarehouse.setImageResource(R.drawable.ic_order_done);
            if (!warehouseTime.isEmpty()) {
                tvWarehouse.setText("Đã rời kho\n" + warehouseTime);
            }

            layoutDelivering.setAlpha(1.0f);
            ivDelivering.setBackgroundResource(R.drawable.bg_timeline_done);
            ivDelivering.setImageResource(R.drawable.ic_order_done);
            if (!deliveringTime.isEmpty()) {
                tvDelivering.setText("Đang giao hàng\n" + deliveringTime + "\nĐơn hàng đang được shipper vận chuyển đến bạn.");
            }

            layoutCompleted.setAlpha(1.0f);
            ivCompleted.setBackgroundResource(R.drawable.bg_timeline_done);
            ivCompleted.setImageResource(R.drawable.ic_check);
            if (!completedTime.isEmpty()) {
                tvCompleted.setText("Đã giao hàng\n" + completedTime);
            }
        } else {
            // Default "Chờ xác nhận" or empty initial state
            layoutConfirmed.setAlpha(1.0f);
            ivConfirmed.setBackgroundResource(R.drawable.bg_timeline_active);
            ivConfirmed.setImageResource(R.drawable.ic_check);
        }
    }

    @Override
    protected void onDestroy() {
        if (cartCall != null) {
            cartCall.cancel();
        }
        super.onDestroy();
    }
}
