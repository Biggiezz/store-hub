package com.nguyenmanhphuc.storehubapp;

import com.nguyenmanhphuc.storehubapp.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.nguyenmanhphuc.storehubapp.model.CartItem;
import com.nguyenmanhphuc.storehubapp.model.Order;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.ApiServices;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.DataCache;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.nguyenmanhphuc.storehubapp.zalopay.Constant.AppInfo;
import com.google.android.material.button.MaterialButton;

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
    private ImageView btnBack;
    private MaterialButton btnConfirmPayment;
    private LinearLayout layoutProductSummary, optionZaloPay, optionCod;
    private RadioButton rbZaloPay, rbCod;
    private TextView tvSubtotal, tvShippingFee, tvDiscount, tvTotal;
    private ApiServices apiService;
    private String currentAppTransId = null;

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
        btnConfirmPayment.setOnClickListener(v -> {
            if (hasCompleteShippingProfile()) {
                createOrder();
            } else {
                showProfileIncompleteDialog();
            }
        });
    }

    private boolean hasCompleteShippingProfile() {
        User user = new SharedPreferencesManager(this).getUser();
        return user != null
                && !TextUtils.isEmpty(user.getPhone())
                && !TextUtils.isEmpty(user.getAddress());
    }

    private void showProfileIncompleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.profile_incomplete_title)
                .setMessage(R.string.profile_incomplete_message)
                .setPositiveButton(R.string.update_now, (dialog, which) ->
                        startActivity(new Intent(this, EditProfileActivity.class)))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void bindSummary() {
        for (CartItem item : cartItems) {
            subtotal += item.getTotalItemPrice();
            View product = LayoutInflater.from(this).inflate(
                    R.layout.item_payment_product_summary, layoutProductSummary, false);
            ((TextView) product.findViewById(R.id.tvPaymentProductName)).setText(item.getProductName());
            ((TextView) product.findViewById(R.id.tvPaymentProductPrice)).setText(formatPrice(item.getPrice()));
            ((TextView) product.findViewById(R.id.tvPaymentProductQuantity)).setText(getString(R.string.quantity) + item.getQuantity());
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
            createStoreOrder(null);
        }
    }

    private void createZaloPayOrder() {
        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText(getString(R.string.creating_transaction));

        long total = subtotal + SHIPPING_FEE;
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("amount", total);

        String token = HttpResquest.authorizationHeader(this);
        apiService.createZaloPayOrder(token, body).enqueue(new Callback<Response<com.google.gson.JsonObject>>() {
            @Override
            public void onResponse(@NonNull Call<Response<com.google.gson.JsonObject>> call,
                                   @NonNull retrofit2.Response<Response<com.google.gson.JsonObject>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    com.google.gson.JsonObject data = response.body().getData();
                    String zpTransToken = data.has("zp_trans_token") ? data.get("zp_trans_token").getAsString() : "";
                    currentAppTransId = data.has("app_trans_id") ? data.get("app_trans_id").getAsString() : null;
                    String returnCode = data.has("return_code") ? String.valueOf(data.get("return_code").getAsInt()) : "";

                    if (!"1".equals(returnCode) || zpTransToken.isEmpty()) {
                        currentAppTransId = null;
                        String returnMessage = data.has("return_message") ? data.get("return_message").getAsString() : getString(R.string.zalopay_error);
                        resetZaloPayButton(returnMessage);
                    } else {
                        payWithZaloPay(zpTransToken);
                    }
                } else {
                    currentAppTransId = null;
                    resetZaloPayButton(getString(R.string.zalopay_create_failed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<com.google.gson.JsonObject>> call, @NonNull Throwable t) {
                Log.e("PaymentConfirmation", "Cannot create ZaloPay order", t);
                resetZaloPayButton(getString(R.string.toast_khong_ket_noi_duoc_may_chu));
            }
        });
    }

    private void payWithZaloPay(String token) {
        btnConfirmPayment.setText(getString(R.string.opening_zalopay));
        ZaloPaySDK.getInstance().payOrder(
                this,
                token,
                "merchant-deeplink://app",
                new PayOrderListener() {
                    @Override
                    public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                        currentAppTransId = null;
                        runOnUiThread(() -> {
                            Toast.makeText(PaymentConfirmationActivity.this, PaymentConfirmationActivity.this.getString(R.string.toast_thanh_toan_zalopay_thanh_cong), Toast.LENGTH_SHORT).show();
                            createStoreOrder(appTransID);
                        });
                    }

                    @Override
                    public void onPaymentCanceled(String zpTransToken, String appTransID) {
                        currentAppTransId = null;
                        runOnUiThread(() -> resetZaloPayButton(getString(R.string.zalopay_cancelled)));
                    }

                    @Override
                    public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                        currentAppTransId = null;
                        Log.e("PaymentConfirmation", "ZaloPay error: " + zaloPayError);
                        runOnUiThread(() -> {
                            if (zaloPayError == ZaloPayError.PAYMENT_APP_NOT_FOUND) {
                                Toast.makeText(PaymentConfirmationActivity.this, getString(R.string.zalopay_not_installed), Toast.LENGTH_LONG).show();
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=vn.com.vng.zalopay"));
                                    startActivity(intent);
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/apps/details?id=vn.com.vng.zalopay"));
                                    startActivity(intent);
                                }
                            }
                            resetZaloPayButton(getString(R.string.zalopay_payment_failed));
                        });
                    }
                }
        );
    }

    private void createStoreOrder(String appTransId) {
        final boolean paidWithZaloPay = rbZaloPay.isChecked();
        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText(paidWithZaloPay ? getString(R.string.creating_order) : getString(R.string.xml_xac_nhan_thanh_toan));
        apiService.createOrder(HttpResquest.authorizationHeader(this), paidWithZaloPay ? "ZaloPay" : "COD", appTransId).enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Order>> call, @NonNull retrofit2.Response<Response<Order>> response) {
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText(getString(R.string.xml_xac_nhan_thanh_toan));
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    // Xóa cache giỏ hàng và đơn hàng sau khi đặt hàng thành công
                    DataCache.get().invalidateExact("user_cart");
                    DataCache.get().invalidate("user_orders");
                    if (paidWithZaloPay) {
                        openCustomerOrders();
                    } else {
                        Order order = response.body().getData();
                        // Giai ma JsonElement (rawUser) thanh String truoc khi truyen qua Intent
                        // Tranh crash BadParcelableException / NotSerializableException
                        order.resolveFields();
                        Intent intent = new Intent(PaymentConfirmationActivity.this, ShippingOrderDetailActivity.class);
                        intent.putExtra("order_data", order);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(PaymentConfirmationActivity.this, PaymentConfirmationActivity.this.getString(R.string.toast_dat_hang_that_bai_vui_long_thu_lai), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Order>> call, @NonNull Throwable t) {
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText(getString(R.string.xml_xac_nhan_thanh_toan));
                Toast.makeText(PaymentConfirmationActivity.this, PaymentConfirmationActivity.this.getString(R.string.toast_khong_the_ket_noi_den_may_chu), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetZaloPayButton(String message) {
        btnConfirmPayment.setEnabled(true);
        btnConfirmPayment.setText(getString(R.string.xml_xac_nhan_thanh_toan));
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
    protected void onResume() {
        super.onResume();
        if (currentAppTransId != null) {
            verifyZaloPayPayment(currentAppTransId);
        }
    }

    private void verifyZaloPayPayment(final String appTransId) {
        currentAppTransId = null;
        final android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.verifying_payment));
        progressDialog.setCancelable(false);
        progressDialog.show();

        apiService.createOrder(HttpResquest.authorizationHeader(this), "ZaloPay", appTransId).enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Order>> call, @NonNull retrofit2.Response<Response<Order>> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Toast.makeText(PaymentConfirmationActivity.this, getString(R.string.payment_success_order_created), Toast.LENGTH_SHORT).show();
                    openCustomerOrders();
                } else {
                    resetZaloPayButton(getString(R.string.payment_not_completed));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Order>> call, @NonNull Throwable t) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                resetZaloPayButton(getString(R.string.cannot_verify_payment));
            }
        });
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
