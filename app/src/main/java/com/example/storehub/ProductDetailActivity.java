package com.example.storehub;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.storehub.model.ApiMessageResponse;
import com.example.storehub.model.CartItem;
import com.example.storehub.model.Product;
import com.example.storehub.model.ProductColor;
import com.example.storehub.model.Response;
import com.example.storehub.services.ApiServices;
import com.example.storehub.services.HttpResquest;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";
    private ImageView ivProduct;
    private ImageButton btnBack;
    private TextView tvCategory, tvProductName, tvPrice, tvRatingSummary, tvDescription, tvEmptyReview, tvError, tvQuantity, btnMinus, btnPlus, tvColorLabel;
    private RatingBar ratingProduct;
    private LinearLayout colorContainer;
    private ProgressBar progressBar;
    private MaterialButton btnAddToCart;
    private ApiServices apiService;
    private Call<Response<Product>> productCall;
    private Call<ApiMessageResponse> cartCall;
    private Product currentProduct;
    private String productId;
    private Object selectedColorId;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_product_detail), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUi();

        apiService = new HttpResquest().callAPI();

        if (getIntent() != null && getIntent().hasExtra(EXTRA_PRODUCT_ID)) {
            Object extra = getIntent().getExtras().get(EXTRA_PRODUCT_ID);
            productId = extra != null ? String.valueOf(extra) : "";
        }

        if (TextUtils.isEmpty(productId) || "null".equalsIgnoreCase(productId)) {
            Toast.makeText(this, "Mã sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setUpListener();
        updateQuantity();
        loadProduct();
    }

    private void initUi() {
        ivProduct = findViewById(R.id.ivProduct);
        btnBack = findViewById(R.id.btnBack);

        tvCategory = findViewById(R.id.tvCategory);
        tvProductName = findViewById(R.id.tvProductName);
        tvPrice = findViewById(R.id.tvPrice);
        tvRatingSummary = findViewById(R.id.tvRatingSummary);
        tvDescription = findViewById(R.id.tvDescription);
        tvEmptyReview = findViewById(R.id.tvEmptyReview);
        tvError = findViewById(R.id.tvError);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        tvColorLabel = findViewById(R.id.tvColorLabel);

        ratingProduct = findViewById(R.id.ratingProduct);
        colorContainer = findViewById(R.id.colorContainer);
        progressBar = findViewById(R.id.progressBar);
        btnAddToCart = findViewById(R.id.btnAddToCart);
    }

    private void setUpListener() {
        btnBack.setOnClickListener(view -> finish());

        tvError.setOnClickListener(view -> loadProduct());

        btnMinus.setOnClickListener(view -> {
            if (quantity > 1) {
                quantity--;
                updateQuantity();
            }
        });

        btnPlus.setOnClickListener(view -> {
            if (currentProduct == null) {
                return;
            }

            if (currentProduct.getStock() > 0 && quantity >= currentProduct.getStock()) {
                Toast.makeText(
                        this,
                        "Số lượng đã đạt giới hạn tồn kho (" + currentProduct.getStock() + ")",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            quantity++;
            updateQuantity();
        });

        btnAddToCart.setOnClickListener(view -> addCurrentProductToCart());

    }

    private void loadProduct() {
        setLoading(true);
        tvError.setVisibility(View.GONE);

        productCall = apiService.getProductDetail(productId);

        productCall.enqueue(new Callback<Response<Product>>() {
            @Override
            public void onResponse(
                    @NonNull Call<Response<Product>> call,
                    @NonNull retrofit2.Response<Response<Product>> response
            ) {
                setLoading(false);

                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    showLoadError();
                    return;
                }

                currentProduct = response.body().getData();
                bindProduct(currentProduct);
                btnAddToCart.setEnabled(currentProduct.getStock() != 0);
            }

            @Override
            public void onFailure(
                    @NonNull Call<Response<Product>> call,
                    @NonNull Throwable throwable
            ) {
                if (call.isCanceled()) {
                    return;
                }

                setLoading(false);
                showLoadError();
            }
        });
    }

    private void bindProduct(Product product) {
        tvProductName.setText(nonNullText(product.getName()));
        tvPrice.setText(formatPrice(product.getPriceAsLong()));
        tvDescription.setText(nonNullText(product.getDescription()));

        if (TextUtils.isEmpty(product.getCategory())) {
            tvCategory.setVisibility(View.GONE);
        } else {
            tvCategory.setVisibility(View.VISIBLE);
            tvCategory.setText(product.getCategory().toUpperCase(new Locale("vi", "VN")));
        }

        ratingProduct.setRating(product.getRating());

        String ratingSummary = String.format(
                new Locale("vi", "VN"),
                "%.1f (%d đánh giá)",
                product.getRating(),
                product.getReviewCount()
        );

        tvRatingSummary.setText(ratingSummary);

        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .centerCrop()
                .into(ivProduct);

        prepareDefaultColor(product.getColors());
        renderColors(product.getColors());
        tvEmptyReview.setVisibility(View.VISIBLE);
    }

    private void prepareDefaultColor(List<ProductColor> colors) {
        selectedColorId = null;

        if (colors == null || colors.isEmpty()) {
            return;
        }

        for (ProductColor color : colors) {
            if (color.isDefault()) {
                selectedColorId = color.getId() != null ? color.getId() : color.getMongoId();
                break;
            }
        }

        if (selectedColorId == null && !colors.isEmpty()) {
            ProductColor first = colors.get(0);
            selectedColorId = first.getId() != null ? first.getId() : first.getMongoId();
        }
    }

    private void renderColors(List<ProductColor> colors) {
        colorContainer.removeAllViews();

        boolean hasColors = colors != null && !colors.isEmpty();

        tvColorLabel.setVisibility(hasColors ? View.VISIBLE : View.GONE);
        colorContainer.setVisibility(hasColors ? View.VISIBLE : View.GONE);

        if (!hasColors) {
            return;
        }

        for (ProductColor productColor : colors) {
            View colorView = new View(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(50),
                    dpToPx(50)
            );

            params.setMarginEnd(dpToPx(12));
            colorView.setLayoutParams(params);

            Object currentColorId = productColor.getId() != null ? productColor.getId() : productColor.getMongoId();
            boolean selected = selectedColorId != null
                    && selectedColorId.toString().equals(String.valueOf(currentColorId));

            colorView.setBackground(
                    createColorBackground(productColor.getHex(), selected)
            );

            colorView.setContentDescription(productColor.getName());

            colorView.setOnClickListener(view -> {
                selectedColorId = currentColorId;
                renderColors(colors);
            });

            colorContainer.addView(colorView);
        }
    }

    private GradientDrawable createColorBackground(
            String hexColor,
            boolean selected
    ) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(parseColorSafely(hexColor));

        int strokeWidth = selected ? dpToPx(3) : dpToPx(1);
        int strokeColor = selected
                ? Color.parseColor("#193329")
                : Color.parseColor("#B9B9B9");

        drawable.setStroke(strokeWidth, strokeColor);

        return drawable;
    }

    private int parseColorSafely(String color) {
        try {
            if (TextUtils.isEmpty(color)) return Color.LTGRAY;
            if (!color.startsWith("#")) color = "#" + color;
            return Color.parseColor(color);
        } catch (Exception ignored) {
            return Color.LTGRAY;
        }
    }

    private void addCurrentProductToCart() {
        if (currentProduct == null) {
            return;
        }

        if (currentProduct.getColors() != null
                && !currentProduct.getColors().isEmpty()
                && selectedColorId == null) {

            Toast.makeText(
                    this,
                    "Vui lòng chọn màu sản phẩm",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        setCartLoading(true);

        CartItem.AddToCartRequest request = new CartItem.AddToCartRequest(
                currentProduct.get_id(),
                selectedColorId,
                quantity
        );

        cartCall = apiService.addToCart(request);

        cartCall.enqueue(new Callback<ApiMessageResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiMessageResponse> call,
                    @NonNull retrofit2.Response<ApiMessageResponse> response
            ) {
                setCartLoading(false);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(
                            ProductDetailActivity.this,
                            "Không thể thêm sản phẩm vào giỏ",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                ApiMessageResponse result = response.body();

                String message = TextUtils.isEmpty(result.getMessage())
                        ? "Đã thêm sản phẩm vào giỏ"
                        : result.getMessage();

                MainActivity.shouldOpenCartOnResume = true;

                Toast.makeText(
                        ProductDetailActivity.this,
                        message,
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onFailure(
                    @NonNull Call<ApiMessageResponse> call,
                    @NonNull Throwable throwable
            ) {
                if (call.isCanceled()) {
                    return;
                }

                setCartLoading(false);

                Toast.makeText(
                        ProductDetailActivity.this,
                        "Không thể kết nối đến máy chủ",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        btnAddToCart.setEnabled(!loading && currentProduct != null);
    }

    private void setCartLoading(boolean loading) {
        btnAddToCart.setEnabled(!loading);

        btnAddToCart.setText(
                loading ? "Đang thêm..." : "Thêm vào giỏ"
        );
    }

    private void showLoadError() {
        tvError.setVisibility(View.VISIBLE);
        btnAddToCart.setEnabled(false);
    }

    private void updateQuantity() {
        tvQuantity.setText(String.valueOf(quantity));
        btnMinus.setAlpha(quantity <= 1 ? 0.4f : 1f);
    }

    private String formatPrice(long price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(
                new Locale("vi", "VN")
        );

        return formatter.format(price);
    }

    private String nonNullText(String value) {
        return value == null ? "" : value;
    }

    private int dpToPx(int dp) {
        return Math.round(
                dp * getResources().getDisplayMetrics().density
        );
    }

    @Override
    protected void onDestroy() {
        if (productCall != null) {
            productCall.cancel();
        }

        if (cartCall != null) {
            cartCall.cancel();
        }

        super.onDestroy();
    }
}
