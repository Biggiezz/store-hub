package com.nguyenmanhphuc.storehubapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.adapter.ProductReviewAdapter;
import com.nguyenmanhphuc.storehubapp.model.Product;
import com.nguyenmanhphuc.storehubapp.model.ProductColor;
import com.nguyenmanhphuc.storehubapp.model.request.AddToCartRequest;
import com.nguyenmanhphuc.storehubapp.model.response.Response;
import com.nguyenmanhphuc.storehubapp.model.User;
import com.nguyenmanhphuc.storehubapp.admin.ProductFormManagementActivity;
import com.nguyenmanhphuc.storehubapp.utils.SharedPreferencesManager;
import com.nguyenmanhphuc.storehubapp.services.ApiServices;
import com.nguyenmanhphuc.storehubapp.services.HttpResquest;
import com.nguyenmanhphuc.storehubapp.utils.DataCache;
import com.google.android.material.button.MaterialButton;
import android.content.Intent;
import com.nguyenmanhphuc.storehubapp.auth.LoginActivity;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;

public class ProductDetailActivity extends BaseActivity {

    public static final String EXTRA_PRODUCT_ID = "product_id";
    private ImageView ivProduct, btnBack, btnHeaderCart;
    private View btnHeaderCartContainer;
    private TextView tvCategory, tvProductName, tvPrice, tvRatingSummary, tvReviewScore, tvSold,tvDescription, tvEmptyReview, tvError, tvQuantity, btnMinus, btnPlus, tvColorLabel, tvStockStatus, tvCartBadge;
    private RatingBar ratingProduct;
    private LinearLayout colorContainer;
    private ProgressBar progressBar;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
    private MaterialButton btnAddToCart, btnEditProduct, btnReadAllReviews;
    private RecyclerView rvProductReviews;
    private ProductReviewAdapter reviewAdapter;
    private ApiServices apiService;
    private Call<Response<Product>> productCall;
    private Call<Response<Object>> cartCall;
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
            Log.d("ProductDetail", "Received Product ID: " + productId);
        }

        if (TextUtils.isEmpty(productId) || "null".equalsIgnoreCase(productId)) {
            Log.e("ProductDetail", "Invalid Product ID: " + productId);
            Toast.makeText(this, getString(R.string.invalid_product_id_toast), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setUpListener();
        updateQuantity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentProduct == null) {
            loadProduct();
        }
        fetchCartCount();
    }

    private void initUi() {
        ivProduct = findViewById(R.id.ivProduct);
        btnBack = findViewById(R.id.btnBack);
        btnHeaderCartContainer = findViewById(R.id.btnHeaderCartContainer);
        btnHeaderCart = findViewById(R.id.btnHeaderCart);
        tvCartBadge = findViewById(R.id.tvCartBadge);

        tvCategory = findViewById(R.id.tvCategory);
        tvProductName = findViewById(R.id.tvProductName);
        tvPrice = findViewById(R.id.tvPrice);
        tvRatingSummary = findViewById(R.id.tvRatingSummary);
        tvSold = findViewById(R.id.tvSold);
        tvReviewScore = findViewById(R.id.tvReviewScore);
        tvDescription = findViewById(R.id.tvDescription);
        tvEmptyReview = findViewById(R.id.tvEmptyReview);
        tvError = findViewById(R.id.tvError);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnMinus = findViewById(R.id.btnMinus);
        btnPlus = findViewById(R.id.btnPlus);
        tvColorLabel = findViewById(R.id.tvColorLabel);
        tvStockStatus = findViewById(R.id.tvStockStatus);

        ratingProduct = findViewById(R.id.ratingProduct);
        colorContainer = findViewById(R.id.colorContainer);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnReadAllReviews = findViewById(R.id.btnReadAllReviews);

        rvProductReviews = findViewById(R.id.rvProductReviews);
        rvProductReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ProductReviewAdapter();
        rvProductReviews.setAdapter(reviewAdapter);
        btnEditProduct = findViewById(R.id.btnEditProduct);

        checkAdminRole();
    }

    private void checkAdminRole() {
        User user = SharedPreferencesManager.getInstance(this).getUser();
        if (user != null && user.isAdmin()) {
            btnEditProduct.setVisibility(View.VISIBLE);
        } else {
            btnEditProduct.setVisibility(View.GONE);
        }
    }

    private void setUpListener() {
        if (btnBack != null) {
            btnBack.setOnClickListener(view -> finish());
        }

        if (btnHeaderCartContainer != null) {
            btnHeaderCartContainer.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> loadProduct());
        }

        btnEditProduct.setOnClickListener(v -> {
            if (currentProduct != null) {
                startActivity(ProductFormManagementActivity.createEditIntent(this, currentProduct.get_id()));
            }
        });

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
                Toast.makeText(this, getString(R.string.insufficient_stock_toast), Toast.LENGTH_SHORT).show();
                return;
            }

            quantity++;
            updateQuantity();
        });

        btnAddToCart.setOnClickListener(view -> addCurrentProductToCart());

        btnReadAllReviews.setOnClickListener(view -> {
            reviewAdapter.showAll();
            btnReadAllReviews.setVisibility(View.GONE);
        });

    }

    private void loadProduct() {
        setLoading(true);
        tvError.setVisibility(View.GONE);

        productCall = apiService.getProductDetail(productId);

        productCall.enqueue(new Callback<Response<Product>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Product>> call, @NonNull retrofit2.Response<Response<Product>> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    showLoadError();
                    return;
                }
                currentProduct = response.body().getData();
                bindProduct(currentProduct);
                if (currentProduct.getStock() <= 0) {
                    btnAddToCart.setEnabled(false);
                    btnAddToCart.setText(getString(R.string.out_of_stock));
                } else {
                    btnAddToCart.setEnabled(true);
                    btnAddToCart.setText(getString(R.string.add_to_cart));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<Product>> call, @NonNull Throwable throwable) {
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

        if (tvStockStatus != null) {
            if (product.getStock() <= 0) {
                tvStockStatus.setVisibility(View.VISIBLE);
                tvStockStatus.setText(getString(R.string.out_of_stock).toUpperCase(Locale.getDefault()));
            } else {
                tvStockStatus.setVisibility(View.GONE);
            }
        }

        if (product.getCategory() == null || TextUtils.isEmpty(product.getCategory().getName())) {
            tvCategory.setVisibility(View.GONE);
        } else {
            tvCategory.setVisibility(View.VISIBLE);
            tvCategory.setText(getLocalizedCategoryName(product.getCategory().getName()).toUpperCase(Locale.getDefault()));
        }

        ratingProduct.setRating(product.getRating());

        String ratingSummary = String.format(Locale.getDefault(), getString(R.string.rating_format),
                product.getRating(),
                product.getReviewCount()
        );

        tvRatingSummary.setText(ratingSummary);
        tvSold.setText(getString(R.string.sold_label) + " " + product.getSold());
        tvReviewScore.setText(String.format(new Locale("vi", "VN"), "%.1f", product.getRating()));

        Glide.with(this)
                .load(product.getImageUrl())
                .placeholder(R.drawable.ic_products)
                .error(R.drawable.ic_products)
                .centerCrop()
                .into(ivProduct);

        prepareDefaultColor(product.getColors());
        renderColors(product.getColors());

        if (product.getReviews() != null && !product.getReviews().isEmpty()) {
            tvEmptyReview.setVisibility(View.GONE);
            rvProductReviews.setVisibility(View.VISIBLE);
            reviewAdapter.updateData(product.getReviews());
            btnReadAllReviews.setVisibility(product.getReviews().size() > 2 ? View.VISIBLE : View.GONE);
        } else {
            tvEmptyReview.setVisibility(View.VISIBLE);
            rvProductReviews.setVisibility(View.GONE);
            btnReadAllReviews.setVisibility(View.GONE);
        }
    }

    private void prepareDefaultColor(List<ProductColor> colors) {
        selectedColorId = null;
        if (colors == null || colors.isEmpty()) {
            return;
        }
        for (ProductColor color : colors) {
            if (color.isDefault()) {
                selectedColorId = color.getId();
                break;
            }
        }
        if (selectedColorId == null && !colors.isEmpty()) {
            ProductColor first = colors.get(0);
            selectedColorId = first.getId();
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

        String selectedColorName = "";
        for (ProductColor productColor : colors) {
            String currentColorId = productColor.getId();
            if (selectedColorId != null && selectedColorId.toString().equals(String.valueOf(currentColorId))) {
                selectedColorName = productColor.getName();
                break;
            }
        }
        if (!selectedColorName.isEmpty()) {
            tvColorLabel.setText(getString(R.string.colors_label) + ": " + selectedColorName);
        } else {
            tvColorLabel.setText(getString(R.string.colors_label));
        }

        for (ProductColor productColor : colors) {
            android.widget.FrameLayout frameLayout = new android.widget.FrameLayout(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(50),
                    dpToPx(50)
            );

            params.setMarginEnd(dpToPx(12));
            frameLayout.setLayoutParams(params);

            String currentColorId = productColor.getId();
            boolean selected = selectedColorId != null
                    && selectedColorId.toString().equals(String.valueOf(currentColorId));

            // Outer border
            View borderView = new View(this);
            android.widget.FrameLayout.LayoutParams borderParams = new android.widget.FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            borderView.setLayoutParams(borderParams);
            borderView.setBackground(createOuterBorder(selected));
            frameLayout.addView(borderView);

            // Inner circle
            View colorCircle = new View(this);
            android.widget.FrameLayout.LayoutParams circleParams = new android.widget.FrameLayout.LayoutParams(
                    dpToPx(36),
                    dpToPx(36)
            );
            circleParams.gravity = android.view.Gravity.CENTER;
            colorCircle.setLayoutParams(circleParams);
            colorCircle.setBackground(createInnerCircle(parseColorSafely(productColor.getHex())));
            frameLayout.addView(colorCircle);

            frameLayout.setContentDescription(productColor.getName());

            frameLayout.setOnClickListener(view -> {
                selectedColorId = currentColorId;
                renderColors(colors);
            });

            colorContainer.addView(frameLayout);
        }
    }

    private boolean isLightColor(int color) {
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance > 0.85;
    }

    private GradientDrawable createInnerCircle(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        if (isLightColor(color)) {
            drawable.setStroke(dpToPx(1), Color.parseColor("#DDDDDD"));
        }
        return drawable;
    }

    private GradientDrawable createOuterBorder(boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.TRANSPARENT);
        if (selected) {
            drawable.setStroke(dpToPx(2), Color.parseColor("#112D21"));
        }
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

        SharedPreferencesManager pref = new SharedPreferencesManager(this);
        if (!pref.isLoggedIn()) {
            Toast.makeText(this, getString(R.string.login_to_buy_toast), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }
        if (currentProduct.getColors() != null && !currentProduct.getColors().isEmpty() && selectedColorId == null) {
            Toast.makeText(this, getString(R.string.select_product_color_toast), Toast.LENGTH_SHORT).show();
            return;
        }

        setCartLoading(true);

        AddToCartRequest request = new AddToCartRequest(
                currentProduct.get_id(),
                selectedColorId,
                quantity
        );

        cartCall = apiService.addToCart(HttpResquest.authorizationHeader(this), request);

        cartCall.enqueue(new Callback<Response<Object>>() {
            @Override
            public void onResponse(@NonNull Call<Response<Object>> call, @NonNull retrofit2.Response<Response<Object>> response) {
                setCartLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ProductDetailActivity.this, getString(R.string.add_to_cart_failed_toast), Toast.LENGTH_SHORT).show();
                    return;
                }

                Response<Object> result = response.body();

                String message = TextUtils.isEmpty(result.getMessage())
                        ? getString(R.string.toast_added_to_cart)
                        : result.getMessage();

                MainActivity.shouldOpenCartOnResume = true;
                // Xóa cache giỏ hàng → lần sau vào Cart sẽ thấy sản phẩm mới
                DataCache.get().invalidateExact("user_cart");

                Toast.makeText(ProductDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                runFlyToCartAnimation(btnAddToCart, btnHeaderCart != null ? btnHeaderCart : btnHeaderCartContainer);
            }

            @Override
            public void onFailure(@NonNull Call<Response<Object>> call, @NonNull Throwable throwable) {
                if (call.isCanceled()) {
                    return;
                }
                if (!com.nguyenmanhphuc.storehubapp.utils.NetworkUtils.isNetworkAvailable(ProductDetailActivity.this)) {
                    com.nguyenmanhphuc.storehubapp.utils.NetworkUtils.showNoNetworkToast(ProductDetailActivity.this);
                } else {
                    Toast.makeText(ProductDetailActivity.this, getString(R.string.cannot_connect_server_toast), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchCartCount() {
        SharedPreferencesManager prefManager = new SharedPreferencesManager(this);
        if (!prefManager.isLoggedIn()) {
            if (tvCartBadge != null) tvCartBadge.setVisibility(View.GONE);
            return;
        }
        apiService.getCart(HttpResquest.authorizationHeader(this)).enqueue(new Callback<Response<java.util.ArrayList<com.nguyenmanhphuc.storehubapp.model.CartItem>>>() {
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

    private void runFlyToCartAnimation(View sourceView, View targetView) {
        if (sourceView == null || targetView == null) return;

        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        if (decorView == null) return;

        int[] startLoc = new int[2];
        int[] targetLoc = new int[2];

        sourceView.getLocationOnScreen(startLoc);
        targetView.getLocationOnScreen(targetLoc);

        ImageView animImg = new ImageView(this);
        animImg.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (currentProduct != null && currentProduct.getImage() != null && !currentProduct.getImage().isEmpty()) {
            Glide.with(this).load(currentProduct.getImage()).into(animImg);
        } else {
            animImg.setImageResource(R.drawable.ic_products);
        }

        int animSize = (int) (60 * getResources().getDisplayMetrics().density);
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(animSize, animSize);
        params.leftMargin = startLoc[0] + (sourceView.getWidth() - animSize) / 2;
        params.topMargin = startLoc[1] + (sourceView.getHeight() - animSize) / 2;

        animImg.setLayoutParams(params);
        decorView.addView(animImg);

        float deltaX = (targetLoc[0] - startLoc[0]) + (targetView.getWidth() - sourceView.getWidth()) / 2f;
        float deltaY = (targetLoc[1] - startLoc[1]) + (targetView.getHeight() - sourceView.getHeight()) / 2f;

        animImg.animate()
                .translationX(deltaX)
                .translationY(deltaY)
                .scaleX(0.2f)
                .scaleY(0.2f)
                .alpha(0.4f)
                .setDuration(1000)
                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    decorView.removeView(animImg);
                    targetView.animate()
                            .scaleX(1.3f)
                            .scaleY(1.3f)
                            .setDuration(120)
                            .withEndAction(() -> targetView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(120).start())
                            .start();
                    fetchCartCount();
                })
                .start();
    }

    private void setLoading(boolean loading) {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (!loading) swipeRefreshLayout.setRefreshing(false);
            btnAddToCart.setEnabled(!loading && currentProduct != null);
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (swipeRefreshLayout != null && !loading) {
            swipeRefreshLayout.setRefreshing(false);
        }
        btnAddToCart.setEnabled(!loading && currentProduct != null);
    }

    private void setCartLoading(boolean loading) {
        btnAddToCart.setEnabled(!loading);

        btnAddToCart.setText(loading ? getString(R.string.adding) : getString(R.string.add_to_cart));
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
        return Math.round(dp * getResources().getDisplayMetrics().density
        );
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
