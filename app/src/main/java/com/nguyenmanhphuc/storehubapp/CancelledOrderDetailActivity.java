package com.nguyenmanhphuc.storehubapp;

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

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Giao diện chi tiết đơn hàng đã hủy kết nối dữ liệu thật.
 */
public class CancelledOrderDetailActivity extends BaseActivity {

    private Toolbar toolbar;
    private Order order;
    private RecyclerView rvOrderProducts;
    private OrderProductAdapter adapter;
    private TextView tvSubtotal, tvShippingFee, tvTotal, tvPaymentMethod, tvOrderDetailCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_cancelled);
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

        rvOrderProducts = findViewById(R.id.rvOrderProducts);
    }

    private void setUpListener() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
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

        TextView tvVoucher = findViewById(R.id.tvVoucher);
        if (tvVoucher != null) tvVoucher.setText("-" + formatPrice(0));

        TextView tvCancelReason = findViewById(R.id.tvCancelReason);
        if (tvCancelReason != null) {
            String reason = order.getCancelReason();
            if (reason == null || reason.isEmpty()) {
                reason = getString(R.string.reason_changed_mind);
            }
            tvCancelReason.setText(getString(R.string.cancel_reason_prefix, reason));
        }
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }
}
