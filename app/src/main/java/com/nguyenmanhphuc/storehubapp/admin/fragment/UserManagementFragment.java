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
    private final List<User> displayedUsers = new ArrayList<>();

    private boolean isStaffTabSelected = true;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 10;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isLoading = false;
    private boolean hasMoreData = true;

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
                hasMoreData = true;
                fetchUsersFromServer(1, false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

//        btnFilterUser.setOnClickListener(v -> Toast.makeText(requireContext(), requireContext().getString(R.string.toast_bo_loc_nang_cao), Toast.LENGTH_SHORT).show());

        userAdapter.setOnUserClickListener(user -> {
            boolean isSuperAdmin = currentUser != null && currentUser.isSuperAdmin();
            if (isSuperAdmin) {
                if (!currentUser.canManage(user)) {
                    Toast.makeText(requireContext(), requireContext().getString(R.string.toast_ban_khong_co_quyen_chinh_suaxoa_tai_khoa), Toast.LENGTH_LONG).show();
                    return;
                }
                new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.management_options_title))
                    .setMessage(String.format(getString(R.string.management_action_question), user.getName()))
                    .setPositiveButton(getString(R.string.edit), (dialog, which) -> {
                        Intent intent = new Intent(requireContext(), AddUserActivity.class);
                        intent.putExtra("user_edit", new com.google.gson.Gson().toJson(user));
                        startActivity(intent);
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            } else {
                showCustomerDetailDialog(user);
            }
        });

        userAdapter.setOnUserDeleteListener(user -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.confirm_delete_title))
                    .setMessage(String.format(getString(R.string.confirm_delete_user_msg), user.getName()))
                    .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                        performDeleteUser(user);
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
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
                    
                    if (hasMoreData && !isLoading) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                                && firstVisibleItemPosition >= 0) {
                            currentPage++;
                            fetchUsersFromServer(currentPage, true);
                        }
                    }
                }
            }
        });

        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                // String query = etSearchUser.getText().toString();
            }
        });
    }

    private void performDeleteUser(User user) {
        SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
        String token = "Bearer " + prefManager.getToken();
        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().deleteUser(token, user.getId()).enqueue(new retrofit2.Callback<Response<Void>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Response<Void>> call, @NonNull retrofit2.Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), getString(R.string.user_deleted_success), Toast.LENGTH_SHORT).show();
                    fetchUsersFromServer();
                } else {
                    Toast.makeText(requireContext(), getString(R.string.user_delete_failed), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Response<Void>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), getString(R.string.server_connection_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchTab(boolean selectStaff) {
        isStaffTabSelected = selectStaff;
        currentPage = 1;
        hasMoreData = true;

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
        }

        fetchUsersFromServer(1, false);
    }

    private void fetchUsersFromServer() {
        currentPage = 1;
        hasMoreData = true;
        fetchUsersFromServer(1, false);
    }

    private void fetchUsersFromServer(int page, boolean isLoadMore) {
        if (isLoading) return;
        isLoading = true;

        boolean isSwipeRefreshing = swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing();
        if (!isSwipeRefreshing && !isLoadMore) {
            pbLoadingUsers.setVisibility(View.VISIBLE);
            if (rvUsers != null) rvUsers.setVisibility(View.GONE);
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
        } else if (isLoadMore) {
            pbLoadingUsers.setVisibility(View.VISIBLE);
        }

        SharedPreferencesManager prefManager = new SharedPreferencesManager(requireContext());
        String token = "Bearer " + prefManager.getToken();
        String type = isStaffTabSelected ? "staff" : "customer";
        String search = etSearchUser != null ? etSearchUser.getText().toString().trim() : "";

        HttpResquest httpResquest = new HttpResquest();
        httpResquest.callAPI().getListUsers(token, page, PAGE_SIZE, type, search).enqueue(new retrofit2.Callback<Response<ArrayList<User>>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<Response<ArrayList<User>>> call, @NonNull retrofit2.Response<Response<ArrayList<User>>> response) {
                if (!isAdded()) return;
                isLoading = false;
                pbLoadingUsers.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    ArrayList<User> serverUsers = response.body().getData();
                    // Read total counts from headers
                    String totalStaff = response.headers().get("X-Total-Staff");
                    String totalCustomers = response.headers().get("X-Total-Customers");
                    if (totalStaff != null) tvStaffCount.setText(totalStaff);
                    if (totalCustomers != null) tvCustomerCount.setText(totalCustomers);

                    if (!isLoadMore) {
                        displayedUsers.clear();
                    }

                    if (serverUsers.size() < PAGE_SIZE) {
                        hasMoreData = false;
                    } else {
                        hasMoreData = true;
                    }

                    displayedUsers.addAll(serverUsers);
                    userAdapter.updateData(displayedUsers);
                } else {
                    if (!isLoadMore) {
                        displayedUsers.clear();
                        userAdapter.updateData(displayedUsers);
                    }
                    Toast.makeText(requireContext(), requireContext().getString(R.string.toast_khong_the_lay_du_lieu_tu_server), Toast.LENGTH_SHORT).show();
                }

                if (displayedUsers.isEmpty()) {
                    if (tvEmptyState != null) tvEmptyState.setVisibility(View.VISIBLE);
                    if (rvUsers != null) rvUsers.setVisibility(View.GONE);
                } else {
                    if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
                    if (rvUsers != null) rvUsers.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<Response<ArrayList<User>>> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                isLoading = false;
                pbLoadingUsers.setVisibility(View.GONE);
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(), requireContext().getString(R.string.toast_loi_ket_noi_may_chu), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCustomerDetailDialog(User user) {
        if (getContext() == null || user == null) return;
        String notUpdated = getString(R.string.not_updated);
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.full_name_label)).append(user.getName() != null ? user.getName() : notUpdated).append("\n\n");
        sb.append(getString(R.string.phone_label)).append(user.getPhone() != null && !user.getPhone().isEmpty() ? user.getPhone() : notUpdated).append("\n\n");
        sb.append(getString(R.string.email_label)).append(user.getEmail() != null ? user.getEmail() : notUpdated).append("\n\n");
        sb.append(getString(R.string.role_label)).append(user.getRole() != null ? getLocalizedRole(user.getRole()) : notUpdated).append("\n\n");
        sb.append(getString(R.string.address_label)).append(user.getAddress() != null && !user.getAddress().isEmpty() ? user.getAddress() : notUpdated).append("\n\n");
        sb.append(getString(R.string.last_active_label)).append(DateTimeUtils.getRelativeTime(user.getLastActive(), user.isOnline()));

        new AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.customer_details_title))
            .setMessage(sb.toString())
            .setPositiveButton(getString(R.string.close), null)
            .show();
    }

    private String getLocalizedRole(String role) {
        if (role == null) return "";
        String r = role.toLowerCase();
        if (r.contains("admin") || r.contains("quản lý")) {
            return getString(R.string.store_manager);
        } else if (r.contains("super")) {
            return getString(R.string.super_admin_role);
        } else if (r.contains("khách") || r.contains("customer")) {
            return getString(R.string.customer_role);
        }
        return role;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchUsersFromServer();
    }
}
