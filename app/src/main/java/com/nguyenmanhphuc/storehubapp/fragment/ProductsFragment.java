package com.nguyenmanhphuc.storehubapp.fragment;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import androidx.appcompat.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.nguyenmanhphuc.storehubapp.CartActivity;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.adapter.ProductAdapter;
import com.nguyenmanhphuc.storehubapp.model.Pagination;
import com.nguyenmanhphuc.storehubapp.model.Product;
import com.nguyenmanhphuc.storehubapp.model.Category;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.DataCache;
import androidx.appcompat.widget.PopupMenu;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.content.ContextCompat;

public class ProductsFragment extends Fragment {
    private RecyclerView rvProducts;
    private ImageView btnBack;
    private View btnCart;
    private TextView tvCartBadge;
    private ProductAdapter productAdapter;
    private TextInputEditText edtSearch;
    private NestedScrollView nestedScrollView;
    private ProgressBar progressBar, progressBarLoadMore;
    private MaterialButton btnPrice, btnAZ, btnZA;
    private ImageView btnFilterCategory;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final ArrayList<Product> allProducts = new ArrayList<>();
    private final ArrayList<Category> categoriesList = new ArrayList<>();
    private String currentCategory = "";

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private static final int LIMIT = 6;
    private static final long SEARCH_DEBOUNCE_MS = 400;

    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;
    private String currentSearchKeyword = "";
    private String currentSort = "";
    private Runnable searchRunnable;
    private Call<Response<ArrayList<Product>>> currentCall;
    private int loadGeneration;
    private static final String CACHE_KEY = "user_products";
    private static final String CACHE_PAGE_KEY = "user_products_current_page";
    private static final String CACHE_TOTAL_PAGES_KEY = "user_products_total_pages";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUi(view);
        setUpAdapter();
        setUpListener();
        loadCategories();

