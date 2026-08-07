package com.nguyenmanhphuc.storehubapp.admin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.nguyenmanhphuc.storehubapp.model.response.DashboardData;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
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
        fetchDashboardStats();
        fetchProductCount();
    }

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
                        android.content.Context appContext = requireContext().getApplicationContext();
                        Toast.makeText(appContext, "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(appContext, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if (getActivity() != null) {
                            getActivity().finish();
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
            if (txtTitle != null) txtTitle.setText("Doanh số bán hàng");
            ImageView imgIcon = cardSales.findViewById(R.id.imgDashboardIcon);
            if (imgIcon != null) imgIcon.setImageResource(R.drawable.ic_store_admin);
        }
        if (cardUsers != null) {
            txtTitle = cardUsers.findViewById(R.id.txtTitle);
            txtValue = cardUsers.findViewById(R.id.txtValue);
            txtStatus = cardUsers.findViewById(R.id.txtStatus);
            if (txtTitle != null) txtTitle.setText("Người dùng đăng ký");
            if (txtValue != null) txtValue.setText("0");
            if (txtStatus != null) txtStatus.setText("+0 thành viên");
            ImageView imgIcon = cardUsers.findViewById(R.id.imgDashboardIcon);
            if (imgIcon != null) imgIcon.setImageResource(R.drawable.ic_users);
        }
        if (cardProducts != null) {
            txtTitle = cardProducts.findViewById(R.id.txtTitle);
            txtValue = cardProducts.findViewById(R.id.txtValue);
            txtStatus = cardProducts.findViewById(R.id.txtStatus);
            if (txtTitle != null) txtTitle.setText("Tổng số sản phẩm");
            if (txtValue != null) txtValue.setText("0");
            if (txtStatus != null) txtStatus.setText("Đang kinh doanh");
            ImageView imgIcon = cardProducts.findViewById(R.id.imgDashboardIcon);
            if (imgIcon != null) imgIcon.setImageResource(R.drawable.ic_products);
        }
        if (cardOrders != null) {
            txtTitle = cardOrders.findViewById(R.id.txtTitle);
            txtValue = cardOrders.findViewById(R.id.txtValue);
            txtStatus = cardOrders.findViewById(R.id.txtStatus);
            if (txtTitle != null) txtTitle.setText("Quản lý đơn hàng");
            if (txtValue != null) txtValue.setText("...");
            if (txtStatus != null) txtStatus.setText("Xem danh sách đơn hàng");
            ImageView imgIcon = cardOrders.findViewById(R.id.imgDashboardIcon);
            if (imgIcon != null) imgIcon.setImageResource(R.drawable.ic_receipt);
        }
    }

    private void refreshData() {
        completedCalls = 0;
        fetchDashboardStats();
        fetchProductCount();
    }

    private synchronized void checkRefreshComplete() {
        completedCalls++;
        if (completedCalls >= 2) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void fetchDashboardStats() {
        HttpResquest request = new HttpResquest();
        request.callAPI().getAdminDashboardStats().enqueue(new Callback<Response<DashboardData>>() {
            @Override
            public void onResponse(@NonNull Call<Response<DashboardData>> call, @NonNull retrofit2.Response<Response<DashboardData>> response) {
                checkRefreshComplete();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bindData(response.body().getData());
                } else {
                    Log.e("AdminHomeFragment", "Không thể lấy dữ liệu thống kê từ máy chủ");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<DashboardData>> call, @NonNull Throwable t) {
                checkRefreshComplete();
                Log.e("AdminHomeFragment", "Lỗi khi gọi API thống kê", t);
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchProductCount() {
        new HttpResquest().callAPI().getListProduct(1, 1, "", false, "").enqueue(new Callback<Response<ArrayList<Product>>>() {
                    @Override
                    public void onResponse(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull retrofit2.Response<Response<ArrayList<Product>>> response) {
                        checkRefreshComplete();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getPagination() != null && cardProducts != null) {
                            TextView txtValue = cardProducts.findViewById(R.id.txtValue);
                            if (txtValue != null) {
                                txtValue.setText(String.valueOf(
                                        response.body().getPagination().getTotalProducts()));
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull Throwable t) {
                        checkRefreshComplete();
                        Log.e("AdminHomeFragment", "Lỗi khi lấy tổng số sản phẩm", t);
                    }
                });
    }

    private void bindData(DashboardData data) {
        if (data == null) return;

        if (cardSales != null) {
            TextView txtValue = cardSales.findViewById(R.id.txtValue);
            TextView txtStatus = cardSales.findViewById(R.id.txtStatus);

            if (txtValue != null) {
                DecimalFormat formatter = new DecimalFormat("#,###");
                String formattedSales = formatter.format(data.getTotalSales()) + " đ";
                txtValue.setText(formattedSales);
            }
            if (txtStatus != null) {
                txtStatus.setText("Đã bán " + data.getTotalSalesCount() + " sản phẩm");
            }
        }

        if (cardUsers != null) {
            TextView txtValue = cardUsers.findViewById(R.id.txtValue);
            TextView txtStatus = cardUsers.findViewById(R.id.txtStatus);

            if (txtValue != null) {
                txtValue.setText(String.valueOf(data.getTotalUsers()));
            }
            if (txtStatus != null) {
                txtStatus.setText(data.getUsersStatus() != null ? data.getUsersStatus() : "+0 thành viên");
            }
        }

        if (cardOrders != null) {
            TextView txtValue = cardOrders.findViewById(R.id.txtValue);
            TextView txtStatus = cardOrders.findViewById(R.id.txtStatus);

            if (txtValue != null) {
                txtValue.setText(String.valueOf(data.getTotalOrders()) + " đơn");
            }
            if (txtStatus != null) {
                txtStatus.setText(data.getPendingOrders() + " đơn chờ xử lý");
            }
        }
    }
}
