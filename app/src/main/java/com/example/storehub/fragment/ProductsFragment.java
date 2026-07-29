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
import android.widget.TextView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.example.storehub.MainActivity;
import com.example.storehub.R;
import com.example.storehub.adapter.ProductAdapter;
import com.example.storehub.model.Product;
import com.example.storehub.model.response.Response;
import com.example.storehub.services.HttpResquest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import retrofit2.Call;
import retrofit2.Callback;

public class ProductsFragment extends Fragment {
    private RecyclerView rvProducts;
    private MaterialToolbar toolbar;
    private ProductAdapter productAdapter;
    private TextInputEditText edtSearch;
    private final ArrayList<Product> allProducts = new ArrayList<>();
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private static final int LIMIT = 6;
    private static final long SEARCH_DEBOUNCE_MS = 400;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSearchKeyword = "";
    private Runnable searchRunnable;
    private Call<Response<ArrayList<Product>>> currentCall;
    private int loadGeneration;
    private TextView page1, page2, page3, lastPage, tvEllipsis;
    private ProgressBar progressBar;
    private View layoutPagination;

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
        setUpListener(view);

        loadFirstPage();
    }

    private void initUi(View view) {
        rvProducts = view.findViewById(R.id.rvProducts);
        toolbar = view.findViewById(R.id.toolbar);
        edtSearch = view.findViewById(R.id.edtSearch);
        page1 = view.findViewById(R.id.btnPage1);
        page2 = view.findViewById(R.id.btnPage2);
        page3 = view.findViewById(R.id.btnPage3);
        lastPage = view.findViewById(R.id.tvLastPage);
        tvEllipsis = view.findViewById(R.id.tvEllipsis);
        progressBar = view.findViewById(R.id.progressBar);
        layoutPagination = view.findViewById(R.id.layoutPagination);
    }

    private void setUpAdapter() {
        productAdapter = new ProductAdapter(requireContext());
        if (rvProducts != null) {
            rvProducts.setAdapter(productAdapter);
        }
    }

    private void setUpListener(View view) {
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> ((MainActivity) requireActivity()).showHome());
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_receipt) {
                    ((MainActivity) requireActivity()).showOder();
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

        if (page1 != null) {
            page1.setOnClickListener(v -> {
                try {
                    goToPage(Integer.parseInt(page1.getText().toString()));
                } catch (Exception ignored) {}
            });
        }
        if (page2 != null) {
            page2.setOnClickListener(v -> {
                try {
                    goToPage(Integer.parseInt(page2.getText().toString()));
                } catch (Exception ignored) {}
            });
        }
        if (page3 != null) {
            page3.setOnClickListener(v -> {
                try {
                    goToPage(Integer.parseInt(page3.getText().toString()));
                } catch (Exception ignored) {}
            });
        }
        if (lastPage != null) {
            lastPage.setOnClickListener(v -> goToPage(totalPages));
        }

        View prevBtn = view.findViewById(R.id.btnPreviousPage);
        if (prevBtn != null) {
            prevBtn.setOnClickListener(v -> goToPage(currentPage - 1));
        }

        View nextBtn = view.findViewById(R.id.btnNextPage);
        if (nextBtn != null) {
            nextBtn.setOnClickListener(v -> goToPage(currentPage + 1));
        }
    }

    private void searchFromServer(String keyword) {
        cancelCurrentCall();
        currentSearchKeyword = keyword;
        currentPage = 1;
        allProducts.clear();
        productAdapter.updateData(allProducts);
        fetchProducts(currentPage, keyword);
    }

    private void loadFirstPage() {
        cancelCurrentCall();
        currentPage = 1;
        allProducts.clear();
        productAdapter.updateData(allProducts);
        fetchProducts(currentPage, "");
    }

    private void goToPage(int page) {
        if (page < 1 || page > totalPages || page == currentPage) return;
        currentPage = page;
        allProducts.clear();
        productAdapter.updateData(allProducts);
        fetchProducts(currentPage, currentSearchKeyword);
    }

    private void fetchProducts(int page, String keyword) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (rvProducts != null) rvProducts.setVisibility(View.GONE);
        if (layoutPagination != null) layoutPagination.setVisibility(View.GONE);

        int requestGeneration = ++loadGeneration;

        HttpResquest request = new HttpResquest();
        currentCall = keyword.isEmpty()
                ? request.apiServices.getListProduct(page, LIMIT, "", "active")
                : request.apiServices.searchProduct(page, LIMIT, keyword, "", "active");

        currentCall.enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(@NonNull Call<Response<ArrayList<Product>>> call,
                                   @NonNull retrofit2.Response<Response<ArrayList<Product>>> response) {
                if (call.isCanceled() || productAdapter == null || requestGeneration != loadGeneration) return;

                if (response.isSuccessful() && response.body() != null
                        && response.body().getCode() == 200 && response.body().getData() != null) {
                    ArrayList<Product> products = response.body().getData();
                    com.example.storehub.model.Pagination pagination = response.body().getPagination();
                    preloadPageImages(products, requestGeneration,
                            () -> showProducts(products, pagination, requestGeneration));
                } else {
                    showLoadFailure(requestGeneration, "Không thể tải danh sách sản phẩm", null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<ArrayList<Product>>> call, @NonNull Throwable t) {
                if (call.isCanceled()) return;

                showLoadFailure(requestGeneration, "Lỗi tải sản phẩm", t);
            }
        });
    }

    private void preloadPageImages(ArrayList<Product> products, int requestGeneration, Runnable onComplete) {
        if (products.isEmpty()) {
            onComplete.run();
            return;
        }

        int[] remaining = {products.size()};
        for (Product product : products) {
            Glide.with(this)
                    .load(product.getImage())
                    .placeholder(R.drawable.ic_product)
                    .error(R.drawable.ic_product)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            finishImagePreload();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target,
                                                       com.bumptech.glide.load.DataSource dataSource,
                                                       boolean isFirstResource) {
                            finishImagePreload();
                            return false;
                        }

                        private void finishImagePreload() {
                            if (requestGeneration == loadGeneration && --remaining[0] == 0) {
                                onComplete.run();
                            }
                        }
                    })
                    .preload();
        }
    }

    private void showProducts(ArrayList<Product> products,
                              com.example.storehub.model.Pagination pagination,
                              int requestGeneration) {
        if (!isAdded() || productAdapter == null || requestGeneration != loadGeneration) return;
        allProducts.clear();
        allProducts.addAll(products);
        totalPages = pagination == null ? 1 : Math.max(1, pagination.getTotalPages());
        currentPage = pagination == null ? 1 : pagination.getCurrentPage();
        productAdapter.updateData(allProducts);
        updatePagination();
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (rvProducts != null) rvProducts.setVisibility(View.VISIBLE);
        if (layoutPagination != null) layoutPagination.setVisibility(View.VISIBLE);
    }

    private void showLoadFailure(int requestGeneration, String message, Throwable error) {
        if (!isAdded() || requestGeneration != loadGeneration) return;
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (rvProducts != null) rvProducts.setVisibility(View.VISIBLE);
        if (layoutPagination != null) layoutPagination.setVisibility(View.VISIBLE);
        Log.e("ProductsFragment", message, error);
    }

    private void updatePagination() {
        if (lastPage == null) return;
        lastPage.setText(String.valueOf(totalPages));
        lastPage.setVisibility(totalPages > 3 ? View.VISIBLE : View.GONE);
        
        int p1, p2, p3;
        if (totalPages <= 3) {
            p1 = 1;
            p2 = 2;
            p3 = 3;
            if (page2 != null) page2.setVisibility(totalPages >= 2 ? View.VISIBLE : View.GONE);
            if (page3 != null) page3.setVisibility(totalPages >= 3 ? View.VISIBLE : View.GONE);
        } else {
            if (page2 != null) page2.setVisibility(View.VISIBLE);
            if (page3 != null) page3.setVisibility(View.VISIBLE);
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
        
        if (page1 != null) page1.setText(String.valueOf(p1));
        if (page2 != null) page2.setText(String.valueOf(p2));
        if (page3 != null) page3.setText(String.valueOf(p3));

        if (tvEllipsis != null) {
            tvEllipsis.setVisibility(totalPages > p3 + 1 ? View.VISIBLE : View.GONE);
        }

        TextView[] pages = {page1, page2, page3, lastPage};
        for (TextView tv : pages) {
            if (tv == null) continue;
            try {
                int val = Integer.parseInt(tv.getText().toString());
                boolean active = currentPage == val;
                tv.setBackgroundResource(active ? R.drawable.bg_admin_chip_active : R.drawable.bg_circle_button);
                tv.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), active ? R.color.white : R.color.text_secondary));
            } catch (Exception ignored) {}
        }
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
