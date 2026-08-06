package com.nguyenmanhphuc.storehubapp.admin.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.adapter.UserManagementAdapter;
import com.nguyenmanhphuc.storehubapp.admin.AddUserActivity;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.DateTimeUtils;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class UserManagementFragment extends Fragment {

    private MaterialButton btnAddNewUser;
    private LinearLayout btnTabStaff, btnTabCustomer;
    private TextView tvStaffTabTitle, tvCustomerTabTitle, tvStaffCount, tvCustomerCount, tvEmptyState;
    private EditText etSearchUser;
//    private FrameLayout btnFilterUser;
    private RecyclerView rvUsers;
    private ProgressBar pbLoadingUsers;
    private LinearLayout llPagination;
    private TextView btnPrevPage, btnPage1, btnPage2, btnPage3, btnNextPage;

    private UserManagementAdapter userAdapter;
    private final List<User> allStaffList = new ArrayList<>();
    private final List<User> allCustomerList = new ArrayList<>();
    private List<User> currentList = new ArrayList<>();
    private final List<User> displayedUsers = new ArrayList<>();

    private boolean isStaffTabSelected = true;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;
    private SwipeRefreshLayout swipeRefreshLayout;

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
//        btnFilterUser = view.findViewById(R.id.btnFilterUser);
        rvUsers = view.findViewById(R.id.rvUsers);
        pbLoadingUsers = view.findViewById(R.id.pbLoadingUsers);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        llPagination = view.findViewById(R.id.llPagination);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnPage1 = view.findViewById(R.id.btnPage1);
        btnPage2 = view.findViewById(R.id.btnPage2);
        btnPage3 = view.findViewById(R.id.btnPage3);
        btnNextPage = view.findViewById(R.id.btnNextPage);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.dark_green));
            swipeRefreshLayout.setOnRefreshListener(this::fetchUsersFromServer);
        }
    }

    private void setUpAdapter() {
        SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
        User currentUser = prefManager.getUser();
        userAdapter = new UserManagementAdapter(requireContext(), currentUser);
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUsers.setAdapter(userAdapter);
    }

    private void setUpListener() {
        SharedPreferencesManager prefManager = new com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager(requireContext());
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

//        btnFilterUser.setOnClickListener(v -> Toast.makeText(requireContext(), "Bộ lọc nâng cao", Toast.LENGTH_SHORT).show());

        userAdapter.setOnUserClickListener(user -> {
            boolean isSuperAdmin = currentUser != null && currentUser.isSuperAdmin();
            if (isSuperAdmin) {
                if (!currentUser.canManage(user)) {
                    Toast.makeText(requireContext(), "Bạn không có quyền chỉnh sửa/xóa tài khoản Super Admin này!", Toast.LENGTH_LONG).show();
                    return;
                }
                new AlertDialog.Builder(requireContext())
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

        if (llPagination != null) {
            llPagination.setVisibility(View.GONE);
        }

        rvUsers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                androidx.recyclerview.widget.LinearLayoutManager layoutManager =
                        (androidx.recyclerview.widget.LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && dy > 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    
                    String query = etSearchUser == null ? "" : etSearchUser.getText().toString().trim().toLowerCase();
                    List<User> filtered = new ArrayList<>();
                    for (User user : currentList) {
                        boolean nameMatches = user.getName() != null && user.getName().toLowerCase().contains(query);
                        boolean emailMatches = user.getEmail() != null && user.getEmail().toLowerCase().contains(query);
                        if (nameMatches || emailMatches) {
                            filtered.add(user);
                        }
                    }
                    int totalItems = filtered.size();
                    int totalPagesVal = (totalItems + PAGE_SIZE - 1) / PAGE_SIZE;
                    if (totalPagesVal < 1) totalPagesVal = 1;

                    if (currentPage < totalPagesVal) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                                && firstVisibleItemPosition >= 0) {
                            currentPage++;
                            int start = (currentPage - 1) * PAGE_SIZE;
                            int end = Math.min(start + PAGE_SIZE, totalItems);
                            if (start < totalItems) {
                                displayedUsers.addAll(filtered.subList(start, end));
                                userAdapter.updateData(displayedUsers);
                            }
                        }
                    }
                }
            }
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
        currentPage = 1;
        displayedUsers.clear();

        int start = 0;
        int end = Math.min(PAGE_SIZE, totalItems);
        if (start < totalItems) {
            displayedUsers.addAll(filtered.subList(start, end));
        }

        userAdapter.updateData(displayedUsers);

        if (displayedUsers.isEmpty()) {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
            if (rvUsers != null) rvUsers.setVisibility(View.GONE);
        } else {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
            if (rvUsers != null) rvUsers.setVisibility(View.VISIBLE);
        }
        if (llPagination != null) {
            llPagination.setVisibility(View.GONE);
        }
    }

    private void fetchUsersFromServer() {
        boolean isSwipeRefreshing = swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing();
        if (!isSwipeRefreshing) {
            pbLoadingUsers.setVisibility(View.VISIBLE);
        }
        if (rvUsers != null) rvUsers.setVisibility(View.GONE);
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
        if (llPagination != null) llPagination.setVisibility(View.GONE);
        SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
        String token = "Bearer " + prefManager.getToken();
        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().getListUsers(token).enqueue(new retrofit2.Callback<Response<ArrayList<User>>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Response<ArrayList<User>>> call, @NonNull retrofit2.Response<Response<ArrayList<User>>> response) {
                pbLoadingUsers.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                allStaffList.clear();
                allCustomerList.clear();
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    ArrayList<User> serverUsers = response.body().getData();
                    for (User u : serverUsers) {
                        String role = u.getRole() != null ? u.getRole().toLowerCase() : "";
                        if (role.contains("khách hàng") || role.contains("customer")) {
                            allCustomerList.add(u);
                        } else {
                            allStaffList.add(u);
                        }
                    }
                }
                tvStaffCount.setText(String.valueOf(allStaffList.size()));
                tvCustomerCount.setText(String.valueOf(allCustomerList.size()));
                switchTab(isStaffTabSelected);
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Response<ArrayList<User>>> call, @NonNull Throwable t) {
                pbLoadingUsers.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                allStaffList.clear();
                allCustomerList.clear();
                tvStaffCount.setText("0");
                tvCustomerCount.setText("0");
                switchTab(isStaffTabSelected);
                Toast.makeText(requireContext(), "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
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
        sb.append("Hoạt động lần cuối: ").append(DateTimeUtils.getRelativeTime(user.getLastActive(), user.isOnline()));

        new AlertDialog.Builder(requireContext())
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
