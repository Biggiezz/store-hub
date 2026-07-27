package com.example.storehub.fragment;

import static com.example.storehub.R.drawable.ic_receipt;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.storehub.CancelledOrderDetailActivity;
import com.example.storehub.CompletedOrderDetailActivity;
import com.example.storehub.MainActivity;
import com.example.storehub.ProfileActivity;
import com.example.storehub.R;
import com.example.storehub.ShippingOrderDetailActivity;
import com.example.storehub.model.CartItem;
import com.example.storehub.model.Order;
import com.example.storehub.model.Order.CancelOrderRequest;
import com.example.storehub.model.Response;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Màn hình quản lý danh sách đơn hàng.
 * Tải trực tiếp dữ liệu từ Database, không chứa mockup, ngoại trừ lộ trình timeline.
 */
public class OderFragment extends Fragment {

    private LinearLayout ordersContainer;
    private ProgressBar progressBar;
    private ApiServices apiService;
    private Call<Response<ArrayList<CartItem>>> cartCall;
    private Call<Response<ArrayList<Order>>> ordersCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ordersContainer = view.findViewById(R.id.ordersContainer);
        progressBar = view.findViewById(R.id.progressBar);
        apiService = new HttpResquest().callAPI();

        // Bind profile click
        View btnProfile = view.findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ProfileActivity.class);
                startActivity(intent);
            });
        }

        // Bind back button
        View btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        loadOrdersAndCart();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrdersAndCart();
    }

    private void loadOrdersAndCart() {
        setLoading(true);
        ordersContainer.removeAllViews();

        if (cartCall != null) {
            cartCall.cancel();
        }
        if (ordersCall != null) {
            ordersCall.cancel();
        }

        // 1. Lấy dữ liệu Giỏ hàng để kiểm tra mục tạm
        cartCall = apiService.getCart();
        cartCall.enqueue(new Callback<Response<ArrayList<CartItem>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<CartItem>>> call,
                                   @NonNull retrofit2.Response<Response<ArrayList<CartItem>>> response) {
                final ArrayList<CartItem> cartItems = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    cartItems.addAll(response.body().getData());
                }

                // 2. Lấy danh sách Đơn hàng thật từ Server
                fetchRealOrders(cartItems);
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<CartItem>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                fetchRealOrders(new ArrayList<>());
            }
        });
    }

    private void fetchRealOrders(final ArrayList<CartItem> cartItems) {
        SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
        String userId = (prefManager.getUser() != null) ? prefManager.getUser().getId() : "";
        ordersCall = apiService.getOrders(userId);

        ordersCall.enqueue(new Callback<Response<ArrayList<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Order>>> call, @NonNull retrofit2.Response<Response<ArrayList<Order>>> response) {
                setLoading(false);
                ArrayList<Order> orders = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    orders = response.body().getData();
                    Log.d("OderFragment", "Orders: " + orders);
                }
                renderAll(cartItems, orders);
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Order>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                setLoading(false);
                Log.e("OderFragment", "Error fetching orders", t);
                renderAll(cartItems, new ArrayList<>());
            }
        });
    }

    private void renderAll(ArrayList<CartItem> cartItems, ArrayList<Order> orders) {
        ordersContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        // 1. Hiển thị Giỏ hàng tạm nếu có sản phẩm
        if (!cartItems.isEmpty()) {
            View tempOrderView = inflater.inflate(R.layout.item_order_mockup, ordersContainer, false);

            TextView tvOrderCode = tempOrderView.findViewById(R.id.tvOrderCode);
            TextView tvOrderStatus = tempOrderView.findViewById(R.id.tvOrderStatus);
            ImageView imgProduct = tempOrderView.findViewById(R.id.imgProduct);
            TextView tvProductName = tempOrderView.findViewById(R.id.tvProductName);
            TextView tvProductQty = tempOrderView.findViewById(R.id.tvProductQty);
            TextView tvOrderTotal = tempOrderView.findViewById(R.id.tvOrderTotal);
            TextView btnCancelOrder = tempOrderView.findViewById(R.id.btnCancelOrder);
            TextView btnViewShippingOrder = tempOrderView.findViewById(R.id.btnViewShippingOrder);

            tvOrderCode.setText("#GIỎ-HÀNG-TẠM");
            tvOrderStatus.setText("Chưa đặt hàng");
            tvOrderStatus.setCompoundDrawablesWithIntrinsicBounds(ic_receipt, 0, 0, 0);
            tvOrderStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green));

            CartItem firstItem = cartItems.get(0);
            tvProductName.setText(firstItem.getProductName());

            Glide.with(this)
                    .load(firstItem.getProductImage())
                    .placeholder(R.drawable.ic_product)
                    .error(R.drawable.ic_product)
                    .into(imgProduct);

            int totalQty = 0;
            long totalPrice = 0;
            for (CartItem item : cartItems) {
                totalQty += item.getQuantity();
                totalPrice += item.getTotalItemPrice();
            }

            if (cartItems.size() > 1) {
                tvProductQty.setText("Số lượng: " + totalQty + " (và " + (cartItems.size() - 1) + " sản phẩm khác)");
            } else {
                tvProductQty.setText("Số lượng: " + totalQty);
            }

            tvOrderTotal.setText(formatPrice(totalPrice));

            btnCancelOrder.setText("Xóa");
            btnCancelOrder.setOnClickListener(v -> showClearCartConfirmDialog());

            btnViewShippingOrder.setText("Thanh toán");
            btnViewShippingOrder.setOnClickListener(v -> createOrderFromTempCart());

            tempOrderView.setOnClickListener(v -> createOrderFromTempCart());

            ordersContainer.addView(tempOrderView);
        }

        // 2. Hiển thị danh sách Đơn hàng thật từ DB
        for (final Order order : orders) {
            View orderView = inflater.inflate(R.layout.item_order_mockup, ordersContainer, false);

            TextView tvOrderCode = orderView.findViewById(R.id.tvOrderCode);
            TextView tvOrderStatus = orderView.findViewById(R.id.tvOrderStatus);
            ImageView imgProduct = orderView.findViewById(R.id.imgProduct);
            TextView tvProductName = orderView.findViewById(R.id.tvProductName);
            TextView tvProductQty = orderView.findViewById(R.id.tvProductQty);
            TextView tvOrderTotal = orderView.findViewById(R.id.tvOrderTotal);
            TextView btnCancelOrder = orderView.findViewById(R.id.btnCancelOrder);
            TextView btnViewShippingOrder = orderView.findViewById(R.id.btnViewShippingOrder);

            tvOrderCode.setText(order.getOrderCode());

            String status = order.getStatus();
            if ("Đã hoàn thành".equalsIgnoreCase(status) || "Đã giao hàng".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) {
                tvOrderStatus.setText("Đã hoàn thành");
                tvOrderStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_done, 0, 0, 0);
                tvOrderStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green));
                tvOrderStatus.setBackgroundResource(R.drawable.bg_order_status);
                btnCancelOrder.setVisibility(View.GONE);
            } else if ("Đã hủy".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status) || "cancel".equalsIgnoreCase(status)) {
                tvOrderStatus.setText("Đã hủy");
                tvOrderStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reject, 0, 0, 0);
                tvOrderStatus.setTextColor(Color.parseColor("#BA1A1A"));
                tvOrderStatus.setBackgroundResource(R.drawable.bg_order_status_cancelled);
                btnCancelOrder.setVisibility(View.GONE);
            } else {
                tvOrderStatus.setText(status != null ? status : "Đang giao hàng");
                tvOrderStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_car_waiting, 0, 0, 0);
                tvOrderStatus.setTextColor(Color.parseColor("#625E58"));
                tvOrderStatus.setBackgroundResource(R.drawable.bg_order_status);
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setText("Hủy đơn");
                btnCancelOrder.setOnClickListener(v -> showCancelOrderDialog(order));
            }

            // Hiển thị sản phẩm đầu tiên của đơn hàng
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                CartItem firstItem = order.getItems().get(0);
                tvProductName.setText(firstItem.getProductName());

                Glide.with(this)
                        .load(firstItem.getProductImage())
                        .placeholder(R.drawable.ic_product)
                        .error(R.drawable.ic_product)
                        .into(imgProduct);

                int totalQty = 0;
                for (CartItem item : order.getItems()) {
                    totalQty += item.getQuantity();
                }

                if (order.getItems().size() > 1) {
                    tvProductQty.setText("Số lượng: " + totalQty + " (và " + (order.getItems().size() - 1) + " sản phẩm khác)");
                } else {
                    tvProductQty.setText("Số lượng: " + totalQty);
                }
            }

            long totalPayment = order.getTotalPrice() + order.getShippingFee();
            tvOrderTotal.setText(formatPrice(totalPayment));

            btnViewShippingOrder.setText("Chi tiết");
            btnViewShippingOrder.setOnClickListener(v -> openOrderDetail(order));
            orderView.setOnClickListener(v -> openOrderDetail(order));

            ordersContainer.addView(orderView);
        }

        if (cartItems.isEmpty() && orders.isEmpty()) {
            TextView emptyText = new TextView(requireContext());
            emptyText.setText("Không có đơn hàng nào.");
            emptyText.setGravity(android.view.Gravity.CENTER);
            emptyText.setPadding(0, 48, 0, 0);
            ordersContainer.addView(emptyText);
        }
    }

    private void openCartScreen() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showCart();
        }
    }

    private void openOrderDetail(Order order) {
        Class<? extends AppCompatActivity> destination;
        if ("Đã hoàn thành".equals(order.getStatus()) || "Đã giao hàng".equals(order.getStatus())) {
            destination = CompletedOrderDetailActivity.class;
        } else if ("Đã hủy".equals(order.getStatus())) {
            destination = CancelledOrderDetailActivity.class;
        } else {
            destination = ShippingOrderDetailActivity.class;
        }

        Intent intent = new Intent(requireContext(), destination);
        intent.putExtra("order_data", order);
        startActivity(intent);
    }

    private void showCancelOrderDialog(final Order order) {
        if (getActivity() == null || !isAdded()) return;
        final android.app.Dialog dialog = new android.app.Dialog(requireContext());
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
                    Toast.makeText(requireContext(), "Vui lòng chọn hoặc nhập lý do hủy", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                executeCancelOrder(order, reason);
            });
        }

        dialog.show();
    }

    private void executeCancelOrder(Order order, String reason) {
        setLoading(true);
        CancelOrderRequest request = new CancelOrderRequest(order.getOrderId(), reason);
        apiService.cancelOrder(request).enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Order>> call, @NonNull retrofit2.Response<Response<Order>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Đã hủy đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                    loadOrdersAndCart();
                } else {
                    Toast.makeText(requireContext(), "Không thể hủy đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Order>> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), "Không kết nối được máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showClearCartConfirmDialog() {
        if (getActivity() == null || !isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm trong giỏ hàng tạm không?")
                .setPositiveButton("Xóa", (dialog, which) -> executeClearCart())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void executeClearCart() {
        setLoading(true);
        apiService.clearCart().enqueue(new Callback<Response<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Object>> call, @NonNull retrofit2.Response<Response<Object>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Đã xóa giỏ hàng tạm!", Toast.LENGTH_SHORT).show();
                    loadOrdersAndCart();
                } else {
                    Toast.makeText(requireContext(), "Không thể xóa giỏ hàng tạm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Object>> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), "Không kết nối được máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOrderFromTempCart() {
        setLoading(true);
        SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
        String userId = (prefManager.getUser() != null) ? prefManager.getUser().getId() : "";
        apiService.createOrder(userId).enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Order>> call, @NonNull retrofit2.Response<Response<Order>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Order createdOrder = response.body().getData();
                    Toast.makeText(requireContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(requireContext(), com.example.storehub.ShippingOrderDetailActivity.class);
                    intent.putExtra("order_data", createdOrder);
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), "Đặt hàng thất bại. Vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Order>> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e("OderFragment", "Error creating order", t);
                Toast.makeText(requireContext(), "Không thể kết nối đến máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(price);
    }

    @Override
    public void onDestroyView() {
        if (cartCall != null) {
            cartCall.cancel();
        }
        if (ordersCall != null) {
            ordersCall.cancel();
        }
        super.onDestroyView();
    }
}
