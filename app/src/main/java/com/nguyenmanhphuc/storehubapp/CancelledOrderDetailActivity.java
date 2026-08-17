package com.nguyenmanhphuc.storehubapp;

import com.nguyenmanhphuc.storehubapp.R;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import com.nguyenmanhphuc.storehubapp.adapter.OrderProductAdapter;
import com.nguyenmanhphuc.storehubapp.model.CartItem;
import com.nguyenmanhphuc.storehubapp.model.Order;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import retrofit2.Call;
import retrofit2.Callback;
import androidx.annotation.NonNull;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Giao diện chi tiết đơn hàng đã hủy kết nối dữ liệu thật.
 */
public class CancelledOrderDetailActivity extends BaseActivity {

    private ImageView btnBack;
    private Order order;
    private RecyclerView rvOrderProducts;
    private OrderProductAdapter adapter;
    private TextView tvSubtotal, tvShippingFee, tvTotal, tvPaymentMethod, tvOrderDetailCode;
    private TextView btnReorder, btnContactSupport;

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
            Toast.makeText(this, this.getString(R.string.toast_khong_tim_thay_du_lieu_don_hang), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initUi() {
        btnBack = findViewById(R.id.btnBack);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvOrderDetailCode = findViewById(R.id.tvOrderDetailCode);
        btnReorder = findViewById(R.id.btnReorder);
        btnContactSupport = findViewById(R.id.btnContactSupport);

        rvOrderProducts = findViewById(R.id.rvOrderProducts);
    }

    private void setUpListener() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnContactSupport != null) {
            btnContactSupport.setOnClickListener(v -> showContactSupportDialog());
        }
        if (btnReorder != null) {
            btnReorder.setOnClickListener(v -> reorderProducts());
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

        java.util.ArrayList<CartItem> safeItems = order.getItems() != null ? order.getItems() : new java.util.ArrayList<>();
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

    private void reorderProducts() {
        if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }

        com.nguyenmanhphuc.storehubapp.utils.LoadingDialogHelper loading = new com.nguyenmanhphuc.storehubapp.utils.LoadingDialogHelper(this);
        loading.setMessage("Đang thêm sản phẩm vào giỏ...");
        loading.show();

        String token = com.nguyenmanhphuc.storehubapp.services.HttpResquest.authorizationHeader(this);
        com.nguyenmanhphuc.storehubapp.services.ApiServices apiServices = new com.nguyenmanhphuc.storehubapp.services.HttpResquest().callAPI();

        addItemsToCart(apiServices, token, order.getItems(), 0, loading);
    }

    private void addItemsToCart(com.nguyenmanhphuc.storehubapp.services.ApiServices apiServices, String token, java.util.ArrayList<CartItem> items, int index, com.nguyenmanhphuc.storehubapp.utils.LoadingDialogHelper loading) {
        if (index >= items.size()) {
            loading.dismiss();
            Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
            com.nguyenmanhphuc.storehubapp.utils.DataCache.get().invalidateExact("user_cart");
            startActivity(new Intent(this, CartActivity.class));
            return;
        }

        CartItem item = items.get(index);
        com.nguyenmanhphuc.storehubapp.model.request.AddToCartRequest request = new com.nguyenmanhphuc.storehubapp.model.request.AddToCartRequest(
                item.getProductId(),
                item.getColorId(),
                item.getQuantity()
        );

        apiServices.addToCart(token, request).enqueue(new Callback<Response<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Object>> call, @NonNull retrofit2.Response<Response<Object>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 200) {
                    addItemsToCart(apiServices, token, items, index + 1, loading);
                } else {
                    loading.dismiss();
                    String errorMsg = "Không thể thêm sản phẩm '" + item.getProductName() + "' vào giỏ hàng.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(CancelledOrderDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Object>> call, @NonNull Throwable t) {
                loading.dismiss();
                Toast.makeText(CancelledOrderDetailActivity.this, "Lỗi kết nối máy chủ khi thêm '" + item.getProductName() + "'", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showContactSupportDialog() {
        if (order == null) return;

        String[] options = {"Gọi Hotline (1900 1234)", "Gửi Email hỗ trợ"};

        new android.app.AlertDialog.Builder(this)
                .setTitle("Liên hệ hỗ trợ")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(android.net.Uri.parse("tel:19001234"));
                        startActivity(callIntent);
                    } else if (which == 1) {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(android.net.Uri.parse("mailto:"));
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@storehub.com"});

                        String subject = "[StoreHub] Hỗ trợ đơn hàng #" + order.getOrderCode();
                        String body = "Xin chào StoreHub,\n\nTôi cần hỗ trợ về đơn hàng này:\n" +
                                "- Mã đơn hàng: " + order.getOrderCode() + " (ID: " + order.getOrderId() + ")\n" +
                                "- Trạng thái: Đã hủy (Cancelled)\n" +
                                "- Số tiền: " + (order.getTotalPrice() + order.getShippingFee()) + " đ\n\n" +
                                "Vấn đề của tôi là: (Vui lòng điền chi tiết tại đây)\n\n" +
                                "Xin cảm ơn!";

                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

                        try {
                            startActivity(Intent.createChooser(emailIntent, "Gửi Email hỗ trợ qua"));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(this, "Không tìm thấy ứng dụng gửi Email trên thiết bị!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
