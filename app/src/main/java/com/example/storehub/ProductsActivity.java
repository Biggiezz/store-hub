package com.example.storehub;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.widget.NestedScrollView;

import android.text.Editable;
import android.text.TextWatcher;
import com.example.storehub.adapter.ProductAdapter;
import com.example.storehub.model.Product;
import com.example.storehub.model.Response;
import com.example.storehub.model.Pagination;
import com.example.storehub.services.HttpResquest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;

public class ProductsActivity extends AppCompatActivity {
    private RecyclerView rvProducts;
    private ProductAdapter productAdapter;
    private TextInputEditText edtSearch;
    private NestedScrollView nestedScrollView;

    private ArrayList<Product> allProducts = new ArrayList<>();
    private int currentPage = 1;
    private final int limit = 6;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_products);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_products), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initUi();
        productAdapter = new ProductAdapter(this);
        rvProducts.setAdapter(productAdapter);
    }


    private void initUi() {
        rvProducts = findViewById(R.id.rvProducts);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        findViewById(R.id.btnHome).setOnClickListener(v -> finish());

        edtSearch = findViewById(R.id.edtSearch);
        if (edtSearch != null) {
            edtSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        nestedScrollView = findViewById(R.id.nestedScrollView);
        if (nestedScrollView != null) {
            nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                        if (!isLoading && !isLastPage) {
                            loadMoreProducts();
                        }
                    }
                }
            });
        }
    }

    private void filter(String text) {
        if (text == null) text = "";
        ArrayList<Product> filteredList = new ArrayList<>();
        for (Product item : allProducts) {
            if (item.getName() != null && item.getName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        productAdapter.updateData(filteredList);
    }

    private void loadFirstPage() {
        currentPage = 1;
        isLastPage = false;
        allProducts.clear();
        productAdapter.updateData(allProducts);
        fetchProducts(currentPage, limit);
    }

    private void loadMoreProducts() {
        isLoading = true;
        currentPage++;
        fetchProducts(currentPage, limit);
    }

    private void fetchProducts(int page, int limit) {
        isLoading = true;
        HttpResquest httpResquest = new HttpResquest();
        httpResquest.apiServices.getListProduct(page, limit).enqueue(new Callback<Response<ArrayList<Product>>>() {
            @Override
            public void onResponse(Call<Response<ArrayList<Product>>> call, retrofit2.Response<Response<ArrayList<Product>>> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    Response<ArrayList<Product>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        ArrayList<Product> newProducts = apiResponse.getData();
                        allProducts.addAll(newProducts);
                        
                        if (apiResponse.getPagination() != null) {
                            int totalPages = apiResponse.getPagination().getTotalPages();
                            if (currentPage >= totalPages) {
                                isLastPage = true;
                            }
                        } else {
                            if (newProducts.size() < limit) {
                                isLastPage = true;
                            }
                        }

                        if (edtSearch != null && edtSearch.getText() != null && edtSearch.getText().length() > 0) {
                            filter(edtSearch.getText().toString());
                        } else {
                            productAdapter.updateData(allProducts);
                        }
                        Log.e("ProductsActivity", "Product List: " + allProducts);

                    } else {
                        Log.e("ProductsActivity", "Server response error: " + apiResponse.getMessage());
                    }
                } else {
                    Log.e("ProductsActivity", "Failed to fetch products: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Response<ArrayList<Product>>> call, Throwable t) {
                isLoading = false;
                Log.e("ProductsActivity", "Error fetching products", t);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFirstPage();
    }
}
