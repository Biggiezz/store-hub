package com.example.storehub.admin.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.admin.AdminProductDetailActivity;
import com.example.storehub.R;
import com.example.storehub.admin.ProductFormManagementActivity;
import com.example.storehub.admin.adapter.AdminProductAdapter;
import com.example.storehub.model.Pagination;
import com.example.storehub.model.Product;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class ProductsFragmentManagement extends Fragment {
    private static final int PAGE_SIZE = 6;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private AdminProductAdapter adapter;
    private Call<Response<ArrayList<Product>>> currentCall;
    private EditText searchInput;
    private TextView chipAll, chipPhone, chipComputer, chipHeadphone, page1, page2, page3, lastPage;
    private int currentPage = 1;
    private int totalPages = 1;
    private String selectedCategory = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView grid = view.findViewById(R.id.rvAdminProducts);
        grid.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        grid.setNestedScrollingEnabled(false);
        adapter = new AdminProductAdapter(product -> {
            Intent intent = new Intent(requireContext(), AdminProductDetailActivity.class);
            String pid = product.get_id();
            if (pid == null || pid.isEmpty()) pid = product.getId();
            intent.putExtra(AdminProductDetailActivity.EXTRA_PRODUCT_ID, pid);
            startActivity(intent);
        });
        grid.setAdapter(adapter);

        searchInput = view.findViewById(R.id.edtAdminProductSearch);
        chipAll = view.findViewById(R.id.chipAllProducts);
        chipPhone = view.findViewById(R.id.chipPhoneProducts);
        chipComputer = view.findViewById(R.id.chipComputerProducts);
        chipHeadphone = view.findViewById(R.id.chipHeadphoneProducts);
        page1 = view.findViewById(R.id.btnAdminPage1);
        page2 = view.findViewById(R.id.btnAdminPage2);
        page3 = view.findViewById(R.id.btnAdminPage3);
        lastPage = view.findViewById(R.id.tvAdminLastPage);

        setCategoryListener(chipAll, "");
        setCategoryListener(chipPhone, "Điện thoại");
        setCategoryListener(chipComputer, "Máy tính");
        setCategoryListener(chipHeadphone, "Tai nghe");

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);
                searchHandler.postDelayed(() -> { currentPage = 1; loadProducts(); }, 350);
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        page1.setOnClickListener(v -> goToPage(1));
        page2.setOnClickListener(v -> goToPage(2));
        page3.setOnClickListener(v -> goToPage(3));
        lastPage.setOnClickListener(v -> goToPage(totalPages));
        view.findViewById(R.id.btnAdminPreviousPage).setOnClickListener(v -> goToPage(currentPage - 1));
        view.findViewById(R.id.btnAdminNextPage).setOnClickListener(v -> goToPage(currentPage + 1));

        FloatingActionButton add = view.findViewById(R.id.fabAddProduct);
        add.setOnClickListener(v -> startActivity(ProductFormManagementActivity.createAddIntent(requireContext())));
    }

    @Override public void onResume() { super.onResume(); loadProducts(); }

    private void setCategoryListener(TextView chip, String category) {
        chip.setOnClickListener(v -> {
            selectedCategory = category;
            currentPage = 1;
            updateCategoryChips();
            loadProducts();
        });
    }

    private void updateCategoryChips() {
        TextView[] chips = {chipAll, chipPhone, chipComputer, chipHeadphone};
        String[] values = {"", "Điện thoại", "Máy tính", "Tai nghe"};
        for (int i = 0; i < chips.length; i++) {
            boolean active = values[i].equals(selectedCategory);
            chips[i].setBackgroundResource(active ? R.drawable.bg_admin_chip_active : R.drawable.bg_admin_chip);
            chips[i].setTextColor(ContextCompat.getColor(requireContext(),
                    active ? R.color.white : R.color.text_secondary));
        }
    }

    private void goToPage(int page) {
        if (page < 1 || page > totalPages || page == currentPage) return;
        currentPage = page;
        loadProducts();
    }

    private void loadProducts() {
        if (!isAdded() || adapter == null) return;
        if (currentCall != null) currentCall.cancel();
        String keyword = searchInput == null ? "" : searchInput.getText().toString().trim();
        HttpResquest request = new HttpResquest();
        currentCall = keyword.isEmpty()
                ? request.callAPI().getListProduct(currentPage, PAGE_SIZE, selectedCategory)
                : request.callAPI().searchProduct(currentPage, PAGE_SIZE, keyword, selectedCategory);
        currentCall.enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull retrofit2.Response<Response<ArrayList<Product>>> response) {
                if (!isAdded() || call.isCanceled()) return;
                if (response.isSuccessful() && response.body() != null) {
                    adapter.submitList(response.body().getData());
                    Pagination pagination = response.body().getPagination();
                    totalPages = pagination == null ? 1 : Math.max(1, pagination.getTotalPages());
                    currentPage = pagination == null ? 1 : pagination.getCurrentPage();
                    updatePagination();
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull Throwable throwable) {
                if (!isAdded() || call.isCanceled()) return;
                Toast.makeText(requireContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePagination() {
        lastPage.setText(String.valueOf(totalPages));
        lastPage.setVisibility(totalPages > 3 ? View.VISIBLE : View.GONE);
        page2.setVisibility(totalPages >= 2 ? View.VISIBLE : View.GONE);
        page3.setVisibility(totalPages >= 3 ? View.VISIBLE : View.GONE);
        TextView[] pages = {page1, page2, page3, lastPage};
        int[] values = {1, 2, 3, totalPages};
        for (int i = 0; i < pages.length; i++) {
            boolean active = currentPage == values[i];
            pages[i].setBackgroundResource(active ? R.drawable.bg_admin_chip_active : R.drawable.bg_circle_button);
            pages[i].setTextColor(ContextCompat.getColor(requireContext(), active ? R.color.white : R.color.text_secondary));
        }
    }

    @Override
    public void onDestroyView() {
        if (currentCall != null) currentCall.cancel();
        searchHandler.removeCallbacksAndMessages(null);
        adapter = null;
        super.onDestroyView();
    }
}
