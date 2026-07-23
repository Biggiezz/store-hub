package com.example.storehub.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.R;
import com.example.storehub.adapter.UserManagementAdapter;
import com.example.storehub.admin.AddUserActivity;
import com.example.storehub.model.User;
import com.example.storehub.services.HttpResquest;
import com.example.storehub.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment {

    private MaterialButton btnAddNewUser;
    private LinearLayout btnTabStaff, btnTabCustomer;
    private TextView tvStaffTabTitle, tvCustomerTabTitle, tvStaffCount, tvCustomerCount, tvEmptyState;
    private EditText etSearchUser;
    private FrameLayout btnFilterUser;
    private RecyclerView rvUsers;
    private ProgressBar pbLoadingUsers;
    private LinearLayout llPagination;
    private TextView btnPrevPage, btnPage1, btnPage2, btnPage3, btnNextPage;

    private UserManagementAdapter userAdapter;
    private List<User> allStaffList = new ArrayList<>();
    private List<User> allCustomerList = new ArrayList<>();
    private List<User> currentList = new ArrayList<>();

    private boolean isStaffTabSelected = true;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;

    public UserManagementFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initUi(view);
        setUpAdapter();
        setUpListener();

        loadMockUserData();

        SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
        User currentUser = prefManager.getUser();
        boolean isSuperAdmin = currentUser != null && currentUser.isSuperAdmin();

        if (!isSuperAdmin) {
            if (btnAddNewUser != null) btnAddNewUser.setVisibility(View.GONE);
            if (btnTabStaff != null) btnTabStaff.setVisibility(View.GONE);
            switchTab(false);
        } else {
            switchTab(true);
        }
    }

    private void initUi(View view) {
        btnAddNewUser = view.findViewById(R.id.btnAddNewUser);
        btnTabStaff = view.findViewById(R.id.btnTabStaff);
        btnTabCustomer = view.findViewById(R.id.btnTabCustomer);
        tvStaffTabTitle = view.findViewById(R.id.tvStaffTabTitle);
        tvCustomerTabTitle = view.findViewById(R.id.tvCustomerTabTitle);
        tvStaffCount = view.findViewById(R.id.tvStaffCount);
        tvCustomerCount = view.findViewById(R.id.tvCustomerCount);
        etSearchUser = view.findViewById(R.id.etSearchUser);
        btnFilterUser = view.findViewById(R.id.btnFilterUser);
        rvUsers = view.findViewById(R.id.rvUsers);
        pbLoadingUsers = view.findViewById(R.id.pbLoadingUsers);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        llPagination = view.findViewById(R.id.llPagination);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnPage1 = view.findViewById(R.id.btnPage1);
        btnPage2 = view.findViewById(R.id.btnPage2);
        btnPage3 = view.findViewById(R.id.btnPage3);
        btnNextPage = view.findViewById(R.id.btnNextPage);
    }

    private void setUpAdapter() {
        SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
        User currentUser = prefManager.getUser();
        userAdapter = new UserManagementAdapter(requireContext(), currentUser);
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUsers.setAdapter(userAdapter);
    }

    private void setUpListener() {
        SharedPreferencesManager prefManager = new com.example.storehub.utils.SharedPreferencesManager(requireContext());
        User currentUser = prefManager.getUser();

        btnAddNewUser.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddUserActivity.class);
            startActivity(intent);
        });

        btnTabStaff.setOnClickListener(v -> switchTab(true));
        btnTabCustomer.setOnClickListener(v -> switchTab(false));

        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentPage = 1;
                filterUserList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btnFilterUser.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Bộ lọc nâng cao", Toast.LENGTH_SHORT).show();
        });

        userAdapter.setOnUserClickListener(user -> {
            boolean isSuperAdmin = currentUser != null && currentUser.isSuperAdmin();
            if (isSuperAdmin) {
                if (!currentUser.canManage(user)) {
                    Toast.makeText(requireContext(), "Bạn không có quyền chỉnh sửa/xóa tài khoản Super Admin này!", Toast.LENGTH_LONG).show();
                    return;
                }
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Tùy chọn quản lý")
                    .setMessage("Bạn muốn thực hiện thao tác gì với " + user.getName() + "?")
                    .setPositiveButton("Chỉnh sửa", (dialog, which) -> {
                        Intent intent = new Intent(requireContext(), AddUserActivity.class);
                        intent.putExtra("user_edit", new com.google.gson.Gson().toJson(user));
                        startActivity(intent);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            } else {
                showCustomerDetailDialog(user);
            }
        });

        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                String query = etSearchUser.getText().toString();
                filterUserList(query);
            }
        });

        btnNextPage.setOnClickListener(v -> {
            currentPage++;
            String query = etSearchUser.getText().toString();
            filterUserList(query);
        });

        btnPage1.setOnClickListener(v -> {
            try {
                currentPage = Integer.parseInt(btnPage1.getText().toString());
                String query = etSearchUser.getText().toString();
                filterUserList(query);
            } catch (Exception ignored) {}
        });

        btnPage2.setOnClickListener(v -> {
            try {
                currentPage = Integer.parseInt(btnPage2.getText().toString());
                String query = etSearchUser.getText().toString();
                filterUserList(query);
            } catch (Exception ignored) {}
        });

        btnPage3.setOnClickListener(v -> {
            try {
                currentPage = Integer.parseInt(btnPage3.getText().toString());
                String query = etSearchUser.getText().toString();
                filterUserList(query);
            } catch (Exception ignored) {}
        });
    }

    private void switchTab(boolean selectStaff) {
        isStaffTabSelected = selectStaff;
        currentPage = 1;

        if (selectStaff) {
            btnTabStaff.setBackgroundResource(R.drawable.bg_admin_chip_active);
            btnTabCustomer.setBackgroundResource(R.drawable.bg_admin_chip);

            tvStaffTabTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tvStaffTabTitle.setAlpha(1.0f);
            tvStaffCount.setBackgroundResource(R.drawable.bg_tab_badge_selected);
            tvStaffCount.setTextColor(Color.parseColor("#172C22"));

            tvCustomerTabTitle.setTextColor(Color.parseColor("#8A8077"));
            tvCustomerCount.setBackgroundResource(R.drawable.bg_tab_badge_unselected);
            tvCustomerCount.setTextColor(Color.parseColor("#8A8077"));

            currentList = new ArrayList<>(allStaffList);
        } else {
            btnTabStaff.setBackgroundResource(R.drawable.bg_admin_chip);
            btnTabCustomer.setBackgroundResource(R.drawable.bg_admin_chip_active);

            tvCustomerTabTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tvCustomerTabTitle.setAlpha(1.0f);
            tvCustomerCount.setBackgroundResource(R.drawable.bg_tab_badge_selected);
            tvCustomerCount.setTextColor(Color.parseColor("#172C22"));

            tvStaffTabTitle.setTextColor(Color.parseColor("#8A8077"));
            tvStaffCount.setBackgroundResource(R.drawable.bg_tab_badge_unselected);
            tvStaffCount.setTextColor(Color.parseColor("#8A8077"));

            currentList = new ArrayList<>(allCustomerList);
        }

        String query = etSearchUser.getText().toString();
        filterUserList(query);
    }

    private void filterUserList(String keyword) {
        List<User> filtered = new ArrayList<>();
        String query = keyword != null ? keyword.trim().toLowerCase() : "";

        for (User user : currentList) {
            boolean nameMatches = user.getName() != null && user.getName().toLowerCase().contains(query);
            boolean emailMatches = user.getEmail() != null && user.getEmail().toLowerCase().contains(query);
            if (nameMatches || emailMatches) {
                filtered.add(user);
            }
        }

        int totalItems = filtered.size();
        int totalPages = (totalItems + PAGE_SIZE - 1) / PAGE_SIZE;
        if (totalPages < 1) totalPages = 1;

        if (currentPage > totalPages) {
            currentPage = totalPages;
        }

        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, totalItems);
        List<User> pagedList = new ArrayList<>();
        if (start < totalItems) {
            pagedList = filtered.subList(start, end);
        }

        userAdapter.updateData(pagedList);

        if (pagedList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
            llPagination.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvUsers.setVisibility(View.VISIBLE);
            if (totalItems <= PAGE_SIZE) {
                llPagination.setVisibility(View.GONE);
            } else {
                llPagination.setVisibility(View.VISIBLE);
                updatePaginationUi(currentPage, totalPages);
            }
        }
    }

    private void updatePaginationUi(int page, int totalPages) {
        int p1, p2, p3;
        if (totalPages <= 3) {
            p1 = 1;
            p2 = 2;
            p3 = 3;
            btnPage1.setVisibility(View.VISIBLE);
            btnPage2.setVisibility(totalPages >= 2 ? View.VISIBLE : View.GONE);
            btnPage3.setVisibility(totalPages >= 3 ? View.VISIBLE : View.GONE);
        } else {
            btnPage1.setVisibility(View.VISIBLE);
            btnPage2.setVisibility(View.VISIBLE);
            btnPage3.setVisibility(View.VISIBLE);
            if (page <= 2) {
                p1 = 1;
                p2 = 2;
                p3 = 3;
            } else if (page >= totalPages - 1) {
                p1 = totalPages - 2;
                p2 = totalPages - 1;
                p3 = totalPages;
            } else {
                p1 = page - 1;
                p2 = page;
                p3 = page + 1;
            }
        }

        btnPage1.setText(String.valueOf(p1));
        btnPage2.setText(String.valueOf(p2));
        btnPage3.setText(String.valueOf(p3));

        setActiveStyle(btnPage1, p1 == page);
        setActiveStyle(btnPage2, p2 == page);
        setActiveStyle(btnPage3, p3 == page);

        if (page > 1) {
            btnPrevPage.setEnabled(true);
            btnPrevPage.setAlpha(1.0f);
        } else {
            btnPrevPage.setEnabled(false);
            btnPrevPage.setAlpha(0.4f);
        }

        if (page < totalPages) {
            btnNextPage.setEnabled(true);
            btnNextPage.setAlpha(1.0f);
        } else {
            btnNextPage.setEnabled(false);
            btnNextPage.setAlpha(0.4f);
        }
    }

    private void setActiveStyle(TextView tv, boolean isActive) {
        if (isActive) {
            tv.setBackgroundResource(R.drawable.bg_pagination_active);
            tv.setTextColor(Color.WHITE);
        } else {
            tv.setBackgroundResource(R.drawable.bg_pagination_inactive);
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        }
    }

    private void loadMockUserData() {
        allStaffList.clear();
        allStaffList.add(new User("1", "Eleanor Vance", "eleanor.v@storehub.com", "0912345678", "admin", "", "Hà Nội", "2 giờ trước"));
        allStaffList.add(new User("2", "Margaret Atwood", "m.atwood@storehub.com", "0987654321", "Chuyên viên kho", "", "TP. Hồ Chí Minh", "1 ngày trước"));
        allStaffList.add(new User("3", "Julianne Moore", "j.moore@storehub.com", "0933445566", "Nhân viên bán hàng", "", "Đà Nẵng", "Vừa xong"));
        allStaffList.add(new User("4", "Arthur Pendelton", "arthur.p@storehub.com", "0977889900", "Hỗ trợ khách hàng", "", "Cần Thơ", "3 ngày trước"));
        allStaffList.add(new User("5", "Robert Langdon", "robert.l@storehub.com", "0944556677", "Kế toán viên", "", "Hải Phòng", "5 giờ trước"));

        allCustomerList.clear();
        allCustomerList.add(new User("101", "Nguyễn Văn An", "an.nguyen@gmail.com", "0901112233", "customer", "", "Hà Nội", "10 phút trước"));
        allCustomerList.add(new User("102", "Trần Thị Bình", "binh.tran@yahoo.com", "0902223344", "Khách hàng VIP", "", "TP. Hồ Chí Minh", "1 giờ trước"));
        allCustomerList.add(new User("103", "Lê Hoàng Cường", "cuong.le@gmail.com", "0903334455", "Khách hàng thân thiết", "", "Đà Nẵng", "4 giờ trước"));
        allCustomerList.add(new User("104", "Phạm Minh Dung", "dung.pham@gmail.com", "0904445566", "customer", "", "Cần Thơ", "1 ngày trước"));

        tvStaffCount.setText(String.valueOf(allStaffList.size()));
        tvCustomerCount.setText("1,492");
    }

    private void fetchUsersFromServer() {
        pbLoadingUsers.setVisibility(View.VISIBLE);
        SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
        String token = "Bearer " + prefManager.getToken();
        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().getListUsers(token).enqueue(new retrofit2.Callback<com.example.storehub.model.Response<ArrayList<User>>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<com.example.storehub.model.Response<ArrayList<User>>> call,
                                   @NonNull retrofit2.Response<com.example.storehub.model.Response<ArrayList<User>>> response) {
                pbLoadingUsers.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    ArrayList<User> serverUsers = response.body().getData();
                    allStaffList.clear();
                    allCustomerList.clear();
                    for (User u : serverUsers) {
                        String role = u.getRole() != null ? u.getRole().toLowerCase() : "";
                        if (role.contains("khách hàng") || role.contains("customer")) {
                            allCustomerList.add(u);
                        } else {
                            allStaffList.add(u);
                        }
                    }
                    tvStaffCount.setText(String.valueOf(allStaffList.size()));
                    tvCustomerCount.setText(String.valueOf(allCustomerList.size()));
                    switchTab(isStaffTabSelected);
                } else {
                    // Fallback to mock data if server response is empty
                    loadMockUserData();
                    switchTab(isStaffTabSelected);
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<com.example.storehub.model.Response<ArrayList<User>>> call, @NonNull Throwable t) {
                pbLoadingUsers.setVisibility(View.GONE);
                // Fallback to mock data if network request fails
                loadMockUserData();
                switchTab(isStaffTabSelected);
            }
        });
    }

    private void showCustomerDetailDialog(User user) {
        if (getContext() == null || user == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("Họ và tên: ").append(user.getName() != null ? user.getName() : "Chưa cập nhật").append("\n\n");
        sb.append("Số điện thoại: ").append(user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : "Chưa cập nhật").append("\n\n");
        sb.append("Email: ").append(user.getEmail() != null ? user.getEmail() : "Chưa cập nhật").append("\n\n");
        sb.append("Vai trò: ").append(user.getRole() != null ? user.getRole() : "Chưa cập nhật").append("\n\n");
        sb.append("Địa chỉ: ").append(user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : "Chưa cập nhật").append("\n\n");
        sb.append("Hoạt động lần cuối: ").append(user.getLastActive() != null ? user.getLastActive() : "Không khả dụng");

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Thông tin khách hàng")
            .setMessage(sb.toString())
            .setPositiveButton("Đóng", null)
            .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchUsersFromServer();
    }
}