        // Nếu cache còn hợp lệ → hiện data ngay, không fetch lại
        ArrayList<Product> cached = DataCache.get().get(CACHE_KEY, ArrayList.class);
        if (cached != null && !cached.isEmpty()) {
            allProducts.clear();
            allProducts.addAll(cached);
            productAdapter.updateData(allProducts);
            Integer cachedPage = DataCache.get().get(CACHE_PAGE_KEY, Integer.class);
            Integer cachedTotalPages = DataCache.get().get(CACHE_TOTAL_PAGES_KEY, Integer.class);
            currentPage = cachedPage == null ? Math.max(1, (cached.size() + LIMIT - 1) / LIMIT) : cachedPage;
            totalPages = cachedTotalPages == null ? currentPage + 1 : Math.max(currentPage, cachedTotalPages);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (rvProducts != null) rvProducts.setVisibility(View.VISIBLE);
        } else {
            loadFirstPage();
        }
    }

    private void initUi(View view) {
        rvProducts = view.findViewById(R.id.rvProducts);
        btnBack = view.findViewById(R.id.btnBack);
        btnCart = view.findViewById(R.id.btnCartContainer);
        tvCartBadge = view.findViewById(R.id.tvCartBadge);
        edtSearch = view.findViewById(R.id.edtSearch);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        progressBar = view.findViewById(R.id.progressBar);
        progressBarLoadMore = view.findViewById(R.id.progressBarLoadMore);
        btnPrice = view.findViewById(R.id.btnPrice);
        btnAZ = view.findViewById(R.id.btnAZ);
        btnZA = view.findViewById(R.id.btnZA);
        btnFilterCategory = view.findViewById(R.id.btnFilterCategory);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.dark_green));
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // SwipeRefresh: xóa cache rồi fetch mới
                DataCache.get().invalidate(CACHE_KEY);
                loadFirstPage();
            });
        }
        updateSortButtons();
    }

    private void setUpAdapter() {
        productAdapter = new ProductAdapter(requireContext());
        if (rvProducts != null) {
            rvProducts.setAdapter(productAdapter);
        }
    }

    private void setUpListener() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }
        if (btnCart != null) {
            btnCart.setOnClickListener(v -> startActivity(new Intent(requireContext(), CartActivity.class)));
        }

        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                    searchRunnable = () -> {
                        String keyword = s.toString().trim();
                        if (!keyword.equals(currentSearchKeyword)) searchFromServer(keyword);
                    };
                    searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        btnPrice.setOnClickListener(v -> changeSort("price_asc"));
        btnAZ.setOnClickListener(v -> changeSort("name_asc"));
        btnZA.setOnClickListener(v -> changeSort("name_desc"));
        if (btnFilterCategory != null) {
            btnFilterCategory.setOnClickListener(v -> showCategoryFilterMenu(v));
        }

        if (nestedScrollView != null) {
            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
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
    }

    private void searchFromServer(String keyword) {
        cancelCurrentCall();
        currentSearchKeyword = keyword;
        currentPage = 1;
        isLoading = false;
        allProducts.clear();
        productAdapter.updateData(allProducts);
        fetchProducts(currentPage, keyword, false);
    }

    private void loadFirstPage() {
        cancelCurrentCall();
        currentPage = 1;
        isLoading = false;
        allProducts.clear();
        productAdapter.updateData(allProducts);
        fetchProducts(currentPage, "", false);
    }

    private void loadNextPage() {
        if (isLoading || currentPage >= totalPages) return;
        isLoading = true;
        fetchProducts(currentPage + 1, currentSearchKeyword, true);
    }

    private void changeSort(String sort) {
        currentSort = currentSort.equals(sort) ? "" : sort;
        updateSortButtons();
        loadFirstPage();
    }

    private void updateSortButtons() {
        updateSortButton(btnPrice, "price_asc");
        updateSortButton(btnAZ, "name_asc");
        updateSortButton(btnZA, "name_desc");
    }

    private void updateSortButton(MaterialButton button, String sort) {
        boolean selected = sort.equals(currentSort);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(selected ? "#F0DED2" : "#EEEDEA")));
        button.setTextColor(Color.parseColor(selected ? "#6B6157" : "#5D625F"));
    }

    private void fetchProducts(int page, String keyword, boolean isLoadMore) {
        if (!isLoadMore) {
            if (swipeRefreshLayout == null || !swipeRefreshLayout.isRefreshing()) {
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
                if (rvProducts != null) rvProducts.setVisibility(View.GONE);
            }
        } else {
            if (progressBarLoadMore != null) progressBarLoadMore.setVisibility(View.VISIBLE);
        }

        int requestGeneration = ++loadGeneration;

        HttpResquest request = new HttpResquest();
        currentCall = keyword.isEmpty()
                ? request.apiServices.getListProduct(page, LIMIT, currentCategory, false, currentSort)
                : request.apiServices.searchProduct(page, LIMIT, keyword, currentCategory, false, currentSort);

        currentCall.enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Product>>> call,
                                   @NonNull retrofit2.Response<Response<ArrayList<Product>>> response) {
                if (call.isCanceled() || productAdapter == null || requestGeneration != loadGeneration) return;

                if (response.isSuccessful() && response.body() != null
                        && response.body().getCode() == 200 && response.body().getData() != null) {
                    ArrayList<Product> products = response.body().getData();
                    Pagination pagination = response.body().getPagination();
                    showProducts(products, pagination, requestGeneration, isLoadMore);
                } else {
                    showLoadFailure(requestGeneration, getString(R.string.failed_to_load_products_toast));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;

                showLoadFailure(requestGeneration, getString(R.string.error_loading_products_toast));
            }
        });
    }

    private void showProducts(ArrayList<Product> products, Pagination pagination, int requestGeneration, boolean isLoadMore) {
        if (!isAdded() || productAdapter == null || requestGeneration != loadGeneration) return;

        if (!isLoadMore) {
            allProducts.clear();
        }
        allProducts.addAll(products);

        if (pagination != null) {
            totalPages = Math.max(1, pagination.getTotalPages());
            currentPage = pagination.getCurrentPage();
        } else {
            if (products.size() < LIMIT) {
                totalPages = currentPage;
            }
        }
        isLoading = false;

        if (currentSearchKeyword.isEmpty() && currentCategory.isEmpty() && currentSort.isEmpty()) {
            DataCache.get().put(CACHE_KEY, new ArrayList<>(allProducts));
            DataCache.get().put(CACHE_PAGE_KEY, currentPage);
            DataCache.get().put(CACHE_TOTAL_PAGES_KEY, totalPages);
        }

        productAdapter.updateData(allProducts);

        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (progressBarLoadMore != null) progressBarLoadMore.setVisibility(View.GONE);
        if (rvProducts != null) rvProducts.setVisibility(View.VISIBLE);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showLoadFailure(int requestGeneration, String message) {
        if (!isAdded() || requestGeneration != loadGeneration) return;
        isLoading = false;
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (progressBarLoadMore != null) progressBarLoadMore.setVisibility(View.GONE);
        if (rvProducts != null) rvProducts.setVisibility(View.VISIBLE);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
        Log.e("ProductsFragment", message);
    }

    private void cancelCurrentCall() {
        if (currentCall != null && !currentCall.isCanceled()) currentCall.cancel();
    }

    private void loadCategories() {
        new HttpResquest().callAPI().getCategories().enqueue(new Callback<Response<ArrayList<Category>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Category>>> call, @NonNull retrofit2.Response<Response<ArrayList<Category>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categoriesList.clear();
                    categoriesList.addAll(response.body().getData());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Category>>> call, @NonNull Throwable t) {
                Log.e("ProductsFragment", "Error loading categories", t);
            }
        });
    }

    private void showCategoryFilterMenu(View anchorView) {
        if (categoriesList.isEmpty()) {
            new HttpResquest().callAPI().getCategories().enqueue(new Callback<Response<ArrayList<Category>>>() {
                @Override
                public void onResponse(@NonNull Call<Response<ArrayList<Category>>> call, @NonNull retrofit2.Response<Response<ArrayList<Category>>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        categoriesList.clear();
                        categoriesList.addAll(response.body().getData());
                        displayCategoryFilterPopup(anchorView);
                    } else {
                        displayCategoryFilterPopup(anchorView);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Response<ArrayList<Category>>> call, @NonNull Throwable t) {
                    displayCategoryFilterPopup(anchorView);
                }
            });
        } else {
            displayCategoryFilterPopup(anchorView);
        }
    }

    private void displayCategoryFilterPopup(View anchorView) {
        if (!isAdded() || getContext() == null) return;

        View popupView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_category_filter, null);
        LinearLayout layoutCategoriesList = popupView.findViewById(R.id.layoutCategoriesList);

        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        // Add "Tất cả danh mục" item
        View allItemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_dialog_category, layoutCategoriesList, false);
        TextView tvAllName = allItemView.findViewById(R.id.tvCategoryName);
        ImageView ivAllCheck = allItemView.findViewById(R.id.ivCheck);
        tvAllName.setText(getString(R.string.all_categories));
        boolean allSelected = currentCategory.isEmpty();
        ivAllCheck.setVisibility(allSelected ? View.VISIBLE : View.GONE);
        if (allSelected) {
            allItemView.setBackgroundColor(Color.parseColor("#F0DED2"));
            tvAllName.setTextColor(Color.parseColor("#6B6157"));
        }
        allItemView.setOnClickListener(v -> {
            currentCategory = "";
            loadFirstPage();
            popupWindow.dismiss();
        });
        layoutCategoriesList.addView(allItemView);

        // Add each category item
        for (Category category : categoriesList) {
            View itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_dialog_category, layoutCategoriesList, false);
            TextView tvName = itemView.findViewById(R.id.tvCategoryName);
            ImageView ivCheck = itemView.findViewById(R.id.ivCheck);
            
            tvName.setText(getLocalizedCategoryName(category.getName()));
            boolean isSelected = category.get_id().equals(currentCategory);
            ivCheck.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            if (isSelected) {
                itemView.setBackgroundColor(Color.parseColor("#F0DED2"));
                tvName.setTextColor(Color.parseColor("#6B6157"));
            }

            itemView.setOnClickListener(v -> {
                currentCategory = category.get_id();
                loadFirstPage();
                popupWindow.dismiss();
            });
            layoutCategoriesList.addView(itemView);
        }

        popupWindow.showAsDropDown(anchorView, 0, 10);
    }

    private String getLocalizedCategoryName(String rawName) {
        if (rawName == null) return "";
        String lower = rawName.trim().toLowerCase();
        if (lower.contains("điện thoại") || lower.contains("phone")) {
            return getString(R.string.category_phones);
        } else if (lower.contains("máy tính") || lower.contains("computer") || lower.contains("laptop")) {
            return getString(R.string.category_computers);
        } else if (lower.contains("tai nghe") || lower.contains("headphone") || lower.contains("earphone")) {
            return getString(R.string.category_headphones);
        } else if (lower.contains("đồng hồ") || lower.contains("watch")) {
            return getString(R.string.category_watches);
        }
        return rawName;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchCartCount();
    }

    private void fetchCartCount() {
        com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager prefManager = new com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager(requireContext());
        if (!prefManager.isLoggedIn()) {
            if (tvCartBadge != null) tvCartBadge.setVisibility(View.GONE);
            return;
        }
        new HttpResquest().apiServices.getCart(HttpResquest.authorizationHeader(requireContext())).enqueue(new Callback<Response<java.util.ArrayList<com.nguyenmanhphuc.storehubapp.model.CartItem>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<java.util.ArrayList<com.nguyenmanhphuc.storehubapp.model.CartItem>>> call, @NonNull retrofit2.Response<Response<java.util.ArrayList<com.nguyenmanhphuc.storehubapp.model.CartItem>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    int count = 0;
                    for (com.nguyenmanhphuc.storehubapp.model.CartItem item : response.body().getData()) {
                        count += item.getQuantity();
                    }
                    updateCartBadge(count);
                } else {
                    updateCartBadge(0);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<java.util.ArrayList<com.nguyenmanhphuc.storehubapp.model.CartItem>>> call, @NonNull Throwable t) {
                updateCartBadge(0);
            }
        });
    }

    private void updateCartBadge(int count) {
        if (tvCartBadge != null) {
            if (count > 0) {
                tvCartBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                tvCartBadge.setVisibility(View.VISIBLE);
            } else {
                tvCartBadge.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        cancelCurrentCall();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        productAdapter = null;
        edtSearch = null;
        btnFilterCategory = null;
        btnCart = null;
        tvCartBadge = null;
        super.onDestroyView();
    }
}
