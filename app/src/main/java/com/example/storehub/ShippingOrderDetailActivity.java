package com.example.storehub;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.adapter.OrderProductAdapter;
import com.example.storehub.model.CartItem;
import com.example.storehub.model.Order;
import com.example.storehub.model.Response;
import com.example.storehub.model.CancelOrderRequest;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

/** Giao diện chi tiết đơn hàng đang vận chuyển kết nối dữ liệu thật. */
public class ShippingOrderDetailActivity extends AppCompatActivity {

    private RecyclerView rvOrderProducts;
    private OrderProductAdapter adapter;
    private TextView tvSubtotal, tvShippingFee, tvTotal, tvOrderDetailCode;
    private ApiServices apiService;
    private Call<Response<ArrayList<CartItem>>> cartCall;
    private static final long DEFAULT_SHIPPING_FEE = 40000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_shipping);
        
        applySystemBarInsets();

        // Bind views
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        tvOrderDetailCode = findViewById(R.id.tvOrderDetailCode);

        rvOrderProducts = findViewById(R.id.rvOrderProducts);
        rvOrderProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderProductAdapter(this);
        rvOrderProducts.setAdapter(adapter);

        apiService = new HttpResquest().callAPI();

        // Nhận dữ liệu Order từ Intent
        Order order = (Order) getIntent().getSerializableExtra("order_data");
        if (order != null) {
            bindOrderData(order);
        } else {
            // Fallback load các sản phẩm từ cart nếu đi trực tiếp (hoặc phòng hờ)
            loadOrderProductsFallback();
        }

        android.view.View btnCancelOrder = findViewById(R.id.btnCancelOrder);
        if (btnCancelOrder != null) {
            btnCancelOrder.setOnClickListener(v -> {
                if (order != null) {
                    showCancelOrderDialog(order);
                }
            });
        }
    }

    private void bindOrderData(Order order) {
        if (tvOrderDetailCode != null) {
            tvOrderDetailCode.setText("Mã đơn: " + order.getOrderCode());
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
                    if (tvShippingFee != null) tvShippingFee.setText(formatPrice(DEFAULT_SHIPPING_FEE));
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

    private void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.orderDetailRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
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

    @Override
    protected void onDestroy() {
        if (cartCall != null) {
            cartCall.cancel();
        }
        super.onDestroy();
    }
}
