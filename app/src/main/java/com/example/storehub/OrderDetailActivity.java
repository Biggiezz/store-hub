package com.example.storehub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.storehub.adapter.TimelineAdapter;
import com.example.storehub.model.Order;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;

public class OrderDetailActivity extends AppCompatActivity {

    private ImageView btnBack, ivStatusIcon, ivDetailProductImage;
    private TextView tvStatusTitle, tvStatusBadge, tvDetailOrderId, tvStatusSubtitle;
    private TextView tvRecipientNamePhone, tvRecipientAddress;
    private TextView tvDetailProductName, tvDetailProductVariant, tvDetailProductQty, tvDetailProductUnitPrice;
    private TextView tvSubtotalPrice, tvShippingFee, tvDiscountPrice, tvTotalPrice;
    private View cardTimeline;
    private RecyclerView rvTimeline;
    private MaterialButton btnPrimaryAction, btnSecondaryAction;

    private Order order;
    private DecimalFormat formatter = new DecimalFormat("#,###đ");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initViews();

        btnBack.setOnClickListener(v -> finish());

        order = (Order) getIntent().getSerializableExtra("order_item");
        if (order != null) {
            bindData();
        } else {
            Toast.makeText(this, "Không thể tải chi tiết đơn hàng!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        tvStatusTitle = findViewById(R.id.tvStatusTitle);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvDetailOrderId = findViewById(R.id.tvDetailOrderId);
        tvStatusSubtitle = findViewById(R.id.tvStatusSubtitle);

        cardTimeline = findViewById(R.id.cardTimeline);
        rvTimeline = findViewById(R.id.rvTimeline);

        tvRecipientNamePhone = findViewById(R.id.tvRecipientNamePhone);
        tvRecipientAddress = findViewById(R.id.tvRecipientAddress);

        ivDetailProductImage = findViewById(R.id.ivDetailProductImage);
        tvDetailProductName = findViewById(R.id.tvDetailProductName);
        tvDetailProductVariant = findViewById(R.id.tvDetailProductVariant);
        tvDetailProductQty = findViewById(R.id.tvDetailProductQty);
        tvDetailProductUnitPrice = findViewById(R.id.tvDetailProductUnitPrice);

        tvSubtotalPrice = findViewById(R.id.tvSubtotalPrice);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvDiscountPrice = findViewById(R.id.tvDiscountPrice);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        btnPrimaryAction = findViewById(R.id.btnPrimaryAction);
        btnSecondaryAction = findViewById(R.id.btnSecondaryAction);
    }

    private void bindData() {
        tvDetailOrderId.setText("Mã đơn: " + order.getId());
        tvRecipientNamePhone.setText(order.getRecipientName() + " • " + order.getRecipientPhone());
        tvRecipientAddress.setText(order.getRecipientAddress());

        tvDetailProductName.setText(order.getProductName());
        tvDetailProductVariant.setText(order.getProductVariant());
        tvDetailProductQty.setText("Số lượng: " + order.getQuantity());
        tvDetailProductUnitPrice.setText(formatter.format(order.getUnitPrice()));

        Glide.with(this)
                .load(order.getProductImage())
                .placeholder(R.drawable.ic_product)
                .error(R.drawable.ic_product)
                .into(ivDetailProductImage);

        tvSubtotalPrice.setText(formatter.format(order.getSubtotal()));
        tvShippingFee.setText(formatter.format(order.getShippingFee()));
        tvDiscountPrice.setText("-" + formatter.format(order.getDiscount()));
        tvTotalPrice.setText(formatter.format(order.getTotalAmount()));

        // Bind layout dynamically based on status
        switch (order.getStatus()) {
            case "shipping":
            case "processing":
                setupShippingStatus();
                break;
            case "completed":
                setupCompletedStatus();
                break;
            case "cancelled":
                setupCancelledStatus();
                break;
            default:
                setupShippingStatus();
                break;
        }
    }

    private void setupShippingStatus() {
        ivStatusIcon.setImageResource(R.drawable.ic_truck_delivering);
        tvStatusTitle.setText("Đang giao hàng");
        tvStatusBadge.setText("Đang giao hàng");
        tvStatusBadge.setBackgroundResource(R.drawable.bg_status_delivering);
        tvStatusSubtitle.setText("Dự kiến giao: " + (order.getEstimatedDeliveryDate() != null ? order.getEstimatedDeliveryDate() : "24 Tháng 5, 2024"));

        cardTimeline.setVisibility(View.VISIBLE);
        if (order.getTimeline() != null && !order.getTimeline().isEmpty()) {
            rvTimeline.setLayoutManager(new LinearLayoutManager(this));
            rvTimeline.setAdapter(new TimelineAdapter(this, order.getTimeline()));
        }

        btnPrimaryAction.setText("Liên hệ hỗ trợ");
        btnPrimaryAction.setOnClickListener(v -> Toast.makeText(this, "Đang kết nối tổng đài hỗ trợ...", Toast.LENGTH_SHORT).show());

        btnSecondaryAction.setText("Hủy đơn hàng");
        btnSecondaryAction.setOnClickListener(v -> showCancelDialog());
    }

    private void setupCompletedStatus() {
        ivStatusIcon.setImageResource(R.drawable.ic_check_circle_green);
        tvStatusTitle.setText("Đã hoàn thành");
        tvStatusBadge.setText("Đã hoàn thành");
        tvStatusBadge.setBackgroundResource(R.drawable.bg_status_completed);
        tvStatusSubtitle.setText("Hoàn thành lúc: " + (order.getCompletedDate() != null ? order.getCompletedDate() : "10:30, 20/05/2024"));

        cardTimeline.setVisibility(View.GONE);

        btnPrimaryAction.setText("Đánh giá");
        btnPrimaryAction.setOnClickListener(v -> {
            Intent intent = new Intent(OrderDetailActivity.this, WriteReviewActivity.class);
            intent.putExtra("order_item", order);
            startActivity(intent);
        });

        btnSecondaryAction.setText("Mua lại");
        btnSecondaryAction.setOnClickListener(v -> Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show());
    }

    private void setupCancelledStatus() {
        ivStatusIcon.setImageResource(R.drawable.ic_cancel_red);
        tvStatusTitle.setText("Đã hủy");
        tvStatusBadge.setText("Đã hoàn tiền");
        tvStatusBadge.setBackgroundResource(R.drawable.bg_status_cancelled);
        tvStatusSubtitle.setText("Lý do hủy: " + (order.getCancelReason() != null ? order.getCancelReason() : "Thay đổi ý định"));

        cardTimeline.setVisibility(View.GONE);

        btnPrimaryAction.setText("Mua lại sản phẩm");
        btnPrimaryAction.setOnClickListener(v -> Toast.makeText(this, "Đã thêm sản phẩm vào giỏ hàng!", Toast.LENGTH_SHORT).show());

        btnSecondaryAction.setText("Liên hệ hỗ trợ");
        btnSecondaryAction.setOnClickListener(v -> Toast.makeText(this, "Đang kết nối tổng đài hỗ trợ...", Toast.LENGTH_SHORT).show());
    }

    private void showCancelDialog() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_cancel_order, null);
        bottomSheet.setContentView(view);

        view.findViewById(R.id.btnCloseDialog).setOnClickListener(v -> bottomSheet.dismiss());
        view.findViewById(R.id.btnDismissCancel).setOnClickListener(v -> bottomSheet.dismiss());

        view.findViewById(R.id.btnConfirmCancel).setOnClickListener(v -> {
            order.setStatus("cancelled");
            order.setCancelReason("Thay đổi ý định mua hàng");
            bindData();
            bottomSheet.dismiss();
            Toast.makeText(this, "Hủy đơn hàng thành công!", Toast.LENGTH_SHORT).show();
        });

        bottomSheet.show();
    }
}
