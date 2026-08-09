package com.nguyenmanhphuc.storehubapp;

import com.nguyenmanhphuc.storehubapp.R;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nguyenmanhphuc.storehubapp.adapter.OrderProductAdapter;
import com.nguyenmanhphuc.storehubapp.model.CartItem;
import com.nguyenmanhphuc.storehubapp.model.Order;
import com.nguyenmanhphuc.storehubapp.model.request.CancelOrderRequest;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.ApiServices;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.DataCache;
import com.nguyenmanhphuc.storehubapp.utils.DateTimeUtils;
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

    private ImageView btnBack;
    private Order order;
    private LinearLayout layoutConfirmed, layoutWarehouse, layoutDelivering, layoutCompleted, layoutStatusHeader;
    private View btnCancelOrder;
    private ImageView ivConfirmed, ivWarehouse, ivDelivering, ivCompleted;
    private RecyclerView rvOrderProducts;
    private OrderProductAdapter adapter;
    private TextView tvSubtotal, tvShippingFee, tvTotal, tvPaymentMethod, tvOrderDetailCode, tvShippingNamePhone, tvShippingAddress, tvStatusTitle, tvStatusBadge, tvEstimatedDelivery, tvConfirmed, tvWarehouse, tvDelivering, tvCompleted;
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
        btnBack = findViewById(R.id.btnBack);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvOrderDetailCode = findViewById(R.id.tvOrderDetailCode);
        tvShippingNamePhone = findViewById(R.id.tvShippingNamePhone);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);
        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvEstimatedDelivery = findViewById(R.id.tvEstimatedDelivery);
        rvOrderProducts = findViewById(R.id.rvOrderProducts);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        layoutStatusHeader = findViewById(R.id.layoutStatusHeader);
    }

    private void setUpListener() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
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

    private String getLocalizedStatus(String status) {
        if (status == null) return "";
        if ("Chờ xác nhận".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status)) {
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

    private void bindOrderData(Order order) {
        if (tvOrderDetailCode != null) {
            tvOrderDetailCode.setText(getString(R.string.order_code_prefix, order.getOrderCode()));
        }

        String status = order.getStatus();
        if (status == null || status.isEmpty()) {
            status = "Chờ xác nhận";
        }
        if (tvStatusTitle != null) {
            tvStatusTitle.setText(getLocalizedStatus(status));
        }
        if (tvStatusBadge != null) {
            tvStatusBadge.setText(getLocalizedStatus(status));
            if ("Đã hoàn thành".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status) || "done".equalsIgnoreCase(status)) {
                tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_completed);
            } else if ("Đã hủy".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status) || "cancel".equalsIgnoreCase(status)) {
                tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#C62828"));
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_cancelled);
            } else if ("Chờ xác nhận".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status) || "Chờ xử lý".equalsIgnoreCase(status)) {
                tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#B78103"));
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_pending);
            } else if ("Đã xác nhận".equalsIgnoreCase(status) || "confirmed".equalsIgnoreCase(status)) {
                tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#1565C0"));
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_shipping);
            } else {
                tvStatusBadge.setTextColor(android.graphics.Color.parseColor("#1565C0"));
                tvStatusBadge.setBackgroundResource(R.drawable.bg_status_shipping);
            }
        }
        if (layoutStatusHeader != null) {
            if ("Đã hoàn thành".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status) || "done".equalsIgnoreCase(status)) {
                layoutStatusHeader.setBackgroundResource(R.drawable.bg_detail_section_completed);
            } else if ("Đã hủy".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status) || "cancel".equalsIgnoreCase(status)) {
                layoutStatusHeader.setBackgroundResource(R.drawable.bg_detail_section_cancelled);
            } else if ("Chờ xác nhận".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status) || "Chờ xử lý".equalsIgnoreCase(status)) {
                layoutStatusHeader.setBackgroundResource(R.drawable.bg_detail_section_warm);
            } else {
                layoutStatusHeader.setBackgroundResource(R.drawable.bg_detail_section_shipping);
            }
        }
        if (btnCancelOrder != null) {
            String norm = status.trim().toLowerCase();
            if (norm.contains("chờ xác nhận") || norm.contains("pending") || norm.contains("chờ xử lý") ||
                norm.contains("đã xác nhận") || norm.contains("confirmed")) {
                btnCancelOrder.setVisibility(View.VISIBLE);
            } else {
                btnCancelOrder.setVisibility(View.GONE);
            }
        }
        if (tvEstimatedDelivery != null) {
            tvEstimatedDelivery.setText(getString(R.string.estimated_delivery_prefix, DateTimeUtils.calculateVNEstimatedDelivery(this, order.getCreatedAt())));
        }

        updateTimeline(order);

        if (tvShippingNamePhone != null) {
            String name = order.getRecipientName();
            String phone = order.getRecipientPhone();
            if ((name == null || name.isEmpty()) && (phone == null || phone.isEmpty())) {
                tvShippingNamePhone.setText(getString(R.string.no_recipient_info));
            } else {
                tvShippingNamePhone.setText((name != null ? name : "") + "  •  " + (phone != null ? phone : ""));
            }
        }

        if (tvShippingAddress != null) {
            String address = order.getRecipientAddress();
            if (address == null || address.isEmpty()) {
                tvShippingAddress.setText(getString(R.string.no_shipping_address));
            } else {
                tvShippingAddress.setText(address);
            }
        }

        ArrayList<CartItem> safeItems = order.getItems() != null ? order.getItems() : new ArrayList<>();
        adapter.updateData(safeItems);

        long subtotal = 0;
        int totalQty = 0;
        for (CartItem item : safeItems) {
            subtotal += item.getTotalItemPrice();
            totalQty += item.getQuantity();
        }

        long shippingFee = order.getShippingFee();
        long discount = order.getDiscount();
        long total = subtotal + shippingFee - discount;
        if (total < 0) total = 0;

        if (tvSubtotal != null) tvSubtotal.setText(formatPrice(subtotal));
        if (tvShippingFee != null) tvShippingFee.setText(formatPrice(shippingFee));
        if (tvTotal != null) tvTotal.setText(formatPrice(total));
        if (tvPaymentMethod != null) tvPaymentMethod.setText("ZaloPay".equalsIgnoreCase(order.getPaymentMethod())
                ? "ZaloPay" : getString(R.string.payment_cod));

        TextView tvSubtotalLabel = findViewById(R.id.tvSubtotalLabel);
        if (tvSubtotalLabel != null) {
            tvSubtotalLabel.setText(String.format(getString(R.string.subtotal_label_template), totalQty));
        }

        TextView tvVoucher = findViewById(R.id.tvVoucher);
        if (tvVoucher != null) tvVoucher.setText("-" + formatPrice(discount));

        TextView tvZaloPayNote = findViewById(R.id.tvZaloPayNote);
        if (tvZaloPayNote != null) {
            tvZaloPayNote.setVisibility("ZaloPay".equalsIgnoreCase(order.getPaymentMethod()) ? android.view.View.VISIBLE : android.view.View.GONE);
        }
    }

    private void loadOrderProductsFallback() {
        if (cartCall != null) {
            cartCall.cancel();
        }

        cartCall = apiService.getCart(HttpResquest.authorizationHeader(this));
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
                    Toast.makeText(ShippingOrderDetailActivity.this, ShippingOrderDetailActivity.this.getString(R.string.toast_khong_the_lay_danh_sach_san_pham), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<CartItem>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                Log.e("ShippingOrderDetail", "Error loading order products", t);
                Toast.makeText(ShippingOrderDetailActivity.this, ShippingOrderDetailActivity.this.getString(R.string.toast_loi_tai_du_lieu), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    private void showCancelOrderDialog(final Order order) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_cancel_order);


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
                        reason += getString(R.string.note_prefix) + note;
                    }
                }

                if (reason.isEmpty()) {
                    Toast.makeText(this, this.getString(R.string.toast_vui_long_chon_hoac_nhap_ly_do_huy), Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                executeCancelOrder(order, reason);
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void executeCancelOrder(Order order, String reason) {
        CancelOrderRequest request = new CancelOrderRequest(order.getOrderId(), reason);
        apiService.cancelOrder(HttpResquest.authorizationHeader(this), request).enqueue(new retrofit2.Callback<Response<Order>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Response<Order>> call,
                                   @NonNull retrofit2.Response<Response<Order>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ShippingOrderDetailActivity.this, ShippingOrderDetailActivity.this.getString(R.string.toast_da_huy_don_hang_thanh_cong), Toast.LENGTH_SHORT).show();
                    // Xóa cache đơn hàng → OderFragment sẽ hiện trạng thái mới ngay khi quay lại
                    DataCache.get().invalidate("user_orders");
                    finish();
                } else {
                    Toast.makeText(ShippingOrderDetailActivity.this, ShippingOrderDetailActivity.this.getString(R.string.toast_khong_the_huy_don_hang), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Response<Order>> call, @NonNull Throwable t) {
                Toast.makeText(ShippingOrderDetailActivity.this, ShippingOrderDetailActivity.this.getString(R.string.toast_khong_ket_noi_duoc_may_chu), Toast.LENGTH_SHORT).show();
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
            tvConfirmed.setText(getString(R.string.status_pending));
            tvWarehouse.setText(getString(R.string.status_dispatched));
            tvDelivering.setText(getString(R.string.status_shipping_desc));
            tvCompleted.setText(getString(R.string.status_completed_desc, (estimatedDateStr.isEmpty() ? getString(R.string.after_5_days) : estimatedDateStr)));
        } else if ("Đã xác nhận".equalsIgnoreCase(status)) {
            tvConfirmed.setText(getString(R.string.status_confirmed_at, confirmedTime));
            tvWarehouse.setText(getString(R.string.status_dispatched));
            tvDelivering.setText(getString(R.string.status_shipping_desc));
            tvCompleted.setText(getString(R.string.status_completed_desc, (estimatedDateStr.isEmpty() ? getString(R.string.after_5_days) : estimatedDateStr)));
        } else if ("Đã rời kho".equalsIgnoreCase(status)) {
            tvConfirmed.setText(getString(R.string.status_confirmed_at, confirmedTime));
            tvWarehouse.setText(getString(R.string.status_warehouse_at, warehouseTime));
            tvDelivering.setText(getString(R.string.status_shipping_desc));
            tvCompleted.setText(getString(R.string.status_completed_desc, (estimatedDateStr.isEmpty() ? getString(R.string.after_5_days) : estimatedDateStr)));
        } else if ("Đang giao hàng".equalsIgnoreCase(status)) {
            tvConfirmed.setText(getString(R.string.status_confirmed_at, confirmedTime));
            tvWarehouse.setText(getString(R.string.status_warehouse_at, warehouseTime));
            tvDelivering.setText(getString(R.string.status_shipping) + "\n" + deliveringTime + "\n" + (getString(R.string.status_shipping_desc).contains("\n") ? getString(R.string.status_shipping_desc).substring(getString(R.string.status_shipping_desc).indexOf("\n") + 1) : ""));
            tvCompleted.setText(getString(R.string.status_completed_desc, (estimatedDateStr.isEmpty() ? getString(R.string.after_5_days) : estimatedDateStr)));
        } else if ("Đã giao hàng".equalsIgnoreCase(status) || "Đã hoàn thành".equalsIgnoreCase(status)) {
            tvConfirmed.setText(getString(R.string.status_confirmed_at, confirmedTime));
            tvWarehouse.setText(getString(R.string.status_warehouse_at, warehouseTime));
            tvDelivering.setText(getString(R.string.status_shipping) + "\n" + deliveringTime + "\n" + (getString(R.string.status_shipping_desc).contains("\n") ? getString(R.string.status_shipping_desc).substring(getString(R.string.status_shipping_desc).indexOf("\n") + 1) : ""));
            tvCompleted.setText(getString(R.string.status_delivered_at, completedTime));
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
