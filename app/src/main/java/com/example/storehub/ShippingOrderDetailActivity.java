package com.example.storehub;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.adapter.OrderProductAdapter;
import com.example.storehub.model.CartItem;
import com.example.storehub.model.Order;
import com.example.storehub.model.Order.CancelOrderRequest;
import com.example.storehub.model.Response;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.DateTimeUtils;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Giao diện chi tiết đơn hàng đang vận chuyển kết nối dữ liệu thật.
 */
public class ShippingOrderDetailActivity extends BaseActivity {

    private Toolbar toolbar;
    private Order order;
    private LinearLayout layoutConfirmed, layoutWarehouse, layoutDelivering, layoutCompleted;
    private View btnCancelOrder;
    private ImageView ivConfirmed, ivWarehouse, ivDelivering, ivCompleted;
    private RecyclerView rvOrderProducts;
    private OrderProductAdapter adapter;
    private TextView tvSubtotal, tvShippingFee, tvTotal, tvOrderDetailCode, tvShippingNamePhone, tvShippingAddress, tvStatusTitle, tvStatusBadge, tvEstimatedDelivery, tvConfirmed, tvWarehouse, tvDelivering, tvCompleted;
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
            tvStatusTitle.setText(status);
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
        int totalQty = 0;
        for (CartItem item : order.getItems()) {
            subtotal += item.getTotalItemPrice();
            totalQty += item.getQuantity();
        }

        long shippingFee = order.getShippingFee();
        long total = subtotal + shippingFee;

        if (tvSubtotal != null) tvSubtotal.setText(formatPrice(subtotal));
        if (tvShippingFee != null) tvShippingFee.setText(formatPrice(shippingFee));
        if (tvTotal != null) tvTotal.setText(formatPrice(total));

        TextView tvSubtotalLabel = findViewById(R.id.tvSubtotalLabel);
        if (tvSubtotalLabel != null) {
            tvSubtotalLabel.setText("Tạm tính (" + totalQty + " sản phẩm)");
        }

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

        final RadioGroup rgCancelReasons = dialog.findViewById(R.id.rgCancelReasons);
        final EditText edtCancelNote = dialog.findViewById(R.id.edtCancelNote);
        ImageView btnCloseDialog = dialog.findViewById(R.id.btnCloseDialog);
        MaterialButton btnDismissCancel = dialog.findViewById(R.id.btnDismissCancel);
        MaterialButton btnConfirmCancel = dialog.findViewById(R.id.btnConfirmCancel);

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
        layoutConfirmed = findViewById(R.id.layoutStepConfirmed);
        layoutWarehouse = findViewById(R.id.layoutStepWarehouse);
        layoutDelivering = findViewById(R.id.layoutStepDelivering);
        layoutCompleted = findViewById(R.id.layoutStepCompleted);

        ivConfirmed = findViewById(R.id.ivStepConfirmed);
        ivWarehouse = findViewById(R.id.ivStepWarehouse);
        ivDelivering = findViewById(R.id.ivStepDelivering);
        ivCompleted = findViewById(R.id.ivStepCompleted);

        tvConfirmed = findViewById(R.id.tvStepConfirmed);
        tvWarehouse = findViewById(R.id.tvStepWarehouse);
        tvDelivering = findViewById(R.id.tvStepDelivering);
        tvCompleted = findViewById(R.id.tvStepCompleted);

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

        String status = order != null ? order.getStatus() : "Chờ xác nhận";
        if (status == null) status = "Chờ xác nhận";

        // Setup dates dynamically if available
        String confirmedTime = "";
        String warehouseTime = "";
        String deliveringTime = "";
        String completedTime = "";

        if (order != null) {
            confirmedTime = getTimelineTime(order, order.getConfirmedAt(), 0, 2);
            warehouseTime = getTimelineTime(order, order.getWarehouseAt(), 1, 0);
            deliveringTime = getTimelineTime(order, order.getDeliveringAt(), 2, 0);
            completedTime = getTimelineTime(order, order.getCompletedAt(), 3, 0);
        }

        String estimatedDateStr = "";
        if (order != null && order.getCreatedAt() != null) {
            Date createdDate = DateTimeUtils.parseISO(order.getCreatedAt());
            if (createdDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(createdDate);
                cal.add(Calendar.DAY_OF_YEAR, 5);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                estimatedDateStr = sdf.format(cal.getTime());
            }
        }

