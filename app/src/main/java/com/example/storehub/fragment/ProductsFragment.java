package com.example.storehub.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.storehub.MainActivity;
import com.example.storehub.R;
import com.example.storehub.adapter.ProductAdapter;
import com.example.storehub.model.Product;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class ProductsFragment extends Fragment {
    private RecyclerView rvProducts;
    private MaterialToolbar toolbar;
    private NestedScrollView nestedScrollView;
    private ProductAdapter productAdapter;
    private TextInputEditText edtSearch;

    private final ArrayList<Product> allProducts = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private static final int LIMIT = 6;
    private static final long SEARCH_DEBOUNCE_MS = 400;
    private int currentPage = 1;
    private boolean isLoading;
    private boolean isLastPage;
    private String currentSearchKeyword = "";
    private Runnable searchRunnable;
    private Call<Response<ArrayList<Product>>> currentCall;

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

        loadFirstPage();
    }

    private void initUi(View view) {
        rvProducts = view.findViewById(R.id.rvProducts);
        toolbar = view.findViewById(R.id.toolbar);
        edtSearch = view.findViewById(R.id.edtSearch);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);
    }

    private void setUpAdapter() {
        productAdapter = new ProductAdapter(requireContext());
        if (rvProducts != null) {
            rvProducts.setAdapter(productAdapter);
        }
    }

    private void setUpListener() {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> ((MainActivity) requireActivity()).showHome());
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_receipt) {
                    ((MainActivity) requireActivity()).showCart();
                    return true;
                }
                return false;
            });
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

        if (nestedScrollView != null) {
            nestedScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                    (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                        if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()
                                && !isLoading && !isLastPage) {
                            loadMoreProducts();
                        }
                    });
        }
    }

    private void searchFromServer(String keyword) {
        cancelCurrentCall();
        currentSearchKeyword = keyword;
        currentPage = 1;
        isLastPage = false;
        allProducts.clear();
        productAdapter.updateData(allProducts);
        fetchProducts(currentPage, keyword);
    }

    private void loadFirstPage() {
        cancelCurrentCall();
        currentPage = 1;
        isLastPage = false;
        allProducts.clear();
        productAdapter.updateData(allProducts);
        fetchProducts(currentPage, "");
    }

    private void loadMoreProducts() {
        fetchProducts(++currentPage, currentSearchKeyword);
    }

    private void fetchProducts(int page, String keyword) {
        isLoading = true;
        HttpResquest request = new HttpResquest();
        currentCall = keyword.isEmpty()
                ? request.apiServices.getListProduct(page, LIMIT, "")
                : request.apiServices.searchProduct(page, LIMIT, keyword, "");

        currentCall.enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Product>>> call,
                                   @NonNull retrofit2.Response<Response<ArrayList<Product>>> response) {
                if (call.isCanceled() || productAdapter == null) return;
                isLoading = false;
                if (response.isSuccessful() && response.body() != null
                        && response.body().getCode() == 200 && response.body().getData() != null) {
                    ArrayList<Product> newProducts = response.body().getData();
                    allProducts.addAll(newProducts);
                    isLastPage = response.body().getPagination() != null
                            ? currentPage >= response.body().getPagination().getTotalPages()
                            : newProducts.size() < LIMIT;
                    productAdapter.updateData(allProducts);
                } else {
                    Log.e("ProductsFragment", "Không thể tải danh sách sản phẩm");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;
                isLoading = false;
                Log.e("ProductsFragment", "Lỗi tải sản phẩm", t);
            }
        });
    }

    private void cancelCurrentCall() {
        if (currentCall != null && !currentCall.isCanceled()) currentCall.cancel();
    }

    @Override
    public void onDestroyView() {
        cancelCurrentCall();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        productAdapter = null;
        edtSearch = null;
        super.onDestroyView();
    }
}
