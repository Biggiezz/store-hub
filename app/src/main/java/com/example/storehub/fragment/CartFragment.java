package com.example.storehub.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.MainActivity;
import com.example.storehub.R;
import com.example.storehub.adapter.CartAdapter;
import com.example.storehub.model.CartItem;
import com.example.storehub.model.Response;
import com.example.storehub.model.UpdateCartQuantityRequest;
import com.example.storehub.model.User;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class CartFragment extends Fragment implements CartAdapter.OnCartChangeListener {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private LinearLayout emptyCartLayout;
    private NestedScrollView cartScrollView;
    private ProgressBar progressBar;
    private TextView tvSubtotalLabel, tvReceiverInformation, tvDeliveryAddress, btnChangeAddress,tvSubtotal, tvShippingFee, tvTotal ;
    private MaterialButton btnCheckout;
    private ImageButton btnBack;
    private ApiServices apiService;
    private Call<Response<ArrayList<CartItem>>> cartCall;
    private long subtotalAmount = 0L;
    private static final long DEFAULT_SHIPPING_FEE = 40000L;
    private long discountAmount = 0L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = new HttpResquest().callAPI();

        initUi(view);
        setUpAdapter();
        setUpListener();
        loadUserInfo();

        loadCartFromServer();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
        loadCartFromServer();
    }

    private void initUi(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        rvCartItems = view.findViewById(R.id.rvCartItems);
        emptyCartLayout = view.findViewById(R.id.emptyCartLayout);
        cartScrollView = view.findViewById(R.id.cartScrollView);
        progressBar = view.findViewById(R.id.progressBar);

        tvReceiverInformation = view.findViewById(R.id.tvReceiverInformation);
        tvDeliveryAddress = view.findViewById(R.id.tvDeliveryAddress);
        btnChangeAddress = view.findViewById(R.id.btnChangeAddress);

        tvSubtotalLabel = view.findViewById(R.id.tvSubtotalLabel);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvShippingFee = view.findViewById(R.id.tvShippingFee);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnCheckout = view.findViewById(R.id.btnCheckout);
    }

    private void loadUserInfo() {
        if (!isAdded()) return;

        SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
        User user = prefManager.getUser();

        if (user != null) {
            String name = !TextUtils.isEmpty(user.getName()) ? user.getName() : "Khách hàng";
            String phone = !TextUtils.isEmpty(user.getPhone()) ? user.getPhone() : "";
            String receiverInfo = !TextUtils.isEmpty(phone) ? name + " | " + phone : name;

            if (tvReceiverInformation != null) {
                tvReceiverInformation.setText(receiverInfo);
            }

            String address = !TextUtils.isEmpty(user.getAddress())
                    ? user.getAddress()
                    : "123 Đường Lê Lợi, Phường Bến Thành, Quận 1, TP. Hồ Chí Minh";

            if (tvDeliveryAddress != null) {
                tvDeliveryAddress.setText(address);
            }
        }
    }

    private void setUpAdapter() {
        cartAdapter = new CartAdapter(requireContext());
        cartAdapter.setOnCartChangeListener(this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void setUpListener() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).showOder();
                } else if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        if (btnChangeAddress != null) {
            btnChangeAddress.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Thay đổi địa chỉ giao hàng", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(v -> {
                if (cartAdapter.getItemCount() == 0) {
                    Toast.makeText(requireContext(), "Giỏ hàng đang trống", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnCheckout.setEnabled(false);
                apiService.createOrder().enqueue(new retrofit2.Callback<Response<com.example.storehub.model.Order>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<Response<com.example.storehub.model.Order>> call,
                                           @NonNull retrofit2.Response<Response<com.example.storehub.model.Order>> response) {
                        btnCheckout.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            com.example.storehub.model.Order createdOrder = response.body().getData();
                            Toast.makeText(requireContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                            android.content.Intent intent = new android.content.Intent(requireContext(), com.example.storehub.ShippingOrderDetailActivity.class);
                            intent.putExtra("order_data", createdOrder);
                            startActivity(intent);
                        } else {
                            Toast.makeText(requireContext(), "Đặt hàng thất bại. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<Response<com.example.storehub.model.Order>> call, @NonNull Throwable t) {
                        btnCheckout.setEnabled(true);
                        Log.e("CartFragment", "Error creating order", t);
                        Toast.makeText(requireContext(), "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    private void loadCartFromServer() {
        setLoading(true);

        if (cartCall != null) {
            cartCall.cancel();
        }

        cartCall = apiService.getCart();
        cartCall.enqueue(new Callback<Response<ArrayList<CartItem>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<CartItem>>> call,
                                   @NonNull retrofit2.Response<Response<ArrayList<CartItem>>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    updateCartUi(response.body().getData());
                } else {
                    updateCartUi(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<CartItem>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                setLoading(false);
                Log.e("CartFragment", "Error loading cart", t);
                updateCartUi(new ArrayList<>());
            }
        });
    }

    private void updateCartUi(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            if (emptyCartLayout != null) emptyCartLayout.setVisibility(View.VISIBLE);
            if (rvCartItems != null) rvCartItems.setVisibility(View.GONE);
            cartAdapter.updateData(new ArrayList<>());
            subtotalAmount = 0L;
            updateOrderTotals();
        } else {
            if (emptyCartLayout != null) emptyCartLayout.setVisibility(View.GONE);
            if (rvCartItems != null) rvCartItems.setVisibility(View.VISIBLE);
            cartAdapter.updateData(cartItems);

            long sum = 0L;
            int totalQuantity = 0;
            for (CartItem item : cartItems) {
                sum += item.getTotalItemPrice();
                totalQuantity += item.getQuantity();
            }
            subtotalAmount = sum;

            if (tvSubtotalLabel != null) {
                tvSubtotalLabel.setText("Tạm tính (" + totalQuantity + " sản phẩm):");
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
        setLoading(true);
        UpdateCartQuantityRequest request = new UpdateCartQuantityRequest(cartItem.getId(), newQuantity);
        apiService.updateCartQuantity(request).enqueue(new Callback<Response<ArrayList<CartItem>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<CartItem>>> call,
                                   @NonNull retrofit2.Response<Response<ArrayList<CartItem>>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    updateCartUi(response.body().getData());
                } else {
                    loadCartFromServer();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<CartItem>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                setLoading(false);
                Toast.makeText(requireContext(), "Không thể cập nhật số lượng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteItem(CartItem cartItem) {
        setLoading(true);
        apiService.deleteCartItem(cartItem.getId()).enqueue(new Callback<Response<ArrayList<CartItem>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<CartItem>>> call,
                                   @NonNull retrofit2.Response<Response<ArrayList<CartItem>>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Toast.makeText(requireContext(), "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                    updateCartUi(response.body().getData());
                } else {
                    loadCartFromServer();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<CartItem>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                setLoading(false);
                Toast.makeText(requireContext(), "Không thể xóa sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                new Locale("vi", "VN")
        );
        return formatter.format(price);
    }

    @Override
    public void onDestroyView() {
        if (cartCall != null) {
            cartCall.cancel();
        }
        super.onDestroyView();
    }
}