package com.nguyenmanhphuc.storehubapp.admin.fragment;

import android.app.Activity;
import android.content.Context;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.admin.AdminOrdersActivity;
import com.nguyenmanhphuc.storehubapp.admin.ManagementReviewsActivity;
import com.nguyenmanhphuc.storehubapp.auth.LoginActivity;
import com.nguyenmanhphuc.storehubapp.model.Product;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.model.response.DashboardData;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.DataCache;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;

import java.text.DecimalFormat;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.content.ContextCompat;

public class AdminHomeFragment extends Fragment {

    private View cardSales, cardUsers, cardProducts, cardOrders;
    private TextView txtTitle, txtValue, txtStatus;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int completedCalls = 0;

    private static final String CACHE_DASHBOARD = "admin_dashboard";
    private static final String CACHE_PRODUCTS  = "admin_dashboard_products";
    private static final String CACHE_USERS     = "admin_dashboard_users";

    public AdminHomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUi(view);

        // Hiện skeleton ngay khi fragment load
        showSkeletonAll();

        // Nếu có cache hợp lệ → dùng ngay, không fetch lại
        DashboardData cachedDash = DataCache.get().get(CACHE_DASHBOARD, DashboardData.class);
        Integer cachedProducts = DataCache.get().get(CACHE_PRODUCTS, Integer.class);
        Integer cachedUsers = DataCache.get().get(CACHE_USERS, Integer.class);

