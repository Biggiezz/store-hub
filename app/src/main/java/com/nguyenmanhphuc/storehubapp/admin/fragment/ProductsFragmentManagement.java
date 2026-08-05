package com.nguyenmanhphuc.storehubapp.admin.fragment;

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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;
import java.util.List;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nguyenmanhphuc.storehubapp.admin.AdminProductDetailActivity;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.admin.ProductFormManagementActivity;
import com.nguyenmanhphuc.storehubapp.admin.adapter.AdminProductAdapter;
import com.nguyenmanhphuc.storehubapp.model.Pagination;
import com.nguyenmanhphuc.storehubapp.model.Product;
import com.nguyenmanhphuc.storehubapp.model.Category;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
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
    private LinearLayout layoutAdminChips;
    private TextView page1, page2, page3, lastPage, tvEllipsis;
    private int currentPage = 1;
    private int totalPages = 1;
    private String selectedCategory = "";
    private ProgressBar progressBar;
    private View layoutPagination;
    private RecyclerView grid;
    private List<Category> categoriesList = new ArrayList<>();
    private final ArrayList<TextView> dynamicChips = new ArrayList<>();
    private final ArrayList<String> dynamicChipValues = new ArrayList<>();
    private final ArrayList<Product> allProducts = new ArrayList<>();
    private boolean isLoading = false;
    private ProgressBar progressBarLoadMore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        grid = view.findViewById(R.id.rvAdminProducts);
        grid.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        grid.setNestedScrollingEnabled(false);
        adapter = new AdminProductAdapter(product -> {
            Intent intent = new Intent(requireContext(), AdminProductDetailActivity.class);
            String pid = product.get_id();
            intent.putExtra(AdminProductDetailActivity.EXTRA_PRODUCT_ID, pid);
            startActivity(intent);
        });
        grid.setAdapter(adapter);

        searchInput = view.findViewById(R.id.edtAdminProductSearch);
        layoutAdminChips = view.findViewById(R.id.layoutAdminChips);
        page1 = view.findViewById(R.id.btnAdminPage1);
        page2 = view.findViewById(R.id.btnAdminPage2);
        page3 = view.findViewById(R.id.btnAdminPage3);
        lastPage = view.findViewById(R.id.tvAdminLastPage);
        tvEllipsis = view.findViewById(R.id.tvAdminEllipsis);
        progressBar = view.findViewById(R.id.progressBar);
        progressBarLoadMore = view.findViewById(R.id.progressBarLoadMore);
        layoutPagination = view.findViewById(R.id.layoutPagination);
        if (layoutPagination != null) {
            layoutPagination.setVisibility(View.GONE);
        }
 
        loadCategoriesFromServer();
 
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHandler.removeCallbacksAndMessages(null);
                searchHandler.postDelayed(() -> { currentPage = 1; loadProducts(); }, 350);
            }
            @Override public void afterTextChanged(Editable s) { }
        });
 
        androidx.core.widget.NestedScrollView nestedScrollView = view.findViewById(R.id.nestedScrollViewAdminProducts);
        if (nestedScrollView != null) {
            nestedScrollView.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                if (scrollY > oldScrollY && !isLoading && currentPage < totalPages) {
                    if (v.getChildAt(0) != null) {
                        int diff = v.getChildAt(0).getMeasuredHeight() - (v.getHeight() + scrollY);
                        if (diff <= 400) {
                            loadNextPage();
                        }
                    }
                }
            });
        }
 
        FloatingActionButton add = view.findViewById(R.id.fabAddProduct);
        add.setOnClickListener(v -> startActivity(ProductFormManagementActivity.createAddIntent(requireContext())));
    }

    @Override public void onResume() { super.onResume(); loadProducts(); }

    private void loadCategoriesFromServer() {
        new HttpResquest().callAPI().getCategories().enqueue(new Callback<Response<ArrayList<Category>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Category>>> call, @NonNull retrofit2.Response<Response<ArrayList<Category>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categoriesList = response.body().getData();
                    renderAdminChips(categoriesList);
                } else {
                    useFallbackCategories();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Category>>> call, @NonNull Throwable t) {
                useFallbackCategories();
            }
        });
    }

    private void useFallbackCategories() {
        categoriesList = java.util.Arrays.asList(
                new Category("1", "Điện thoại"),
                new Category("2", "Máy tính"),
                new Category("3", "Tai nghe"),
                new Category("4", "Đồng hồ")
        );
        renderAdminChips(categoriesList);
    }

    private void renderAdminChips(List<Category> categories) {
        if (layoutAdminChips == null || !isAdded()) return;
        layoutAdminChips.removeAllViews();
        dynamicChips.clear();
        dynamicChipValues.clear();

        // Thêm chip "Tất cả" đầu tiên
        addChip(getString(R.string.all), "");

        // Thêm các chip danh mục động
        for (Category category : categories) {
            addChip(category.getName(), category.get_id());
        }

        updateCategoryChips();
    }

    private void addChip(String label, String value) {
        TextView chip = new TextView(requireContext(), null, 0, R.style.AdminChipStyle);
        chip.setText(label);

        // Đặt LayoutParams và margins
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 20, 0);
        chip.setLayoutParams(params);

        chip.setOnClickListener(v -> {
            selectedCategory = value;
            currentPage = 1;
            updateCategoryChips();
            loadProducts();
        });

        layoutAdminChips.addView(chip);
        dynamicChips.add(chip);
        dynamicChipValues.add(value);
    }

    private void updateCategoryChips() {
        if (!isAdded()) return;
        for (int i = 0; i < dynamicChips.size(); i++) {
            TextView chip = dynamicChips.get(i);
            String value = dynamicChipValues.get(i);
            boolean active = value.equals(selectedCategory);

            chip.setBackgroundResource(active ? R.drawable.bg_admin_chip_active : R.drawable.bg_admin_chip);
            chip.setTextColor(ContextCompat.getColor(requireContext(),
                    active ? R.color.white : R.color.text_secondary));
        }
    }

    private void loadNextPage() {
        if (isLoading || currentPage >= totalPages) return;
        isLoading = true;
        currentPage++;
        loadProducts();
    }

    private void loadProducts() {
        if (!isAdded() || adapter == null) return;
        if (currentCall != null) currentCall.cancel();
        
        if (currentPage == 1) {
            allProducts.clear();
            adapter.submitList(allProducts);
        }
        isLoading = true;
 
        // Chỉ hiện Loading Spinner và ẩn danh sách ở lần đầu tiên tải (khi danh sách đang trống)
        boolean isFirstLoad = allProducts.isEmpty();
        if (isFirstLoad) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            if (grid != null) grid.setVisibility(View.GONE);
        } else {
            if (progressBarLoadMore != null) progressBarLoadMore.setVisibility(View.VISIBLE);
        }
        if (layoutPagination != null) {
            layoutPagination.setVisibility(View.GONE);
        }
 
        String keyword = searchInput == null ? "" : searchInput.getText().toString().trim();
        HttpResquest request = new HttpResquest();
        currentCall = keyword.isEmpty()
                ? request.callAPI().getListProduct(currentPage, PAGE_SIZE, selectedCategory, true, "")
                : request.callAPI().searchProduct(currentPage, PAGE_SIZE, keyword, selectedCategory, true, "");
        currentCall.enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull retrofit2.Response<Response<ArrayList<Product>>> response) {
                if (!isAdded() || call.isCanceled()) return;
                isLoading = false;
 
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (progressBarLoadMore != null) progressBarLoadMore.setVisibility(View.GONE);
                if (grid != null) grid.setVisibility(View.VISIBLE);
 
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<Product> newData = response.body().getData();
                    if (newData != null) {
                        allProducts.addAll(newData);
                        adapter.submitList(allProducts);
                    }
                    Pagination pagination = response.body().getPagination();
                    totalPages = pagination == null ? 1 : Math.max(1, pagination.getTotalPages());
                    currentPage = pagination == null ? 1 : pagination.getCurrentPage();
                } else {
                    Toast.makeText(requireContext(), "Không thể tải danh sách sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }
 
            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull Throwable throwable) {
                if (!isAdded() || call.isCanceled()) return;
                isLoading = false;
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (progressBarLoadMore != null) progressBarLoadMore.setVisibility(View.GONE);
                if (grid != null) grid.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), "Lỗi kết nối server", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePagination() {
        lastPage.setText(String.valueOf(totalPages));
        lastPage.setVisibility(totalPages > 3 ? View.VISIBLE : View.GONE);
        
        int p1, p2, p3;
        if (totalPages <= 3) {
            p1 = 1;
            p2 = 2;
            p3 = 3;
            page2.setVisibility(totalPages >= 2 ? View.VISIBLE : View.GONE);
            page3.setVisibility(totalPages >= 3 ? View.VISIBLE : View.GONE);
        } else {
            page2.setVisibility(View.VISIBLE);
            page3.setVisibility(View.VISIBLE);
            if (currentPage <= 2) {
                p1 = 1;
                p2 = 2;
                p3 = 3;
            } else if (currentPage >= totalPages - 1) {
                p1 = totalPages - 3;
                p2 = totalPages - 2;
                p3 = totalPages - 1;
            } else {
                p1 = currentPage - 1;
                p2 = currentPage;
                p3 = currentPage + 1;
            }
        }
        
        page1.setText(String.valueOf(p1));
        page2.setText(String.valueOf(p2));
        page3.setText(String.valueOf(p3));

        if (tvEllipsis != null) {
            tvEllipsis.setVisibility(totalPages > p3 + 1 ? View.VISIBLE : View.GONE);
        }

        TextView[] pages = {page1, page2, page3, lastPage};
        for (TextView tv : pages) {
            try {
                int val = Integer.parseInt(tv.getText().toString());
                boolean active = currentPage == val;
                tv.setBackgroundResource(active ? R.drawable.bg_admin_chip_active : R.drawable.bg_circle_button);
                tv.setTextColor(ContextCompat.getColor(requireContext(), active ? R.color.white : R.color.text_secondary));
            } catch (Exception ignored) {}
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
