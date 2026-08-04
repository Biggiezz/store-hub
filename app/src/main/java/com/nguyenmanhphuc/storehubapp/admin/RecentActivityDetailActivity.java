package com.nguyenmanhphuc.storehubapp.admin;

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

        String type = getIntent().getStringExtra(EXTRA_TYPE);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String detail = getIntent().getStringExtra(EXTRA_DETAIL);
        String createdAt = getIntent().getStringExtra(EXTRA_CREATED_AT);
        ArrayList<CartItem> products = (ArrayList<CartItem>) getIntent().getSerializableExtra(EXTRA_PRODUCTS);
        String customerName = getIntent().getStringExtra(EXTRA_CUSTOMER_NAME);
        String customerPhone = getIntent().getStringExtra(EXTRA_CUSTOMER_PHONE);
        String paymentMethod = getIntent().getStringExtra(EXTRA_PAYMENT_METHOD);
        long totalAmount = getIntent().getLongExtra(EXTRA_TOTAL_AMOUNT, 0L);

        ImageView imgBack = findViewById(R.id.imgBack);
        ImageView imgActivityIcon = findViewById(R.id.imgActivityIcon);
        TextView tvActivityTitle = findViewById(R.id.tvActivityTitle);
        TextView tvActivityTime = findViewById(R.id.tvActivityTime);
        TextView tvCustomerName = findViewById(R.id.tvCustomerName);
        TextView tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        TextView tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        TextView tvTotalAmount = findViewById(R.id.tvTotalAmount);
        View cardProducts = findViewById(R.id.cardProducts);
        RecyclerView rvPurchasedProducts = findViewById(R.id.rvPurchasedProducts);
        MaterialButton btnViewOrders = findViewById(R.id.btnViewOrders);
        MaterialButton btnBack = findViewById(R.id.btnBack);

        imgBack.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());

        imgActivityIcon.setImageResource(RecentActivityAdapter.getIcon(type));
        imgActivityIcon.setBackgroundTintList(ColorStateList.valueOf(RecentActivityAdapter.getIconBackground(type)));

        tvActivityTitle.setText(value(title));
        tvActivityTime.setText(DateTimeUtils.formatISOToLocal(createdAt, "HH:mm, dd/MM/yyyy"));
        tvCustomerName.setText(value(customerName).isEmpty() ? "Khách hàng" : value(customerName));
        tvCustomerPhone.setText(value(customerPhone));
        tvPaymentMethod.setText(value(paymentMethod).isEmpty() ? "Thanh toán khi nhận hàng" : value(paymentMethod));
        tvTotalAmount.setText(formatPrice(totalAmount));

        boolean isOrder = type != null && type.startsWith("order_");
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

    private static String value(String text) {
        return text == null ? "" : text;
    }

    private static String formatType(String type) {
        if ("order_completed".equals(type)) return "Đơn hàng hoàn thành";
        if ("order_cancelled".equals(type)) return "Đơn hàng đã hủy";
        if ("order_created".equals(type)) return "Đơn hàng mới";
        if ("user_created".equals(type)) return "Người dùng mới";
        if ("login_admin".equals(type)) return "Đăng nhập quản trị";
        if ("login_customer".equals(type)) return "Đăng nhập khách hàng";
        if ("product_created".equals(type)) return "Sản phẩm mới";
        return value(type);
    }

    private static String formatPrice(long amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }
}
