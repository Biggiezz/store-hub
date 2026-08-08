package com.nguyenmanhphuc.storehubapp.admin;

import android.graphics.Color;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nguyenmanhphuc.storehubapp.R;
import com.nguyenmanhphuc.storehubapp.adapter.OrderProductAdapter;
import com.nguyenmanhphuc.storehubapp.admin.adapter.RecentActivityAdapter;
import com.nguyenmanhphuc.storehubapp.model.CartItem;
import com.nguyenmanhphuc.storehubapp.model.response.RecentActivity;
import com.nguyenmanhphuc.storehubapp.utils.DateTimeUtils;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class RecentActivityDetailActivity extends AppCompatActivity {
    private static final String EXTRA_TYPE = "activity_type";
    private static final String EXTRA_TITLE = "activity_title";
    private static final String EXTRA_DETAIL = "activity_detail";
    private static final String EXTRA_CREATED_AT = "activity_created_at";
    private static final String EXTRA_PRODUCTS = "activity_products";
    private static final String EXTRA_CUSTOMER_NAME = "activity_customer_name";
    private static final String EXTRA_CUSTOMER_PHONE = "activity_customer_phone";
    private static final String EXTRA_PAYMENT_METHOD = "activity_payment_method";
    private static final String EXTRA_TOTAL_AMOUNT = "activity_total_amount";
    private static final String EXTRA_PRODUCT_IMAGE = "activity_product_image";
    private static final String EXTRA_PRODUCT_STOCK = "activity_product_stock";
    private static final String EXTRA_PRODUCT_PRICE = "activity_product_price";
    private static final String EXTRA_PRODUCT_STATUS = "activity_product_status";

    private ImageView imgBack;
    private ImageView imgActivityIcon;
    private TextView tvActivityTitle;
    private TextView tvActivityTime;
    private TextView tvCustomerName;
    private TextView tvCustomerPhone;
    private TextView tvPaymentMethod;
    private TextView tvTotalAmount;
    private View cardProducts;
    private RecyclerView rvPurchasedProducts;
    private MaterialButton btnViewOrders;
//    private MaterialButton btnBack;
    private TextView tvSectionHeader;
    private TextView tvNameLabel;

    private View layoutProductStock;
    private TextView tvProductStock;
    private View dividerProductStock;

    private View layoutProductPrice;
    private TextView tvProductPrice;
    private View dividerProductPrice;

    private View layoutProductStatus;
    private TextView tvProductStatus;
    private View dividerProductStatus;

    private View layoutCustomerPhone;
    private View layoutPaymentMethod;
    private View layoutTotalAmount;
    private View dividerPhone;
    private View dividerPayment;
    private View dividerTotal;

    public static Intent createIntent(Context context, RecentActivity activity) {
        Intent intent = new Intent(context, RecentActivityDetailActivity.class);
        intent.putExtra(EXTRA_TYPE, value(activity.getType()));
        intent.putExtra(EXTRA_TITLE, value(activity.getTitle()));
        intent.putExtra(EXTRA_DETAIL, value(activity.getDetail()));
        intent.putExtra(EXTRA_CREATED_AT, value(activity.getCreatedAt()));
        intent.putExtra(EXTRA_PRODUCTS, activity.getProducts());
        intent.putExtra(EXTRA_CUSTOMER_NAME, value(activity.getCustomerName()));
        intent.putExtra(EXTRA_CUSTOMER_PHONE, value(activity.getCustomerPhone()));
        intent.putExtra(EXTRA_PAYMENT_METHOD, value(activity.getPaymentMethod()));
        intent.putExtra(EXTRA_TOTAL_AMOUNT, activity.getTotalAmount());
        intent.putExtra(EXTRA_PRODUCT_IMAGE, value(activity.getProductImage()));
        intent.putExtra(EXTRA_PRODUCT_STOCK, activity.getProductStock() != null ? activity.getProductStock() : -1);
        intent.putExtra(EXTRA_PRODUCT_PRICE, activity.getProductPrice() != null ? activity.getProductPrice() : -1L);
        intent.putExtra(EXTRA_PRODUCT_STATUS, value(activity.getProductStatus()));
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recent_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recentDetailRoot), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUi();

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String detail = getIntent().getStringExtra(EXTRA_DETAIL);
        String createdAt = getIntent().getStringExtra(EXTRA_CREATED_AT);
        ArrayList<CartItem> products = (ArrayList<CartItem>) getIntent().getSerializableExtra(EXTRA_PRODUCTS);
        String customerName = getIntent().getStringExtra(EXTRA_CUSTOMER_NAME);
        String customerPhone = getIntent().getStringExtra(EXTRA_CUSTOMER_PHONE);
        String paymentMethod = getIntent().getStringExtra(EXTRA_PAYMENT_METHOD);
        long totalAmount = getIntent().getLongExtra(EXTRA_TOTAL_AMOUNT, 0L);
        String productImage = getIntent().getStringExtra(EXTRA_PRODUCT_IMAGE);
        int productStock = getIntent().getIntExtra(EXTRA_PRODUCT_STOCK, -1);
        long productPrice = getIntent().getLongExtra(EXTRA_PRODUCT_PRICE, -1L);
        String productStatus = getIntent().getStringExtra(EXTRA_PRODUCT_STATUS);

        imgBack.setOnClickListener(v -> finish());
//        btnBack.setOnClickListener(v -> finish());

        imgActivityIcon.setImageResource(RecentActivityAdapter.getIcon(type));
        imgActivityIcon.setBackgroundTintList(ColorStateList.valueOf(RecentActivityAdapter.getIconBackground(type)));

        tvActivityTitle.setText(value(title));

        if (tvActivityTime != null) {
            String formattedTime = DateTimeUtils.formatISOToVN(createdAt, "HH:mm, dd/MM/yyyy");
            if (formattedTime.isEmpty()) {
                formattedTime = value(createdAt);
            }
            tvActivityTime.setText(formattedTime);
        }
        String resolvedName = value(customerName);
        if (resolvedName.isEmpty() && title != null) {
            resolvedName = title.replace(" vừa đăng nhập", "")
                    .replace("Quản trị viên ", "")
                    .replace("Khách hàng ", "")
                    .replace("Người dùng mới: ", "")
                    .replace("Sản phẩm mới: ", "")
                    .trim();
        }
        if (resolvedName.isEmpty()) {
            resolvedName = "Tài khoản hệ thống";
        }
        tvCustomerName.setText(resolvedName);
        tvCustomerPhone.setText(value(customerPhone));
        tvPaymentMethod.setText(value(paymentMethod).isEmpty() ? "Thanh toán khi nhận hàng" : value(paymentMethod));
        tvTotalAmount.setText(formatPrice(totalAmount));

        boolean isOrder = type != null && type.startsWith("order_");
        boolean isProduct = type != null && type.startsWith("product_");

        if (isProduct) {
            if (tvSectionHeader != null) tvSectionHeader.setText("THÔNG TIN SẢN PHẨM");
            if (tvNameLabel != null) tvNameLabel.setText("Tên sản phẩm");

            // Ẩn các trường thông tin đơn hàng/khách hàng không thuộc về sản phẩm
            if (layoutCustomerPhone != null) layoutCustomerPhone.setVisibility(View.GONE);
            if (layoutPaymentMethod != null) layoutPaymentMethod.setVisibility(View.GONE);
            if (layoutTotalAmount != null) layoutTotalAmount.setVisibility(View.GONE);
            if (dividerPhone != null) dividerPhone.setVisibility(View.GONE);
            if (dividerPayment != null) dividerPayment.setVisibility(View.GONE);
            if (dividerTotal != null) dividerTotal.setVisibility(View.GONE);
            // Xử lý tồn kho fallback từ detail nếu stock == -1
            int finalStock = productStock;
            if (finalStock == -1 && detail != null && detail.contains("Tồn kho: ")) {
                try {
                    String sub = detail.substring(detail.indexOf("Tồn kho: ") + 9).trim();
                    finalStock = Integer.parseInt(sub);
                } catch (Exception ignored) {}
            }

            // Load hình ảnh sản phẩm vào imgActivityIcon ở phần header (được bo góc 8dp tự động qua XML)
            if (productImage != null && !productImage.isEmpty() && imgActivityIcon != null) {
                Glide.with(this)
                        .load(productImage)
                        .placeholder(R.drawable.ic_products)
                        .error(R.drawable.ic_products)
                        .into(imgActivityIcon);
            }

            if (finalStock >= 0) {
                if (layoutProductStock != null) layoutProductStock.setVisibility(View.VISIBLE);
                if (dividerProductStock != null) dividerProductStock.setVisibility(View.VISIBLE);
                if (tvProductStock != null) tvProductStock.setText(finalStock + " sản phẩm");
            } else {
                if (layoutProductStock != null) layoutProductStock.setVisibility(View.GONE);
                if (dividerProductStock != null) dividerProductStock.setVisibility(View.GONE);
            }

            if (productPrice >= 0) {
                if (layoutProductPrice != null) layoutProductPrice.setVisibility(View.VISIBLE);
                if (dividerProductPrice != null) dividerProductPrice.setVisibility(View.VISIBLE);
                if (tvProductPrice != null) tvProductPrice.setText(formatPrice(productPrice));
            } else {
                if (layoutProductPrice != null) layoutProductPrice.setVisibility(View.GONE);
                if (dividerProductPrice != null) dividerProductPrice.setVisibility(View.GONE);
            }

            String finalStatus = productStatus;
            if ((finalStatus == null || finalStatus.isEmpty()) && finalStock >= 0) {
                finalStatus = finalStock > 0 ? "Đang bán" : "Hết hàng";
            }

            if (finalStatus != null && !finalStatus.isEmpty()) {
                if (layoutProductStatus != null) layoutProductStatus.setVisibility(View.VISIBLE);
                if (dividerProductStatus != null) dividerProductStatus.setVisibility(View.VISIBLE);
                if (tvProductStatus != null) {
                    tvProductStatus.setText(finalStatus);
                    boolean isAvailable = "Đang bán".equalsIgnoreCase(finalStatus);
                    tvProductStatus.setTextColor(Color.parseColor(isAvailable ? "#2E7D32" : "#E53935"));
                }
            } else {
                if (layoutProductStatus != null) layoutProductStatus.setVisibility(View.GONE);
                if (dividerProductStatus != null) dividerProductStatus.setVisibility(View.GONE);
            }
        } else {
            if (tvSectionHeader != null) tvSectionHeader.setText("THÔNG TIN KHÁCH HÀNG");
            if (tvNameLabel != null) tvNameLabel.setText("Tên người dùng");

            // Ẩn các trường sản phẩm đối với sự kiện khác
            if (layoutProductStock != null) layoutProductStock.setVisibility(View.GONE);
            if (dividerProductStock != null) dividerProductStock.setVisibility(View.GONE);
            if (layoutProductPrice != null) layoutProductPrice.setVisibility(View.GONE);
            if (dividerProductPrice != null) dividerProductPrice.setVisibility(View.GONE);
            if (layoutProductStatus != null) layoutProductStatus.setVisibility(View.GONE);
            if (dividerProductStatus != null) dividerProductStatus.setVisibility(View.GONE);

            if (!isOrder) {
                // Đối với các hoạt động đăng nhập/người dùng: Chỉ giữ lại Tên người dùng và Thời gian
                if (layoutCustomerPhone != null) layoutCustomerPhone.setVisibility(View.GONE);
                if (layoutPaymentMethod != null) layoutPaymentMethod.setVisibility(View.GONE);
                if (layoutTotalAmount != null) layoutTotalAmount.setVisibility(View.GONE);
                if (dividerPhone != null) dividerPhone.setVisibility(View.GONE);
                if (dividerPayment != null) dividerPayment.setVisibility(View.GONE);
                if (dividerTotal != null) dividerTotal.setVisibility(View.GONE);
            } else {
                boolean hasPhone = customerPhone != null && !customerPhone.trim().isEmpty();
                if (layoutCustomerPhone != null) layoutCustomerPhone.setVisibility(hasPhone ? View.VISIBLE : View.GONE);
                if (dividerPhone != null) dividerPhone.setVisibility(hasPhone ? View.VISIBLE : View.GONE);
                if (layoutPaymentMethod != null) layoutPaymentMethod.setVisibility(View.VISIBLE);
                if (dividerPayment != null) dividerPayment.setVisibility(View.VISIBLE);
                if (layoutTotalAmount != null) layoutTotalAmount.setVisibility(View.VISIBLE);
                if (dividerTotal != null) dividerTotal.setVisibility(View.VISIBLE);
            }
        }

        boolean hasProducts = products != null && !products.isEmpty();
        cardProducts.setVisibility(isOrder && hasProducts ? View.VISIBLE : View.GONE);
        if (isOrder && hasProducts) {
            rvPurchasedProducts.setLayoutManager(new LinearLayoutManager(this));
            OrderProductAdapter adapter = new OrderProductAdapter(this);
            rvPurchasedProducts.setAdapter(adapter);
            adapter.updateData(products);
        }

        btnViewOrders.setVisibility(isOrder ? View.VISIBLE : View.GONE);
        btnViewOrders.setOnClickListener(v -> startActivity(new Intent(this, AdminOrdersActivity.class)));
    }

    private void initUi() {
        imgBack = findViewById(R.id.btnBack);
        imgActivityIcon = findViewById(R.id.imgActivityIcon);
        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        tvActivityTime = findViewById(R.id.tvActivityTime);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        cardProducts = findViewById(R.id.cardProducts);
        rvPurchasedProducts = findViewById(R.id.rvPurchasedProducts);
        btnViewOrders = findViewById(R.id.btnViewOrders);
//        btnBack = findViewById(R.id.btnBack);

        tvSectionHeader = findViewById(R.id.tvSectionHeader);
        tvNameLabel = findViewById(R.id.tvNameLabel);

        layoutProductStock = findViewById(R.id.layoutProductStock);
        tvProductStock = findViewById(R.id.tvProductStock);
        dividerProductStock = findViewById(R.id.dividerProductStock);

        layoutProductPrice = findViewById(R.id.layoutProductPrice);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        dividerProductPrice = findViewById(R.id.dividerProductPrice);

        layoutProductStatus = findViewById(R.id.layoutProductStatus);
        tvProductStatus = findViewById(R.id.tvProductStatus);
        dividerProductStatus = findViewById(R.id.dividerProductStatus);

        layoutCustomerPhone = findViewById(R.id.layoutCustomerPhone);
        layoutPaymentMethod = findViewById(R.id.layoutPaymentMethod);
        layoutTotalAmount = findViewById(R.id.layoutTotalAmount);
        dividerPhone = findViewById(R.id.dividerPhone);
        dividerPayment = findViewById(R.id.dividerPayment);
        dividerTotal = findViewById(R.id.dividerTotal);
    }

    private static String value(String text) {
        return text == null ? "" : text;
    }

    private static String formatPrice(long amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }
}
