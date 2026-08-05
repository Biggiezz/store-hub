package com.nguyenmanhphuc.storehubapp.fragment;

import static com.nguyenmanhphuc.storehubapp.R.drawable.ic_receipt;

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
import com.nguyenmanhphuc.storehubapp.CancelledOrderDetailActivity;
import com.nguyenmanhphuc.storehubapp.CompletedOrderDetailActivity;
import com.nguyenmanhphuc.storehubapp.MainActivity;
import com.nguyenmanhphuc.storehubapp.ProfileActivity;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.ShippingOrderDetailActivity;
import com.nguyenmanhphuc.storehubapp.model.CartItem;
import com.nguyenmanhphuc.storehubapp.model.Order;
import com.nguyenmanhphuc.storehubapp.model.request.CancelOrderRequest;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.ApiServices;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;

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

    private static final String FILTER_ALL = "Tất cả";
    private static final String FILTER_PENDING = "Chờ xác nhận";
    private static final String FILTER_SHIPPING = "Đang giao";
    private static final String FILTER_COMPLETED = "Hoàn thành";
    private static final String FILTER_CANCELLED = "Đã hủy";

    private LinearLayout ordersContainer;
    private ProgressBar progressBar;
    private TextView btnFilterAll;
    private TextView btnFilterPending;
    private TextView btnFilterShipping;
    private TextView btnFilterCompleted;
    private TextView btnFilterCancelled;
    private ApiServices apiService;
    private Call<Response<ArrayList<CartItem>>> cartCall;
    private Call<Response<ArrayList<Order>>> ordersCall;
    private final ArrayList<CartItem> loadedCartItems = new ArrayList<>();
    private final ArrayList<Order> loadedOrders = new ArrayList<>();
    private String selectedFilter = FILTER_ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_oder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUi(view);
        initListener();

        loadOrdersAndCart();
    }

    private void initUi(View view) {
        ordersContainer = view.findViewById(R.id.ordersContainer);
        progressBar = view.findViewById(R.id.progressBar);
        btnFilterAll = view.findViewById(R.id.btnFilterAll);
        btnFilterPending = view.findViewById(R.id.btnFilterPending);
        btnFilterShipping = view.findViewById(R.id.btnFilterShipping);
        btnFilterCompleted = view.findViewById(R.id.btnFilterCompleted);
        btnFilterCancelled = view.findViewById(R.id.btnFilterCancelled);
        apiService = new HttpResquest().callAPI();
    }

    private void initListener() {
        View root = getView();
        if (root == null) return;

        View btnProfile = root.findViewById(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), ProfileActivity.class);
                startActivity(intent);
            });
        }

        View btnBack = root.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }

        btnFilterAll.setOnClickListener(v -> selectFilter(FILTER_ALL));
        btnFilterPending.setOnClickListener(v -> selectFilter(FILTER_PENDING));
        btnFilterShipping.setOnClickListener(v -> selectFilter(FILTER_SHIPPING));
        btnFilterCompleted.setOnClickListener(v -> selectFilter(FILTER_COMPLETED));
        btnFilterCancelled.setOnClickListener(v -> selectFilter(FILTER_CANCELLED));
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
        cartCall = apiService.getCart(HttpResquest.authorizationHeader(requireContext()));
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
        ordersCall = apiService.getOrders(HttpResquest.authorizationHeader(requireContext()));

        ordersCall.enqueue(new Callback<Response<ArrayList<Order>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Order>>> call, @NonNull retrofit2.Response<Response<ArrayList<Order>>> response) {
                setLoading(false);
                ArrayList<Order> orders = new ArrayList<>();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    orders = response.body().getData();
                    Log.d("OderFragment", "Orders: " + orders);
                }
                loadedCartItems.clear();
                loadedCartItems.addAll(cartItems);
                loadedOrders.clear();
                loadedOrders.addAll(orders);
                renderFilteredOrders();
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Order>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                setLoading(false);
                Log.e("OderFragment", "Error fetching orders", t);
                loadedCartItems.clear();
                loadedCartItems.addAll(cartItems);
                loadedOrders.clear();
                renderFilteredOrders();
            }
        });
    }

    private void selectFilter(String filter) {
        selectedFilter = filter;
        updateFilterButtons();
        renderFilteredOrders();
    }

    private void renderFilteredOrders() {
        ArrayList<Order> filteredOrders = new ArrayList<>();
        for (Order order : loadedOrders) {
            if (matchesSelectedFilter(order.getStatus())) {
                filteredOrders.add(order);
            }
        }
        renderAll(FILTER_ALL.equals(selectedFilter) ? loadedCartItems : new ArrayList<>(), filteredOrders);
    }

    private boolean matchesSelectedFilter(String status) {
        if (FILTER_ALL.equals(selectedFilter)) return true;
        if (FILTER_PENDING.equals(selectedFilter)) return "Chờ xác nhận".equalsIgnoreCase(status);
        if (FILTER_SHIPPING.equals(selectedFilter)) {
            return "Đã xác nhận".equalsIgnoreCase(status)
                    || "Đã rời kho".equalsIgnoreCase(status)
                    || "Đang giao hàng".equalsIgnoreCase(status);
        }
        if (FILTER_COMPLETED.equals(selectedFilter)) {
            return "Đã giao hàng".equalsIgnoreCase(status)
                    || "Đã hoàn thành".equalsIgnoreCase(status)
                    || "completed".equalsIgnoreCase(status);
        }
        return "Đã hủy".equalsIgnoreCase(status)
                || "cancelled".equalsIgnoreCase(status)
                || "cancel".equalsIgnoreCase(status);
    }

    private void updateFilterButtons() {
        updateFilterButton(btnFilterAll, FILTER_ALL);
        updateFilterButton(btnFilterPending, FILTER_PENDING);
        updateFilterButton(btnFilterShipping, FILTER_SHIPPING);
        updateFilterButton(btnFilterCompleted, FILTER_COMPLETED);
        updateFilterButton(btnFilterCancelled, FILTER_CANCELLED);
    }

    private void updateFilterButton(TextView button, String filter) {
        boolean selected = filter.equals(selectedFilter);
        button.setBackgroundResource(selected ? R.drawable.bg_order_filter_selected : R.drawable.bg_order_filter);
        button.setTextColor(ContextCompat.getColor(requireContext(), selected ? R.color.text_button : R.color.dark_green));
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

            tvOrderCode.setText(getString(R.string.temp_cart_code));
            tvOrderStatus.setText(getString(R.string.status_not_ordered));
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
                tvProductQty.setText(getString(R.string.qty_other_products_template, totalQty, cartItems.size() - 1));
            } else {
                tvProductQty.setText(getString(R.string.qty_template, totalQty));
            }

            tvOrderTotal.setText(formatPrice(totalPrice));

            btnCancelOrder.setText(getString(R.string.str_delete));
            btnCancelOrder.setOnClickListener(v -> showClearCartConfirmDialog());

            btnViewShippingOrder.setText(getString(R.string.checkout));
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
                tvOrderStatus.setText(getString(R.string.status_completed));
                tvOrderStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_done, 0, 0, 0);
                tvOrderStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.dark_green));
                tvOrderStatus.setBackgroundResource(R.drawable.bg_order_status);
                btnCancelOrder.setVisibility(View.GONE);
            } else if ("Đã hủy".equalsIgnoreCase(status) || "cancelled".equalsIgnoreCase(status) || "cancel".equalsIgnoreCase(status)) {
                tvOrderStatus.setText(getString(R.string.status_cancelled));
                tvOrderStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reject, 0, 0, 0);
                tvOrderStatus.setTextColor(Color.parseColor("#BA1A1A"));
                tvOrderStatus.setBackgroundResource(R.drawable.bg_order_status_cancelled);
                btnCancelOrder.setVisibility(View.GONE);
            } else {
                tvOrderStatus.setText(status != null ? getLocalizedStatus(status) : getString(R.string.status_shipping));
                tvOrderStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_car_waiting, 0, 0, 0);
                tvOrderStatus.setTextColor(Color.parseColor("#625E58"));
                tvOrderStatus.setBackgroundResource(R.drawable.bg_order_status);
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnCancelOrder.setText(getString(R.string.btn_cancel_order));
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
                    tvProductQty.setText(getString(R.string.qty_other_products_template, totalQty, order.getItems().size() - 1));
                } else {
                    tvProductQty.setText(getString(R.string.qty_template, totalQty));
                }
            }

            long totalPayment = order.getTotalPrice() + order.getShippingFee();
            tvOrderTotal.setText(formatPrice(totalPayment));

            btnViewShippingOrder.setText(getString(R.string.btn_details));
            btnViewShippingOrder.setOnClickListener(v -> openOrderDetail(order));
            orderView.setOnClickListener(v -> openOrderDetail(order));

            ordersContainer.addView(orderView);
        }

        if (cartItems.isEmpty() && orders.isEmpty()) {
            TextView emptyText = new TextView(requireContext(), null, 0, R.style.OrderEmptyStateStyle);
            emptyText.setText(getString(R.string.no_orders_label));
            ordersContainer.addView(emptyText);
        }
    }

    private void openCartScreen() {
        if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).showCart();
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
                    Toast.makeText(requireContext(), getString(R.string.select_cancel_reason_toast), Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                executeCancelOrder(order, reason);
            });
        }

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    private void executeCancelOrder(Order order, String reason) {
        setLoading(true);
        CancelOrderRequest request = new CancelOrderRequest(order.getOrderId(), reason);
        apiService.cancelOrder(request).enqueue(new Callback<Response<Order>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Order>> call, @NonNull retrofit2.Response<Response<Order>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), getString(R.string.cancel_order_success_toast), Toast.LENGTH_SHORT).show();
                    loadOrdersAndCart();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.cancel_order_failed_toast), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Order>> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), getString(R.string.cannot_connect_server_toast), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showClearCartConfirmDialog() {
        if (getActivity() == null || !isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.confirm_delete_title))
                .setMessage(getString(R.string.confirm_clear_temp_cart_msg))
                .setPositiveButton(getString(R.string.str_delete), (dialog, which) -> executeClearCart())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void executeClearCart() {
        setLoading(true);
        apiService.clearCart(HttpResquest.authorizationHeader(requireContext())).enqueue(new Callback<Response<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Object>> call, @NonNull retrofit2.Response<Response<Object>> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), getString(R.string.clear_temp_cart_success_toast), Toast.LENGTH_SHORT).show();
                    loadOrdersAndCart();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.clear_temp_cart_failed_toast), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Object>> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), getString(R.string.cannot_connect_server_toast), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createOrderFromTempCart() {
        if (loadedCartItems.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.empty_cart_toast), Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(com.nguyenmanhphuc.storehubapp.PaymentConfirmationActivity.createIntent(
                requireContext(), new ArrayList<>(loadedCartItems)));
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (loading) {
            if (ordersContainer != null) ordersContainer.setVisibility(View.GONE);
        } else {
            if (ordersContainer != null) ordersContainer.setVisibility(View.VISIBLE);
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
