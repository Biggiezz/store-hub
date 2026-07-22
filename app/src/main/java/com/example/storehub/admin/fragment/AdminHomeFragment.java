package com.example.storehub.admin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.storehub.R;
import com.example.storehub.admin.AdminOrdersActivity;
import com.example.storehub.auth.LoginActivity;
import com.example.storehub.model.AdminStats;
import com.example.storehub.model.Product;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class AdminHomeFragment extends Fragment {

    private View cardSales, cardUsers, cardProducts, cardOrders;
    private TextView txtTitle, txtValue, txtStatus;

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

        if (cardOrders != null) {
            cardOrders.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AdminOrdersActivity.class);
                startActivity(intent);
            });
        }

        View btnLogout = view.findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
                prefManager.logout();
                Toast.makeText(requireContext(), "Đã đăng xuất tài khoản", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(requireContext(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        }

        setupCardTitles();
    }

    private void setupCardTitles() {
        if (cardSales != null) {
            txtTitle = cardSales.findViewById(R.id.txtTitle);
            if (txtTitle != null) txtTitle.setText("Doanh số bán hàng");
        }
        if (cardUsers != null) {
            txtTitle = cardUsers.findViewById(R.id.txtTitle);
            txtValue = cardUsers.findViewById(R.id.txtValue);
            txtStatus = cardUsers.findViewById(R.id.txtStatus);
            if (txtTitle != null) txtTitle.setText("Người dùng đăng ký");
            if (txtValue != null) txtValue.setText("0");
            if (txtStatus != null) txtStatus.setText("+0 thành viên");
        }
        if (cardProducts != null) {
            txtTitle = cardProducts.findViewById(R.id.txtTitle);
            txtValue = cardProducts.findViewById(R.id.txtValue);
            txtStatus = cardProducts.findViewById(R.id.txtStatus);
            if (txtTitle != null) txtTitle.setText("Tổng số sản phẩm");
            if (txtValue != null) txtValue.setText("0");
            if (txtStatus != null) txtStatus.setText("Đang kinh doanh");
        }
        if (cardOrders != null) {
            txtTitle = cardOrders.findViewById(R.id.txtTitle);
            txtValue = cardOrders.findViewById(R.id.txtValue);
            txtStatus = cardOrders.findViewById(R.id.txtStatus);
            if (txtTitle != null) txtTitle.setText("Quản lý đơn hàng");
            if (txtValue != null) txtValue.setText("...");
            if (txtStatus != null) txtStatus.setText("Xem danh sách đơn hàng");
        }
    }

    private void fetchDashboardStats() {
        HttpResquest request = new HttpResquest();
        request.callAPI().getAdminDashboardStats().enqueue(new Callback<Response<AdminStats.DashboardData>>() {
            @Override
            public void onResponse(@NonNull Call<Response<AdminStats.DashboardData>> call, @NonNull retrofit2.Response<Response<AdminStats.DashboardData>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bindData(response.body().getData());
                } else {
                    Log.e("AdminHomeFragment", "Không thể lấy dữ liệu thống kê từ máy chủ");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<AdminStats.DashboardData>> call, @NonNull Throwable t) {
                Log.e("AdminHomeFragment", "Lỗi khi gọi API thống kê", t);
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchProductCount() {
        new HttpResquest().callAPI().getListProduct(1, 1, "").enqueue(new Callback<Response<ArrayList<Product>>>() {
                    @Override
                    public void onResponse(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull retrofit2.Response<Response<ArrayList<Product>>> response) {
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
                        Log.e("AdminHomeFragment", "Lỗi khi lấy tổng số sản phẩm", t);
                    }
                });
    }

    private void bindData(AdminStats.DashboardData data) {
        if (data == null) return;

        if (cardSales != null) {
            TextView txtValue = cardSales.findViewById(R.id.txtValue);
            TextView txtStatus = cardSales.findViewById(R.id.txtStatus);

            if (txtValue != null) {
                txtValue.setText(String.valueOf(data.getTotalSalesCount()));
            }
            if (txtStatus != null) {
                txtStatus.setText(data.getSalesStatus() != null ? data.getSalesStatus() : "+0% so với tháng trước");
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

    }
}
