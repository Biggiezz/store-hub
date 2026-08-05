package com.nguyenmanhphuc.storehubapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nguyenmanhphuc.storehubapp.adapter.OrderProductAdapter;
import com.nguyenmanhphuc.storehubapp.model.CartItem;
import com.nguyenmanhphuc.storehubapp.model.Order;
import com.nguyenmanhphuc.storehubapp.utils.DateTimeUtils;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Giao diện chi tiết đơn hàng đã hoàn thành kết nối dữ liệu thật.
 */
public class CompletedOrderDetailActivity extends BaseActivity {
    private Toolbar toolbar;
    private RecyclerView rvOrderProducts;
    private OrderProductAdapter adapter;
    private TextView tvSubtotal, tvShippingFee, tvTotal, tvPaymentMethod, tvOrderDetailCode;
    private TextView tvShippingNamePhone, tvShippingAddress, btnReview, tvVoucher, tvStatusText, tvCompletedTime;
    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_completed);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.orderDetailRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        initUi();
        setUpListener();
        setUpAdapter();

        // Nhận dữ liệu Order từ Intent
        order = (Order) getIntent().getSerializableExtra("order_data");
        if (order != null) {
            bindOrderData(order);
        } else {
            Toast.makeText(this, "Không tìm thấy dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private void initUi() {
        toolbar = findViewById(R.id.toolbar);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvOrderDetailCode = findViewById(R.id.tvOrderDetailCode);
        tvShippingNamePhone = findViewById(R.id.tvShippingNamePhone);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);
        btnReview = findViewById(R.id.btnReview);

        rvOrderProducts = findViewById(R.id.rvOrderProducts);
    }

    private void setUpListener() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
        if (btnReview != null) {
            btnReview.setOnClickListener(v -> {
                if (order != null) {
                    Intent intent = new Intent(CompletedOrderDetailActivity.this, WriteReviewActivity.class);
                    // Prepopulate order fields from the first item if empty
                    if (order.getItems() != null && !order.getItems().isEmpty()) {
                        CartItem firstItem = order.getItems().get(0);
                        if (order.getProductName().isEmpty()) {
                            order.setProductName(firstItem.getProductName());
                        }
                        if (order.getProductImage().isEmpty()) {
                            order.setProductImage(firstItem.getProductImage());
                        }
                        if (order.getProductVariant().isEmpty()) {
                            order.setProductVariant(firstItem.getColorName());
                        }
                    }
                    intent.putExtra("order_item", order);
                    startActivity(intent);
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
            tvOrderDetailCode.setText(getString(R.string.order_code_prefix, order.getOrderCode()));
        }

        if (btnReview != null) {
            if (order.isReviewed()) {
                btnReview.setEnabled(false);
                btnReview.setText(getString(R.string.reviewed));
                btnReview.setAlpha(0.5f);
            } else {
                btnReview.setEnabled(true);
                btnReview.setText(getString(R.string.review));
                btnReview.setAlpha(1.0f);
            }
        }

        if (tvShippingNamePhone != null) {
            String name = order.getRecipientName();
            String phone = order.getRecipientPhone();
            if (name.isEmpty() && phone.isEmpty()) {
                tvShippingNamePhone.setText(getString(R.string.no_recipient_info));
            } else {
                tvShippingNamePhone.setText(name + "  •  " + phone);
            }
        }

        if (tvShippingAddress != null) {
            String address = order.getRecipientAddress();
            if (address.isEmpty()) {
                tvShippingAddress.setText(getString(R.string.no_shipping_address));
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
        if (tvPaymentMethod != null) tvPaymentMethod.setText("ZaloPay".equalsIgnoreCase(order.getPaymentMethod())
                ? "ZaloPay" : getString(R.string.payment_cod));

        TextView tvSubtotalLabel = findViewById(R.id.tvSubtotalLabel);
        if (tvSubtotalLabel != null) {
            tvSubtotalLabel.setText(String.format(getString(R.string.subtotal_label_template), totalQty));
        }

        tvVoucher = findViewById(R.id.tvVoucher);
        if (tvVoucher != null) tvVoucher.setText("-" + formatPrice(0));

        tvStatusText = findViewById(R.id.tvStatusText);
        if (tvStatusText != null) {
            tvStatusText.setText(order.getStatus() != null ? getLocalizedStatus(order.getStatus()) : getString(R.string.status_completed));
        }

        tvCompletedTime = findViewById(R.id.tvCompletedTime);
        if (tvCompletedTime != null) {
            String timeStr = (order.getCompletedAt() != null) ? order.getCompletedAt() : order.getCreatedAt();
            tvCompletedTime.setText(getString(R.string.completed_at_prefix, DateTimeUtils.formatISOToVN(timeStr, "HH:mm, dd/MM/yyyy")));
        }
    }

    private String getLocalizedStatus(String status) {
        if (status == null) return "";
        if ("Chờ xác nhận".equalsIgnoreCase(status) || "Pending".equalsIgnoreCase(status)) {
            return getString(R.string.status_pending);
        }
        if ("Đã xác nhận".equalsIgnoreCase(status) || "Confirmed".equalsIgnoreCase(status)) {
            return getString(R.string.status_confirmed_at, "").trim();
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

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
}
