package com.example.storehub.admin;

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
import com.example.storehub.R;
import com.example.storehub.model.Product;
import com.example.storehub.model.Response;
import com.example.storehub.services.HttpResquest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

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
    private MaterialButton btnEditProduct;
    
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
            Toast.makeText(this, "Không tìm thấy mã sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setUpListeners();
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
    }

    private void setUpListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnEditProduct.setOnClickListener(v -> {
            if (currentProduct != null) {
                Intent intent = ProductFormManagementActivity.createEditIntent(this, productId);
                startActivity(intent);
            }
        });
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
                    Toast.makeText(AdminProductDetailActivity.this, "Lỗi tải thông tin sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Product>> call, @NonNull Throwable t) {
                Log.e("AdminProductDetail", "onFailure: ", t);
                Toast.makeText(AdminProductDetailActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindData(Product product) {
        if (product == null) return;

        tvProductName.setText(product.getName());
        tvCategory.setText(product.getCategory());
        
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        tvProductPrice.setText(formatter.format(product.getPriceAsLong()) + "đ");
        
        tvDescription.setText(product.getDescription());
        tvStockValue.setText(String.valueOf(product.getStock()));
        // Giả lập Sold Value vì model Product có thể chưa có field này
        tvSoldValue.setText("0"); 
        
        ratingBar.setRating(product.getRating());
        tvRatingText.setText(String.format(Locale.getDefault(), "%.1f (%d đánh giá)", product.getRating(), product.getReviewCount()));

        Glide.with(this)
                .load(product.getImage())
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(ivProductImage);
        
        // Setup switch status dựa trên stock hoặc field status nếu có
        switchStatus.setChecked(product.getStock() > 0);
    }
}
