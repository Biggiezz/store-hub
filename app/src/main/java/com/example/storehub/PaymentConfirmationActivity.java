package com.example.storehub;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.storehub.model.CartItem;
import com.example.storehub.model.Order;
import com.example.storehub.model.response.Response;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.zalopay.Api.CreateOrder;
import com.example.storehub.zalopay.Constant.AppInfo;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PaymentConfirmationActivity extends BaseActivity {

    private static final String EXTRA_CART_ITEMS = "cart_items";
    private static final long SHIPPING_FEE = 40000L;
    private final ArrayList<CartItem> cartItems = new ArrayList<>();
    private long subtotal;
    private ImageButton btnBack;
    private MaterialButton btnConfirmPayment;
    private LinearLayout layoutProductSummary, optionZaloPay, optionCod;
    private RadioButton rbZaloPay, rbCod;
    private TextView tvSubtotal, tvShippingFee, tvDiscount, tvTotal;
    private ApiServices apiService;

    public static Intent createIntent(Context context, ArrayList<CartItem> items) {
        Intent intent = new Intent(context, PaymentConfirmationActivity.class);
        intent.putExtra(EXTRA_CART_ITEMS, items);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_confirmation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.paymentRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        ZaloPaySDK.init(AppInfo.APP_ID, Environment.SANDBOX);

        ArrayList<CartItem> items = (ArrayList<CartItem>) getIntent().getSerializableExtra(EXTRA_CART_ITEMS);
        if (items != null) cartItems.addAll(items);
        if (cartItems.isEmpty()) {
            if (isZaloPayReturnIntent(getIntent())) {
                handleZaloPayResult(getIntent());
                return;
            }
            finish();
            return;
        }

        apiService = new HttpResquest().callAPI();
        initUi();
        initListener();
        bindSummary();
    }

    private void initUi() {
        btnBack = findViewById(R.id.btnBack);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        layoutProductSummary = findViewById(R.id.layoutProductSummary);
        optionZaloPay = findViewById(R.id.optionZaloPay);
        optionCod = findViewById(R.id.optionCod);
        rbZaloPay = findViewById(R.id.rbZaloPay);
        rbCod = findViewById(R.id.rbCod);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvTotal = findViewById(R.id.tvTotal);
    }

    private void initListener() {
        btnBack.setOnClickListener(v -> finish());
        optionZaloPay.setOnClickListener(v -> selectPayment(rbZaloPay, rbCod));
        optionCod.setOnClickListener(v -> selectPayment(rbCod, rbZaloPay));
        btnConfirmPayment.setOnClickListener(v -> createOrder());
    }

    private void bindSummary() {
        for (CartItem item : cartItems) {
            subtotal += item.getTotalItemPrice();
            View product = LayoutInflater.from(this).inflate(
                    R.layout.item_payment_product_summary, layoutProductSummary, false);
            ((TextView) product.findViewById(R.id.tvPaymentProductName)).setText(item.getProductName());
            ((TextView) product.findViewById(R.id.tvPaymentProductPrice)).setText(formatPrice(item.getPrice()));
            ((TextView) product.findViewById(R.id.tvPaymentProductQuantity)).setText("Số lượng: " + item.getQuantity());
            layoutProductSummary.addView(product);
        }

        long total = subtotal + SHIPPING_FEE;
        tvSubtotal.setText(formatPrice(subtotal));
        tvShippingFee.setText(formatPrice(SHIPPING_FEE));
        tvDiscount.setText("-" + formatPrice(0));
        tvTotal.setText(formatPrice(total));
    }

    private void createOrder() {
        if (rbZaloPay.isChecked()) {
            createZaloPayOrder();
        } else {
            createStoreOrder();
        }
    }

    // Sandbox demo only: move MAC signing and CreateOrder to StoreHubServer before production.
    private void createZaloPayOrder() {
        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText("Đang tạo giao dịch...");

        long total = subtotal + SHIPPING_FEE;
        new Thread(() -> {
            try {
                JSONObject result = new CreateOrder().createOrder(String.valueOf(total));
                String token = result == null ? "" : result.optString("zp_trans_token");
                if (!"1".equals(result == null ? "" : result.optString("return_code")) || token.isEmpty()) {
                    throw new IllegalStateException(result == null ? "Không nhận được phản hồi từ ZaloPay" : result.optString("return_message"));
                }

                runOnUiThread(() -> payWithZaloPay(token));
            } catch (Exception exception) {
                Log.e("PaymentConfirmation", "Cannot create ZaloPay order", exception);
                runOnUiThread(() -> resetZaloPayButton("Không thể tạo giao dịch ZaloPay"));
            }
        }).start();
    }

    private void payWithZaloPay(String token) {
        btnConfirmPayment.setText("Đang mở ZaloPay...");
        ZaloPaySDK.getInstance().payOrder(
                this,
                token,
                "merchant-deeplink://app",
                new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                        runOnUiThread(() -> {
                            Toast.makeText(PaymentConfirmationActivity.this, "Thanh toán ZaloPay thành công", Toast.LENGTH_SHORT).show();
                            createStoreOrder();
                        });
                    }

                    @Override
                    public void onPaymentCanceled(String zpTransToken, String appTransID) {
                        runOnUiThread(() -> resetZaloPayButton("Bạn đã hủy thanh toán ZaloPay"));
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                        Log.e("PaymentConfirmation", "ZaloPay error: " + zaloPayError);
                        runOnUiThread(() -> resetZaloPayButton("Thanh toán ZaloPay không thành công"));
                    }
                }
        );
    }

    private void createStoreOrder() {
        final boolean paidWithZaloPay = rbZaloPay.isChecked();
        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText(paidWithZaloPay ? "Đang tạo đơn hàng..." : "Xác nhận thanh toán");
        apiService.createOrder(HttpResquest.authorizationHeader(this), paidWithZaloPay ? "ZaloPay" : "COD").enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Order>> call, @NonNull retrofit2.Response<Response<Order>> response) {
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText("Xác nhận thanh toán");
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    if (paidWithZaloPay) {
                        openCustomerOrders();
                    } else {
                        Intent intent = new Intent(PaymentConfirmationActivity.this, ShippingOrderDetailActivity.class);
                        intent.putExtra("order_data", response.body().getData());
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(PaymentConfirmationActivity.this, "Đặt hàng thất bại. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Order>> call, @NonNull Throwable t) {
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText("Xác nhận thanh toán");
                Toast.makeText(PaymentConfirmationActivity.this, "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetZaloPayButton(String message) {
        btnConfirmPayment.setEnabled(true);
        btnConfirmPayment.setText("Xác nhận thanh toán");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void openCustomerOrders() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_OPEN_TAB, MainActivity.TAB_ORDERS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private String formatPrice(long amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }

    private void selectPayment(RadioButton selected, RadioButton other) {
        selected.setChecked(true);
        other.setChecked(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleZaloPayResult(intent);
    }

    private boolean isZaloPayReturnIntent(Intent intent) {
        return intent != null
                && intent.getData() != null
                && "merchant-deeplink".equals(intent.getData().getScheme());
    }

    private void handleZaloPayResult(Intent intent) {
        if (isZaloPayReturnIntent(intent)) {
            Log.d("PaymentConfirmation", "Received ZaloPay return: " + intent.getData());
            ZaloPaySDK.getInstance().onResult(intent);
        }
    }

}