        // Set text label content
        if ("Chờ xác nhận".equalsIgnoreCase(status)) {
            tvConfirmed.setText("Chờ xác nhận");
            tvWarehouse.setText("Đã rời kho");
            tvDelivering.setText("Đang giao hàng\nĐơn hàng đang được shipper vận chuyển đến bạn.");
            tvCompleted.setText("Đã giao hàng\nDự kiến: " + (estimatedDateStr.isEmpty() ? "Sau 5 ngày" : estimatedDateStr));
        } else if ("Đã xác nhận".equalsIgnoreCase(status)) {
            tvConfirmed.setText("Đã xác nhận\n" + confirmedTime);
            tvWarehouse.setText("Đã rời kho");
            tvDelivering.setText("Đang giao hàng\nĐơn hàng đang được shipper vận chuyển đến bạn.");
            tvCompleted.setText("Đã giao hàng\nDự kiến: " + (estimatedDateStr.isEmpty() ? "Sau 5 ngày" : estimatedDateStr));
        } else if ("Đã rời kho".equalsIgnoreCase(status)) {
            tvConfirmed.setText("Đã xác nhận\n" + confirmedTime);
            tvWarehouse.setText("Đã rời kho\n" + warehouseTime);
            tvDelivering.setText("Đang giao hàng\nĐơn hàng đang được shipper vận chuyển đến bạn.");
            tvCompleted.setText("Đã giao hàng\nDự kiến: " + (estimatedDateStr.isEmpty() ? "Sau 5 ngày" : estimatedDateStr));
        } else if ("Đang giao hàng".equalsIgnoreCase(status)) {
            tvConfirmed.setText("Đã xác nhận\n" + confirmedTime);
            tvWarehouse.setText("Đã rời kho\n" + warehouseTime);
            tvDelivering.setText("Đang giao hàng\n" + deliveringTime + "\nĐơn hàng đang được shipper vận chuyển đến bạn.");
            tvCompleted.setText("Đã giao hàng\nDự kiến: " + (estimatedDateStr.isEmpty() ? "Sau 5 ngày" : estimatedDateStr));
        } else if ("Đã giao hàng".equalsIgnoreCase(status) || "Đã hoàn thành".equalsIgnoreCase(status)) {
            tvConfirmed.setText("Đã xác nhận\n" + confirmedTime);
            tvWarehouse.setText("Đã rời kho\n" + warehouseTime);
            tvDelivering.setText("Đang giao hàng\n" + deliveringTime + "\nĐơn hàng đang được shipper vận chuyển đến bạn.");
            tvCompleted.setText("Đã giao hàng\n" + completedTime);
        }

        if ("Đã xác nhận".equalsIgnoreCase(status)) {
            layoutConfirmed.setAlpha(1.0f);
            ivConfirmed.setBackgroundResource(R.drawable.bg_timeline_active);
            ivConfirmed.setImageResource(R.drawable.ic_active);
        } else if ("Đã rời kho".equalsIgnoreCase(status)) {
            layoutConfirmed.setAlpha(1.0f);
            ivConfirmed.setBackgroundResource(R.drawable.bg_timeline_done);
            ivConfirmed.setImageResource(R.drawable.ic_active);

            layoutWarehouse.setAlpha(1.0f);
            ivWarehouse.setBackgroundResource(R.drawable.bg_timeline_active);
            ivWarehouse.setImageResource(R.drawable.ic_active);
        } else if ("Đang giao hàng".equalsIgnoreCase(status)) {
            layoutConfirmed.setAlpha(1.0f);
            ivConfirmed.setBackgroundResource(R.drawable.bg_timeline_done);
            ivConfirmed.setImageResource(R.drawable.ic_active);

            layoutWarehouse.setAlpha(1.0f);
            ivWarehouse.setBackgroundResource(R.drawable.bg_timeline_done);
            ivWarehouse.setImageResource(R.drawable.ic_active);

            layoutDelivering.setAlpha(1.0f);
            ivDelivering.setBackgroundResource(R.drawable.bg_timeline_active);
            ivDelivering.setImageResource(R.drawable.ic_order_shipping);
        } else if ("Đã giao hàng".equalsIgnoreCase(status) || "Đã hoàn thành".equalsIgnoreCase(status)) {
            layoutConfirmed.setAlpha(1.0f);
            ivConfirmed.setBackgroundResource(R.drawable.bg_timeline_done);
            ivConfirmed.setImageResource(R.drawable.ic_active);

            layoutWarehouse.setAlpha(1.0f);
            ivWarehouse.setBackgroundResource(R.drawable.bg_timeline_done);
            ivWarehouse.setImageResource(R.drawable.ic_active);

            layoutDelivering.setAlpha(1.0f);
            ivDelivering.setBackgroundResource(R.drawable.bg_timeline_done);
            ivDelivering.setImageResource(R.drawable.ic_active);

            layoutCompleted.setAlpha(1.0f);
            ivCompleted.setBackgroundResource(R.drawable.bg_timeline_done);
            ivCompleted.setImageResource(R.drawable.ic_active);
        } else {
            // Default "Chờ xác nhận"
            layoutConfirmed.setAlpha(1.0f);
            ivConfirmed.setBackgroundResource(R.drawable.bg_timeline_active);
            ivConfirmed.setImageResource(R.drawable.ic_active);
        }
    }

    private String getTimelineTime(Order order, String actualTime, int addDays, int addHours) {
        if (actualTime != null && !actualTime.isEmpty()) {
            return DateTimeUtils.formatISOToVN(actualTime, "dd/MM/yyyy  •  HH:mm");
        }
        if (order == null || order.getCreatedAt() == null || order.getCreatedAt().isEmpty()) {
            return "";
        }
        try {
            Date createdDate = DateTimeUtils.parseISO(order.getCreatedAt());
            if (createdDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(createdDate);
                if (addDays > 0) cal.add(Calendar.DAY_OF_YEAR, addDays);
                if (addHours > 0) cal.add(Calendar.HOUR_OF_DAY, addHours);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy  •  HH:mm", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
                return sdf.format(cal.getTime());
            }
        } catch (Exception e) {
            Log.e("ShippingOrderDetail", "Error generating fallback timeline time", e);
        }
        return "";
    }

    @Override
    protected void onDestroy() {
        if (cartCall != null) {
            cartCall.cancel();
        }
        super.onDestroy();
    }
}
