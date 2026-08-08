package com.nguyenmanhphuc.storehubapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.admin.adapter.AdminColorAdapter;
import com.nguyenmanhphuc.storehubapp.model.Product;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.appcompat.app.AlertDialog;

import java.text.NumberFormat;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

public class AdminProductDetailActivity extends AppCompatActivity {
    public static final String EXTRA_PRODUCT_ID = "product_id";
    private ImageView ivProductImage;
    private ImageButton btnBack;
    private TextView tvCategory, tvProductName, tvProductPrice, tvRatingText, tvStockValue, tvSoldValue, tvDescription;
    private RatingBar ratingBar;
    private SwitchMaterial switchStatus;
    private RecyclerView rvColors;
    private MaterialButton btnEditProduct, btnDeleteProduct;
    private String productId;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_detail);

        initUi();
        
        if (getIntent() != null) {
            productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        }

        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, this.getString(R.string.toast_khong_tim_thay_ma_san_pham), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setUpListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProductDetail();
    }

    private void initUi() {
        ivProductImage = findViewById(R.id.ivProductImage);
        btnBack = findViewById(R.id.btnBack);
        tvCategory = findViewById(R.id.tvCategory);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvRatingText = findViewById(R.id.tvRatingText);
        tvStockValue = findViewById(R.id.tvStockValue);
        tvSoldValue = findViewById(R.id.tvSoldValue);
        tvDescription = findViewById(R.id.tvDescription);
        ratingBar = findViewById(R.id.ratingBar);
        switchStatus = findViewById(R.id.switchStatus);
        rvColors = findViewById(R.id.rvColors);
        btnEditProduct = findViewById(R.id.btnEditProduct);
        btnDeleteProduct = findViewById(R.id.btnDeleteProduct);

        SharedPreferencesManager prefManager = new SharedPreferencesManager(this);
        User currentUser = prefManager.getUser();
        if (currentUser != null && currentUser.isSuperAdmin()) {
            btnDeleteProduct.setVisibility(View.VISIBLE);
        } else {
            btnDeleteProduct.setVisibility(View.GONE);
        }

        rvColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void setUpListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnEditProduct.setOnClickListener(v -> {
            if (currentProduct != null) {
                Intent intent = ProductFormManagementActivity.createEditIntent(this, productId);
                startActivity(intent);
            }
        });

        btnDeleteProduct.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.confirm_delete_title))
                    .setMessage(getString(R.string.confirm_delete_product_msg))
                    .setPositiveButton(getString(R.string.str_delete), (dialog, which) -> {
                        performDeleteProduct();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });

        switchStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateSwitchColors(isChecked);
            if (currentProduct != null && currentProduct.isActive() != isChecked) {
                currentProduct.setActive(isChecked);
                updateProductOnServer(isChecked);
            }
        });
    }

    private void performDeleteProduct() {
        SharedPreferencesManager prefManager = new SharedPreferencesManager(this);
        String token = "Bearer " + prefManager.getToken();
        HttpResquest request = new HttpResquest();
        request.callAPI().deleteProduct(token, productId).enqueue(new Callback<Response<Void>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Void>> call, @NonNull retrofit2.Response<Response<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminProductDetailActivity.this, AdminProductDetailActivity.this.getString(R.string.toast_product_deleted_success), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AdminProductDetailActivity.this, AdminProductDetailActivity.this.getString(R.string.toast_khong_the_xoa_san_pham), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Void>> call, @NonNull Throwable t) {
                Toast.makeText(AdminProductDetailActivity.this, AdminProductDetailActivity.this.getString(R.string.toast_loi_ket_noi), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProductOnServer(boolean status) {
        if (currentProduct == null) return;

        MediaType textType = MediaType.parse("text/plain");
        RequestBody name = RequestBody.create(textType, currentProduct.getName());
        RequestBody price = RequestBody.create(textType, currentProduct.getPrice());
        RequestBody category = RequestBody.create(textType, currentProduct.getCategory() != null ? currentProduct.getCategory().get_id() : "");
        RequestBody description = RequestBody.create(textType, currentProduct.getDescription());
        RequestBody stock = RequestBody.create(textType, String.valueOf(currentProduct.getStock()));
        RequestBody soldQuantity = RequestBody.create(textType, String.valueOf(currentProduct.getSold()));
        RequestBody statusBody = RequestBody.create(textType, String.valueOf(status));
        RequestBody colors = RequestBody.create(textType, new com.google.gson.Gson().toJson(currentProduct.getColors()));
        okhttp3.MultipartBody.Part imagePart = null;

        HttpResquest request = new HttpResquest();
        String token = HttpResquest.authorizationHeader(this);
        request.callAPI().updateProduct(token, productId, name, price, category, description, stock, soldQuantity, statusBody, colors, imagePart)
                .enqueue(new Callback<Response<Product>>() {
                    @Override
                    public void onResponse(@NonNull Call<Response<Product>> call, @NonNull retrofit2.Response<Response<Product>> response) {
                        if (response.isSuccessful()) {
                            String msg = status ? getString(R.string.toast_product_switch_active) : getString(R.string.toast_product_switch_inactive);
                            Toast.makeText(AdminProductDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminProductDetailActivity.this, AdminProductDetailActivity.this.getString(R.string.toast_khong_the_cap_nhat_trang_thai), Toast.LENGTH_SHORT).show();
                            // Rollback UI
                            switchStatus.setChecked(!status);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Response<Product>> call, @NonNull Throwable t) {
                        Toast.makeText(AdminProductDetailActivity.this, AdminProductDetailActivity.this.getString(R.string.toast_loi_ket_noi), Toast.LENGTH_SHORT).show();
                        switchStatus.setChecked(!status);
                    }
                });
    }

    private void updateSwitchColors(boolean isChecked) {
        int color = isChecked ?
                androidx.core.content.ContextCompat.getColor(this, R.color.dark_green) :
                androidx.core.content.ContextCompat.getColor(this, R.color.error_red);

        switchStatus.setThumbTintList(android.content.res.ColorStateList.valueOf(color));
        switchStatus.setTrackTintList(android.content.res.ColorStateList.valueOf(color));
        switchStatus.setText(isChecked ? getString(R.string.status_active) : getString(R.string.status_inactive));
    }

    private void loadProductDetail() {
        HttpResquest request = new HttpResquest();
        request.callAPI().getProductDetail(productId).enqueue(new Callback<Response<Product>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Product>> call, @NonNull retrofit2.Response<Response<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body().getData();
                    bindData(currentProduct);
                } else {
                    Toast.makeText(AdminProductDetailActivity.this, AdminProductDetailActivity.this.getString(R.string.toast_loi_tai_thong_tin_san_pham), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Product>> call, @NonNull Throwable t) {
                Log.e("AdminProductDetail", "onFailure: ", t);
                Toast.makeText(AdminProductDetailActivity.this, AdminProductDetailActivity.this.getString(R.string.toast_loi_ket_noi), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindData(Product product) {
        if (product == null) return;

        tvProductName.setText(product.getName());
        tvCategory.setText(product.getCategory() != null ? product.getCategory().getName() : "");
        
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvProductPrice.setText(formatter.format(product.getPriceAsLong()) + getString(R.string.currency_suffix));
        
        tvDescription.setText(product.getDescription());
        tvStockValue.setText(String.valueOf(product.getStock()));
        tvSoldValue.setText(String.valueOf(product.getSold()));
        
        ratingBar.setRating(product.getRating());
        tvRatingText.setText(String.format(Locale.getDefault(), getString(R.string.rating_format), product.getRating(), product.getReviewCount()));

        Glide.with(this)
                .load(product.getImage())
                .placeholder(R.drawable.ic_products)
                .error(R.drawable.ic_products)
                .into(ivProductImage);
        
        switchStatus.setChecked(product.isActive());
        updateSwitchColors(product.isActive());

        if (product.getColors() != null) {
            AdminColorAdapter colorAdapter = new AdminColorAdapter(product.getColors());
            rvColors.setAdapter(colorAdapter);
        }
    }

}
