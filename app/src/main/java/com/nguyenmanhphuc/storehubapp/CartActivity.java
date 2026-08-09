package com.nguyenmanhphuc.storehubapp;

import com.nguyenmanhphuc.storehubapp.R;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.nguyenmanhphuc.storehubapp.adapter.CartAdapter;
import com.nguyenmanhphuc.storehubapp.model.CartItem;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.model.request.UpdateQuantityRequest;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.ApiServices;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.DataCache;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private View emptyCartLayout, cardDeliveryAddress, cardOrderSummary;
    private NestedScrollView cartScrollView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvSubtotalLabel, tvReceiverInformation, tvDeliveryAddress, btnChangeAddress, tvSubtotal, tvShippingFee, tvTotal;
    private MaterialButton btnCheckout;
    private ImageView btnBack;
    private ApiServices apiService;
    private Call<Response<ArrayList<CartItem>>> cartCall;
    private long subtotalAmount = 0L;
    private final ArrayList<CartItem> cartItems = new ArrayList<>();
    private static final long DEFAULT_SHIPPING_FEE = 40000L;
    private final long discountAmount = 0L;
    /** TTL riêng cho giỏ hàng: 2 phút */
    private static final long CART_TTL_MS = 2 * 60 * 1000L;
    private static final String CACHE_KEY = "user_cart";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        View root = findViewById(R.id.cart_activity);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        apiService = new HttpResquest().callAPI();

        initUi();
        setUpAdapter();
        setUpListener();
        loadUserInfo();

        // Nếu cache giỏ hàng còn hợp lệ (< 2 phút) → hiện ngay, không spinner
        ArrayList<CartItem> cached = DataCache.get().get(CACHE_KEY, ArrayList.class);
        if (cached != null) {
            updateCartUi(cached);
        } else {
            loadCartFromServer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();

        // Neu cache bi invalidate (vi du: vua them SP vao gio tu ProductDetail)
        // thi fetch lai de hien san pham moi
        ArrayList<CartItem> cached = DataCache.get().get(CACHE_KEY, ArrayList.class);
        if (cached == null) {
            loadCartFromServer();
        }
        // Neu van con cache hop le: giu nguyen, khong reload (tranh nhap nhay)
    }

    private void initUi() {
        rvCartItems = findViewById(R.id.rvCartItems);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        cardDeliveryAddress = findViewById(R.id.cardDeliveryAddress);
        cardOrderSummary = findViewById(R.id.cardOrderSummary);
        cartScrollView = findViewById(R.id.cartScrollView);
        progressBar = findViewById(R.id.progressBar);

        tvReceiverInformation = findViewById(R.id.tvReceiverInformation);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);

        tvSubtotalLabel = findViewById(R.id.tvSubtotalLabel);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        btnCheckout = findViewById(R.id.btnCheckout);
        btnBack = findViewById(R.id.btnBack);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.dark_green));
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // SwipeRefresh: buộc fetch mới
                DataCache.get().invalidateExact(CACHE_KEY);
                loadCartFromServer();
            });
        }
    }

    private void loadUserInfo() {
        SharedPreferencesManager prefManager = new SharedPreferencesManager(this);
        User user = prefManager.getUser();

        if (user != null) {
            String name = !TextUtils.isEmpty(user.getName()) ? user.getName() : getString(R.string.role_customer);
            String phone = !TextUtils.isEmpty(user.getPhone()) ? user.getPhone() : "";
            String receiverInfo = !TextUtils.isEmpty(phone) ? name + " | " + phone : name;

            if (tvReceiverInformation != null) {
                tvReceiverInformation.setText(receiverInfo);
            }

            String address = !TextUtils.isEmpty(user.getAddress())
                    ? user.getAddress()
                    : getString(R.string.default_customer_address);

            if (tvDeliveryAddress != null) {
                tvDeliveryAddress.setText(address);
            }
        }
    }

    private void setUpAdapter() {
        cartAdapter = new CartAdapter(this);
        cartAdapter.setOnCartChangeListener(this);
        if (rvCartItems != null) {
            rvCartItems.setLayoutManager(new LinearLayoutManager(this));
            rvCartItems.setAdapter(cartAdapter);
        }
    }

    private void setUpListener() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnChangeAddress != null) {
            btnChangeAddress.setOnClickListener(v ->
                    Toast.makeText(this, getString(R.string.toast_feature_under_dev), Toast.LENGTH_SHORT).show()
            );
        }

        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(v -> {
                if (cartItems.isEmpty()) {
                    Toast.makeText(this, this.getString(R.string.toast_gio_hang_dang_trong), Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(PaymentConfirmationActivity.createIntent(this, cartItems));
            });
        }
    }

    private void loadCartFromServer() {
        setLoading(true);

        if (cartCall != null) {
            cartCall.cancel();
        }

        cartCall = apiService.getCart(HttpResquest.authorizationHeader(this));
        cartCall.enqueue(new Callback<Response<ArrayList<CartItem>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<CartItem>>> call, @NonNull retrofit2.Response<Response<ArrayList<CartItem>>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ArrayList<CartItem> items = response.body().getData();
                    // Lưu vào cache với TTL 2 phút
                    DataCache.get().put(CACHE_KEY, items, CART_TTL_MS);
                    updateCartUi(items);
                } else {
                    updateCartUi(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<CartItem>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                setLoading(false);
                Log.e("CartActivity", "Error loading cart", t);
                updateCartUi(new ArrayList<>());
            }
        });
    }

    private void updateCartUi(List<CartItem> cartItems) {
        if (cartItems != this.cartItems) {
            this.cartItems.clear();
            if (cartItems != null) this.cartItems.addAll(cartItems);
        }
        if (this.cartItems == null || this.cartItems.isEmpty()) {
            if (emptyCartLayout != null) emptyCartLayout.setVisibility(View.VISIBLE);
            if (cardDeliveryAddress != null) cardDeliveryAddress.setVisibility(View.GONE);
            if (cardOrderSummary != null) cardOrderSummary.setVisibility(View.GONE);
            if (rvCartItems != null) rvCartItems.setVisibility(View.GONE);
            if (cartScrollView != null) cartScrollView.setVisibility(View.VISIBLE);
            cartAdapter.updateData(new ArrayList<>());
            subtotalAmount = 0L;
            updateOrderTotals();
        } else {
            if (emptyCartLayout != null) emptyCartLayout.setVisibility(View.GONE);
            if (cardDeliveryAddress != null) cardDeliveryAddress.setVisibility(View.VISIBLE);
            if (cardOrderSummary != null) cardOrderSummary.setVisibility(View.VISIBLE);
            if (cartScrollView != null) cartScrollView.setVisibility(View.VISIBLE);
            if (rvCartItems != null) rvCartItems.setVisibility(View.VISIBLE);
            cartAdapter.updateData(this.cartItems);

            long sum = 0L;
            int totalQuantity = 0;
            for (CartItem item : this.cartItems) {
                sum += item.getTotalItemPrice();
                totalQuantity += item.getQuantity();
            }
            subtotalAmount = sum;

            if (tvSubtotalLabel != null) {
                tvSubtotalLabel.setText(getString(R.string.subtotal_label_template, totalQuantity));
            }

            updateOrderTotals();
        }
    }

    private void updateOrderTotals() {
        long currentShippingFee = subtotalAmount == 0 ? 0L : DEFAULT_SHIPPING_FEE;
        long total = subtotalAmount + currentShippingFee - discountAmount;
        if (total < 0L) total = 0L;

        if (tvSubtotal != null) {
            tvSubtotal.setText(formatPrice(subtotalAmount));
        }
        if (tvShippingFee != null) {
            tvShippingFee.setText(formatPrice(currentShippingFee));
        }
        if (tvTotal != null) {
            tvTotal.setText(formatPrice(total));
        }
    }

    @Override
    public void onQuantityChange(CartItem cartItem, int newQuantity) {
        if (cartItem == null) return;
        if (newQuantity <= 0) {
            onDeleteItem(cartItem);
            return;
        }

        cartItem.setQuantity(newQuantity);
        recalculateTotals();
        if (cartAdapter != null) {
            cartAdapter.notifyDataSetChanged();
        }
        // Cập nhật cache tại chỗ (optimistic update)
        DataCache.get().put(CACHE_KEY, new ArrayList<>(cartItems), CART_TTL_MS);

        UpdateQuantityRequest request = new UpdateQuantityRequest(cartItem.getId(), newQuantity);
        apiService.updateCartQuantity(HttpResquest.authorizationHeader(this), request).enqueue(new Callback<Response<ArrayList<CartItem>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<CartItem>>> call, @NonNull retrofit2.Response<Response<ArrayList<CartItem>>> response) {
                // Silently synced to server in background
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<CartItem>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                Toast.makeText(CartActivity.this, CartActivity.this.getString(R.string.toast_khong_the_cap_nhat_so_luong), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteItem(CartItem cartItem) {
        if (cartItem == null) return;

        boolean removed = false;
        for (int i = 0; i < this.cartItems.size(); i++) {
            CartItem item = this.cartItems.get(i);
            if (item == cartItem) {
                this.cartItems.remove(i);
                removed = true;
                break;
            }
        }
        if (!removed) {
            for (int i = 0; i < this.cartItems.size(); i++) {
                CartItem item = this.cartItems.get(i);
                if (item.equals(cartItem)) {
                    this.cartItems.remove(i);
                    break;
                }
            }
        }

        Toast.makeText(CartActivity.this, getString(R.string.removed_from_cart_toast), Toast.LENGTH_SHORT).show();
        updateCartUi(this.cartItems);
        // Xóa cache sau khi xóa item → lần sau sẽ fetch lại chính xác
        DataCache.get().invalidateExact(CACHE_KEY);

        String deleteId = !cartItem.getId().isEmpty() ? cartItem.getId() : cartItem.getProductId();
        apiService.deleteCartItem(HttpResquest.authorizationHeader(this), deleteId).enqueue(new Callback<Response<ArrayList<CartItem>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<CartItem>>> call, @NonNull retrofit2.Response<Response<ArrayList<CartItem>>> response) {
                // Silently synced to server in background
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<CartItem>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                Toast.makeText(CartActivity.this, CartActivity.this.getString(R.string.toast_khong_the_xoa_san_pham), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void recalculateTotals() {
        long sum = 0L;
        int totalQuantity = 0;
        for (CartItem item : cartItems) {
            sum += item.getTotalItemPrice();
            totalQuantity += item.getQuantity();
        }
        subtotalAmount = sum;

        if (tvSubtotalLabel != null) {
            tvSubtotalLabel.setText(getString(R.string.subtotal_label_template, totalQuantity));
        }

        updateOrderTotals();
    }

    private void setLoading(boolean loading) {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (!loading) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (loading) {
            if (cartScrollView != null) cartScrollView.setVisibility(View.GONE);
            if (emptyCartLayout != null) emptyCartLayout.setVisibility(View.GONE);
        } else {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                new Locale("vi", "VN")
        );
        return formatter.format(price);
    }

    @Override
    protected void onDestroy() {
        if (cartCall != null) cartCall.cancel();
        super.onDestroy();
    }
}