        boolean allCached = cachedDash != null && cachedProducts != null && cachedUsers != null;
        if (allCached) {
            bindData(cachedDash);
            // Sản phẩm
            if (cardProducts != null) {
                TextView tv = cardProducts.findViewById(R.id.txtValue);
                showSkeleton(cardProducts, false);
                animateCountUp(tv, cachedProducts, "");
            }
            // Người dùng
            if (cardUsers != null) {
                TextView tv = cardUsers.findViewById(R.id.txtValue);
                TextView tvS = cardUsers.findViewById(R.id.txtStatus);
                if (tvS != null) tvS.setText(String.format(getString(R.string.total_customers_format), cachedUsers));
                showSkeleton(cardUsers, false);
                animateCountUp(tv, cachedUsers, "");
            }
        } else {
            fetchDashboardStats();
            fetchProductCount();
            fetchUserCount();
        }
    }

    // ─── Skeleton helpers ────────────────────────────────────────────────────

    /** Bật skeleton trên tất cả 4 card */
    private void showSkeletonAll() {
        showSkeleton(cardSales, true);
        showSkeleton(cardUsers, true);
        showSkeleton(cardProducts, true);
        showSkeleton(cardOrders, true);
    }

    /**
     * Hiện / ẩn skeleton overlay của một card.
     * Khi ẩn (show=false) sẽ fade out nhẹ nhàng.
     */
    private void showSkeleton(View card, boolean show) {
        if (card == null) return;
        View skeleton = card.findViewById(R.id.layoutSkeleton);
        if (skeleton == null) return;

        if (show) {
            skeleton.setVisibility(View.VISIBLE);
            skeleton.setAlpha(1f);
            // Chạy pulse animation cho tất cả skeleton bar bên trong
            Animation pulse = AnimationUtils.loadAnimation(requireContext(), R.anim.skeleton_pulse);
            skeleton.startAnimation(pulse);
        } else {
            skeleton.clearAnimation();
            skeleton.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> skeleton.setVisibility(View.GONE))
                    .start();
        }
    }

    // ─── Count-up animation ───────────────────────────────────────────────────

    /**
     * Hoạt hình đếm số từ 0 lên target (dùng cho các card số nguyên: đơn hàng, người dùng, sản phẩm).
     */
    private void animateCountUp(TextView tv, int target, String suffix) {
        if (tv == null) return;
        ValueAnimator animator = ValueAnimator.ofInt(0, target);
        animator.setDuration(800);
        animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            if (isAdded()) tv.setText(a.getAnimatedValue() + suffix);
        });
        animator.start();
    }

    /**
     * Hoạt hình đếm tiền từ 0 lên target (dùng cho card doanh số).
     */
    private void animateSalesCountUp(TextView tv, long target) {
        if (tv == null) return;
        DecimalFormat formatter = new DecimalFormat("#,###");
        ValueAnimator animator = ValueAnimator.ofFloat(0f, (float) target);
        animator.setDuration(1000);
        animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animator.addUpdateListener(a -> {
            if (isAdded()) {
                long val = ((Number) a.getAnimatedValue()).longValue();
                tv.setText(formatter.format(val) + getString(R.string.admin_price_suffix));
            }
        });
        animator.start();
    }

    // ─── UI init ──────────────────────────────────────────────────────────────

    private void initUi(View view) {
        cardSales = view.findViewById(R.id.cardSales);
        cardUsers = view.findViewById(R.id.cardUsers);
        cardProducts = view.findViewById(R.id.cardProducts);
        cardOrders = view.findViewById(R.id.cardOrders);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.dark_green));
            swipeRefreshLayout.setOnRefreshListener(this::refreshData);
        }

        if (cardOrders != null) {
            cardOrders.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AdminOrdersActivity.class);
                startActivity(intent);
            });
        }

        ImageView imgProductReview = view.findViewById(R.id.imgProductReview);
        imgProductReview.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ManagementReviewsActivity.class);
            startActivity(intent);
        });

        View btnLogout = view.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
                final Context appContext = requireContext().getApplicationContext();
                final Activity activity = getActivity();
                String tokenHeader = "Bearer " + prefManager.getToken();

                HttpResquest httpResquest = new HttpResquest();
                httpResquest.callAPI().logout(tokenHeader).enqueue(new retrofit2.Callback<Response<Void>>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<Response<Void>> call, @NonNull retrofit2.Response<Response<Void>> response) {
                        prefManager.logout();
                        navigateToLogin();
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<Response<Void>> call, @NonNull Throwable t) {
                        prefManager.logout();
                        navigateToLogin();
                    }

                    private void navigateToLogin() {
                        Toast.makeText(appContext, appContext.getString(R.string.toast_da_dang_xuat_tai_khoan), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(appContext, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (activity != null) {
                            activity.finish();
                        }
                    }
                });
            });
        }

        setupCardTitles();
    }

    private void setupCardTitles() {
        if (cardSales != null) {
            txtTitle = cardSales.findViewById(R.id.txtTitle);
            if (txtTitle != null) txtTitle.setText(getString(R.string.sales_title));
            ImageView imgIcon = cardSales.findViewById(R.id.imgDashboardIcon);
            if (imgIcon != null) imgIcon.setImageResource(R.drawable.ic_store_admin);
        }
        if (cardUsers != null) {
            txtTitle = cardUsers.findViewById(R.id.txtTitle);
            txtValue = cardUsers.findViewById(R.id.txtValue);
            txtStatus = cardUsers.findViewById(R.id.txtStatus);
            if (txtTitle != null) txtTitle.setText(getString(R.string.registered_users_title));
            if (txtValue != null) txtValue.setText("0");
            if (txtStatus != null) txtStatus.setText(String.format(getString(R.string.new_members_format), 0));
            ImageView imgIcon = cardUsers.findViewById(R.id.imgDashboardIcon);
            if (imgIcon != null) imgIcon.setImageResource(R.drawable.ic_users);
        }
        if (cardProducts != null) {
            txtTitle = cardProducts.findViewById(R.id.txtTitle);
            txtValue = cardProducts.findViewById(R.id.txtValue);
            txtStatus = cardProducts.findViewById(R.id.txtStatus);
            if (txtTitle != null) txtTitle.setText(getString(R.string.total_products_title));
            if (txtValue != null) txtValue.setText("0");
            if (txtStatus != null) txtStatus.setText(getString(R.string.actively_selling));
            ImageView imgIcon = cardProducts.findViewById(R.id.imgDashboardIcon);
            if (imgIcon != null) imgIcon.setImageResource(R.drawable.ic_products);
        }
        if (cardOrders != null) {
            txtTitle = cardOrders.findViewById(R.id.txtTitle);
            txtValue = cardOrders.findViewById(R.id.txtValue);
            txtStatus = cardOrders.findViewById(R.id.txtStatus);
            if (txtTitle != null) txtTitle.setText(getString(R.string.order_management_title));
            if (txtValue != null) txtValue.setText("...");
            if (txtStatus != null) txtStatus.setText(getString(R.string.view_order_list));
            ImageView imgIcon = cardOrders.findViewById(R.id.imgDashboardIcon);
            if (imgIcon != null) imgIcon.setImageResource(R.drawable.ic_receipt);
        }
    }

    // ─── Refresh ──────────────────────────────────────────────────────────────

    private void refreshData() {
        completedCalls = 0;
        // Xóa cache dashboard khi refresh thủ công
        DataCache.get().invalidate("admin_dashboard");
        DataCache.get().invalidate("admin_dashboard_products");
        DataCache.get().invalidate("admin_dashboard_users");
        showSkeletonAll();
        fetchDashboardStats();
        fetchProductCount();
        fetchUserCount();
    }

    private synchronized void checkRefreshComplete() {
        completedCalls++;
        if (completedCalls >= 3) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    // ─── API Calls ────────────────────────────────────────────────────────────

    private void fetchDashboardStats() {
        HttpResquest request = new HttpResquest();
        String token = HttpResquest.authorizationHeader(requireContext());
        request.callAPI().getAdminDashboardStats(token).enqueue(new Callback<Response<DashboardData>>() {
            @Override
            public void onResponse(@NonNull Call<Response<DashboardData>> call, @NonNull retrofit2.Response<Response<DashboardData>> response) {
                checkRefreshComplete();
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    DashboardData data = response.body().getData();
                    DataCache.get().put(CACHE_DASHBOARD, data);
                    bindData(data);
                } else {
                    showSkeleton(cardSales, false);
                    showSkeleton(cardOrders, false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<DashboardData>> call, @NonNull Throwable t) {
                checkRefreshComplete();
                if (!isAdded()) return;
                showSkeleton(cardSales, false);
                showSkeleton(cardOrders, false);
                Toast.makeText(requireContext(), requireContext().getString(R.string.toast_loi_ket_noi_may_chu), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchProductCount() {
        new HttpResquest().callAPI().getListProduct(1, 1, "", false, "").enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull retrofit2.Response<Response<ArrayList<Product>>> response) {
                checkRefreshComplete();
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null
                        && response.body().getPagination() != null && cardProducts != null) {
                    int total = response.body().getPagination().getTotalProducts();
                    DataCache.get().put(CACHE_PRODUCTS, total);
                    TextView tv = cardProducts.findViewById(R.id.txtValue);
                    showSkeleton(cardProducts, false);
                    animateCountUp(tv, total, "");
                } else {
                    showSkeleton(cardProducts, false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull Throwable t) {
                checkRefreshComplete();
                if (!isAdded()) return;
                showSkeleton(cardProducts, false);
            }
        });
    }

    private void fetchUserCount() {
        SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
        String token = "Bearer " + prefManager.getToken();
        new HttpResquest().callAPI().getListUsers(token).enqueue(new Callback<Response<ArrayList<User>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<User>>> call, @NonNull retrofit2.Response<Response<ArrayList<User>>> response) {
                checkRefreshComplete();
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && cardUsers != null) {
                    ArrayList<User> allUsers = response.body().getData();
                    int customerCount = 0;
                    for (User u : allUsers) {
                        if (u.isSuperAdmin()) continue;
                        String role = u.getRole() != null ? u.getRole().toLowerCase() : "";
                        if (role.contains("khách hàng") || role.contains("customer")) {
                            customerCount++;
                        }
                    }
                    final int finalCount = customerCount;
                    DataCache.get().put(CACHE_USERS, finalCount);
                    TextView tv = cardUsers.findViewById(R.id.txtValue);
                    TextView tvStatus = cardUsers.findViewById(R.id.txtStatus);
                    if (tvStatus != null) {
                        tvStatus.setText(String.format(getString(R.string.total_customers_format), finalCount));
                    }
                    showSkeleton(cardUsers, false);
                    animateCountUp(tv, finalCount, "");
                } else {
                    showSkeleton(cardUsers, false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<com.nguyenmanhphuc.storehubapp.model.User>>> call, @NonNull Throwable t) {
                checkRefreshComplete();
                if (!isAdded()) return;
                showSkeleton(cardUsers, false);
            }
        });
    }

    // ─── Bind data ────────────────────────────────────────────────────────────

    private void bindData(DashboardData data) {
        if (data == null || !isAdded()) return;

        if (cardSales != null) {
            TextView tv = cardSales.findViewById(R.id.txtValue);
            TextView tvStatus = cardSales.findViewById(R.id.txtStatus);
            if (tvStatus != null) {
                tvStatus.setText(String.format(getString(R.string.sold_products_format), data.getTotalSalesCount()));
            }
            showSkeleton(cardSales, false);
            animateSalesCountUp(tv, data.getTotalSales());
        }

        if (cardOrders != null) {
            TextView tv = cardOrders.findViewById(R.id.txtValue);
            TextView tvStatus = cardOrders.findViewById(R.id.txtStatus);
            if (tvStatus != null) {
                tvStatus.setText(String.format(getString(R.string.pending_orders_format), data.getPendingOrders()));
            }
            showSkeleton(cardOrders, false);
            // Format "X đơn" với count-up
            ValueAnimator animator = ValueAnimator.ofInt(0, data.getTotalOrders());
            animator.setDuration(800);
            animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
            animator.addUpdateListener(a -> {
                if (isAdded() && tv != null) {
                    tv.setText(String.format(getString(R.string.orders_count_format), a.getAnimatedValue()));
                }
            });
            animator.start();
        }
    }
}
